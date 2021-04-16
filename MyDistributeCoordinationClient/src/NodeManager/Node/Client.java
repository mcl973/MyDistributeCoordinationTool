package NodeManager.Node;

import NodeManager.Node.NodeInterfaces.DoWatch;

import java.rmi.RemoteException;

public class Client implements DoWatch {
    @Override
    public void doWatch() throws RemoteException {
        System.out.println("你监听的节点发生了改变");
    }

    @Override
    public void doWatch(String nodeType) throws RemoteException {

    }

    @Override
    public void doWatch(NodeChild nodeChild) throws RemoteException {
        if (nodeChild!=null){
            System.out.println();
        }
    }
}
