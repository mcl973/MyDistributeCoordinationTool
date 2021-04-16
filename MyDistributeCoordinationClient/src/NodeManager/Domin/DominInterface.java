package NodeManager.Domin;

import NodeManager.Node.NodeInterfaces.*;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * 这里的容器都是非安全的，后续要将其升级为安全的容器
 */
public interface DominInterface extends Remote, Serializable, PutWatch, DoWatch, GetGlobalLockForNode, GetGlobalIndex {
    public static final long serialVersionUID = -1L;

    /**
     * 返回所有的nodeChild
     * @return
     * @throws RemoteException
     */
    public TreeMap<String, NodeInterface> getAllNodes() throws RemoteException;

    /**
     * 返回窜访节点与密码，key为节点的uid，value为密码。
     * @return
     * @throws RemoteException
     */
    public TreeMap<String,String> getNodePasswordSave() throws RemoteException;

    /**
     * 返回节点的监听列表，key为nodechild，value为DoWatch列表
     * @return
     * @throws RemoteException
     */
    public HashMap<String, ArrayList<DoWatch>> getNodeWatchList() throws RemoteException;

    /**
     * 添加节点，这个时候如果节点携带了密码，那么就会将此节点的密码，owner和currentNode信息去除掉。并存放在NodePasswordSave和AllNodes中
     * @param nodeChild  需要存放的nodeChild
     * @return
     * @throws RemoteException
     */
    public boolean addNode(NodeInterface nodeChild,String ip,int port) throws RemoteException;

    /**
     * 修改节点，如果有密码那么就对比密码。
     * @param nodeChild 修改的节点实例
     * @return
     * @throws RemoteException
     */
    public boolean modifyNode(NodeInterface nodeChild,String ip,int port) throws RemoteException;

    /**
     * 删除节点，如果有密码那么就对比密码。
     * @param nodeType  节点类型
     * @param currentNode 当前节点
     * @param password 密码
     * @return 如果删除成功返回true
     * @throws RemoteException
     */
    public boolean deleteNode(String nodeType, String currentNode, String password) throws RemoteException;
}
