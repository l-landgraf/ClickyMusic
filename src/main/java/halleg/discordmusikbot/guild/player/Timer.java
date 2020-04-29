package halleg.discordmusikbot.guild.player;

public class Timer implements Runnable {

    private long time;
    private long until;
    private Thread thread;
    private TimerListener listener;

    public Timer(long time, TimerListener listener) {
        this.time = time;
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
        try {
            Thread.sleep(this.time);
        } catch (InterruptedException e) {
            return;
        }

        this.listener.onTimerEnd();
    }

    public interface TimerListener {
        public void onTimerEnd();
    }
}
