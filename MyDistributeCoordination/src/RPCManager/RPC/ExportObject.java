package RPCManager.RPC;

import NodeManager.Domin.DominNode;
import NodeManager.Node.NodeChild;
import NodeManager.Node.NodeInterfaces.DistributeLock;
import NodeManager.Node.NodeInterfaces.NodeInterface;
import RPCManager.RPCInstanceInterface.RPCSelectInstance;
import java.rmi.RemoteException;

/**
 * 这里的作用就是为了让客户端能够发现并获取协调器的实例
 */
public interface ExportObject {
    public static final long serialVersionUID = -1L;

    /**
     * 暴露对象，这时端口已经被注册，无需注册端口,为各个协调节点同步准备的
     * @param object  对象
     * @param type  类型
     * @throws RemoteException
     */
    void exportObject(RPCSelectInstance object, String type) throws RemoteException;

    /**
     * 暴露多个对象，这时端口已经被注册，无需注册端口，为各个协调节点同步准备的
     * @param object 对象集合
     * @param type 对象集合类型
     * @throws RemoteException
     */
    void exportObjects(RPCSelectInstance[] object, String type) throws RemoteException;

    /**
     * 注册新的端口，为用户准备
     * @param port 端口
     * @throws RemoteException
     */
    void registryNewPort(int port) throws RemoteException;

    /**
     * 对于新的注册的端口，暴露NodeChild对象，为用户准备的
     * @param object 对象
     * @param type 类型
     * @throws RemoteException
     */
    void exportObjectsForNewRegistry(NodeChild object, String type) throws RemoteException;
    void exportObjectsForNewRegistry(NodeInterface object, String type) throws RemoteException;



    /**
     * 对于新的注册的端口，暴露DominNode对象，为用户准备的
     * @param object 对象
     * @param type 类型
     * @throws RemoteException
     */
    void exportObjectsForNewRegistry(DominNode object, String type) throws RemoteException;

    /**
     * 暴露的对象为分布式锁
     * @param object 分布式锁接口
     * @param type 节点类型
     * @throws RemoteException
     */
    void exportObjectsForNewRegistry(DistributeLock object, String type) throws RemoteException;
}
