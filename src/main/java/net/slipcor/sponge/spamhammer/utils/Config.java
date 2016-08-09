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

    C_SPAM("spam", null, "=== [ Message Spam Settings ] ==="),

    C_CAPS("spam.caps", null, "The caps limiter setting allows for a maximum amount/ratio of caps per message"),

    CAPS_RESTRICT("spam.caps.restrict", true, ""),

    CAPS_MAX_AMOUNT("spam.caps.maxamount", 0, "How many uppercase characters are allowed per message"),
    CAPS_RATIO("spam.caps.ratio", 0f, "How many uppercase characters relatively to the length are allowed"),
    CAPS_THRESHOLD("spam.caps.threshold", 5, "How many letters are required before checking"),

    CHECK_IPS("spam.check_ips", false, "Check for IPs and punish when found"),
    CHECK_URLS("spam.check_urls", false, "Check for URLs and punish when found"),

    C_SPAM_RATE("spam.rate", null, "The message rate settings determine how many messages per period are allowed before they are considered spam"),

    SPAM_RATE_LIMIT("spam.rate.limit", 3, "Amount of messages allowed"),
    SPAM_RATE_PERIOD("spam.rate.period", 1, "Length of the check period in seconds"),
    SPAM_RATE_PREVENT("spam.rate.prevent", true, "Prevents messages above the rate limit from displaying"),

    C_REPEAT("spam.repeat", null, "The repeat settings allow you to prevent users from repeating the same message in a row"),

    REPEAT_BLOCK("spam.repeat.block", true, "If set to true, this will block repeat messages"),
    REPEAT_LIMIT("spam.repeat.limit", 2, "If SpamHammer is set to block repeat messages, this is how many messages before they are considered repeats"),

    SPAM_COMMAND_CHECKLIST("spam.spam_cmd_checklist", Arrays.asList("g", "general", "yell"),"The commands listed here will be included in spam checking"),

    C_PUNISH("punish", null, "=== [ Punishment Settings ] ==="),

    PUNISH_COOLDOWN_SECONDS("punish.cooldown", 300, "This setting determines how long (in seconds) a player will be watched for additional spam before downgrading them to the lowest punishment level"),

    PUNISH_MUTE("punish.mute", true,"Setting this to true will mute players as the first level of punishment"),
    PUNISH_MUTE_TIME("punish.mute_time", 30,"If mute punishment is used, this is how long (in seconds) the player will be muted for"),
    PUNISH_MUTE_TYPE("punish.mute_type", "both","What should be muted? Possible values: chat, command, both"),

    PUNISH_KICK("punish.kick", true,"Setting this to true will kick players as the second level of punishment"),
    PUNISH_BAN("punish.ban", true,"Setting this to true will ban players as the final level of punishment"),

    C_PLUGIN("plugin", null, "=== [ Plugin Settings ] ==="),

    CALL_HOME("plugin.callhome", true, "This activates phoning home to www.slipcor.net"),
    LANGUAGE_FILE("plugin.language_file", "lang_en.conf", "This is the language file you wish to use");

    String nodes;
    Object value;
    String comment;

    Config(final String nodes, final Object value, final String comment) {
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
            if (c.value != null) {
                c.value = node.getValue(c.value);
            }
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
