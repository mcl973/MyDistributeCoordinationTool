package run;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 分布式协调工具配置文件
 */
public class DistributeConfig {
    /**
     * false:local,本地的各种操作，只是在于端口不一样
     * true:remote，远程操作，可能ip和端口都不一样
     */
    public static boolean localOrRemote = true;
    /**
     * ip为key，port为value
     */
    public static HashMap<String,Integer> ipToPortMap = new HashMap<>();
    /**
     * port为key，ip为value
     */
    public static HashMap<Integer,String> portToIpMap = new HashMap<>();
    /**
     * port集合
     */
    public static HashSet<Integer> portSet = new HashSet<>();
    /**
     * ip集合
     */
    public static HashSet<String> ipSet = new HashSet<>();
}
