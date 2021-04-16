package NodeManager.Node.NodeChildSub;

import NodeManager.Node.DateNode;
import NodeManager.Node.NodeChild;
import NodeManager.Node.NodeInterfaces.NodeInterface;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.TreeSet;

public class SayHello extends NodeChild {
    public SayHello(NodeInterface nodeChild) throws RemoteException{
        this(nodeChild.getThisOwner(),
                nodeChild.getThisNodeType(),
                nodeChild.getThisCurrentNode(),
                nodeChild.getThisUpdateTime(),
                nodeChild.getThisPassword(),
                nodeChild.getThisNodevalve(),0);
    }
    public SayHello(String owner, String nodeType, String currentNode, long updateTime, String password,DateNode dateNode,int port) throws RemoteException{
        super(owner,nodeType,currentNode,updateTime,password,dateNode,port);
    }
    public SayHello(String owner, String nodeType, String currentNode, long updateTime, String password,TreeSet<DateNode> nodevalve,int port) throws RemoteException{
        super(owner,nodeType,currentNode,updateTime,password,nodevalve,port);
    }
    @Override
    public void doWatch() throws RemoteException {
        System.out.println("监听的数据发生了改变。。。");
    }

    @Override
    public void doWatch(String nodeType) throws RemoteException {

    }

    @Override
    public void doWatch(NodeChild nodeChild) throws RemoteException {
        TreeSet<DateNode> thisNodevalve = nodeChild.getThisNodevalve();
        Iterator<DateNode> iterator = thisNodevalve.iterator();
        while (iterator.hasNext()){
            DateNode next = iterator.next();
            byte[] values = next.getValues();
            System.out.println(new String(values));
        }
    }
}
