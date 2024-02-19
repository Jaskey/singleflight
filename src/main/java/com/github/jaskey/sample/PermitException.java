package com.github.jaskey.sample;

import com.github.jaskey.SingleFlight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PermitException {
    private static Logger logger = LoggerFactory.getLogger(PermitException.class);

    public static void main(String[] args) throws java.lang.Exception {
        SingleFlight<String> sg = new SingleFlight<>(5);//下游抛出异常的时候，尝试放5个新请求进去
        ExecutorService es = Executors.newCachedThreadPool();
        for (int i =0; i<10;i++) {
            Simple.fakeExceptionResultInNewThread(sg, es, "errorReq");
        }

        logger.info("------------submit error req ends  -------" );
        es.shutdown();



    }




}
