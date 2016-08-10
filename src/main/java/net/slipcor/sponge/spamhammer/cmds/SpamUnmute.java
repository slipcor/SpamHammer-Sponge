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

public class SpamUnmute extends SubCommand {
    final SpamHammer plugin;
    public SpamUnmute(final SpamHammer plugin) {
        super(plugin, GenericArguments.user(Text.of("user")), Perms.CMD_UNMUTE, "Unmutes a player muted by SpamHammer.", "spamunmute", "unmute", "u");
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
        plugin.getHandler().unMutePlayer(src, user);
        return CommandResult.success();
    }
}
