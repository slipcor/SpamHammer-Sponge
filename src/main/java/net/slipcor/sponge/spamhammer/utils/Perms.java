package net.slipcor.sponge.spamhammer.utils;

public enum Perms {
    BYPASS("spamhammer.bypass.*","Allows user to bypass ban punishments"),

    BYPASS_REPEAT("spamhammer.bypass.repeat","Allows user to bypass repeat message limit."),
    BYPASS_PUNISH_ALL("spamhammer.bypass.punish.all","Allows user to bypass ban punishments"),

    BYPASS_MUTE("spamhammer.bypass.punish.mute","Allows user to bypass mute punishments"),
    BYPASS_KICK("spamhammer.bypass.punish.kick","Allows user to bypass kick punishments"),
    BYPASS_BAN("spamhammer.bypass.punish.ban","Allows user to bypass ban punishments"),
    BYPASS_IPS("spamhammer.bypass.punish.ips","Allows user to bypass IP check punishments"),
    BYPASS_URLS("spamhammer.bypass.punish.urls","Allows user to bypass URL punishments"),

    CMD_ALL("spamhammer.cmd.*", "Allows use of all commands"),

    CMD_UNMUTE("spamhammer.cmd.unmute","Allows use of unmute command"),
    CMD_RESET("spamhammer.cmd.reset","Allows use of reset command"),
    CMD_RELOAD("spamhammer.cmd.reload","Allows use of reload command");

    final String node;
    final String description;

    Perms(final String node, final String desc) {
        this.node = node;
        this.description = desc;
    }

    @Override
    public String toString() {
        return node;
    }
}
