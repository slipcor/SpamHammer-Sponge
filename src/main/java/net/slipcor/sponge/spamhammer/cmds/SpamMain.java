package net.slipcor.sponge.spamhammer.cmds;

import net.slipcor.sponge.spamhammer.SpamHammer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.List;

public class SpamMain implements CommandExecutor {
    public SpamMain(final SpamHammer plugin, final List<SubCommand> subCommands) {
        CommandSpec.Builder builder = CommandSpec.builder().description(Text.of("Base command for SpamHammer."));
        for (final SubCommand cmd : subCommands) {
            builder = builder.child(cmd.cs, cmd.labels);
        }
        final CommandSpec spec = builder.build();
        Sponge.getCommandManager().register(plugin, spec, "spamhammer", "sh", "spam"); // only hook the first label as main command!
    }

    @Override
    public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
        src.sendMessage(Text.of("/spamhammer reload - reload the configuration"));
        src.sendMessage(Text.of("/spamhammer reset [player] - reset a player's history"));
        src.sendMessage(Text.of("/spamhammer unmute [player] - unmute a player"));
        return CommandResult.success();
    }
}
