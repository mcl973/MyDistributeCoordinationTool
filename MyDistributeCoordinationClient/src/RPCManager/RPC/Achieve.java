package RPCManager.RPC;

import NodeManager.Domin.DominInterface;
import NodeManager.Node.NodeInterfaces.DistributeLock;
import NodeManager.Node.NodeInterfaces.NodeInterface;
import RPCManager.RPCInstanceInterface.RPCSelectInstance;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Achieve implements GetRemoteObject {

    @Override
    public RPCSelectInstance getRemoteObject(String type) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(RPCConfig.masterPort);
        RPCSelectInstance lookup = null;
        try {
            lookup = (RPCSelectInstance)registry.lookup(type+"");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return lookup;
    }

    @Override
    public RPCSelectInstance getRemoteObject(String host, int port, String type) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(host,port);
        RPCSelectInstance lookup = null;
        try {
            lookup = (RPCSelectInstance)registry.lookup(type+"");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return lookup;
    }

    @Override
    public NodeInterface getRemoteObject(String type, int port) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(port);
        NodeInterface lookup = null;
        try {
            lookup = (NodeInterface)registry.lookup(type+"");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return lookup;
    }

    @Override
    public NodeInterface getRemoteObject(String type, String host, int port) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(host,port);
        NodeInterface lookup = null;
        try {
            lookup = (NodeInterface)registry.lookup(type+"");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return lookup;
    }

    @Override
    public RPCSelectInstance getRemoteObjectForDistributeNode(String type, int port) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(port);
        RPCSelectInstance lookup = null;
        try {
            lookup = (RPCSelectInstance)registry.lookup(type+"");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return lookup;
    }

    @Override
    public RPCSelectInstance getRemoteObjectForDistributeNode(String type, String host, int port) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(host,port);
        RPCSelectInstance lookup = null;
        try {
            lookup = (RPCSelectInstance)registry.lookup(type+"");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return lookup;
    }

    @Override
    public DominInterface getRemoteObjectForDomin(String type, int port) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(port);
        DominInterface lookup = null;
        try {
            lookup = (DominInterface)registry.lookup(type+"");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return lookup;
    }

    @Override
    public DominInterface getRemoteObjectForDomin(String type, String host, int port) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(host,port);
        DominInterface lookup = null;
        try {
            lookup = (DominInterface)registry.lookup(type+"");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return lookup;
    }

    @Override
    public DistributeLock getRemoteObjectForDistributeLock(String type, int port) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(port);
        DistributeLock lookup = null;
        try {
            lookup = (DistributeLock)registry.lookup(type+"");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return lookup;
    }

    @Override
    public DistributeLock getRemoteObjectForDistributeLock(String type, String host, int port) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(host,port);
        DistributeLock lookup = null;
        try {
            lookup = (DistributeLock)registry.lookup(type+"");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return lookup;
    }

    @Override
    public RPCSelectInstance[] getRemoteObjects() throws RemoteException {
        return null;
    }
}
