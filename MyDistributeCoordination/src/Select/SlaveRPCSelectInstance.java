package Select;

import NodeManager.Node.NodeChild;
import NodeManager.Node.NodeInterfaces.NodeInterface;
import RPCManager.RPC.Achieve;
import RPCManager.RPC.Export;
import RPCManager.RPC.RPCConfig;
import RPCManager.RPC.rpcType;
import RPCManager.RPCInstanceInterface.RPCSelectInstance;
import run.DistributeConfig;
import run.start;
import run.threadPoolManager;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class SlaveRPCSelectInstance extends UnicastRemoteObject implements RPCSelectInstance {
    private transient static boolean masterIsUp = false;
    public transient static RPCSelectInstance master = null;
    /**
     * 对于主节点的定时器
     */
    private transient static Thread listenMasterThread = null;
    public static volatile boolean isTimeStart = true;

    public SlaveRPCSelectInstance() throws RemoteException{
        this(0);
    }
    public SlaveRPCSelectInstance(int port) throws RemoteException{
//        super(port);
        Export export = new Export();
        export.exportObject(this,rpcType.RPCSLAVE.toString());
    }

    /**
     * 将数据传递给master并处理
     * 先获取到master的实例
     * @param thisuid
     * @return
     * @throws RemoteException
     */
    @Override
    public int receiveList(String thisuid, RPCSelectInstance rpcInstance) throws RemoteException {
        Achieve achieve = new Achieve();
        if (!DistributeConfig.localOrRemote)
            master = achieve.getRemoteObject(rpcType.RPCMASTER.toString());
        else
            master = achieve.getRemoteObject(DistributeConfig.portToIpMap.get(RPCConfig.masterPort),
                    RPCConfig.masterPort,rpcType.RPCMASTER.toString());

        return master.receiveList(thisuid,rpcInstance);
    }


    /**
     * 将数据传给master
     * @param thisuid 当前节点的uid
     * @throws RemoteException
     */
    @Override
    public void putList(String thisuid) throws RemoteException {
        receiveList(thisuid,this);
    }

    @Override
    public void putVoteList(HashSet<String> voteResults) throws RemoteException {
        Iterator<String> iterator = voteResults.iterator();
        while (iterator.hasNext()){
            String next = iterator.next();
            SelectConfig.list.add(next);
            String port = next.split(":")[1];
            int i = Integer.parseInt(port) + 100;
            RPCConfig.exportPortList.add(i);
        }
    }

    @Override
    public boolean receiveVoteList(HashSet<String> voteResults) throws RemoteException {
        if (voteResults.size()>= SelectConfig.halfover) {
            if (isTimeStart) {
                Runnable timeRunnable = new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * 此时数据还是要返回的，等到主节点收到数据后并开启线程，这是才能开始线程，所以预留的时间时10毫秒。
                         */
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        while (true) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!masterIsUp) {
                                unBindJmi();
                                removeMasterInfoAndUpdateInfo();
                                chooseMasterAgain();
                                break;
                            }
                            masterIsUp = false;
                        }
                        System.out.println("Thread dead....");
                    }
                    /**
                     * 删除当前的句柄和暴露的接口和实例
                     */
                    public void unBindJmi(){
                        try {
                            Export.registry.unbind(rpcType.RPCSLAVE.toString());
                            UnicastRemoteObject.unexportObject(startToSelect.rpcInstance, true);
                        } catch (RemoteException | NotBoundException e) {
                            e.printStackTrace();
                        }
                        startToSelect.rpcInstance = null;
                        System.gc();
                    }
                    /**
                     * 去除主节点的信息,更新变量信息
                     */
                    public void removeMasterInfoAndUpdateInfo(){
                        String master = "localhost:"+(RPCConfig.masterPort-100);
                        SelectConfig.list.remove(master);
                        RPCConfig.exportPortList.remove(RPCConfig.masterPort);
                        SelectConfig.halfover = (SelectConfig.list.size() - 1) / 2;
                        if (SelectConfig.halfover == 0)
                            SelectConfig.halfover = 1;
                    }
                    /**
                     * 重新选主
                     */
                    public void chooseMasterAgain(){
                        RPCConfig.masterPort = RPCConfig.exportPortList.first();
                        System.out.println("当前的主节点port是："+RPCConfig.masterPort);
                        System.out.println("当前的主节点是："+SelectConfig.list.first());
                        if (SelectConfig.list.first().equals(SelectConfig.thisUid)) {
                            try {
                                System.out.println("主节点已建立");
                                startToSelect.rpcInstance = new MasterRPCSelectInstance(SelectConfig.port);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Thread.sleep(5000);
                                startToSelect.rpcInstance = new SlaveRPCSelectInstance(SelectConfig.port);
                                startToSelect.rpcInstance.putList(SelectConfig.thisUid);
                                isTimeStart = true;
                            } catch (RemoteException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                threadPoolManager.threadPoolExecutor.execute(timeRunnable);
                isTimeStart = false;
            }
            return true;
        } else return false;
    }

    @Override
    public boolean sendRpcHeart(RPCSelectInstance rpcHeart, String uid) throws RemoteException {
        System.out.println("主节点还活着。。。");
        masterIsUp = true;
        return true;
    }

    @Override
    public void removeNodes(ArrayList<String> removeNodes) throws RemoteException {
        for (String removeNode : removeNodes) {
            SelectConfig.list.remove(removeNode);
            String exportPort = removeNode.split(":")[1]+100;
            RPCConfig.exportPortList.remove(Integer.parseInt(exportPort));
        }
    }

    @Override
    public void synchronizedData(TreeMap<String,NodeInterface> allNodes,String synType) throws RemoteException {
        if (synType.equals("ADD")||synType.equals("MODIFY"))
            start.dominNode.allNodes.putAll(allNodes);
        else if(synType.equals("DELETE")){
            Set<String> strings = allNodes.keySet();
            for (String string : strings) {
                start.dominNode.allNodes.remove(string);
            }
        }
    }

    @Override
    public boolean tryLock(NodeInterface nodeLock) throws RemoteException {
        return false;
    }

    @Override
    public boolean tryRelease(NodeInterface nodeRelease) throws RemoteException {
        return false;
    }

    @Override
    public boolean setGlobalLockForNode(String nodeType) throws RemoteException {
        return true;
    }

    @Override
    public long getGlobalIndex() throws RemoteException {
        return 0;
    }

    @Override
    public void doWatch() throws RemoteException {

    }

    @Override
    public void doWatch(String nodeType) throws RemoteException {

    }

    @Override
    public void doWatch(NodeChild nodeChild) throws RemoteException {

    }

    @Override
    public void setWatch(String type, String uid, String password) throws RemoteException {

    }

    @Override
    public boolean addNode(NodeInterface nodeChild) throws RemoteException {
        return false;
    }

    @Override
    public boolean modifyNode(NodeInterface nodeChild) throws RemoteException {
        return false;
    }

    @Override
    public boolean deleteNode(String nodeType, String currentNode, String password) throws RemoteException {
        return false;
    }
}
