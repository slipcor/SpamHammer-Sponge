package net.slipcor.sponge.spamhammer.utils;

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public enum Language {

    BAD_SPAMMING_IP("bad.spamming_ip", "Sharing IPs is not allowed!"),
    BAD_SPAMMING_MESSAGE("bad.spamming_message", "You are spamming! Chill out!"),
    BAD_SPAMMING_URL("bad.spamming_url", "Sharing URLs is not allowed!"),
    BAD_YOU_ARE_BANNED("bad.you_are_banned", "You have been banned for spamming."),
    BAD_YOU_ARE_MUTED("bad.you_are_muted", "You are muted!"),
    BAD_YOU_ARE_NOW_MUTED("bad.you_are_now_muted", "You will now be muted for %0% second(s), for spamming.  Keep it up and you'll be kicked."),
    BAD_YOU_HAVE_BEEN_KICKED("bad.you_have_been_kicked", "You have been kicked for spamming.  Keep it up and you'll be banned."),

    ERROR_BAN_FAILED("error.ban_failed", "Player %0% could not be banned!"),
    ERROR_CONFIG_LOAD("error.config_load", "Error while loading config! Plugin will not work properly!"),
    ERROR_PLAYER_NOT_FOUND("error.player_not_found","Player not found: %0%."),
    ERROR_UNBAN_NOT_BANNED("error.unban_not_banned", "Player was not banned: %0%"),
    ERROR_UNMUTE_NOT_MUTED("error.unmute_notmuted", "%0% is not muted."),

    GOOD_BAN_SUCCESS("good.ban_success", "Player %0% has been banned!"),
    GOOD_KICKED_SUCCESS("good.kick_success", "Player %0% has been kicked!"),
    GOOD_MUTED_SUCCESS("good.mute_success", "Player %0% has been muted!"),
    GOOD_RELOAD_SUCCESS("good.reload_success", "Configs reloaded!"),
    GOOD_RESET_MUTE_SUCCESS("good.reset_mute_success", "Reset %0%'s mute punishment level."),
    GOOD_RESET_KICK_SUCCESS("good.reset_kick_success", "Reset %0%'s kick punishment level."),
    GOOD_UNBAN_SUCCESS("good.unban_success", "Player unbanned: %0%"),
    GOOD_UNMUTE_SUCCESS("good.unmute_success", "%0% has been unmuted."),
    GOOD_YOU_ARE_UNMUTED("good.you_are_unmuted", "You are no longer muted."),

    INFO_CAUGHT_IP("info.ip_caught", "Found IP by %0%"),
    INFO_CAUGHT_URL("info.url_caught", "Found URL by %0%"),
    INFO_COOLDOWN_OVER("info.cooldown_over", "Spamming punishment reset.  Be nice!");

    String nodes;
    String msg;

    static ConfigurationLoader<CommentedConfigurationNode> loader = null;
    static CommentedConfigurationNode rootNode = null;

    Language(final String nodes, final String msg) {
        this.nodes = nodes;
        this.msg = msg;
    }

    public static void init(final Path path) throws IOException {
        File file = path.toFile();
        if (!file.exists()) {
            file.createNewFile();
        }
        loader = HoconConfigurationLoader.builder().setPath(path).setDefaultOptions(ConfigurationOptions.defaults().setShouldCopyDefaults(true)).build();
        rootNode = loader.load();

        boolean changed = false;

        for (Language l : Language.values()) {
            CommentedConfigurationNode node = rootNode.getNode((Object[])l.nodes.split("\\."));
            if (node.isVirtual() && !changed) {
                changed = true;
            }
            l.msg = rootNode.getNode((Object[])l.nodes.split("\\.")).getString(l.msg);
        }
        if (changed) {
            loader.save(rootNode);
        }
    }

    @Override
    public String toString() {
        return this.msg;
    }

    public Text green() {
        return Text.builder(this.msg).color(TextColors.GREEN).build();
    }

    public Text red() {
        return Text.builder(this.msg).color(TextColors.RED).build();
    }

    public Text yellow() {
        return Text.builder(this.msg).color(TextColors.YELLOW).build();
    }

    public Text green(final String... args) {
        return Text.builder(replace(this.msg, args)).color(TextColors.GREEN).build();
    }

    public Text red(final String... args) {
        return Text.builder(replace(this.msg, args)).color(TextColors.RED).build();
    }

    public Text yellow(final String... args) {
        return Text.builder(replace(this.msg, args)).color(TextColors.YELLOW).build();
    }

    private String replace(String content, final String... args) {
        for(int i=0;i<args.length;i++) {
            content = content.replace("%"+i+"%", args[i]);
        }
        return content;
    }
}
