package NodeManager.Node.NodeInterfaces;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GetGlobalIndex extends Serializable, Remote {
    public static final long serialVersionUID = -1L;

    /**
     * 获取全局的唯一的递增序列
     * @return 返回当前序列
     */
    public long getGlobalIndex() throws RemoteException;
}
