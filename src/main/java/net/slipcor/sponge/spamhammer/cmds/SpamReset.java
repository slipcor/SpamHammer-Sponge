package net.slipcor.sponge.spamhammer.cmds;

import net.slipcor.sponge.spamhammer.SpamHammer;
import net.slipcor.sponge.spamhammer.utils.Perms;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class SpamReset extends SubCommand {
    final SpamHammer plugin;
    public SpamReset(final SpamHammer plugin) {
        super(plugin, GenericArguments.user(Text.of("user")), Perms.CMD_RESET, "Resets a player's history with SpamHammer", "spamreset", "reset", "rs");
        this.plugin = plugin;
    }
    @Override
    public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
        if (!args.hasAny("user")) {
            return CommandResult.empty();
        }
        final Optional<User> oUser = args.getOne("user");
        if (!oUser.isPresent()) {
            return CommandResult.empty();
        }
        final User user = oUser.get();
        plugin.getHandler().clearKickHistory(src, user);
        plugin.getHandler().clearMuteHistory(src, user);
        return CommandResult.success();
    }
}
