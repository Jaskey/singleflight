package com.github.jaskey;

import java.util.concurrent.*;
import java.util.function.Function;



public class SingleFlight<T> {
    private final ConcurrentHashMap<String/*key*/, Result<T>> resultsMap = new ConcurrentHashMap<>();
    //出现错误是否放行等待请求数。
    //注意，可能会使得压力瞬间放大的情况，请谨慎评估。
    //0表示不放过，透传异常。负数表示全部放过，不透传任何一个异常，慎用。
    private  int permitsWhenException = 0;
    
    //默认是不超
    private TimeoutStrategy timeoutStrategy = TimeoutStrategy.throwException(Long.MAX_VALUE);

    public SingleFlight() {}

    public SingleFlight(int permitsWhenException) {
        this.permitsWhenException = permitsWhenException;
    }

    public T doEntry(String key, Function<String,T> loadFunc) throws Exception {
        Result<T> thisResult  = new Result<>(permitsWhenException, timeoutStrategy.getPermitsWhenTimeout());
        Result<T> ingResult = resultsMap.putIfAbsent(key, thisResult);//放不进去证明有进行中的请求
        T ret = null;

        //第一个进入
        if (ingResult == null) {
            ret = execute(key, loadFunc, thisResult);
        } else {//已经有请求在执行，那么等待一下，直接拿之前请求的结果
            boolean isFinish;
            do { //一直等之前的请求完成
                isFinish = ingResult.latch.await(timeoutStrategy.getTimeout(), TimeUnit.MILLISECONDS);
                //通行的线程完成了countdown允许通过，或者超时了
                ret = ingResult.res;
                if (isFinish) {//成功了
                    if (ingResult.lastException != null) {//有错误
                        //避免一个错全部等待的都跟着错，以下适当放过
                        if (ingResult.exceptionPermitsSemaphore.availablePermits()<0 || //一开始设置负数，直接放过
                                ingResult.exceptionPermitsSemaphore.tryAcquire()) {//放行数量还够
                            ret = loadFunc.apply(key);
                        } else {
                            throw ingResult.lastException;
                        }
                    }
                }
                else {//超时
                    if (timeoutStrategy.isThrowExceptionWhenTimeout()) {
                       throw new TimeoutStrategy.TimeoutException("execute load function timeout " + timeoutStrategy.getTimeout());
                    } else if (ingResult.timeoutPermitsSemaphore.availablePermits() < 0 || //一开始设置负数，直接放过
                            ingResult.timeoutPermitsSemaphore.tryAcquire()) { // 适当放行请求，避免一个错全部等待的都跟着错
                        ret = execute(key, loadFunc, ingResult);
                    }
                }

            } while (!isFinish);
        }

        return ret;
    }

    private T execute(String key, Function<String, T> loadFunc, Result<T> thisResult) {
        T ret;
        try {
            thisResult.res = loadFunc.apply(key);
            ret = thisResult.res;
        } catch (Exception e) {
            thisResult.lastException = e;
            throw e;
        } finally {
            thisResult.latch.countDown();//唤醒等待的线程直接拿结果
            resultsMap.remove(key);//调用完成后就不用记住了
        }
        return ret;
    }

    public void setTimeoutStrategy(TimeoutStrategy timeoutStrategy) {
        this.timeoutStrategy = timeoutStrategy;
    }

    private static class Result<T> {
        private volatile T res;
        private volatile Exception lastException;
        private final CountDownLatch latch = new CountDownLatch(1);
        private final Semaphore exceptionPermitsSemaphore;
        private final Semaphore timeoutPermitsSemaphore;

        public Result(int permitsWhenException, int permitsWhenTimeout) {
            this.exceptionPermitsSemaphore = new Semaphore(permitsWhenException);
            this.timeoutPermitsSemaphore = new Semaphore(permitsWhenTimeout);
        }
    }




}
