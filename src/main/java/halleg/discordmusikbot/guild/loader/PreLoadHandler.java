package halleg.discordmusikbot.guild.loader;

import halleg.discordmusikbot.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class PreLoadHandler extends LoadHandler {
    private String initialSource;

    public PreLoadHandler(GuildHandler handler, String source, String initialSource, Member member, Message message) {
        super(handler, source, member, message);
        this.initialSource = initialSource;
    }

    public String getInitialSource() {
        return this.initialSource;
    }
}
