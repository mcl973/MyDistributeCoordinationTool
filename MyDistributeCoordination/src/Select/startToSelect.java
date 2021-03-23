package Select;

import RPCManager.RPC.Achieve;
import RPCManager.RPC.RPCConfig;
import RPCManager.RPC.rpcType;
import RPCManager.RPCInstanceInterface.RPCSelectInstance;
import run.DistributeConfig;

import java.rmi.RemoteException;

public class startToSelect {
    public static RPCSelectInstance rpcInstance = null;
    public boolean isMaster(){
        return SelectConfig.list.first().equals(SelectConfig.thisUid);
    }
    public void startSelect(){
        /**
         * 重启恢复使用
         */
        boolean isRestart = false;
        for (Integer integer : RPCConfig.exportPortList) {
            if (integer!=RPCConfig.currentPort){
                Achieve achieve = new Achieve();
                try {
                    RPCSelectInstance remoteObject = null;
                    if (!DistributeConfig.localOrRemote)
                        remoteObject = achieve.getRemoteObjectForDistributeNode(rpcType.RPCMASTER.toString(), integer);
                    else
                        remoteObject = achieve.getRemoteObjectForDistributeNode(rpcType.RPCMASTER.toString(),
                                DistributeConfig.portToIpMap.get(integer), integer);

                    if (remoteObject!=null){
                        isRestart = true;
                        RPCConfig.masterPort = integer;

                        rpcInstance = new SlaveRPCSelectInstance(SelectConfig.port);
                        rpcInstance.putList(SelectConfig.thisUid);
                        break;
                    }
                } catch (RemoteException e) {
                    System.out.println("节点"+integer+"不是主节点");
                }
            }
        }

        /**
         * 正常开启
         */
        if (!isRestart) {
            if (isMaster()) {
                try {
                    rpcInstance = new MasterRPCSelectInstance(SelectConfig.port);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    rpcInstance = new SlaveRPCSelectInstance(SelectConfig.port);
                    rpcInstance.putList(SelectConfig.thisUid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
