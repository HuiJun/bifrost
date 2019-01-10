package net.zerofill.domains;

public class UserMetadata {
    private String discordName;
    private String displayName;

    public UserMetadata(String discordName, String displayName) {
        setDiscordName(discordName);
        setDisplayName(displayName);
    }

    public String getDiscordName() {
        return discordName;
    }

    public void setDiscordName(String discordName) {
        this.discordName = discordName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
