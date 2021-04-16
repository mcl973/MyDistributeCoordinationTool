package NodeManager.Node;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SynchronizedQueueClass {
    public static BlockingQueue<NodeChild> blockingQueue = new LinkedBlockingQueue<>();
}
