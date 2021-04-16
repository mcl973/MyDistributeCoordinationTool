package NodeManager.Domin;

import NodeManager.Node.NodeChild;
import NodeManager.Node.NodeInterfaces.DistributeLock;
import NodeManager.Node.NodeInterfaces.DoWatch;
import NodeManager.Node.NodeInterfaces.NodeInterface;
import RPCManager.RPC.Achieve;

import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 存放NodeChild数据并同步数据向其他协调节点
 */
public class DominNode extends UnicastRemoteObject implements DominInterface {

    enum synOperation{
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
        DELETE
    }

    public  volatile long lastTime = 0L;
    public DominNode() throws RemoteException{
        this(0);
    }

    public DominNode(int port) throws RemoteException{
        super(port);

    }

    /**
     * TreeMap<String,NodeChild> map = new TreeMap<type,NodeChild>
     */
   public TreeMap<String, NodeInterface> allNodes = new TreeMap<String, NodeInterface>();
    /**
     * TreeMap<String,String> map = new TreeMap<uid,password>
     */
   public TreeMap<String,String> nodePasswordSave = new TreeMap<String,String>();

   private HashMap<String,DistributeLock> distributeLockHashMap = new HashMap<>();
   private AtomicLong globalLong = new AtomicLong();
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
            TreeMap<String, NodeInterface> temp = new TreeMap<String, NodeInterface>();
            Iterator<NodeInterface> iterator = allNodes.values().iterator();
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
            //startToSelect.rpcInstance.synchronizedData(temp,synType.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TreeMap<String, NodeInterface> getAllNodes() throws RemoteException {
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
    public boolean addNode(NodeInterface nodeChild,String ip,int  port) throws RemoteException {
        String nodeType = nodeChild.getThisNodeType();

        if (nodeChild.getThisPassword()!=null&&!nodeChild.getThisPassword().equals("")) {
            String thisCurrentNode = nodeChild.getThisCurrentNode();
            String thisPassword = nodeChild.getThisPassword();
            nodePasswordSave.put(thisCurrentNode,thisPassword);
            //去除必要的关键信息，如password，currentNode，owner，此时的owner和currentNode应该是一样的。

        }
        allNodes.put(nodeType,nodeChild);
        SynchronizedAllNodes(synOperation.ADD);
        return true;
    }

    @Override
    public boolean modifyNode(NodeInterface nodeChild,String ip,int port) throws RemoteException {
        String currentNode = nodeChild.getThisCurrentNode();
        String password = nodeChild.getThisPassword();
        String nodeType = nodeChild.getThisNodeType();
        if(nodePasswordSave.containsKey(nodeChild.getThisCurrentNode())) {
            String savepassword = nodePasswordSave.get(currentNode);
            if (password != null && savepassword != null && savepassword.equals(password)) {
                if (!allNodes.containsKey(nodeType)) {
                    return false;
                }
                SynchronizedAllNodes(synOperation.MODIFY);
                doWatch(nodeChild.getThisNodeType());
                return true;
            }
        }else {
//            allNodes.put(nodeType, nodeChild);
            SynchronizedAllNodes(synOperation.MODIFY);
            doWatch(nodeType);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteNode(String nodeType,String currentNode,String password) throws RemoteException {
        if (nodePasswordSave.containsKey(nodeType)) {
            String savepassword = nodePasswordSave.get(currentNode);
            if (password != null && savepassword != null && savepassword.equals(password)) {
                if (!allNodes.containsKey(nodeType)) {
                    return false;
                }
                allNodes.remove(nodeType);
                return true;
            }
        }else{
            allNodes.remove(nodeType);
            return true;
        }
        return false;
    }

    /**
     * 修改对象中的某个属性的值
     * @param nodeChild  对象
     * @param fieldName  属性名
     * @param value      修改值
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void changeObjectFieldValue(NodeChild nodeChild, String fieldName, Object value) throws RemoteException, NoSuchFieldException, IllegalAccessException {
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
        ArrayList<DoWatch> doWatches = nodeWatchList.get(nodeType);
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
        String s = uid.split(":")[1];
        NodeInterface nodeChild = achieve.getRemoteObject(nodeType, Integer.parseInt(s));
        String s1 = nodePasswordSave.get(nodeType);
        if (password!=null&&s1!=null&&password.equals(s1)){
            if (!nodeWatchList.containsKey(nodeType)){
                ArrayList<DoWatch> doWatches = new ArrayList<>();
                nodeWatchList.put(nodeType,doWatches);
                doWatches.add(nodeChild);
            }else
                nodeWatchList.get(nodeType).add(nodeChild);
        }else {
            if (!nodeWatchList.containsKey(nodeType)){
                ArrayList<DoWatch> doWatches = new ArrayList<>();
                nodeWatchList.put(nodeType,doWatches);
                doWatches.add(nodeChild);
            }else
                nodeWatchList.get(nodeType).add(nodeChild);
        }
    }

    @Override
    public long getGlobalIndex() throws RemoteException {
        return 0L;
    }

    @Override
    public boolean setGlobalLockForNode(String nodeType) throws RemoteException {
        return true;
    }
}
