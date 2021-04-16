package NodeManager.Node.NodeInterfaces;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GetGlobalLockForNode extends Serializable, Remote {
    public static final long serialVersionUID = -1L;

    /**
     * 得到全局唯一针对某个节点的锁
     * @param nodeType 被争抢的节点的类型
     * @return 返回拥有这个的锁
     * @throws RemoteException
     */
    public boolean setGlobalLockForNode(String nodeType) throws RemoteException;
}
