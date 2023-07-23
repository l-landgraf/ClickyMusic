package halleg.discordmusikbot.guild.player;

public class Timer implements Runnable {

    private long time;
    private boolean repeat;
    private Thread thread;
    private TimerListener listener;

    public Timer(long time, boolean repeat, TimerListener listener) {
        this.time = time;
        this.repeat = repeat;
        this.listener = listener;
        this.thread = new Thread(this);
    }

    public void start() {
        if (!this.thread.isAlive()) {
            this.thread = new Thread(this);
            this.thread.start();
        }
    }

    public void stop() {
        if (this.thread.isAlive()) {
            this.thread.interrupt();
        }
    }

    @Override
    public void run() {
        do {
            try {
                Thread.sleep(this.time);
            } catch (InterruptedException e) {
                return;
            }

            this.listener.onTimerEnd();
        } while (this.repeat);
    }

    public interface TimerListener {
        public void onTimerEnd();
    }
}
