package NodeManager.Node.NodeInterfaces;

import RPCManager.RPCInstanceInterface.RPCSelectInstance;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface DistributeLock extends Serializable, Remote {
    public static final long serialVersionUID = -1L;
    /**
     * 尝试枷锁
     * @return 如果成功返回true
     * @throws RemoteException
     */
    public boolean tryLock(NodeInterface lockNode) throws RemoteException, ExecutionException, InterruptedException, TimeoutException;

    /**
     * 尝试解锁
     * @return 成功返回true
     * @throws RemoteException
     */
    public boolean tryRelease(NodeInterface releaseNode) throws RemoteException;
}
