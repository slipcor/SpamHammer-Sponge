package net.slipcor.sponge.spamhammer;

import net.slipcor.sponge.spamhammer.utils.Config;
import net.slipcor.sponge.spamhammer.utils.Language;
import net.slipcor.sponge.spamhammer.utils.Perms;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpamHandler {
    final SpamHammer plugin;

    final private ConcurrentMap<String, ArrayDeque<Long>> playerChatTimes = new ConcurrentHashMap<>();
    final private ConcurrentMap<String, ArrayDeque<String>> playerChatHistory = new ConcurrentHashMap<>();
    final private ConcurrentMap<String, Long> playerActionTime = new ConcurrentHashMap<>();

    final private List<String> mutedPlayers = new ArrayList<>();
    final private List<String> beenMutedPlayers = new ArrayList<>();
    final private List<String> beenKickedPlayers = new ArrayList<>();

    final Pattern patIP = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    final Pattern patURL = Pattern.compile("(http://)|(https://)?(www)?\\S{2,}((\\.com)|(\\.net)|(\\.org)|(\\.co\\.uk)|(\\.tk)|(\\.info)|(\\.es)|(\\.de)|(\\.arpa)|(\\.edu)|(\\.firm)|(\\.int)|(\\.mil)|(\\.mobi)|(\\.nato)|(\\.to)|(\\.fr)|(\\.ms)|(\\.vu)|(\\.eu)|(\\.nl)|(\\.us)|(\\.dk))");

    final Task task;

    public SpamHandler(final SpamHammer plugin) {
        this.plugin = plugin;
        task = Sponge.getScheduler().createTaskBuilder().interval(1, TimeUnit.SECONDS).execute(() -> checkTimes()).submit(plugin);
    }

    public void banPlayer(final CommandSource src, final User user) {
        if (playerChatHistory.containsKey(user.getName())) {
            playerChatHistory.get(user.getName()).clear();
        }
        if (user.isOnline()) {
            Optional<Player> oPlayer = user.getPlayer();
            if (oPlayer.isPresent()) {
                final Player onlinePlayer = oPlayer.get();
                if (!onlinePlayer.hasPermission(Perms.BYPASS_BAN.toString()) && !onlinePlayer.hasPermission(Perms.BYPASS_KICK.toString())) {
                    onlinePlayer.kick(Language.BAD_YOU_ARE_BANNED.red());
                }
            }
        }

        Ban ban = Ban.builder().type(BanTypes.PROFILE).profile(user.getProfile()).reason(Language.BAD_YOU_ARE_BANNED.red()).build();
        if (Sponge.getServiceManager().provide(BanService.class).get().addBan(ban).isPresent()) {
            src.sendMessage(Language.GOOD_BAN_SUCCESS.green(user.getName()));
        } else {
            src.sendMessage(Language.ERROR_BAN_FAILED.red(user.getName()));
        }
    }

    private boolean beenKicked(final User user) {
        return beenKickedPlayers.contains(user.getName());
    }

    private boolean beenMuted(final User user) {
        return beenMutedPlayers.contains(user.getName());
    }

    private boolean checkRegEx(final Pattern regEx, final String message) {
        final Matcher matcher = regEx.matcher(message);
        return matcher.find();
    }

    private void checkTimes() {
        final long time = System.nanoTime() / 1000000;
        for (final String playerName : playerActionTime.keySet()) {
            final User user = getUser(playerName);
            final long action = playerActionTime.get(playerName);
            if (isMuted(user)) {
                final long muteLength = Config.getInt(Config.PUNISH_MUTE_TIME) * 1000;
                if (time > (action + muteLength)) {
                    unMutePlayer(Sponge.getServer().getConsole(), user);
                }
            }
            final long coolOff = Config.getInt(Config.PUNISH_COOLDOWN_SECONDS) * 1000;
            if ((time > (action + coolOff))
                    && (Config.getInt(Config.PUNISH_COOLDOWN_SECONDS) != 0)) {
                if (beenKicked(user)) {
                    clearKickHistory(Sponge.getServer().getConsole(), user);
                }
                if (beenMuted(user)) {
                    final User onlinePlayer = getUser(user.getName());
                    Optional<Player> oPlayer = onlinePlayer.getPlayer();
                    if (oPlayer.isPresent()) {
                        Player player = oPlayer.get();
                        player.sendMessage(Language.INFO_COOLDOWN_OVER.yellow());
                    }
                    clearMuteHistory(Sponge.getServer().getConsole(), user);
                }
            }
        }
    }

    public void clearKickHistory(final CommandSource src, final User user) {
        beenKickedPlayers.remove(user.getName());
        src.sendMessage(Language.GOOD_RESET_KICK_SUCCESS.green(user.getName()));
    }

    public void clearMuteHistory(final CommandSource src, final User user) {
        beenMutedPlayers.remove(user.getName());
        src.sendMessage(Language.GOOD_RESET_MUTE_SUCCESS.green(user.getName()));
    }

    private User getUser(final String name) {
        final Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        if (userStorage.isPresent()) {
            UserStorageService service = userStorage.get();
            Optional<User> oUser = service.get(name);
            if (oUser.isPresent()) {
                return oUser.get();
            }
        }
        return null;
    }

    public boolean hasDuplicateMessages(final User user) {
        if (user.hasPermission(Perms.BYPASS_REPEAT.toString())) {
            return false; // if he has the permission, he never has any duplicates
        }

        boolean isSpamming = false;
        int samecount = 1;
        String lastMessage = null;
        for (String message : playerChatHistory.get(user.getName())) {
            if (lastMessage == null) {
                lastMessage = message;
                continue;
            }
            if (message.equals(lastMessage)) {
                samecount++;
            } else {
                playerChatHistory.get(user.getName()).clear();
                playerChatHistory.get(user.getName()).add(message);
                break;
            }
        }
        isSpamming = (samecount > Config.getInt(Config.REPEAT_LIMIT));
        return isSpamming;
    }

    public boolean isMuted(final User user) {
        return mutedPlayers.contains(user.getName());
    }

    public void kickPlayer(CommandSource src, final User user) {
        if (playerChatHistory.containsKey(user.getName())) {
            playerChatHistory.get(user.getName()).clear();
        }
        beenKickedPlayers.add(user.getName());
        playerActionTime.put(user.getName(), System.nanoTime() / 1000000);
        Optional<Player> oPlayer = user.getPlayer();
        if (!oPlayer.isPresent()) {
            src.sendMessage(Language.ERROR_PLAYER_NOT_FOUND.red(user.getName()));
            return;
        }
        Player player = oPlayer.get();
        if (!player.hasPermission(Perms.BYPASS_KICK.toString())) {
            player.kick(Language.BAD_YOU_HAVE_BEEN_KICKED.red());
            src.sendMessage(Language.GOOD_KICKED_SUCCESS.green(player.getName()));
        }
    }

    public void mutePlayer(CommandSource src, final User user) {
        mutedPlayers.add(user.getName());
        beenMutedPlayers.add(user.getName());
        playerActionTime.put(user.getName(), System.nanoTime() / 1000000);
        playerChatTimes.get(user.getName()).clear();
        if (playerChatHistory.containsKey(user.getName())) {
            playerChatHistory.get(user.getName()).clear();
        }

        Optional<Player> oPlayer = user.getPlayer();
        if (!oPlayer.isPresent()) {
            src.sendMessage(Language.ERROR_PLAYER_NOT_FOUND.red(user.getName()));
            return;
        }
        Player player = oPlayer.get();
        player.sendMessage(Language.BAD_YOU_ARE_NOW_MUTED.red(String.valueOf(Config.getInt(Config.PUNISH_MUTE_TIME))));
        src.sendMessage(Language.GOOD_MUTED_SUCCESS.green(player.getName()));
    }

    private void playerIsSpamming(CommandSource src, final User name) {
        if (Config.getBoolean(Config.PUNISH_MUTE) && (!beenMuted(name) || (!Config.getBoolean(Config.PUNISH_KICK) && !Config.getBoolean(Config.PUNISH_BAN)))) {
            mutePlayer(src, name);
            return;
        }
        if (Config.getBoolean(Config.PUNISH_KICK) && (!beenKicked(name) || !Config.getBoolean(Config.PUNISH_BAN))) {
            kickPlayer(src, name);
            return;
        }
        if (Config.getBoolean(Config.PUNISH_BAN)) {
            banPlayer(src, name);
        }
    }

    public void unBanPlayer(CommandSource src, final User user) {
        Optional<BanService> oService = Sponge.getServiceManager().provide(BanService.class);
        if (!oService.isPresent()) {
            src.sendMessage(Text.of("No BanService present !?"));
            return;
        }
        BanService service = oService.get();

        if (service.pardon(user.getProfile())) {
            src.sendMessage(Language.GOOD_UNBAN_SUCCESS.green(user.getName()));
        } else {
            src.sendMessage(Language.ERROR_UNBAN_NOT_BANNED.red(user.getName()));
        }
    }

    public void unMutePlayer(CommandSource src, User user) {
        if (!mutedPlayers.contains(user.getName())) {
            src.sendMessage(Language.ERROR_UNMUTE_NOT_MUTED.red(user.getName()));
            return;
        }
        mutedPlayers.remove(user.getName());
        src.sendMessage(Language.GOOD_UNMUTE_SUCCESS.green(user.getName()));
        Optional<Player> oPlayer = user.getPlayer();
        if (!oPlayer.isPresent()) {
            return;
        }
        Player player = oPlayer.get();
        player.sendMessage(Language.GOOD_YOU_ARE_UNMUTED.green());
    }

    public boolean handleChatURL(final Player player, final Text text) {
        if (checkRegEx(patURL, text.toString())) {
            playerIsSpamming(Sponge.getServer().getConsole(), player);
            return true;
        }
        return false;
    }

    public boolean handleChatIP(final Player player, final Text text) {
        if (checkRegEx(patIP, text.toString())) {
            playerIsSpamming(Sponge.getServer().getConsole(), player);
            return true;
        }
        return false;
    }

    public boolean handleChat(final Player player, final Text txt) {
        boolean isSpamming = false;

        String message = txt.toPlain();

        // Detect rate limited messages
        ArrayDeque<Long> times = playerChatTimes.get(player.getName());
        if (times == null) {
            times = new ArrayDeque<Long>();
        }
        final long curtime = System.nanoTime() / 1000000;
        times.add(curtime);
        if (times.size() > Config.getInt(Config.SPAM_RATE_LIMIT)) {
            times.remove();
        }
        if (times.isEmpty()) {
            times.add(curtime);
        } else {
            final long timediff = times.getLast() - times.getFirst();
            if (timediff > (Config.getInt(Config.SPAM_RATE_PERIOD) * 1000)) {
                times.clear();
                times.add(curtime);
            }
        }

        if (times.size() >= Config.getInt(Config.SPAM_RATE_LIMIT)) {
            isSpamming = true;
        }
        playerChatTimes.put(player.getName(), times);

        // Detect duplicate messages
        if (Config.getBoolean(Config.REPEAT_BLOCK) && !isSpamming) {
            ArrayDeque<String> playerChat = playerChatHistory.get(player.getName());
            if (playerChat == null) {
                playerChat = new ArrayDeque<>();
            }
            if (playerChat.size() > Config.getInt(Config.REPEAT_LIMIT)) {
                playerChat.remove();
            }
            playerChat.add(message);
            playerChatHistory.put(player.getName(), playerChat);
            isSpamming = hasDuplicateMessages(player);
        }

        if (Config.getBoolean(Config.CAPS_RESTRICT) && !isSpamming) {
            // nothing bad yet. But maybe there is a caps issue?
            if (Config.getInt(Config.CAPS_THRESHOLD) < message.length() &&
                    (Config.getInt(Config.CAPS_MAX_AMOUNT) > 0 || Config.getFloat(Config.CAPS_RATIO) > 0f)) {
                int sum = 0;
                int uppercase = 0;
                final char[] cArray = message.toCharArray();
                for (char c : cArray) {
                    if (Character.isUpperCase(c)) {
                        uppercase++;
                        sum++;
                    } else if (Character.isLowerCase(c)) {
                        sum++;
                    }
                }
                // sum now is all actual letters and signs
                // uppercase is the actual uppercase count, including signs
                isSpamming = sum>0 &&(Config.getInt(Config.CAPS_MAX_AMOUNT) < uppercase) || (Config.getFloat(Config.CAPS_RATIO) < (float)uppercase/(float)sum);
            }
        }

        if (isSpamming) {
            playerIsSpamming(Sponge.getServer().getConsole(), player);
        }
        return isSpamming;
    }

    public void killTask() {
        this.task.cancel();
    }
}
