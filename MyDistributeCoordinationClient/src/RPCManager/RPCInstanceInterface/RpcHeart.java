package RPCManager.RPCInstanceInterface;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 心跳包
 */
public interface RpcHeart extends Remote , Serializable {
    public static final long serialVersionUID = -1L;

    /**
     * 定期发送心跳包给其他节点，时间为5秒
     * @param rpcHeart
     * @param uid
     * @return
     * @throws RemoteException
     */
    public boolean sendRpcHeart(RPCSelectInstance rpcHeart, String uid) throws RemoteException;
}
