package com.github.jaskey.sample;

import com.github.jaskey.SingleFlight;
import com.github.jaskey.TimeoutStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThrowExTimeout {
    private static Logger logger = LoggerFactory.getLogger(ThrowExTimeout.class);

    public static void main(String[] args) throws Exception {
        SingleFlight<String> sg = new SingleFlight<>();
        sg.setTimeoutStrategy(TimeoutStrategy.throwException(500));

        ExecutorService es = Executors.newCachedThreadPool();

        for (int i =0; i<10;i++) {
            Simple.fakeRPC(sg, es, "timeoutReq", 1000);
        }
        logger.info("------------submit timeout req  ---------" );
        es.shutdown();
    }




}
