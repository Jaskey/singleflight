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

        //并发请求N次，只有一个请求进去
        for (int i=0;i<10;i++) {
            fakeRPCInNewThread(sg, es, "req_x",1000);
            fakeRPCInNewThread(sg, es, "req_y",1000); //不同的请求key，相互独立不受影响
        }

        logger.info("------------submit timeout req ends ---------" );

        es.shutdown();
    }

    public static Future<String> fakeExceptionResultInNewThread(SingleFlight<String> sg, ExecutorService es, String requestId) {
        return fakeRPCInNewThread(sg,es,requestId,-1);
    }


        //假装有一个rpc请求，cost是其这个RPC的耗时，cost是负数则会抛出异常
    public static Future<String> fakeRPCInNewThread(SingleFlight<String> sg, ExecutorService es, String requestId, long cost) {
        return es.submit(() -> {
            String res = null;
            try {
                res = sg.goFlight(requestId, keyInner -> {
                    String now = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
                    String result = "CALL_RESULT: "+ keyInner + "_" + now;
                    logger.info("[ACTUAL CALLS][{}] load func calls, res: {}", String.valueOf(cnt.incrementAndGet()), result);
                    if (cost>0) {
                        try {
                            sleep(cost);//假装rpc耗时
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return result;
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
