package NodeManager.Node;

import NodeManager.Node.NodeInterfaces.DistributeLock;
import NodeManager.Node.NodeInterfaces.NodeInterface;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class DistributeLockImplement extends UnicastRemoteObject implements DistributeLock {
    // 锁
    private transient final Object distributeLock = new Object();
    // 存放等待锁的线程,需要设置为静态，用来保存数据使用，不然会出现幽灵数据，即无论怎么天剑，数据都不会出现。
    // 可以定制策略，是随机调用等待者还是先到先得还是后到先得，默认是先到先得。
    @Deprecated
    private transient volatile ArrayList<Thread> waiterList = new ArrayList<Thread>();
    //    private ConcurrentHashMap<NodeInterface,Object> waiterList = new ConcurrentHashMap<NodeInterface,Object>();
    // 锁的持有者，这是一个问题，由于是static的，所以会造成多线程的问题，即被其他线程修改了数据，后再去释放。
    private transient volatile NodeInterface lockOwner = null;
    // 持有锁的节点
    private transient volatile waitLink holdLockNode = null;
    // 重入次数
    private transient volatile AtomicInteger reLockTimes = new AtomicInteger();
    // 等待列表
    private volatile waitLink head = null,tail = null;
    // 用于快速检查当前线程是否已经存在于列表中，其实在这里加锁和释放锁还是有所交锋的（虽然概率很小），只是锁的粒度很小而已。
    private ConcurrentHashMap<Thread,waitLink> isInLink = new ConcurrentHashMap<>();
    // 尾锁
    private final Object linkLastLock = new Object();

    //    private volatile int reLockTimes = 0;
    // 用于填充nodeChildren
    //private final Object object = new Object();
    /**
     * 用于唤醒等待着的队列
     */
    private BlockingQueue<Integer> blockingQueue =new ArrayBlockingQueue<Integer>(1);

    class waitNode extends WeakReference<Thread> {
        public waitNode(Thread referent) {
            super(referent);
        }
    }

    /**
     * 删除或是在尾部或是在中间位置，极少会使头部，当然如果并发量没有那么高的话其实是会在头部的。
     *      断开      断开        断开
     *    （可能）（lockOwner）  （唤醒）
     *      ----      ----      ----
     *     |node|<-->|node|<-->|node|
     *      ----      ----      ----
     * 添加总是在头部添加
     *     添加
     *    等待者
     *     ----      ----      ----
     *    |node|<-->|node|<-->|node|
     *     ----      ----      ----
     */
    class waitLink{
        waitLink pre = null,next = null;
        waitNode node = null;
        public waitLink(){

        }
        public waitLink(waitNode node){
            this.node = node;
        }
    }
    /**
     * 设置头部，考虑到新节点，尾结点和中间节点
     * @param node  需要调整的节点
     */
    public void setHead(waitLink node){
        if (head == null) {
            head = node;
            tail = node;
        }else {
            node.next = head;
            head.pre = node;
            head = node;
        }
    }

    /**
     * 设置尾部，考虑到新节点，头结点和中间节点
     * @param node  需要调整的节点
     */
    @Deprecated
    public void setTail(waitLink node){
        if (node.pre == null && node.next == null){
            if (tail == null) {
                head = node;
                tail = node;
            }else {
                tail.next = node;
                node.pre = tail;
                tail = node;
            }
        }else if(node.pre == null){
            node.next.pre = null;
            head = node.next;
            node.next = null;
            node.pre = tail;
            tail.next = node;
            tail = node;
        }else if (node.pre != null && node.next != null){
            node.pre.next = node.next;
            node.next.pre = node.pre;
            node.next = null;
            node.pre = tail;
            tail.next = node;
            tail = node;
        }
    }

    /**
     * 添加节点
     * 这里假设head为null的话那么tail也为null
     */
    public void addNode(waitLink node){
        if (head == null)
            setHead(node);
        else {

            tail.next = node;
            node.pre = tail;
            tail = node;
        }
    }

    /**
     * 删除尾部，快速删除
     */
    public waitLink deleteTailNode(){
        if (tail == null)
            return null;
        waitLink temp = null;
        if (tail == head) {
            temp = head;
            head.next = null;
            head = null;
            tail.pre = null;
            tail = null;
        }else{
            waitLink pre = tail.pre;
            temp = tail;
            pre.next = null;
            tail.pre = null;
            tail = pre;
        }
        return temp;
    }

    /**
     *  删除头部
     */
    public void deleteHeadNode(){
        if (head == null)
            return;
        head.node = null;
        if (tail == head){
            head.next = null;
            head = null;
            tail.pre = null;
            tail = null;
        }else {
            waitLink next = head.next;
            next.pre = null;
            head.next = null;
            head = next;
        }
    }

    /**
     * 删除节点，从中间删除
     *
     * @param node 待删除的节点
     */
    public void deleteNode(waitLink node){
        if (node == null)
            System.out.println("//////////////////");
        if (node.next == null) {
            waitLink waitLink = deleteTailNode();
            waitLink.node = null;
        } else if (node.pre == null) {
            deleteHeadNode();
        } else {
            node.next.pre = node.pre;
            node.pre.next = node.next;
            node.next = null;
            node.pre = null;
            node.node = null;
            node = null;
        }
    }

    public DistributeLockImplement() throws RemoteException{

    }

    /**
     * 使用了双重检查机制来保证lockOwner只会被一个线程访问，所以是安全的。
     * 1.先判断lockOwner是否为null，如果为null那么就抢锁
     * 2.在判断当前抢锁的client是不是为lockOwner，如果为lockOwner那么就增加冲入次数。
     * 3.如果都不是那么就加入等待队列，并等待当前的持有锁的释放。
     * 4.非公平
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean tryLock(NodeInterface lockNode) throws RemoteException{
        /**
         * 每一次的远程调用使用的都是不同的对象，所以锁也是不一样的，这个时候应该将锁提取出来，不应该放在这里。
         * 使用不同的线程锁都不一样。
         */

        System.out.println(Thread.currentThread().getName()+"-----"+lockNode.getThisNodeType());
        Thread thread = Thread.currentThread();
        if (lockOwner == null) {
            synchronized (distributeLock) {
                if (lockOwner == null) {
                    System.out.println(Thread.currentThread().getName()+"-----"+lockNode.getThisNodeType());

                    lockOwner = lockNode;
                    waitLink waitLink = new waitLink(new waitNode(thread));
                    setHead(waitLink);
                    isInLink.put(thread,waitLink);
//                    waiterList.add(thread);
                    reLockTimes.incrementAndGet();
                    System.out.println(Thread.currentThread().getName()+"-----"+lockNode.getThisNodeType() + "获取锁");
                    return true;
                } else if (lockOwner == lockNode) {  //在这里，如程序走到这里，其他线程恰好将lockOwner置空，那么将会报错
                    reLockTimes.incrementAndGet();
                    return true;
                }else {
                    if (!isCurrentThreadInLink(thread)){
                        waitLink waitLink = new waitLink(new waitNode(thread));
                        addNode(waitLink);
                        isInLink.put(thread,waitLink);
                        System.out.println(Thread.currentThread().getName() + "-----" + lockNode.getThisNodeType() + "加入队列等待");
                    }
//                    if (!waiterList.contains(thread)) {
//                        waiterList.add(thread);
//                        System.out.println(Thread.currentThread().getName() + "-----" + lockNode.getThisNodeType() + "加入队列等待");
//                    }
                }
            }
        }else if (lockOwner == lockNode){
            reLockTimes.incrementAndGet();
            return true;
        }else {
            synchronized (distributeLock) {
                if (!isCurrentThreadInLink(thread)){
                    waitLink waitLink = new waitLink(new waitNode(thread));
                    addNode(waitLink);
                    isInLink.put(thread,waitLink);
                    System.out.println(Thread.currentThread().getName() + "-----" + lockNode.getThisNodeType() + "加入队列等待");
                }
//                if (!waiterList.contains(thread)) {
//                    waiterList.add(thread);
//                    System.out.println(Thread.currentThread().getName() + "-----" + lockNode.getThisNodeType() + "加入队列等待");
//                }
            }
        }

        /**
         * 枷锁失败则将其加入到等待列表中，等待主节点释放锁
         */
        try {
            /**
             * 等待锁释放并重新抢锁
             * 非公平抢锁
             */
            LockSupport.park();
            System.out.println(Thread.currentThread().getName() + "-----" + lockNode.getThisNodeType() + "被唤醒");
            return tryLock(lockNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(thread.getName()+"失败了");
        return false;
    }

    /**
     * 将lockOwner置为null，将当前的第一个从候选列表中去除
     * 1.判断当前的client是不是lockOwner，如果是那么就将冲入次数减一
     * 2.如果重入次数为0，那么就将lockOwner置为null，并将当前的client从等待队列中删除.
     * 3.向blockingQueue添加数据，是的阻塞的线程被唤醒。
     * @return  返回true
     * @throws RemoteException
     */
    @Override
    public boolean tryRelease(NodeInterface releaseNode) throws RemoteException {
//        System.out.println(Thread.currentThread().getName());
        if (isLockOwner(releaseNode)) {
            Thread thread = Thread.currentThread();
            synchronized (distributeLock) {
                reLockTimes.decrementAndGet();
                if (reLockTimes.get() == 0) {
                    //安全的置空
                    lockOwner = null;
                    System.out.println(Thread.currentThread().getName() + "-----" + releaseNode.getThisNodeType() + "锁释放");
                }
            }
            try {
                /**
                 * 唤醒后面的线程
                 * 使用holdLockNode进行加速删除
                 * 这里使用的原因是在删除holdLockNode的时候会用到前一个节点，
                 * 所以要保证前一个节点也不是head的情况下可以实现安全操作
                 */
                synchronized (linkLastLock){
                    waitLink waitLink1 = isInLink.get(thread);
                    // ||(head.next != null && waitLink1 == head.next) 不需要再加上这个了，修改的是不同的数据，所以没有数据的冲突。
                    if (waitLink1 == head) {
                        synchronized (distributeLock) {
                            deleteNode(waitLink1);
                            isInLink.remove(thread);
//                        waiterList.remove(Thread.currentThread());
                            if (head != null) {
                                waitLink waitLink = deleteTailNode();
                                LockSupport.unpark(waitLink.node.get());
                                Thread thread1 = waitLink.node.get();
                                /**
                                 * 如果这里不删除，只是简单的执行waitLink = null;那么代码会出现问题，
                                 * 所以还需要将其从map中删除
                                 */
                                if (thread1!=null)
                                    isInLink.remove(thread1);
                                waitLink.node = null;
                                waitLink = null;
                            }
                        }
                    }else{
                        /**
                         * 由于holdLockNode会在trylock中被重新赋值，之前的那个就没有人格引用指向了，
                         * 会被垃圾回收期发现并回收掉所以没有内存泄漏的问题
                         */

                        deleteNode(waitLink1);
                        isInLink.remove(thread);
//                        waiterList.remove(Thread.currentThread());
                        if (head != null) {
                            waitLink waitLink = deleteTailNode();
                            LockSupport.unpark(waitLink.node.get());
                            Thread thread1 = waitLink.node.get();
                            if (thread1!=null)
                                isInLink.remove(thread1);
                            waitLink.node = null;
                            waitLink = null;
                        }
                    }
                }
//                        if (waiterList.size() > 0)
//                            LockSupport.unpark(waiterList.remove(0));
//                System.out.println("当前剩余的等待者数量为：" + waiterList.size());
//                Thread.sleep(10);
            } catch(Exception e){
                e.printStackTrace();
            }
            return true;
        }else return false;
    }

    /**
     * 判断是不是当前实例是不是当前锁对象
     * @param releaseNode  当前实例
     * @return  如果节点类型、节点密码和节点uid一样那么就认为是一样的。
     * @throws RemoteException
     */
    public boolean isLockOwner(NodeInterface releaseNode) throws RemoteException {
        try {
            return releaseNode.getThisNodeType().equals(lockOwner.getThisNodeType())
                    && releaseNode.getThisPassword().equals(lockOwner.getThisPassword())
                    && releaseNode.getThisCurrentNode().equals(lockOwner.getThisCurrentNode());
        }catch (Exception e){
            System.out.println(releaseNode.getThisNodeType()+"lockOwner为空");
            return false;
        }
    }

    /**
     * 判断当前线程是否在列表中
     * @param thread
     * @return
     */
    public boolean isCurrentThreadInLink(Thread thread){
        return isInLink.containsKey(thread);
    }
}
