package RPCManager.RPC;

import NodeManager.Domin.DominNode;
import NodeManager.Node.NodeChild;
import NodeManager.Node.NodeInterfaces.DistributeLock;
import RPCManager.RPCInstanceInterface.RPCSelectInstance;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Export  implements ExportObject {
    public static Registry registry;
    public static Registry newRegistry;

    static {
        try {
            registry = LocateRegistry.createRegistry(RPCConfig.currentPort);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exportObject(RPCSelectInstance object, String type) throws RemoteException {
        registry.rebind(type,object);
    }

    @Override
    public void exportObjects(RPCSelectInstance[] object, String type) throws RemoteException {

    }

    @Override
    public void registryNewPort(int port) throws RemoteException {
        try {
            newRegistry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exportObjectsForNewRegistry(NodeChild object, String type) throws RemoteException {
        newRegistry.rebind(type,object);
    }

    @Override
    public void exportObjectsForNewRegistry(DominNode object, String type) throws RemoteException {
        newRegistry.rebind(type,object);
    }

    @Override
    public void exportObjectsForNewRegistry(DistributeLock object, String type) throws RemoteException {
        newRegistry.rebind(type,object);
    }
}
