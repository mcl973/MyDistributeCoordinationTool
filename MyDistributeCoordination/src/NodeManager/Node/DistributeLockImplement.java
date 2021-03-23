package NodeManager.Node;

import NodeManager.Node.NodeInterfaces.DistributeLock;
import NodeManager.Node.NodeInterfaces.NodeInterface;
import RPCManager.RPCInstanceInterface.RPCSelectInstance;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DistributeLockImplement extends UnicastRemoteObject implements DistributeLock {
    // 锁
    private NodeInterface nodeChild = null;
    // 存放等待着
    private ConcurrentHashMap<NodeInterface,Object> nodeChildren = new ConcurrentHashMap<NodeInterface,Object>();
    // 锁的持有者
    private static NodeInterface lockOwner = null;
    // 重入次数
    private int reLockTimes = 0;
    // 用于填充nodeChildren
    private final Object object = new Object();
    /**
     * 用于唤醒等待着的队列
     */
    private BlockingQueue<Integer> blockingQueue =new ArrayBlockingQueue<Integer>(1);

    public DistributeLockImplement(NodeInterface nodeChild) throws RemoteException{
        this.nodeChild = nodeChild;
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
    public boolean tryLock(NodeInterface lockNode) throws RemoteException {
        if (lockOwner == null) {
            synchronized (nodeChild) {
                if (lockOwner == null) {
                    lockOwner = lockNode;
                    nodeChildren.put(lockNode,object);
                    return true;
                }
            }
        }else if (lockOwner == lockNode) {
                reLockTimes++;
                return true;
        }

        /**
         * 枷锁失败则将其加入到等待列表中，等待主节点释放锁
         */
        nodeChildren.put(lockNode,object);
        try {
            /**
             * 等待锁释放并重新抢锁
             */
            blockingQueue.take();
            tryLock(lockNode);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
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
        if (releaseNode == lockOwner) {
            reLockTimes--;
            if (reLockTimes == 0) {
                lockOwner = null;
                nodeChildren.remove(releaseNode);
                try {
                    /**
                     * 唤醒后面的线程
                     */
                    blockingQueue.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
