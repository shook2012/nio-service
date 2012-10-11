package org.jboss.netty.channel.socket.nio;

import java.lang.reflect.Field;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.slf4j.Logger;
import com.xunlei.util.Log;

/**
 * NioWorker监控
 * 
 * @author ZengDong
 * @since 2010-5-24 下午04:12:26
 */
public class NioWorkerStat {

    private static final Logger log = Log.getLogger();
    public static AbstractNioWorker[] workers;
    private static final String workerinfoFmt = "%-40s %-16s %-16s %-16s %-16s\n";

    private static String sampleSelectorStr = null;

    public static boolean isStarted(AbstractNioWorker w) {
        return Boolean.valueOf(getNioWorkerFieldValue(w, "started").toString());
    }

    @SuppressWarnings("unchecked")
    private static <T> T getNioWorkerFieldValue(AbstractNioWorker w, String field) {
        try {
            Field f = null;
            try {
                f = AbstractNioWorker.class.getDeclaredField(field);
            } catch (Exception e) {
                f = w.getClass().getDeclaredField(field);
            }
            f.setAccessible(true);
            return (T) f.get(w);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    public static Queue<Runnable> getRegisterTaskQueue(AbstractNioWorker w) {
        return getNioWorkerFieldValue(w, "registerTaskQueue");
    }

    public static Queue<Runnable> getWriteTaskQueue(AbstractNioWorker w) {
        return getNioWorkerFieldValue(w, "writeTaskQueue");
    }

    public static int getCancelledKeys(AbstractNioWorker w) {
        return Integer.valueOf(getNioWorkerFieldValue(w, "cancelledKeys").toString());
    }

    // public static int getBossId(AbstractNioWorker w) {
    // return Integer.valueOf(getNioWorkerFieldValue(w, "bossId").toString());
    // }
    //
    // public static int getId(AbstractNioWorker w) {
    // return Integer.valueOf(getNioWorkerFieldValue(w, "id").toString());
    // }

    public static AtomicBoolean getWakenUp(AbstractNioWorker w) {
        return getNioWorkerFieldValue(w, "wakenUp");
    }

    public static Selector getSelector(AbstractNioWorker w) {
        return getNioWorkerFieldValue(w, "selector");
    }

    public static ReadWriteLock getSelectorGuard(AbstractNioWorker w) {
        return getNioWorkerFieldValue(w, "selectorGuard");
    }

    public static SocketReceiveBufferAllocator getRecvBufferPool(AbstractNioWorker w) {
        return getNioWorkerFieldValue(w, "recvBufferPool");
    }

    public static SocketSendBufferPool getSendBufferPool(AbstractNioWorker w) {
        return getNioWorkerFieldValue(w, "sendBufferPool");
    }

    public static Object getStartStopLock(AbstractNioWorker w) {
        return getNioWorkerFieldValue(w, "startStopLock");
    }

    public static Executor getExecutor(AbstractNioWorker w) {
        return getNioWorkerFieldValue(w, "executor");
    }

    public static Thread getThread(AbstractNioWorker w) {
        return getNioWorkerFieldValue(w, "thread");
    }

    public static String statNioWorkers() {
        StringBuilder tmp = new StringBuilder();
        int allRegisterTasksNum = 0;
        int allWriteTasksNum = 0;
        tmp.append(getSampleSelectorStr());
        tmp.append("\n");

        tmp.append(String.format(workerinfoFmt, "Running-NioWorker", "RegisterTasks", "WriteTasks", "CancelledKeys", "WakenUp"));
        for (AbstractNioWorker w : workers) {
            if (isStarted(w)) {
                int rSize = getRegisterTaskQueue(w).size();
                int wSize = getWriteTaskQueue(w).size();
                allRegisterTasksNum += rSize;
                allWriteTasksNum += wSize;
                int cancelledKeys = getCancelledKeys(w);
                String name = new StringBuilder().append(getThread(w)).toString();
                tmp.append(String.format(workerinfoFmt, name, rSize > 0 ? rSize : "", wSize > 0 ? wSize : "", cancelledKeys > 0 ? cancelledKeys : "", getWakenUp(w).get() ? "Y" : ""));
            }
        }
        StringBuilder head = new StringBuilder();
        head.append("当前NioWorker数 :\t\t").append(workers.length).append("\n");
        head.append("要运行的RegisterTasks:\t\t").append(allRegisterTasksNum).append("\n");
        head.append("要运行的WriteTasks:\t\t").append(allWriteTasksNum).append("\n");
        head.append("\n");
        tmp.insert(0, head);
        return tmp.toString();
    }

    public static String statNioWorkersAll() {
        StringBuilder tmp = new StringBuilder();
        int allRegisterTasksNum = 0;
        int allWriteTasksNum = 0;
        for (AbstractNioWorker w : workers) {
            if (isStarted(w)) {
                tmp.append(getThread(w)).append("\n");

                int rSize = getRegisterTaskQueue(w).size();
                if (rSize > 0) {
                    allRegisterTasksNum += rSize;
                    tmp.append("要运行的RegisterTasks:").append(rSize).append("\n");
                }
                int wSize = getWriteTaskQueue(w).size();
                if (wSize > 0) {
                    allWriteTasksNum += wSize;
                    tmp.append("要运行的WriteTasks:").append(wSize).append("\n");
                }

                if (getCancelledKeys(w) > 0)
                    tmp.append("CancelledKeys:").append(getCancelledKeys(w)).append("\n");
                if (getWakenUp(w).get())
                    tmp.append("WakenUp:").append("true").append("\n");

                tmp.append("RegisterTaskQueue:").append(getRegisterTaskQueue(w)).append("\n");
                tmp.append("WriteTaskQueue:").append(getWriteTaskQueue(w)).append("\n");

                tmp.append("Selector:").append(getSelector(w)).append("\n");
                tmp.append("SelectorGuard:").append(getSelectorGuard(w)).append("\n");

                tmp.append("RecvBufferPool:").append(getRecvBufferPool(w)).append("\n");
                tmp.append("SendBufferPool:").append(getSendBufferPool(w)).append("\n");
                tmp.append("StartStopLock:").append(getStartStopLock(w)).append("\n");
                tmp.append("Executor:").append(getExecutor(w)).append("\n");
                tmp.append("Thread:").append(getThread(w)).append("\n");

                tmp.append("\n");
            }
        }
        StringBuilder head = new StringBuilder();
        head.append("当前NioWorker数 :\t\t").append(workers.length).append("\n");
        head.append("要运行的RegisterTasks:\t\t").append(allRegisterTasksNum).append("\n");
        head.append("要运行的WriteTasks:\t\t").append(allWriteTasksNum).append("\n");
        head.append("\n");
        tmp.insert(0, head);
        return tmp.toString();
    }

    private static String getSampleSelectorStr() {
        if (sampleSelectorStr == null) {
            if (workers.length > 0) {
                StringBuilder tmp = new StringBuilder();
                AbstractNioWorker sample = workers[0];
                Selector s = getSelector(sample);
                ReadWriteLock rwl = getSelectorGuard(sample);
                if (s != null)
                    tmp.append("Selector:\t\t\t").append(s.getClass().getName()).append("\n");
                if (rwl != null)
                    tmp.append("SelectorGuard:\t\t\t").append(rwl.getClass().getName()).append("\n");
                sampleSelectorStr = tmp.toString();
            }
        }
        return sampleSelectorStr;
    }

    public static void registerNioWorkers(ServerSocketChannelFactory serverSocketChannelFactory) {
        try {
            Field f = serverSocketChannelFactory.getClass().getDeclaredField("sink");
            f.setAccessible(true);
            Object sink = f.get(serverSocketChannelFactory);
            // Field f1 = sink.getClass().getDeclaredField("workers");
            // f1.setAccessible(true);
            // workers = (NioWorker[]) f1.get(sink);

            Field f0 = sink.getClass().getDeclaredField("workerPool");
            f0.setAccessible(true);
            @SuppressWarnings("unchecked")
            WorkerPool<NioWorker> workerPool = (WorkerPool<NioWorker>) f0.get(sink);

            Field f1 = AbstractNioWorkerPool.class.getDeclaredField("workers");
            f1.setAccessible(true);
            workers = (AbstractNioWorker[]) f1.get(workerPool);

        } catch (Exception e) {
            log.error("", e);
        }
    }
}
