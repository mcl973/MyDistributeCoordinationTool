package NodeManager.Node.NodeInterfaces;

import NodeManager.Node.DateNode;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 创建节点
 * 权限只有两种：有密码的所有权限和没有密码只读权限
 */
public interface CreateNode extends Remote, Serializable {
    public static final long serialVersionUID = -1L;

    /**
     * 创建节点，不监听
     * @param uid  创建者
     * @param nodeType 节点类型
     * @param value 节点数据
     * @return 返回创建的节点
     * @throws RemoteException
     */
    public NodeInterface createNode(String uid, String nodeType, DateNode value) throws RemoteException;

    /**
     *  创建节点，不监听,并携带创建密码，只有直到密码的节点才可以删除和修改节点，其他节点只有只读权限
     * @param uid  创建者
     * @param nodeType 节点类型
     * @param value 节点数据
     * @param password  节点密码
     * @return 返回创建的节点
     * @throws RemoteException
     */
    public NodeInterface createNode(String uid, String nodeType, DateNode value,String password) throws RemoteException;
}
