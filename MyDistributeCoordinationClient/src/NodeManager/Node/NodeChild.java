package NodeManager.Node;

import NodeManager.Node.NodeInterfaces.NodeInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.TreeSet;

/**
 * 基类
 */
public class NodeChild extends UnicastRemoteObject implements NodeInterface {
    private String owner =null;
    private String nodeType = null;
    private TreeSet<DateNode> nodevalve = new TreeSet<>();
    private String currentNode = null;
    private long updateTime = -1L;
    private String password = null;
    private String registerIp = null;
    public NodeChild() throws RemoteException{
//        this(0);
    }
    public NodeChild(int port) throws RemoteException{
//        super(port);
    }
    public NodeChild(String owner, String nodeType, String currentNode,
                     long updateTime, String password,TreeSet<DateNode> nodevalve,int port) throws RemoteException{
        this(port);
        this.owner = owner;
        this.nodeType = nodeType;
        this.currentNode = currentNode;
        this.updateTime = updateTime;
        this.password = password;
        this.nodevalve.addAll(nodevalve);
    }
    public NodeChild(String owner, String nodeType, String currentNode, long updateTime, String password,DateNode dateNode,int port) throws RemoteException {
        this(port);
        this.owner = owner;
        this.nodeType = nodeType;
        this.currentNode = currentNode;
        this.updateTime = updateTime;
        this.password = password;
        this.nodevalve.add(dateNode);
    }

    @Override
    public int hashCode() {
        return nodeType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        NodeChild other = (NodeChild)obj;
        return this.nodeType.equals(other.nodeType);
    }

    @Override
    public void doWatch() throws RemoteException {

    }

    @Override
    public void doWatch(String nodeType) throws RemoteException {

    }

    @Override
    public void doWatch(NodeChild nodeChild) throws RemoteException {

    }

    @Override
    public NodeInterface createNode(String uid, String nodeType, DateNode value) throws RemoteException {
        NodeChild nodeChild = new NodeChild();
        nodeChild.nodeType = nodeType;
        nodeChild.getThisNodevalve().add(value);
        nodeChild.owner = uid;
        nodeChild.updateTime = System.currentTimeMillis();
        nodeChild.currentNode = uid;
        try {
            SynchronizedQueueClass.blockingQueue.put(nodeChild);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return nodeChild;
    }

    @Override
    public NodeInterface createNode(String uid, String nodeType, DateNode value, String password) throws RemoteException {
        NodeChild nodeChild = new NodeChild();
        nodeChild.getThisNodevalve().add(value);
        nodeChild.nodeType = nodeType;
        nodeChild.owner = uid;
        nodeChild.updateTime = System.currentTimeMillis();
        nodeChild.password = password;
        nodeChild.currentNode = uid;
        try {
            SynchronizedQueueClass.blockingQueue.put(nodeChild);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return nodeChild;
    }

    @Override
    public String getThisOwner() throws RemoteException{
        return owner;
    }

    @Override
    public String getThisNodeType() throws RemoteException{
        return nodeType;
    }

    @Override
    public TreeSet<DateNode> getThisNodevalve() throws RemoteException{
        return nodevalve;
    }

    @Override
    public String getThisCurrentNode() throws RemoteException{
        return currentNode;
    }

    @Override
    public long getThisUpdateTime() throws RemoteException{
        return updateTime;
    }

    @Override
    public String getThisPassword() throws RemoteException {
        return password;
    }

    @Override
    public void setRegisterIP(String registerIP) throws RemoteException {
        this.registerIp = registerIP;
    }

    @Override
    public String getRegisterIP() throws RemoteException {
        return registerIp;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        NodeChild clone = null;
        try {
            clone = new NodeChild();
            clone.registerIp = this.getRegisterIP();
            clone.nodevalve = (TreeSet<DateNode>)this.getThisNodevalve().clone();
            clone.currentNode = this.getThisCurrentNode();
            clone.password = this.getThisPassword();
            clone.updateTime = this.getThisUpdateTime();
            clone.owner = this.getThisOwner();
            clone.nodeType = this.getThisNodeType();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return clone;
    }
}