package com.github.jaskey.sample;

import com.github.jaskey.SingleFlight;
import com.github.jaskey.TimeoutStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThrowExTimeout {
    private static Logger logger = LoggerFactory.getLogger(ThrowExTimeout.class);

    public static void main(String[] args) throws Exception {
        SingleFlight<String> sg = new SingleFlight<>();

        // 500毫秒超时，超时则抛出异常
        sg.setTimeoutStrategy(TimeoutStrategy.throwException(500));

        ExecutorService es = Executors.newCachedThreadPool();
        for (int i =0; i<10;i++) {
            Simple.fakeRPCInNewThread(sg, es, "timeoutReq", 1000);//耗时1秒，那么请求都会超时
        }

        logger.info("------------submit timeout req  ---------" );
        es.shutdown();
    }




}
