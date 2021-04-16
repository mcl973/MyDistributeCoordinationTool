package NodeManager.Node;

import NodeManager.Node.NodeInterfaces.DistributeLock;
import NodeManager.Node.NodeInterfaces.NodeInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class DistributeLockImplement extends UnicastRemoteObject implements DistributeLock {
    // 锁
    private transient final Object distributeLock = new Object();
    // 存放等待锁的线程,需要设置为静态，用来保存数据使用，不然会出现幽灵数据，即无论怎么天剑，数据都不会出现。
    // 可以定制策略，是随机调用等待者还是先到先得还是后到先得，默认是先到先得。
    private transient volatile ArrayList<Thread> waiterList = new ArrayList<Thread>();
//    private ConcurrentHashMap<NodeInterface,Object> waiterList = new ConcurrentHashMap<NodeInterface,Object>();
    // 锁的持有者，这是一个问题，由于是static的，所以会造成多线程的问题，即被其他线程修改了数据，后再去释放。
    private transient volatile NodeInterface lockOwner = null;
    // 重入次数
    private transient volatile AtomicInteger reLockTimes = new AtomicInteger();
//    private volatile int reLockTimes = 0;
    // 用于填充nodeChildren
    //private final Object object = new Object();
    /**
     * 用于唤醒等待着的队列
     */
    private BlockingQueue<Integer> blockingQueue =new ArrayBlockingQueue<Integer>(1);

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
    public boolean tryLock(NodeInterface lockNode) throws RemoteException, ExecutionException, InterruptedException, TimeoutException {
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
                    waiterList.add(thread);
                    reLockTimes.incrementAndGet();
                    System.out.println(Thread.currentThread().getName()+"-----"+lockNode.getThisNodeType() + "获取锁");
                    return true;
                } else if (lockOwner == lockNode) {  //在这里，如程序走到这里，其他线程恰好将lockOwner置空，那么将会报错
                    reLockTimes.incrementAndGet();
                    return true;
                }else {
                    if (!waiterList.contains(thread)) {
                        waiterList.add(thread);
                        System.out.println(Thread.currentThread().getName() + "-----" + lockNode.getThisNodeType() + "加入队列等待");
                    }
                }
            }
        }else if (lockOwner == lockNode){
            reLockTimes.incrementAndGet();
            return true;
        }else {
            synchronized (distributeLock) {
                if (!waiterList.contains(thread)) {
                    waiterList.add(thread);
                    System.out.println(Thread.currentThread().getName() + "-----" + lockNode.getThisNodeType() + "加入队列等待");
                }
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
        } catch (InterruptedException e) {
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
            synchronized (distributeLock) {
                reLockTimes.decrementAndGet();
                if (reLockTimes.get() == 0) {
                    //安全的置空
                    lockOwner = null;
                    System.out.println(Thread.currentThread().getName() + "-----" + releaseNode.getThisNodeType() + "锁释放");
                    try {
                        /**
                         * 唤醒后面的线程
                         */
                        waiterList.remove(Thread.currentThread());
                        if (waiterList.size() > 0)
                            LockSupport.unpark(waiterList.remove(0));
                        System.out.println("当前剩余的等待者数量为：" + waiterList.size());
                        Thread.sleep(10);
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
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
}
