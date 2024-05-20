package halleg.discordmusikbot;

import halleg.discordmusikbot.guild.player.Timer;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DisconnectListener extends ListenerAdapter implements Timer.TimerListener {
    private Timer timer;

    public DisconnectListener() {
        this.timer = new Timer(30, false, this);
    }

    @Override
    public void onSessionResume(SessionResumeEvent event) {
        System.out.println("Disconnect timer stopped.");
        this.timer.stop();
    }

    @Override
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        System.out.println("Disconnect timer started.");
        this.timer.start();
    }

    @Override
    public void onTimerEnd() {
        System.out.println("Disconnect timer run out. shutting down.");
        //System.exit(1);
    }
}
