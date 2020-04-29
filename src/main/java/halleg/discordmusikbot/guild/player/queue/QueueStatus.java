package halleg.discordmusikbot.guild.player.queue;

public enum QueueStatus {
    QUEUED(true), PLAYING(true), PLAYED(false), REMOVED(false), SKIPPED(false);

    private boolean keepLoading;

    QueueStatus(boolean keepLoading) {
        this.keepLoading = keepLoading;
    }

    public boolean getKeepLoading() {
        return this.keepLoading;
    }
}
