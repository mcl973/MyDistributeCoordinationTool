package RPCManager.RPCInstanceInterface;

import NodeManager.Node.NodeChild;
import NodeManager.Node.NodeInterfaces.*;
import RPCManager.RPC.Achieve;
import RPCManager.RPC.Export;
import RPCManager.RPC.rpcType;
import run.DistributeConfig;
import run.start;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * 不再需要，因为是使用的jmi，而jmi底层使用的是java的序列化技术，所以必须要是接口才可以，所以将所有的接口放在了RPCSelectInstance里。
 */
@Deprecated
public class AbstractRPCInstance extends UnicastRemoteObject implements GetGlobalLockForNode, GetGlobalIndex, PutWatch{
    private RPCSelectInstance rpcSelectInstance = null;
    /**
     * slaveOrMaster is true is master,or is slave
     * default is slave
     */
    private boolean slaveOrMaster = false;
    private String nodeType = null;
    public AbstractRPCInstance() throws RemoteException{}
    public AbstractRPCInstance(boolean slaveOrMaster,String type) throws RemoteException{
        this.slaveOrMaster = slaveOrMaster;
        this.nodeType = type;
        Export export = new Export();
        try {
            export.exportObject(rpcSelectInstance, type);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getGlobalIndex() throws RemoteException {
        if (slaveOrMaster)
            return start.dominNode.getGlobalIndex();
        return 0;
    }

    @Override
    public boolean setGlobalLockForNode(String nodeType) throws RemoteException {
        if (slaveOrMaster)
            return start.dominNode.setGlobalLockForNode(nodeType);
        return true;
    }

    @Override
    public void setWatch(String type, String uid, String password) throws RemoteException {
        if (slaveOrMaster)
            start.dominNode.setWatch(type,uid,password);
    }


    public RPCSelectInstance getRpcSelectInstance() {
        return rpcSelectInstance;
    }

    public void setRpcSelectInstance(RPCSelectInstance rpcSelectInstance) {
        this.rpcSelectInstance = rpcSelectInstance;
    }

    public boolean isSlaveOrMaster() {
        return slaveOrMaster;
    }

    public String getNodeType() {
        return nodeType;
    }
}
