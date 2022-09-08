package com.github.jaskey.sample;

import com.github.jaskey.SingleFlight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PermitException {
    private static Logger logger = LoggerFactory.getLogger(PermitException.class);

    public static void main(String[] args) throws java.lang.Exception {
        SingleFlight<String> sg = new SingleFlight<>(5);//报错的时候，放5进去
        ExecutorService es = Executors.newCachedThreadPool();
        for (int i =0; i<10;i++) {
            Simple.fakeRPC(sg, es, "errorReq", -1);
        }

        logger.info("------------submit error req ends  -------" );
        es.shutdown();



    }




}
