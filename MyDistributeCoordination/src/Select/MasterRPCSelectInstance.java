package Select;

import NodeManager.Node.NodeChild;
import NodeManager.Node.NodeInterfaces.NodeInterface;
import RPCManager.RPC.*;
import RPCManager.RPCInstanceInterface.AbstractRPCInstance;
import RPCManager.RPCInstanceInterface.RPCSelectInstance;
import RPCManager.RPCInstanceInterface.SelectOperate;
import run.DistributeConfig;
import run.start;
import run.threadPoolManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class MasterRPCSelectInstance extends UnicastRemoteObject implements RPCSelectInstance {
    // 投票结果列表
    public static transient HashSet<String> voteList = new HashSet<>();
    /**
     * 防止其他协调节点的容器
     */
    public static transient HashMap<String, RPCSelectInstance> selectOperateHashMap = new HashMap<>();
    /**
     * 心跳包，记录节点是否存在
     */
    public static transient HashMap<String,Boolean> heartMap = new HashMap<>();

    /**
     * 检测是否超时
     */
    public static volatile boolean isTimeStart = true;

    /**
     * 用作锁
     */
    public static final Object object = new Object();

    public MasterRPCSelectInstance() throws RemoteException{
        Export export = new Export();
        try {
            export.exportObject(this, rpcType.RPCMASTER.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int receiveList(String thisuid, RPCSelectInstance rpcInstance) throws RemoteException {
        synchronized (object) {
            if (!selectOperateHashMap.containsKey(thisuid)){
                SendHeartPacket sendHeartPacket = new SendHeartPacket(this, thisuid);
                threadPoolManager.threadPoolExecutor.execute(sendHeartPacket);
            }
            selectOperateHashMap.put(thisuid,rpcInstance);
            SelectConfig.list.add(thisuid);
            voteList.add(thisuid);
            for (String s : SelectConfig.list) {
                if (!s.equals(SelectConfig.thisUid)&&!s.equals(thisuid)){
                    RPCSelectInstance rpcSelectInstance = selectOperateHashMap.get(s);
                    HashSet<String> strings = new HashSet<>();
                    strings.add(thisuid);
                    rpcSelectInstance.putVoteList(strings);
                }
            }

            SelectConfig.halfover = (SelectConfig.list.size()-1)/2;
            if (voteList.size() >= SelectConfig.halfover) {
                putVoteList(voteList);
                return 1;
            }
            return 0;
        }
    }

    @Override
    public void putList(String thisuid) throws RemoteException {

    }

    /**
     * 将数据发送给其他slave
     * @param voteResults
     * @throws RemoteException
     */
    @Override
    public void putVoteList(HashSet<String> voteResults) throws RemoteException {
        if (receiveVoteList(voteResults)){

        }else
            System.out.println("主节点不是本节点");
    }

    /**
     * 先获取到slave的实例，在进行下一步的操作
     * @param voteResults
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean receiveVoteList(HashSet<String> voteResults) throws RemoteException {
        Set<String> strings = selectOperateHashMap.keySet();
        Iterator<String> iterator = strings.iterator();
        while (iterator.hasNext()){
            String next = iterator.next();
            SelectOperate selectOperate = selectOperateHashMap.get(next);
            selectOperate.receiveVoteList(voteResults);
        }
        return true;
    }

    /**
     * @param rpcHeart  心跳实例
     * @param uid  节点uid
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean sendRpcHeart(RPCSelectInstance rpcHeart, String uid) throws RemoteException {
        RPCSelectInstance rpcInstance = selectOperateHashMap.get(uid);
        try {
            rpcInstance.sendRpcHeart(this, uid);
            heartMap.put(uid,true);
        }catch (Exception e){
            heartMap.put(uid, false);
            synchronized (object) {
                SelectConfig.list.remove(uid);
                selectOperateHashMap.remove(uid);
                voteList.remove(uid);
                ArrayList<String> list = new ArrayList<>();
                list.add(uid);
                rpcInstance.removeNodes(list);
            }
        }
        return true;
    }

    @Override
    public void removeNodes(ArrayList<String> removeNodes) throws RemoteException {

    }

    @Override
    public void synchronizedData(TreeMap<String,NodeInterface> allNodes,String synType) throws RemoteException {
        Set<String> strings = selectOperateHashMap.keySet();
        Iterator<String> iterator = strings.iterator();
        ArrayList<Runnable> runnables = new ArrayList<>();

        while (iterator.hasNext()){
            String next = iterator.next();
            RPCSelectInstance rpcSelectInstance = selectOperateHashMap.get(next);
            System.out.println("/////////////////"+next);
            runnables.add(()->{
                try {
                    /**
                     * 此操作是一个阻塞的操作，所以将其放到这里
                     */
                    rpcSelectInstance.synchronizedData(allNodes, synType);
                } catch (RemoteException e) {
                    System.out.println("Select.MasterRPCInstance->主节点想副本同步数据出现问题。");
                    e.printStackTrace();
                }
            });
        }
        for (Runnable runnable : runnables) {
            threadPoolManager.threadPoolExecutor.execute(runnable);
        }
    }

    @Override
    public boolean setGlobalLockForNode(String nodeType) throws RemoteException {
       return start.dominNode.setGlobalLockForNode(nodeType);
    }

    @Override
    public long getGlobalIndex() throws RemoteException {
        return start.dominNode.getGlobalIndex();
    }

    @Override
    public void setWatch(String type, String uid, String password) throws RemoteException {
        start.dominNode.setWatch(type,uid,password);
    }

    @Override
    public boolean addNode(NodeInterface nodeChild,String ip,int port) throws RemoteException {
        Achieve achieve = new Achieve();
        String thisCurrentNode = nodeChild.getThisCurrentNode();
        String[] split = thisCurrentNode.split(":");
        NodeInterface remoteObject = null;
        if (!DistributeConfig.localOrRemote)
            remoteObject = achieve.getRemoteObject("add_" + nodeChild.getThisNodeType(),port);
        else
            remoteObject = achieve.getRemoteObject(
                    "add_" + nodeChild.getThisNodeType(),ip,port);
        return start.dominNode.addNode(remoteObject,ip,port);
    }

    @Override
    public boolean modifyNode(NodeInterface nodeChild,String ip,int port) throws RemoteException {
        Achieve achieve = new Achieve();
        String thisCurrentNode = nodeChild.getThisCurrentNode();
        String[] split = thisCurrentNode.split(":");
        NodeInterface remoteObject  = null;
        if (!DistributeConfig.localOrRemote)
            remoteObject = achieve.getRemoteObject("modify_" + nodeChild.getThisNodeType(),port);
        else
            remoteObject = achieve.getRemoteObject(
                    "modify_" + nodeChild.getThisNodeType(),ip,port);
        return start.dominNode.modifyNode(remoteObject,ip,port);
    }

    @Override
    public boolean deleteNode(String nodeType, String currentNode, String password) throws RemoteException {
        return start.dominNode.deleteNode(nodeType,currentNode,password);
    }

    class SendHeartPacket implements Runnable{
        private RPCSelectInstance rpcSelectInstance;
        private String uid;

        public SendHeartPacket(RPCSelectInstance rpcSelectInstance, String uid) {
            this.rpcSelectInstance = rpcSelectInstance;
            this.uid = uid;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sendRpcHeart(this.uid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        public boolean sendRpcHeart(String uid) throws RemoteException {
            RPCSelectInstance rpcInstance = selectOperateHashMap.get(uid);
            try {
                rpcInstance.sendRpcHeart(this.rpcSelectInstance, uid);
                heartMap.put(uid,true);
            }catch (Exception e){
                heartMap.put(uid, false);
                synchronized (object) {
                    SelectConfig.list.remove(uid);
                    selectOperateHashMap.remove(uid);
                    voteList.remove(uid);
                    ArrayList<String> list = new ArrayList<>();
                    list.add(uid);
                    rpcInstance.removeNodes(list);
                }
            }
            return true;
        }
    }
}
