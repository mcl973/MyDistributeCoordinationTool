package RPCManager.RPCInstanceInterface;

import NodeManager.Node.NodeInterfaces.*;

import java.rmi.Remote;

public  interface RPCSelectInstance extends SelectOperate,RpcHeart,NodeDown,SynchronizedData, GetGlobalLockForNode, DistributeLock, GetGlobalIndex, PutWatch, DoWatch,ModifyNodeOperateInterface {
}
