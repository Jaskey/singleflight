package com.github.jaskey;

/**
 * 超时策略。注：超时策略只影响在等待中的线程，正在执行任务的线程不受超时策略影响。
 * 即如果有一个线程已经穿过了SingleFlight在执行任务，这个任务不会中断
 * 有两个超时策略：
 *     throwException：返回一个会抛出超时异常的策略。
 *     permitSome：    返回一个超时的时候适量放行部分在外等待的线程通行的策略。
 *     此时，每达到一个Timeout都会放行，例如：总共有100个任务在等待，timeout=1000, permitsWhenTimeout=1，则每过1000ms都会放一个通行，此策略适用于控制下游QPS的情况。
 */
public class TimeoutStrategy {
    private final long timeout;
    private final boolean throwExceptionWhenTimeout;
    private final int permitsWhenTimeout;

    public static class TimeoutException extends RuntimeException{
        public TimeoutException(String message) {
            super(message);
        }
    }

    public static TimeoutStrategy throwException(long timeout){
        return new TimeoutStrategy(timeout, true, 0);
    }

    public static TimeoutStrategy permitSome(long timeout, int permitsWhenTimeout){
        return new TimeoutStrategy(timeout, false, permitsWhenTimeout);
    }

    private TimeoutStrategy(long timeout, boolean throwExceptionWhenTimeout, int permitsWhenTimeout) {
        this.timeout = timeout;
        this.throwExceptionWhenTimeout = throwExceptionWhenTimeout;
        this.permitsWhenTimeout = permitsWhenTimeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public boolean isThrowExceptionWhenTimeout() {
        return throwExceptionWhenTimeout;
    }

    public int getPermitsWhenTimeout() {
        return permitsWhenTimeout;
    }
}
