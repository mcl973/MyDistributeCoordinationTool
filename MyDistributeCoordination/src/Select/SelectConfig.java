package Select;

import run.DistributeConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

/**
 * 基础配置，欧诺个来选举主节点和记录信息
 */
public class SelectConfig {
    public volatile static TreeSet<String> list = new TreeSet<>();
    static {
        list.add("localhost:10000");
        list.add("localhost:11001");
        list.add("localhost:12002");
        DistributeConfig.ipSet.add("localhost");
//        DistributeConfig.ipSet.add("localhost");
//        DistributeConfig.ipSet.add("localhost");
        DistributeConfig.portSet.add(10000);
        DistributeConfig.portSet.add(11001);
        DistributeConfig.portSet.add(12002);
        DistributeConfig.portToIpMap.put(10000,"localhost");
        DistributeConfig.portToIpMap.put(11001,"localhost");
        DistributeConfig.portToIpMap.put(12002,"localhost");
    }
    public static int halfover = (list.size()-1)/2;
    public static int port = 10000;
    public static String localhost = "localhost";
    public static String thisUid = localhost+":"+port;
}
