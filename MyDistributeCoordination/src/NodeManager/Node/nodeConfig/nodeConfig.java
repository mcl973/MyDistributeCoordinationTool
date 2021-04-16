package NodeManager.Node.nodeConfig;

import Select.SelectConfig;

public class nodeConfig {
    /**
     * port是在SelectConfig中的port的基础上加上了200
     * 这个是暴露Domain和NodeChild的port
     */
    public static int port = 10200;
    public static String create = "create";
    public static String distributeLock = "distributeLock";
    public static String domin = "domin";
}
