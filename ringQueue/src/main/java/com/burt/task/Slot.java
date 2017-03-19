package com.burt.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 队列位置
 * Created by Burt on 2017/3/19 0019.
 */
public class Slot {

    private ExecutorService pools = Executors.newFixedThreadPool(20);
    private Slot.submitLogWork submitLogWork = new Slot.submitLogWork();
    /**
     * 日志容器
     */
//    private List<String> logList = new CopyOnWriteArrayList<String>();
    private Map<String, String> logMap = new ConcurrentHashMap<String, String>();

    /**
     * 本地存储日志
     *
     * @param operateLog 操作日志
     */
    public void storeLog(String operateLog) {
        //存储日志
        String key = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:S").format(new Date())
                + "|" + new Random().nextInt(10000);
        logMap.put(key, operateLog);
        System.out.println("save log : " + operateLog);
    }

    /**
     * 提交日志
     */
    public void submitLog() {
        //TODO 提交日志
        if (logMap.isEmpty()) {
            System.out.println("日志为空");
            return;
        }
        pools.submit(submitLogWork);
    }

    class submitLogWork implements Runnable {
        public void run() {
            System.out.println("log size : " + logMap.size() + " submit log : " + logMap.toString());
            //清空本地日志列表
            logMap.clear();
            System.out.println("clear current slot local log");
        }
    }
}
