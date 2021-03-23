package RPCManager.RPC;

import NodeManager.Domin.DominInterface;
import NodeManager.Node.NodeInterfaces.DistributeLock;
import NodeManager.Node.NodeInterfaces.NodeInterface;
import RPCManager.RPCInstanceInterface.RPCSelectInstance;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 在这里主要是为了各个master之间的协调做准备，即协调器不会主动的去找客户端要数据，因为不知道客户端在哪里
 */
public interface GetRemoteObject extends Remote {
    public static final long serialVersionUID = -1L;

    /**
     * 得到RPCSelectInstance实例，面向分布式协调节点使用
     * @param type  类型，一般为master或是slave
     * @return 返回RPCSelectInstance实例
     * @throws RemoteException
     */
    public RPCSelectInstance getRemoteObject(String type) throws RemoteException;

    /**
     * 获取指定主机上的指定端口暴露的RPCSelectInstance类
     * @param host 主机
     * @param port 暴露的端口
     * @param type 暴露的类类型
     * @return  返回RPCSelectInstance实例
     * @throws RemoteException
     */
    public RPCSelectInstance getRemoteObject(String host,int port,String type) throws RemoteException;

    /**
     * 得到RPCSelectInstance实例，面向分布式协调节点使用，主要是在一开始判断是否有主节点使用
     * @param type  类型，为amster
     * @param port  暴露的端口
     * @return  返回RPCSelectInstance实例
     * @throws RemoteException
     */
    public RPCSelectInstance getRemoteObjectForDistributeNode(String type, int port) throws RemoteException;

    /**
     *获取指定主机上的指定端口暴露的RPCSelectInstance类
     * @param host 主机
     * @param port 暴露的端口
     * @param type 类型
     * @return
     * @throws RemoteException
     */
    public RPCSelectInstance getRemoteObjectForDistributeNode(String type, String host,int port) throws RemoteException;

    /**
     * 得到NodeInterface实例，面向client使用，用于获取具体的NodeChild实例
     * @param type  实例类型
     * @param port  对clcient暴露的端口
     * @return  返回NodeInterface实例
     * @throws RemoteException
     */
    public NodeInterface getRemoteObject(String type, int port) throws RemoteException;

    /**
     *  获取指定主机上的指定端口暴露的NodeInterface类
     * @param type 类类型
     * @param host 主机
     * @param port 暴露的端口
     * @return
     * @throws RemoteException
     */
    public NodeInterface getRemoteObject(String type, String host,int port) throws RemoteException;

    /**
     * 获取DominInterface实例，用于client使用获取具体的NodeChild
     * @param type 类型，一般为domin
     * @param port 端口为服务端对client暴露的接口，一般为10200
     * @return  返回DominInterface实例
     * @throws RemoteException
     */
    public DominInterface getRemoteObjectForDomin(String type, int port) throws RemoteException;

    /**
     *  获取指定主机上的指定端口暴露的DominInterface类
     * @param type 类类型
     * @param host 主机
     * @param port 暴露的端口
     * @return
     * @throws RemoteException
     */
    public DominInterface getRemoteObjectForDomin(String type, String host,int port) throws RemoteException;

    /**
     * 获取DistributeLock实例，用于client使用获取具体的NodeChild
     * @param type 类型，一般为domin
     * @param port 端口为服务端对client暴露的接口，一般为10200
     * @return  返回DistributeLock实例
     * @throws RemoteException
     */
    public DistributeLock getRemoteObjectForDistributeLock(String type, int port) throws RemoteException;

    /**
     *  获取指定主机上的指定端口暴露的DistributeLock类
     * @param type 类类型
     * @param host 主机
     * @param port 暴露的端口
     * @return 返回DistributeLock实例
     * @throws RemoteException
     */
    public DistributeLock getRemoteObjectForDistributeLock(String type, String host,int port) throws RemoteException;

    RPCSelectInstance[] getRemoteObjects() throws RemoteException;
}
