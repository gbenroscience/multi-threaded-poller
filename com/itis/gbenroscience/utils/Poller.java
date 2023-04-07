/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itis.gbenroscience.utils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author JIBOYE, Oluwagbemiro Olaoluwa <gbenroscience@yahoo.com>
 */
public abstract class Poller implements Runnable {

    /**
     * You may want to poll only for a certain number of times. Once you get to
     * maxPolls, polling will cease immediately.
     */
    private int maxPolls;
    /**
     * The number of polls already executed.
     */
    private AtomicInteger polls = new AtomicInteger(0);
    private AtomicInteger countThreads = new AtomicInteger(0);

    private int pollType = POLL_RATE_MEDIUM;

    public static final int POLL_RATE_DRAGGY = 0;
    public static final int POLL_RATE_VERY_SLOW = 1;
    public static final int POLL_RATE_LAGGY = 2;
    public static final int POLL_RATE_SLOW = 3;
    public static final int POLL_RATE_MEDIUM = 4;
    public static final int POLL_RATE_QUICK = 5;
    public static final int POLL_RATE_HIGH = 6;
    public static final int POLL_RATE_ULTRA_HIGH = 7;
    public static final int POLL_RATE_UNLIMITED = 8;
    public static final int POLL_RATE_CUSTOM = 9;

    private Thread[] runners;

    private boolean switchOff;

    public Poller(int numOfThreads) {
        runners = new Thread[numOfThreads];
        for (int i = 0; i < runners.length; i++) {
            runners[i] = new Thread(this, "" + i);
        }
    }

    public void startPolling() {
        switchOff = false;
        beforePoll();
        countThreads.set(runners.length);
        for (Thread t : this.runners) {
            t.start();
        }
    }

    /**
     * End polling. This issues the command that schedules polling to end as
     * soon as possible
     */
    public void stopPolling() {
        switchOff = true;
    }

    /**
     * Override this to set a custom poll rate if you have set
     * {@link Poller#pollType} to {@link Poller#POLL_RATE_CUSTOM}
     */
    public long getCustomPollRateMs() {
        return 5000L;
    }

    public void setPollType(int pollType) {
        this.pollType = pollType;
    }

    public int getPollType() {
        return pollType;
    }

    /**
     *
     * @return the type of the polling based on any conditions determined by the
     * programmer. The type must be one of:
     * <ol>
     * <li>{@link Poller#POLL_RATE_DRAGGY}</li>
     * <li>{@link Poller#POLL_RATE_VERY_SLOW}</li>
     * <li>{@link Poller#POLL_RATE_LAGGY}</li>
     * <li>{@link Poller#POLL_RATE_SLOW}</li>
     * <li>{@link Poller#POLL_RATE_MEDIUM}</li>
     * <li>{@link Poller#POLL_RATE_QUICK}</li>
     * <li>{@link Poller#POLL_RATE_HIGH}</li>
     * <li>{@link Poller#POLL_RATE_ULTRA_HIGH}</li>
     * <li>{@link Poller#POLL_RATE_UNLIMITED}</li>
     * <li>{@link Poller#POLL_RATE_CUSTOM}</li>
     * </ol>
     *
     */
    protected abstract int computePollType();

    public void setMaxPolls(int maxPolls) {
        this.maxPolls = maxPolls;
    }

    public int getMaxPolls() {
        return maxPolls;
    }

    private void incrementPolls() {
        this.polls.addAndGet(1);
    }

    /**
     * @return the rate of polling in milliseconds.
     */
    private long getRate() {

        setPollType(computePollType());

        switch (pollType) {
            case POLL_RATE_DRAGGY:
                return 10000;
            case POLL_RATE_VERY_SLOW:
                return 3000L;
            case POLL_RATE_LAGGY:
                return 500L;
            case POLL_RATE_SLOW:
                return 100L;
            case POLL_RATE_MEDIUM:
                return 80L;
            case POLL_RATE_QUICK:
                return 50L;
            case POLL_RATE_HIGH:
                return 30L;
            case POLL_RATE_ULTRA_HIGH:
                return 1L;
            case POLL_RATE_CUSTOM:
                return getCustomPollRateMs();
            case POLL_RATE_UNLIMITED:
                return 0L;

            default:

                return 300L;

        }
    }

    /**
     * Run this code before the poll starts
     */
    public void beforePoll(){}

    /**
     * The code to run repeatedly
     */
    public void poll(){};

    /**
     * Run this code after the poll ends
     */
    public void afterPoll(){}
 

    public void run() {
        Thread t = Thread.currentThread();

        while (!t.isInterrupted()) {
            try {
                boolean lightsOut = (maxPolls > 0 && polls.get() >= maxPolls) || switchOff;
                if (lightsOut) {
                    t.interrupt();
                    if (countThreads.decrementAndGet() == 0) {
                        polls.set(0);
                        countThreads.set(0);
                        afterPoll();
                    }
                    return;
                }
                poll();
                incrementPolls();
                long rate = getRate();
                if (pollType != POLL_RATE_UNLIMITED || rate > 0) {
                    Thread.sleep(rate);
                }
            } catch (InterruptedException e) {
                System.err.println("Stop signal detected... Closing Workers");
                e.printStackTrace();
                break;
            }
        }
    }    

    public static void main(String[] args) {

        Poller poller = new Poller(10) {
            private ConcurrentLinkedQueue<String> data = new ConcurrentLinkedQueue<>();
            private ConcurrentLinkedQueue<String> trash = new ConcurrentLinkedQueue<>();

            @Override
            protected int computePollType() {
                return POLL_RATE_UNLIMITED;
            }

            @Override
            public void beforePoll() {
                for (int i = 0; i < 12000; i++) {
                    data.add(i + "");
                }

                System.out.println("Before polling...");
            }

            @Override
            public void poll() {
                String polled = data.poll();
                if (polled != null) {
                    trash.add(polled);
                } else {
                    stopPolling();
                }
            }

            @Override
            public void afterPoll() {
               System.out.println("Polling done...data now has " + data.size()
                        + " items while trash now has " + trash.size() + " items");
            }
        };
       // poller.setMaxPolls(1800);
        poller.startPolling();

    }
}
