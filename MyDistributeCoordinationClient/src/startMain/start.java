package startMain;

import NodeManager.Domin.DominInterface;
import NodeManager.Node.DateNode;
import NodeManager.Node.NodeChild;
import NodeManager.Node.NodeChildSub.SayHello;
import NodeManager.Node.NodeInterfaces.DistributeLock;
import NodeManager.Node.NodeInterfaces.NodeInterface;
import NodeManager.Node.nodeConfig.nodeAllConfig;
import NodeManager.Node.nodeConfig.nodeConfig;
import RPCManager.RPC.Achieve;
import RPCManager.RPC.Export;
import com.sun.corba.se.impl.orbutil.graph.NodeData;

import java.lang.reflect.Field;
import java.nio.channels.Selector;
import java.rmi.RemoteException;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class start {
    public static AtomicInteger integer = new AtomicInteger();

    /**
     *
     * @param args 参数，0：domin端口
     */
    public static void main(String[] args) {
        String remoteHost = null;
        if (args.length>0) {
            nodeConfig.port = Integer.parseInt(args[0]);
            nodeAllConfig.localhost = args[1];
            nodeAllConfig.thisuid = nodeAllConfig.localhost + ":" + nodeAllConfig.port;
            nodeAllConfig.port = Integer.parseInt(args[2]);
            remoteHost = args[3];
        }else remoteHost = "localhost";

        Export export = new Export();
        Achieve achieve = new Achieve();
        try {
            export.registryNewPort(nodeAllConfig.port);
            DominInterface domin = null;
            domin = (DominInterface)achieve.getRemoteObjectForDomin("domin", remoteHost, nodeConfig.port);
            TreeMap<String, NodeInterface> allNodes = domin.getAllNodes();
            NodeInterface create = allNodes.get("create");
            DateNode dateNode = new DateNode();
            dateNode.setValues("hello".getBytes());
            SayHello sayHello = new SayHello(create.createNode(nodeAllConfig.thisuid, Thread.currentThread().getName(),dateNode,"test"));
            export.exportObjectsForNewRegistry(sayHello,sayHello.getThisNodeType());
            domin.addNode(sayHello, nodeAllConfig.localhost,nodeAllConfig.port);
            domin.setWatch(sayHello.getThisNodeType(),sayHello.getThisCurrentNode(),"test");

            sayHello.getThisNodevalve().clear();
            dateNode.setValues("再来一次".getBytes());
            sayHello.getThisNodevalve().add(dateNode);
            Class<? extends SayHello> aClass = sayHello.getClass();
            Class<?> superclass = aClass.getSuperclass();
            Field upDateTime = superclass.getDeclaredField("updateTime");
            upDateTime.setAccessible(true);
            upDateTime.set(sayHello,System.currentTimeMillis());

            domin.modifyNode(sayHello,nodeAllConfig.localhost,nodeAllConfig.port);
            System.out.println(domin.getGlobalIndex());
            domin.setGlobalLockForNode(Thread.currentThread().getName());
            DistributeLock remoteObjectForDistributeLock = null;
            String password = Thread.currentThread().getName();
//            try {
//                NodeChild nodeChild = null;
//                nodeChild = new NodeChild(nodeAllConfig.thisuid, Thread.currentThread().getName(), nodeAllConfig.thisuid,
//                        System.currentTimeMillis(), password, new DateNode("lock".getBytes()), -1);
//                remoteObjectForDistributeLock = achieve.getRemoteObjectForDistributeLock(nodeConfig.distributeLock + "_" + password,
//                        remoteHost, nodeConfig.port);
//
//                boolean b = remoteObjectForDistributeLock.tryLock(nodeChild);
//                if (b) {
//                    System.out.println("/////////////上锁" + Thread.currentThread().getName());
//                    boolean b1 = remoteObjectForDistributeLock.tryRelease(nodeChild);
//                    if (b1) {
//                        System.out.println("/////////////开锁" + Thread.currentThread().getName());
//                        int i = integer.incrementAndGet();
//                        System.out.println("成功--》"+i);
//                    }else
//                        System.out.println(Thread.currentThread().getName()+"解锁失败"+b1);
//                }else{
//                    System.out.println(Thread.currentThread().getName()+"加锁失败"+b);
//                }
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new myThread(password,achieve);
            }
            for (int i = 0; i < threads.length; i++) {
                threads[i].start();
                Thread.sleep(5);
            }
        } catch (RemoteException | NoSuchFieldException | IllegalAccessException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class myThread extends Thread{
        private String password = null;
        private Achieve achieve = null;

        public myThread(String password,Achieve achieve) {
            this.password = password;
            this.achieve = achieve;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName());
            try {
                NodeChild nodeChild = null;
                nodeChild = new NodeChild(nodeAllConfig.thisuid, Thread.currentThread().getName(), nodeAllConfig.thisuid,
                        System.currentTimeMillis(), password, new DateNode("lock".getBytes()), -1);
                DistributeLock remoteObjectForDistributeLock = achieve.getRemoteObjectForDistributeLock(nodeConfig.distributeLock + "_" + password,
                        "localhost", nodeConfig.port);

                boolean b = remoteObjectForDistributeLock.tryLock(nodeChild);
                if (b) {
                    System.out.println("/////////////上锁" + Thread.currentThread().getName());
                    boolean b1 = remoteObjectForDistributeLock.tryRelease(nodeChild);
                    if (b1) {
                        System.out.println("/////////////开锁" + Thread.currentThread().getName());
                        int i = integer.incrementAndGet();
                        System.out.println("成功--》"+i);
                    }else
                        System.out.println(Thread.currentThread().getName()+"解锁失败"+b1);
                }else{
                    System.out.println(Thread.currentThread().getName()+"加锁失败"+b);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}