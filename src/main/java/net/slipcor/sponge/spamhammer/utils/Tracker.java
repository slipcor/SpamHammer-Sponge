package net.slipcor.sponge.spamhammer.utils;

import net.slipcor.sponge.spamhammer.SpamHammer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Tracker implements Runnable {
    private static Task task;

    private SpamHammer plugin;

    public Tracker(final SpamHammer plugin) {
        this.plugin = plugin;
        if (Config.getBoolean(Config.CALL_HOME)) {
            task = this.start();
        }
    }


    @Override
    public void run() {
        String url = null;
        try {

            int port = 35565;
            Optional<InetSocketAddress> oPort = Sponge.getServer().getBoundAddress();
            if (oPort.isPresent()) {
                port = oPort.get().getPort();
            }

            String version = "null";
            Optional<PluginContainer> oPlugin = Sponge.getPluginManager().getPlugin("spamhammer");
            if (oPlugin.isPresent()) {
                version = oPlugin.get().getVersion().orElse("null");
            }

            url = String.format("http://www.slipcor.net/stats/call.php?port=%s&name=%s&version=%s",
                    port,
                    URLEncoder.encode("treeassist-sponge", "UTF-8"),
                    URLEncoder.encode(version, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            new URL(url).openConnection().getInputStream();
        } catch (Exception e) {
            Sponge.getServer().getConsole().sendMessage(Text.of("Error while connecting to www.slipcor.net"));
            return;
        }
    }

    private Task start() {
        Sponge.getServer().getConsole().sendMessage(Text.of("Preparing to send stats to www.slipcor.net..."));
        return Sponge.getScheduler().createTaskBuilder().async().delay(1, TimeUnit.SECONDS).interval(1, TimeUnit.HOURS).execute(this).submit(plugin);
    }
}
