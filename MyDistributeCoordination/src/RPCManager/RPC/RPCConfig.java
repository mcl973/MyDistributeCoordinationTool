package RPCManager.RPC;

import Select.SelectConfig;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * 通过选举的端口获取各自暴露的端口即在选举的端口上加100
 */
public class RPCConfig {
    public static TreeSet<Integer> exportPortList = new TreeSet<>();
    // 暴露MasterRPCSelectInstance和SlaveRPCSelectInstance的端口
    public static int currentPort = 0;
    // 主节点所使用的的port
    public static int masterPort = 0;
   static {
       TreeSet<String> list = SelectConfig.list;
       Iterator<String> iterator = list.iterator();
       while (iterator.hasNext()){
           String s = iterator.next();
           String[] split = s.split(":");
           int i1 = Integer.parseInt(split[1])+100;
           exportPortList.add(i1);
           if (s.equals(SelectConfig.thisUid))
               currentPort = i1;
       }
       masterPort = exportPortList.first();
   }
}
