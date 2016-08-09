package net.slipcor.sponge.spamhammer.cmds;

import net.slipcor.sponge.spamhammer.SpamHammer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public abstract class SubCommand implements CommandExecutor {
    final CommandSpec cs;
    final String[] labels;
    SubCommand(final SpamHammer plugin, final CommandElement args, final String description, final String... label) {
        cs = CommandSpec.builder()
                .description(Text.of(description))
                .arguments(args)
                .executor(this)
                .build();
        labels = label;
        Sponge.getCommandManager().register(plugin, cs, label[0]); // only hook the first label as main command!
    }
}
