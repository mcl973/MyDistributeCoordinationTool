package NodeManager.Domin;

import NodeManager.Node.*;
import NodeManager.Node.NodeInterfaces.DistributeLock;
import NodeManager.Node.NodeInterfaces.DoWatch;
import NodeManager.Node.NodeInterfaces.GetGlobalLockForNode;
import NodeManager.Node.NodeInterfaces.NodeInterface;
import NodeManager.Node.nodeConfig.nodeConfig;
import RPCManager.RPC.Achieve;
import RPCManager.RPC.Export;
import RPCManager.RPC.RPCConfig;
import RPCManager.RPCInstanceInterface.RPCSelectInstance;
import Select.SelectConfig;
import Select.SlaveRPCSelectInstance;
import Select.startToSelect;
import run.DistributeConfig;

import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 存放NodeChild数据并同步数据向其他协调节点
 */
public class DominNode extends UnicastRemoteObject implements DominInterface {

    public enum synOperation{
        /**
         * 同步添加
         */
        ADD,
        /**
         * 同步修改
         */
        MODIFY,
        /**
         * 同步删除
         */
        DELETE,
        /**
         * 全局唯一id
         */
        GLOBALINDEX
    }

    public  volatile long lastTime = 0L;
    public DominNode() throws RemoteException{
//        this(0);
    }

    public DominNode(int port) throws RemoteException{
//        super(port);
    }

    /**
     * TreeMap<String,NodeChild> map = new TreeMap<type,NodeChild>
     */
   public TreeMap<String, NodeInterface> allNodes = new TreeMap<String,NodeInterface>();
    /**
     * 同步锁
     */
   private final Object synLock = new Object();
    /**
     * TreeMap<String,String> map = new TreeMap<uid,password>
     */
   public TreeMap<String,String> nodePasswordSave = new TreeMap<String,String>();
    /**
     * HashMap<String,DistributeLock> map = new HashMap<lockType,DistributeLock>();
     */
   private HashMap<String,DistributeLock> distributeLockHashMap = new HashMap<>();
    /**
     * 删除节点的结合
     */
   private TreeMap<String,NodeInterface> deleteNodeSet = new TreeMap<>();
    /**
     * 全局唯一id
     */
   public static final AtomicLong globalLong = new AtomicLong();
    /**
     * key:nodetype
     * value:DoWatch list
     */
    public static final HashMap<String, ArrayList<DoWatch>> nodeWatchList = new HashMap<>();
    /**
     * 同步数据，这里应该是每一次改动都需要更新，但应该是增量更新
     */
    private void SynchronizedAllNodes(synOperation synType){
        try {
            TreeMap<String,NodeInterface> temp = new TreeMap<String,NodeInterface>();
            Iterator<NodeInterface> iterator = null;
            TreeMap<String, NodeInterface> clone = null;
            synchronized (synLock) {
                clone = (TreeMap<String, NodeInterface>) allNodes.clone();
            }
            iterator = clone.values().iterator();
            long maxtime = lastTime;
            while (iterator.hasNext()){
                NodeInterface next = iterator.next();
                if(next.getThisUpdateTime()>lastTime){
                    temp.put(next.getThisNodeType(),next);
                    if (next.getThisUpdateTime()>maxtime){
                        maxtime = next.getThisUpdateTime();
                    }
                }
            }
            lastTime = maxtime;
            if (synType == synOperation.DELETE)
                startToSelect.rpcInstance.synchronizedData(deleteNodeSet,synType.toString());
            else
                startToSelect.rpcInstance.synchronizedData(temp,synType.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TreeMap<String,NodeInterface> getAllNodes() throws RemoteException {
        return allNodes;
    }

    @Override
    public TreeMap<String, String> getNodePasswordSave() throws RemoteException {
        return nodePasswordSave;
    }

    @Override
    public HashMap<String, ArrayList<DoWatch>> getNodeWatchList() throws RemoteException {
        return nodeWatchList;
    }

    @Override
    public boolean addNode(NodeInterface nodeChild,String ip,int port) throws RemoteException {
        String nodeType = nodeChild.getThisNodeType();
        if (isMaster()) {
            if (nodeChild.getThisPassword() != null && !nodeChild.getThisPassword().equals("")) {
                String thisCurrentNode = nodeChild.getThisCurrentNode();
                String thisPassword = nodeChild.getThisPassword();
                nodePasswordSave.put(thisCurrentNode, thisPassword);
                //去除必要的关键信息，如password，currentNode，owner，此时的owner和currentNode应该是一样的。
            }
            allNodes.put(nodeType, nodeChild);
            SynchronizedAllNodes(synOperation.ADD);
            return true;
        }
        Achieve achieve = new Achieve();
        NodeInterface remoteObject = achieve.getRemoteObject(nodeType, ip, port);
        Export export = new Export();
        export.exportObjectsForNewRegistry(remoteObject,"add_"+nodeType);
        nodeChild.setRegisterIP(DistributeConfig.portToIpMap.get(RPCConfig.currentPort));
        return SlaveRPCSelectInstance.master.addNode(nodeChild,SelectConfig.localhost,nodeConfig.port);
    }

    @Override
    public boolean modifyNode(NodeInterface nodeChild,String ip,int port) throws RemoteException {
        String currentNode = nodeChild.getThisCurrentNode();
        String[] split = currentNode.split(":");
        System.out.println(nodeChild.getThisUpdateTime()+"<---->"+lastTime);
        if (isMaster()) {
            String password = nodeChild.getThisPassword();
            String nodeType = nodeChild.getThisNodeType();
            /**
             * 验证密码的正确性，如果该节点没有持有密码，那么返回false；
             */
            if (nodePasswordSave.containsKey(nodeChild.getThisCurrentNode())) {
                String savepassword = nodePasswordSave.get(currentNode);
                if (savepassword != null && savepassword.equals(password)) {
                    if (!allNodes.containsKey(nodeType)) {
                        return false;
                    }
                    allNodes.put(nodeType,nodeChild);
                    SynchronizedAllNodes(synOperation.MODIFY);
                    doWatch(nodeChild.getThisNodeType());
                    return true;
                }
            } else {
//                SynchronizedAllNodes(synOperation.MODIFY);
//                doWatch(nodeType);
                return false;
            }
        }
        Achieve achieve = new Achieve();
        NodeInterface remoteObject = achieve.getRemoteObject(nodeChild.getThisNodeType(), ip, port);
        Export export = new Export();
        export.exportObjectsForNewRegistry(remoteObject,"modify_"+nodeChild.getThisNodeType());
        return SlaveRPCSelectInstance.master.modifyNode(nodeChild,SelectConfig.localhost,nodeConfig.port);
    }

    @Override
    public synchronized boolean deleteNode(String nodeType,String currentNode,String password) throws RemoteException {
        deleteNodeSet.clear();
        if (isMaster()) {
            if (nodePasswordSave.containsKey(nodeType)) {
                String savepassword = nodePasswordSave.get(currentNode);
                if (password != null && savepassword != null && savepassword.equals(password)) {
                    if (!allNodes.containsKey(nodeType)) {
                        return false;
                    }
                    deleteNodeSet.put(nodeType,allNodes.get(nodeType));
                    allNodes.remove(nodeType);
                    SynchronizedAllNodes(synOperation.DELETE);
                    return true;
                }
            } else {
                deleteNodeSet.put(nodeType,allNodes.get(nodeType));
                allNodes.remove(nodeType);
                SynchronizedAllNodes(synOperation.DELETE);
                return true;
            }
            return false;
        }
        return SlaveRPCSelectInstance.master.deleteNode(nodeType,currentNode,password);
    }

    /**
     * 修改对象中的某个属性的值
     * @param nodeChild  对象
     * @param fieldName  属性名
     * @param value      修改值
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void changeObjectFieldValue(NodeChild nodeChild,String fieldName,Object value) throws RemoteException, NoSuchFieldException, IllegalAccessException {
        Class<? extends NodeChild> aClass = nodeChild.getClass();
        try {
            Field declaredField = aClass.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(nodeChild,value);
        }catch (NoSuchFieldException e){
            Class<?> superclass = aClass.getSuperclass();
            Field declaredField = superclass.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(nodeChild,value);
        }
    }

    @Override
    public void doWatch() throws RemoteException {

    }

    @Override
    public void doWatch(String nodeType) throws RemoteException {
        ArrayList<DoWatch> doWatches = (ArrayList<DoWatch>)nodeWatchList.get(nodeType).clone();
        for (DoWatch doWatch : doWatches) {
            doWatch.doWatch();
        }
    }

    @Override
    public void doWatch(NodeChild nodeChild) throws RemoteException {

    }

    @Override
    public void setWatch(String type,String uid,String password) throws RemoteException {
        String nodeType = type;
        Achieve achieve = new Achieve();
        String[] s = uid.split(":");
        if (isMaster()) {
            NodeInterface nodeChild = null;
            if (!DistributeConfig.localOrRemote)
                nodeChild = achieve.getRemoteObject(nodeType, Integer.parseInt(s[1]));
            else
                nodeChild = achieve.getRemoteObject(nodeType, s[0], Integer.parseInt(s[1]));

            String s1 = nodePasswordSave.get(nodeType);
            if (password != null && s1 != null && password.equals(s1)) {
                if (!nodeWatchList.containsKey(nodeType)) {
                    ArrayList<DoWatch> doWatches = new ArrayList<>();
                    nodeWatchList.put(nodeType, doWatches);
                    doWatches.add(nodeChild);
                } else
                    nodeWatchList.get(nodeType).add(nodeChild);
            } else {
                if (!nodeWatchList.containsKey(nodeType)) {
                    ArrayList<DoWatch> doWatches = new ArrayList<>();
                    nodeWatchList.put(nodeType, doWatches);
                    doWatches.add(nodeChild);
                } else
                    nodeWatchList.get(nodeType).add(nodeChild);
            }
        }else {
            SlaveRPCSelectInstance.master.setWatch(type,uid,password);
        }
    }

    @Override
    public long getGlobalIndex() throws RemoteException {
        if (isMaster()) {
            long l = globalLong.incrementAndGet();
            TreeMap<String,NodeInterface> allNodes = new TreeMap<>();
            NodeChild nodeChild = new NodeChild();
            Class<? extends NodeChild> aClass = nodeChild.getClass();
            try {
                Field updateTime = aClass.getDeclaredField("updateTime");
                updateTime.setAccessible(true);
                updateTime.set(nodeChild,l);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            allNodes.put("GlobalIndex",nodeChild);
            startToSelect.rpcInstance.synchronizedData(allNodes,synOperation.GLOBALINDEX.toString());
            return l;
        }
        /**
         * 如果不是主节点那么就调用节点的getGlobalIndex
         * 并缓存当前的数据。
         */
        long globalIndex = SlaveRPCSelectInstance.master.getGlobalIndex();
        globalLong.set(globalIndex);
        return globalIndex;
    }

    @Override
    public boolean setGlobalLockForNode(String nodeType) throws RemoteException {
        if (isMaster()) {
            if (!distributeLockHashMap.containsKey(nodeType)) {
                System.out.println("开始暴露");
                Export export = new Export();
                DistributeLock distributeLock = new DistributeLockImplement();
                export.exportObjectsForNewRegistry(distributeLock, nodeConfig.distributeLock+"_"+nodeType);
                distributeLockHashMap.put(nodeConfig.distributeLock+"_"+nodeType,distributeLock);
            }
            return true;
        }
        /**
         * 副本向主节点发起分布式节点锁的获取
         */
        RPCSelectInstance master = SlaveRPCSelectInstance.master;
        boolean b = master.setGlobalLockForNode(nodeType);
        if (b){
            /**
             * jmi的传递性
             * 1.从master哪里获取暴露的分布式锁类。
             * 2.将获取的分布式锁类通过自己的接口暴露出去。
             */
            DistributeLock remoteObjectForDistributeLock = null;
            Achieve achieve = new Achieve();
            if (!DistributeConfig.localOrRemote)
                remoteObjectForDistributeLock =
                        achieve.getRemoteObjectForDistributeLock(
                                nodeConfig.distributeLock+"_"+nodeType,RPCConfig.masterPort+100);
            else
                remoteObjectForDistributeLock =
                        achieve.getRemoteObjectForDistributeLock(
                                nodeConfig.distributeLock+"_"+nodeType,
                                DistributeConfig.portToIpMap.get(RPCConfig.masterPort-100),
                                RPCConfig.masterPort+100);

            Export export = new Export();
            export.exportObjectsForNewRegistry(remoteObjectForDistributeLock,nodeConfig.distributeLock+"_"+nodeType);
        }
        return true;
    }

    /**
     * 是否是主节点
     * @return
     */
    public boolean isMaster(){
        return RPCConfig.masterPort == RPCConfig.currentPort;
    }
}
