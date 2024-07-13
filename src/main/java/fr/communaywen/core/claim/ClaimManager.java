package fr.communaywen.core.claim;

import fr.communaywen.core.AywenCraftPlugin;
import fr.communaywen.core.teams.Team;
import fr.communaywen.core.teams.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimManager implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new GamePlayer(event.getPlayer().getName());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        for (RegionManager region : AywenCraftPlugin.getInstance().regions) {
            if (region.isInArea(event.getBlock().getLocation()) && !region.isTeamMember(playerUuid)) {
                event.setCancelled(true);
                player.sendMessage("§cCe n'est pas chez vous");
                return;
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        for (RegionManager region : AywenCraftPlugin.getInstance().regions) {
            if (region.isInArea(event.getBlock().getLocation()) && !region.isTeamMember(playerUuid)) {
                event.setCancelled(true);
                player.sendMessage("§cCe n'est pas chez vous");
                return;
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            for (RegionManager region : AywenCraftPlugin.getInstance().regions) {
                if (region.isInArea(event.getClickedBlock().getLocation()) && !region.isTeamMember(playerUuid)) {
                    event.setCancelled(true);
                    player.sendMessage("§cCe n'est pas chez vous");
                }
            }
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (player.getItemInHand().getType() == Material.STICK) {
                event.setCancelled(true);

                GamePlayer gp = GamePlayer.gamePlayers.get(player.getName());

                if (gp.getPos1() == null) {

                    if(gp.getCountClaims() >= 5) {
                        player.getInventory().removeItem(player.getItemInHand());
                        player.sendMessage("§cVotre team possède déjà 5 claims, qui est la limite maximale de claim possible par team.");
                        gp.setPos1(null);
                        gp.setPos2(null);
                        return;
                    }

                    gp.setPos1(event.getClickedBlock().getLocation());
                    player.sendMessage("§aPosition 1 défini.");

                    Bukkit.getScheduler().runTaskLater(AywenCraftPlugin.getInstance(), () -> {
                        gp.setPos1(null);
                        gp.setPos2(null);
                    }, 20 * 60 * 5); // 5 Minutes sans interactions
                    return;
                }

                if (gp.getPos1() != null && gp.getPos2() == null) {
                    player.getInventory().removeItem(player.getItemInHand());
                    gp.setPos2(event.getClickedBlock().getLocation());

                    if (!gp.getPos1().getWorld().equals(gp.getPos2().getWorld())) {
                        player.sendMessage("§cVous devez rester dans le même monde entre les deux points !");
                        gp.setPos1(null);
                        gp.setPos2(null);
                        return;
                    } else if(gp.isRegionConflict(player, gp.getPos1(), gp.getPos2())) {
                        player.sendMessage("§cUne régions WorldGuard traverse votre claim.");
                        gp.setPos1(null);
                        gp.setPos2(null);
                        return;
                    }

                    gp.setCountClaims(gp.getCountClaims() + 1);

                    Team playerTeam = AywenCraftPlugin.getInstance().getTeamManager().getTeamByPlayer(playerUuid);
                    if (playerTeam == null) {
                        player.sendMessage("§cVous devez être dans une équipe pour créer une région !");
                        gp.setPos1(null);
                        gp.setPos2(null);
                        return;
                    }

                    RegionManager region = new RegionManager(gp.getPos1(), gp.getPos2(), playerTeam);

                    for (RegionManager regionCheck : AywenCraftPlugin.getInstance().regions) {
                        if (regionCheck.isInArea(region.maxLoc) || regionCheck.isInArea(region.minLoc) || regionCheck.isInArea(region.getMiddle())) {
                            player.sendMessage("§cIl y a déjà une région dans cette zone !");
                            gp.setPos1(null);
                            gp.setPos2(null);
                            return;
                        }
                    }

                    String[] loc = new String[]{
                            "" + gp.getPos1().getX(),
                            "" + gp.getPos1().getZ(),
                            "" + gp.getPos2().getX(),
                            "" + gp.getPos2().getZ(),
                            gp.getPos1().getWorld().getName()
                    };

                    AywenCraftPlugin.getInstance().claimConfigFile.addClaim(playerTeam.getName() + "_" + gp.getCountClaims(), loc);
                    AywenCraftPlugin.getInstance().claimConfigFile.save();
                    AywenCraftPlugin.getInstance().regions.add(region);

                    player.sendMessage("§aPosition 2 définie.");
                    player.sendMessage("§aVous venez de créer une nouvelle région.");

                    gp.setPos1(null);
                    gp.setPos2(null);
                }
            }
        }
    }

    public void updateRegionList(RegionManager oldRegion, RegionManager newRegion) {
        AywenCraftPlugin.getInstance().regions.remove(oldRegion);
        AywenCraftPlugin.getInstance().regions.add(newRegion);
    }
}
