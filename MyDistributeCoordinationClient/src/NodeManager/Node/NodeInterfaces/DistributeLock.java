package NodeManager.Node.NodeInterfaces;

import RPCManager.RPCInstanceInterface.RPCSelectInstance;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DistributeLock extends Serializable, Remote {
    public static final long serialVersionUID = -1L;
    /**
     * 尝试枷锁
     * @return 如果成功返回true
     * @throws RemoteException
     */
    public boolean tryLock(NodeInterface nodeLock) throws RemoteException;

    /**
     * 尝试解锁
     * @return 成功返回true
     * @throws RemoteException
     */
    public boolean tryRelease(NodeInterface nodeRelease) throws RemoteException;
}
