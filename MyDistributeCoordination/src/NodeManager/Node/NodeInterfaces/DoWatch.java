package NodeManager.Node.NodeInterfaces;

import NodeManager.Node.NodeChild;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 做监听器监听的事
 */
public interface DoWatch extends Serializable, Remote {
    public static final long serialVersionUID = -1L;

    /**
     * 只是简单的通知远程节点，他所监听的节点已经发生了改变。
     * @throws RemoteException
     */
    public void doWatch() throws RemoteException;

    /**
     * 监听的节点发生了变化，根据节点类型来做具体的事情。
     * @param nodeType 节点类型
     * @throws RemoteException
     */
    public void doWatch(String nodeType) throws RemoteException;

    /**
     * 监听的节点发生了变化，根据节点实例来做具体的事情。
     * @param nodeChild  具体的节点实例
     * @throws RemoteException
     */
    public void doWatch(NodeChild nodeChild) throws RemoteException;
}
