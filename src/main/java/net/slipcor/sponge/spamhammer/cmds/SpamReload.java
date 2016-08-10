package net.slipcor.sponge.spamhammer.cmds;

import net.slipcor.sponge.spamhammer.SpamHammer;
import net.slipcor.sponge.spamhammer.utils.Language;
import net.slipcor.sponge.spamhammer.utils.Perms;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;

public class SpamReload extends SubCommand {
    final SpamHammer plugin;
    public SpamReload(final SpamHammer plugin) {
        super(plugin, GenericArguments.none(), Perms.CMD_RELOAD, "Reloads the SpamHammer configs.", "spamreload", "reload", "rl");
        this.plugin = plugin;
    }
    @Override
    public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
        if (plugin.loadConfig() && plugin.loadLanguage()) {
            src.sendMessage(Language.GOOD_RELOAD_SUCCESS.green());
        } else {
            src.sendMessage(Language.ERROR_CONFIG_LOAD.red());
        }
        return CommandResult.success();
    }
}
