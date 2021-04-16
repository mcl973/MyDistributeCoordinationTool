package RPCManager.RPCInstanceInterface;

import NodeManager.Node.NodeChild;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 在各个节点之间同步数据
 */
public interface SynchronizedData extends Remote, Serializable {
    public static final long serialVersionUID = -1L;

    /**
     * 在各个分布式协调节点之间同步数据
     * @param allNodes  带同步的数据
     * @throws RemoteException
     */
    public void synchronizedData(ConcurrentSkipListSet<NodeChild> allNodes) throws RemoteException;
}
