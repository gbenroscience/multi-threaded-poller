# multi-threaded-poller
A simple and powerful Java class that implements polling with a configurable number of threads

## Usage
You only need to implement 2 methods to use this class to perform repeated tasks, such as **polling a database**, **polling an API**, **running an animation** etc.
They are the `computePollType` and the `poll` methods.


### computePollType method
This is a simple method and returns one of a predefined number of constants.

```Java
    public static final int POLL_RATE_DRAGGY = 0; //10000ms
    public static final int POLL_RATE_VERY_SLOW = 1;//3000ms
    public static final int POLL_RATE_LAGGY = 2;//500ms
    public static final int POLL_RATE_SLOW = 3;//100ms
    public static final int POLL_RATE_MEDIUM = 4;//80ms
    public static final int POLL_RATE_QUICK = 5;//50ms
    public static final int POLL_RATE_HIGH = 6;//30ms
    public static final int POLL_RATE_ULTRA_HIGH = 7;//1ms
    public static final int POLL_RATE_UNLIMITED = 8;//0ms
    public static final int POLL_RATE_CUSTOM = 9;// override getCustomPollRateMs method and return a value in milliseconds from it
```

Each of these constants will make the `Poller` run at a certain rate specified in the comments beside each constant above.

if you return `POLL_RATE_CUSTOM` from the `computePollType` method, then you must override the `getCustomPollRateMs` method and specify the polling duration in **milliseconds** from it.

### poll method
Within this method, you must type the code that you wish to run repeatedly.

Here is an example:

```Java
      
        Poller p = new Poller(10) {
            private ConcurrentLinkedQueue<String> data = new ConcurrentLinkedQueue<>();
            private ConcurrentLinkedQueue<String> trash = new ConcurrentLinkedQueue<>();

            @Override
            protected int computePollType() {
                return POLL_RATE_UNLIMITED;
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
        };

```
