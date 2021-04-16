package RPCManager.RPCInstanceInterface;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;

/**
 * 选主操作，主要利用这个阶段将主节点的消息传递个其他节点
 */
public interface SelectOperate extends Serializable, Remote {
    public static final long serialVersionUID = -1L;

    /**
     * 如果同意此节点为master，那么就将自己uid通过这个操作返回给master，
     *  这调用jmi来实现远程调用，是slave调用的
     * @param thisuid
     * @return 自己的uid
     */
    public int receiveList(String thisuid, RPCSelectInstance rpcInstance) throws RemoteException;

    /**
     *  其他节点将自己的数据返回给主节点
     * @param thisuid
     * @return
     */
    public void putList(String thisuid) throws RemoteException;

    /**
     * 主节点将选举的结果告知给其他节点，在这里面需要调用其他的节点的实例了
     * @param voteResults
     */
    public void putVoteList(HashSet<String> voteResults) throws RemoteException;

    /**
     * 其他节点收到了主节点告知的投票的结果,这里就需要调用远程的代码，
     *  通过jmi来实现远程调用,是master调用
     * @param voteResults
     */
    public boolean receiveVoteList(HashSet<String> voteResults) throws RemoteException;
}
