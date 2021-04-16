package RPCManager.RPC;

/**
 * rpc事件类型
 */
public enum rpcType {
    /**
     * RPCMASTER,用于选举主节点时，作为master使用
     */
    RPCMASTER,
    /**
     * RPCSLAVE,用于选举主节点时作为slave使用
     */
    RPCSLAVE,
    /**
     * 用于放置监听器使用
     */
    PUTATCH
    /**
     * 用于监听事件出发使用
     */
    ,DOWATCH
}
