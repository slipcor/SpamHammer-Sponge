package net.slipcor.sponge.spamhammer.utils;

import net.slipcor.sponge.spamhammer.SpamHammer;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Config {

    //MESSAGE(Null.class, "settings.message",null,new String[]{"# === [ Message Spam Settings ] ==="}),
    //MESSAGE_RATE(Null.class, "settings.message.rate",null,new String[]{
    //    "# The message rate settings determine how many messages per time frame are allowed before they are considered spam.",
    //            "# The default of limit: 3 and period: 1 means more than 3 messages per 1 second will be considered spam"}),

    SPAM_RATE_LIMIT("spam_rate_limit", 3, ""),
    SPAM_RATE_PERIOD("spam_rate_period", 1, ""),

    SPAM_RATE_QUICKMUTE("spam_rate_quickmute", true, "Prevents messages above the rate limit from displaying"),

    //MESSAGE_REPEAT(Null.class, "settings.message.repeat",null,new String[]{"# The repeat settings allow you to prevent users from repeating the same message in a row"}),

    REPEAT_BLOCK("repeat_block", true, "If set to true, this will block repeat messages."),
    REPEAT_LIMIT("repeat_limit", 2, "If SpamHammer is set to block repeat messages, this is how many messages before they are considered repeats."),

    //CAPS(Null.class, "settings.message.caps", null, new String[]{"# The caps limiter setting allows for a maximum amount/ratio of caps per message"}),

    CAPS_RESTRICT("caps_restrict", true, ""),

    CAPS_MAX_AMOUNT("caps_maxamount", 0, "How many uppercase characters are allowed per message?"),
    CAPS_RATIO("caps_ratio", Float.valueOf(0), "How many uppercase characters relatively to the length are allowed?"),
    CAPS_THRESHOLD("caps_threshold", 5, "How many letters are required before checking?"),

    COMMAND_SPAM_CHECKLIST("command_spam_checklist", Arrays.asList("g", "general", "yell"),"The commands listed here will be included in spam checking."),
    COMMAND_FIRST_RUN("command_first_run", new ArrayList<>(), "Will make the plugin perform tasks only done on a first run (if any)."),

    // # === [ Punishment Settings ] ===

    PUNISH_MUTE("punish_mute", true,"Setting this to true will mute players as the first level of punishment."),
    PUNISH_MUTE_TIME("punish_mute_time", 30,"If mute punishment is used, this is how long (in seconds) the player will be muted for."),
    PUNISH_MUTE_TYPE("punish_mute_type", "both","What should be muted? Possible values: chat, command, both"),

    PUNISH_KICK("punish_kick", true,"Setting this to true will kick players as the second level of punishment."),
    PUNISH_BAN("punish_ban", true,"Setting this to true will ban players as the final level of punishment."),

    LANGUAGE_FILE("language_file", "lang_en.conf", "This is the language file you wish to use."),
    COOLDOWN_SECONDS("cooldown", 300, "This setting determines how long (in seconds) a player will be watched for additional spam before downgrading them to the lowest punishment level."),
    CALL_HOME("callhome", true, "This activates phoning home to www.slipcor.net"),

    CHECK_IPS("checkips", false, "Check for IPs and punish when found"),
    CHECK_URLS("checkurls", false, "Check for URLs and punish when found");

    String nodes;
    Object value;
    String comment;

    Config(String nodes, Object value, String comment) {
        this.nodes = nodes;
        this.value = value;
        this.comment = comment;
    }


    static ConfigurationLoader<CommentedConfigurationNode> loader = null;
    static CommentedConfigurationNode rootNode = null;

    public static void init(final Path path) throws IOException {
        loader = HoconConfigurationLoader.builder().setPath(path).setDefaultOptions(ConfigurationOptions.defaults().setShouldCopyDefaults(true)).build();
        rootNode = loader.load();
        boolean changed = false;

        for (Config c : Config.values()) {
            final CommentedConfigurationNode node = rootNode.getNode((Object[])c.nodes.split("\\."));
            if (node.isVirtual()) {
                changed = true;
                node.setComment(c.comment);
            }
            c.value = node.getValue(c.value);
        }
        if (changed) {
            loader.save(rootNode);
        }
    }

    public static Boolean getBoolean(Config cfg) {
        return (Boolean) cfg.value;
    }

    public static Integer getInt(Config cfg) {
        return (Integer) cfg.value;
    }

    public static Float getFloat(Config cfg) {
        return rootNode.getNode((Object[])cfg.nodes.split("\\.")).getFloat();
    }

    public static List<String> getList(Config cfg) {
        return (List<String>) cfg.value;
    }

    public static String getString(Config cfg) {
        return (String) cfg.value;
    }
}
