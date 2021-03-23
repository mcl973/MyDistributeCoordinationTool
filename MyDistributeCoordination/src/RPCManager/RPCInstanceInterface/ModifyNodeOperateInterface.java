package RPCManager.RPCInstanceInterface;

import NodeManager.Node.NodeInterfaces.NodeInterface;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ModifyNodeOperateInterface extends Serializable, Remote {
    public static final long serialVersionUID = -1L;
    /**
     * 添加节点，这个时候如果节点携带了密码，那么就会将此节点的密码，owner和currentNode信息去除掉。并存放在NodePasswordSave和AllNodes中
     * @param nodeChild  需要存放的nodeChild
     * @return
     * @throws RemoteException
     */
    public boolean addNode(NodeInterface nodeChild) throws RemoteException;

    /**
     * 修改节点，如果有密码那么就对比密码。
     * @param nodeChild 修改的节点实例
     * @return
     * @throws RemoteException
     */
    public boolean modifyNode(NodeInterface nodeChild) throws RemoteException;

    /**
     * 删除节点，如果有密码那么就对比密码。
     * @param nodeType  节点类型
     * @param currentNode 当前节点
     * @param password 密码
     * @return 如果删除成功返回true
     * @throws RemoteException
     */
    public boolean deleteNode(String nodeType,String currentNode,String password) throws RemoteException;
}
