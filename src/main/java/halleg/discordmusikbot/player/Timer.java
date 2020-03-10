package halleg.discordmusikbot.player;

public class Timer implements Runnable {

    private long time;
    private long until;
    private Thread thread;
    private TimerListener listener;

    public Timer(long time, TimerListener listener){
        this.time = time;
        this.listener = listener;
        thread = new Thread(this);
    }

    public void start(){
        if(!thread.isAlive()){
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop(){
        if(thread.isAlive()){
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            return;
        }

        listener.onTimerEnd();
    }

    public interface TimerListener{
        public void onTimerEnd();
    }
}
