package RPCManager.RPC;


import java.util.Iterator;
import java.util.TreeSet;

/**
 * 通过选举的端口获取各自暴露的端口即在选举的端口上加100
 */
public class RPCConfig {
    public static TreeSet<Integer> exportPortList = new TreeSet<>();
    public static int currentPort = 0;
    public static int masterPort = 0;
   static {

   }
}
