package NodeManager.Node.NodeInterfaces;

import NodeManager.Node.DateNode;

import java.rmi.RemoteException;
import java.util.TreeSet;

/**
 * 获取NoedChild的各种属性，这样做的原因是使用jmi，底层是被动态代理过的，所以最终传输的是接口而不是具体的实现类
 */
public interface NodeInterface extends DoWatch,CreateNode,Cloneable/*, Comparable<NodeInterface> */ {
    public String getThisOwner() throws RemoteException;
    public String getThisNodeType() throws RemoteException;
    public TreeSet<DateNode> getThisNodevalve() throws RemoteException;
    public String getThisCurrentNode() throws RemoteException;
    public long getThisUpdateTime() throws RemoteException;
    public String getThisPassword() throws RemoteException;
    void setRegisterIP(String registerIP) throws RemoteException;
    String getRegisterIP() throws RemoteException;
}
