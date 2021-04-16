package NodeManager.Node.NodeInterfaces;

import NodeManager.Node.NodeChild;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 设置监听器
 */
public interface PutWatch extends Serializable, Remote {
    public static final long serialVersionUID = -1L;

    /**
     * 设置监听器
     * @param type  节点类型
     * @param uid  想要设置监听的节点的uid
     * @throws RemoteException
     */
    public void setWatch(String type,String uid,String password) throws RemoteException;
}
