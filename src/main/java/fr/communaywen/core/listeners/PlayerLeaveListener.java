package fr.communaywen.core.listeners;

import fr.communaywen.core.AywenCraftPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        AywenCraftPlugin.getInstance().getManagers().getStatsManager().saveStat(event.getPlayer());
    }
}
