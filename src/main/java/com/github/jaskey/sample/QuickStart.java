package com.github.jaskey.sample;

import com.github.jaskey.SingleFlight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class QuickStart {
    public static void main(String[] args) throws Exception {
        SingleFlight<String> sg = new SingleFlight<>();
        //并发请求10次，只有一个请求真正进回调，其他9个请求均复用第一次的结果，所以N出来的结果都是一样的
        for (int i=0;i<10;i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String result = sg.goFlight("id_1", key -> {
                            System.out.println("querying from database...");
                            Thread.sleep(100);//假装需要100毫秒处理数据库
                            return "data_1_" + new Random().nextInt(100000);
                        });
                        System.out.println("singleflight result = " + result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
