package RPCManager.RPCInstanceInterface;

import NodeManager.Node.NodeInterfaces.*;

import java.rmi.Remote;

/**
 *  jmi底层使用的是java的序列化技术，所以必须要是接口才可以，所以将所有的接口放在了RPCSelectInstance里。
 *  主要是SlaveRPCSelector使用了MasterRPCSelectInstance，所以需要将这个传给SlaveRPCSelector，所以需要一个接口来承接所有的方法。
 */
public  interface RPCSelectInstance extends SelectOperate, RpcHeart, NodeDown,
        SynchronizedData, GetGlobalLockForNode,
        GetGlobalIndex,PutWatch,ModifyNodeOperateInterface{

}
