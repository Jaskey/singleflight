package com.github.jaskey.sample;

import com.github.jaskey.SingleFlight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

public class Simple {
    private static Logger logger = LoggerFactory.getLogger(Simple.class);

    private static AtomicInteger cnt = new AtomicInteger(0);

    public static void main(String[] args) throws  ExecutionException, InterruptedException {
        SingleFlight<String> sg = new SingleFlight<>();

        ExecutorService es = Executors.newCachedThreadPool();

        //相同的请求并发来N次，会只有一个请求进去
        for (int i=0;i<1000;i++) {
            fakeRPC(sg, es, "req1",1000);
            fakeRPC(sg, es, "req2",1000);
        }

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //不同的请求相互独立不受影响
        for (int i=3;i<10;i++) {
            fakeRPC(sg, es, "req"+i, 100);
        }

        logger.info("------------submit timeout req ends ---------" );

    }

    //假装有一个rpc请求，cost是其这个RPC的耗时，cost是负数则会抛出异常
    public static Future<String> fakeRPC(SingleFlight<String> sg, ExecutorService es, String requestId, long cost) {

        return es.submit(() -> {
            String res = null;
            try {
                res = sg.doEntry(requestId, keyInner -> {
                    String now = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
                    logger.info("[INNER CALLS][{}] load func calls {}", String.valueOf(cnt.incrementAndGet()), keyInner);
                    if (cost>0) {
                        try {
                            sleep(cost);//假装rpc耗时
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return "res_"+ keyInner + "_" + now;
                    } else { //cost是负数，则抛异常
                        throw new RuntimeException("fake rpc error");
                    }
                });
                logger.info("request {} success: {}", requestId, res);
            } catch (Exception e) {
                logger.error("ex throws when request " + requestId+" "+e.getMessage());
                throw e;
            }
            return res;
        });
    }


}
