package run;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class threadPoolManager {
    public static ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(10,50,10, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
}
