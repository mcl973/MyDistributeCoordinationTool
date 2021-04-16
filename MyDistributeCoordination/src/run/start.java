package run;

import NodeManager.Domin.DominNode;
import NodeManager.Node.NodeChild;
import NodeManager.Node.NodeInterfaces.NodeInterface;
import NodeManager.Node.nodeConfig.nodeConfig;
import RPCManager.RPC.Export;
import Select.startToSelect;

import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

public class start {
    public static DominNode dominNode;

    static {
        try {
            dominNode = new DominNode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Export export = new Export();
            export.registryNewPort(nodeConfig.port);

            NodeChild nodeChild = new NodeChild();
            /**
             * 改变属性值
             */
            //////改变updateTime
            Class<? extends NodeChild> aClass = nodeChild.getClass();
            Field updateTime = aClass.getDeclaredField("updateTime");
            updateTime.setAccessible(true);
            updateTime.set(nodeChild,System.currentTimeMillis());
            /////改变nodeType
            Field nodeType = aClass.getDeclaredField("nodeType");
            nodeType.setAccessible(true);
            nodeType.set(nodeChild,nodeConfig.create);
            ///暴露nodechild
            export.exportObjectsForNewRegistry(nodeChild,nodeConfig.create);
            //将create装填进domin
            TreeMap<String,NodeInterface> allNodes = dominNode.getAllNodes();
            allNodes.put(nodeChild.getThisNodeType(),nodeChild);
            /**
             * 暴露dominnode
             */
            export.exportObjectsForNewRegistry(dominNode,nodeConfig.domin);

            startToSelect startToSelect = new startToSelect();
            startToSelect.startSelect();
            ReentrantLock reentrantLock = new ReentrantLock();
            reentrantLock.lock();
            reentrantLock.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
