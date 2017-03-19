package com.burt.test;

import com.burt.task.Slot;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 环形队列
 * 模仿处理向系统监控中心提交操作日志时的线程安全问题（日志先缓存在本地，再提交到监控中心）
 * 为什么会有线程安全问题?因为有这种可能：有线程在往本地缓存写数据的时候，这个时候又要把本地缓存的数据全部提交到监控中心，这存在线程安全问题
 * Created by Burt on 2017/3/19 0019.
 */
public class RingQueueLogTest {

    //当前指针的位置
    private static int currentIndex = 1;

    public static void main(String[] args) throws InterruptedException {

        //形式队列位置数量
        int slotSize = 3;
        //定时时间
        final int time = 1;
        //首次执行的延时时间
        final int initialDelay = 1;

        final Slot[] slots = buildSlots(slotSize);

        Thread timerThread = new Thread(new Runnable() {
            public void run() {
                //定时器
                myTimerTask(slots, initialDelay, time);
            }
        });
        timerThread.start();

        ExecutorService pools = Executors.newFixedThreadPool(20);
        RingQueueLogTest.SaveLogWork saveLogWork = new RingQueueLogTest.SaveLogWork(slots);
        while (true) {
            Random random = new Random();
            int sleepTime = random.nextInt(100);
            Thread.sleep(sleepTime);
            pools.submit(saveLogWork);
        }

    }

    /**
     * 构建队列
     */
    private static Slot[] buildSlots(int slotSize) {
        Slot[] slots = new Slot[slotSize];
        for (int i = 0; i < slotSize; i++) {
            slots[i] = new Slot();
        }
        return slots;
    }

    /**
     * 定时任务
     *
     * @param slots 队列
     * @param initialDelay 首次执行的延时时间
     * @param period 定时执行的间隔时间
     */
    private static void myTimerTask(final Slot[] slots, int initialDelay, int period) {
        Runnable runnable = new Runnable() {
            public void run() {
                //指针每秒移动一次，指针所到位置提交日志到监控中心
                int index = currentIndex % slots.length;
                System.out.println("指针当前位置: " + index);
                slots[index].submitLog();
                //担心currentIndex会超出边界，所示不在计算index里对currentIndex进行++操作
                currentIndex = index + 1;
            }
        };
        ScheduledExecutorService service = Executors
                .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, initialDelay, period, TimeUnit.SECONDS);
    }

    /**
     * 写日志
     */
    static class SaveLogWork implements Runnable {
        private Slot[] slots;

        public SaveLogWork(Slot[] slots) {
            this.slots = slots;
        }

        public void run() {
            //把日志写入到当前指针所指的上一个位置
            int index = (currentIndex - 1) % slots.length;
            System.out.println("写入日志的位置: " + index);
            this.slots[index].storeLog(new Random().nextInt() + "");
        }
    }
}
