package fr.communaywen.core.listeners;

import fr.communaywen.core.AywenCraftPlugin;
import fr.communaywen.core.utils.DiscordWebhook;
import fr.communaywen.core.utils.database.Blacklist;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.BroadcastMessageEvent;

public class ChatListener implements Listener {
    private final DiscordWebhook discordWebhook;

    private Blacklist blacklist;
    private AywenCraftPlugin plugin;

    public ChatListener(AywenCraftPlugin plugin, DiscordWebhook discordWebhook) {
        this.plugin = plugin;
        this.blacklist = new Blacklist(plugin);
        this.discordWebhook = discordWebhook;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        plugin.quizManager.onPlayerChat(event);

        String username = event.getPlayer().getName();
        String avatarUrl = "https://minotar.net/helm/" + username;
        String message = event.getMessage();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()-> discordWebhook.sendMessage(username, avatarUrl, message));

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.equals(event.getPlayer())) { return; }
            if (message.toLowerCase().contains(player.getName().toLowerCase())) {
                if (blacklist.isBlacklisted(player, event.getPlayer())) { return; }
                player.playSound(player.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
            }
        });
    }

    @EventHandler
    public void onBroadcastMessage(BroadcastMessageEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()-> discordWebhook.sendBroadcast(event.getMessage()));
    }
}
