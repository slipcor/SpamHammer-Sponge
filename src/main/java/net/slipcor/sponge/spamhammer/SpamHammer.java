package net.slipcor.sponge.spamhammer;

import com.google.inject.Inject;
import net.slipcor.sponge.spamhammer.cmds.*;
import net.slipcor.sponge.spamhammer.utils.Config;
import net.slipcor.sponge.spamhammer.utils.Language;
import net.slipcor.sponge.spamhammer.utils.Perms;
import net.slipcor.sponge.spamhammer.utils.Tracker;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Plugin(id = "spamhammer", name = "SpamHammer", version = "4.0.2")
public class SpamHammer {
    private Logger logger;
    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path configDir;

    @Inject
    private PluginContainer container;
    private SpamHandler handler;

    @Inject
    public SpamHammer(Logger logger) {
        this.logger = logger;
    }

    public SpamHandler getHandler() {
        return handler;
    }


    public boolean loadConfig() {
        try {
            Config.init(configDir);
        } catch (IOException e) {
            System.out.print(e);
            return false;
        }
        if (handler != null) {
            handler.killTask();
        }
        handler = new SpamHandler(this);
        return true;
    }

    public boolean loadLanguage() {
        try {
            Language.init(configDir.resolveSibling(Config.getString(Config.LANGUAGE_FILE)));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Listener
    public void onPreInit(final GameInitializationEvent event){
        if (!loadConfig() || !loadLanguage()) {
            Sponge.getServer().getConsole().sendMessage(Language.ERROR_CONFIG_LOAD.red());
            return;
        }
    }

    @Listener
    public void onServerStart(final GameStartedServerEvent event) {
        // initiate child commands
        List<SubCommand> subCommands = new ArrayList<>();
        subCommands.add(new SpamReload(this));
        subCommands.add(new SpamReset(this));
        subCommands.add(new SpamUnmute(this));

        // initiate main command
        new SpamMain(this, subCommands);
        final Tracker trackMe = new Tracker(this);
    }

    @Listener
    public void onChat(final MessageChannelEvent.Chat event)  {
        final Optional<Player> oPlayer = event.getCause().first(Player.class);
        if (!oPlayer.isPresent()) {
            return;
        }

        final Player player = oPlayer.get();
        if (player.hasPermission(Perms.BYPASS.toString())) {
            return;
        }

        if (!"command".equals(Config.getString(Config.PUNISH_MUTE_TYPE)) && handler.isMuted(player) && !player.hasPermission(Perms.BYPASS_MUTE.toString())) {
            event.setCancelled(true);
            player.sendMessage(Language.BAD_YOU_ARE_MUTED.red());
            return;
        }

        if (handler.handleChat(player, event.getRawMessage())
                && Config.getBoolean(Config.SPAM_RATE_PREVENT) && !player.hasPermission(Perms.BYPASS_REPEAT.toString())) {
            event.setCancelled(true);
            player.sendMessage(Language.BAD_SPAMMING_MESSAGE.red());
            return;
        }

        if (Config.getBoolean(Config.CHECK_IPS) && handler.handleChatIP(player, event.getRawMessage())
                && !player.hasPermission(Perms.BYPASS_IPS.toString())) {
            Sponge.getServer().getConsole().sendMessage(
                    Language.INFO_CAUGHT_IP.yellow(player.getName() + ": " + event.getRawMessage()));
            event.setCancelled(true);
            player.sendMessage(Language.BAD_SPAMMING_IP.red());
            return;
        }

        if (Config.getBoolean(Config.CHECK_URLS) && handler.handleChatURL(player, event.getRawMessage())
                && !player.hasPermission(Perms.BYPASS_URLS.toString())) {
            Sponge.getServer().getConsole().sendMessage(
                    Language.INFO_CAUGHT_URL.yellow(player.getName() + ": " + event.getRawMessage()));
            event.setCancelled(true);
            player.sendMessage(Language.BAD_SPAMMING_URL.red());
        }
    }

    @Listener
    public void onCommand(final SendCommandEvent event) {
        final Optional<Player> oPlayer = event.getCause().first(Player.class);
        if (!oPlayer.isPresent()) {
            return;
        }

        final Player player = oPlayer.get();
        if (player.hasPermission(Perms.BYPASS.toString())) {
            return;
        }

        final List<String> chatcmds = Config.getList(Config.SPAM_COMMAND_CHECKLIST);
        boolean chat = false;
        for (String entry : chatcmds) {
            if (event.getCommand().startsWith(entry)) {
                chat = true;
                break;
            }
        }

        if ((chat || !"chat".equals(Config.getString(Config.PUNISH_MUTE_TYPE)))
                && handler.isMuted(player) && !player.hasPermission(Perms.BYPASS_MUTE.toString())) {

            // this in here is the punishment, so the opposite of what we allow
            event.setCancelled(true);
            player.sendMessage(Language.BAD_YOU_ARE_MUTED.red());
            return;
        }
        if (!chatcmds.contains(event.getCommand())) {
            return;
        }

        if (handler.handleChat(player, Text.of(event.getCommand() + " " + event.getArguments()))
                && Config.getBoolean(Config.SPAM_RATE_PREVENT) && !player.hasPermission(Perms.BYPASS_REPEAT.toString())) {
            event.setCancelled(true);
            player.sendMessage(Language.BAD_SPAMMING_MESSAGE.red());
        }
    }
}
