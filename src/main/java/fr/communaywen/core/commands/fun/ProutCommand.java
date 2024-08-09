package fr.communaywen.core.commands.fun;

import fr.communaywen.core.AywenCraftPlugin;
import fr.communaywen.core.credit.Credit;
import fr.communaywen.core.credit.Feature;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import org.bukkit.entity.Boat;

import java.util.concurrent.TimeUnit;

/**
 * THE Prout command.
 * <p>
 * Usage: /prout
 * Permission: PREFIX.command.prout
 */

@Feature("Prout")
@Credit("dandan")
public final class ProutCommand {
    @Command("prout")
    @Description("Prout !")
    @CommandPermission("ayw.command.prout")
    @Cooldown(value = 5, unit = TimeUnit.MINUTES)
    public void onCommand(Player player) {
        player.sendMessage("§2Beuuurk, ça pue !");

        if(player.isInsideVehicle()){
            if(player.getVehicle() instanceof Boat){
                Boat boat = (Boat) player.getVehicle();
                Material boatMaterial = getBoatType(boat);
                ItemStack itemBoat = new ItemStack(boatMaterial);
                player.getVehicle().remove();
                player.getWorld().dropItemNaturally(player.getLocation(), itemBoat);

                player.sendMessage("Votre bateau a coulé.");
            }
        }

        // Make the player jump
        final Vector currentVelocity = player.getVelocity();
        currentVelocity.setY(0.55d);

        player.setVelocity(currentVelocity);

        // Spawn some cloud particles
        final Location location = player.getLocation();
        final @Nullable World world = location.getWorld();

        if (world != null) {
            world.spawnParticle(Particle.CLOUD, location, 3, 0.02d, -0.04d, 0.02d, 0.09d);

            // Funny sound!
            world.playSound(location, Sound.ENTITY_VILLAGER_NO,  SoundCategory.PLAYERS, 0.8f, 2.3f);
            world.playSound(location, Sound.ENTITY_GOAT_EAT,  SoundCategory.PLAYERS,0.7f, 0.2f);
        }

        // Add glowing effect for 30 seconds
        addGlowingEffect(player);

        // Broadcast the message
        String broadcastMessage = "[§c§l§ka§r] §f§lPROUT !!! §r" + player.getName() + " a §f§lpété§r. §2§lBeurk !";
        Bukkit.broadcastMessage(broadcastMessage);
    }

    private void addGlowingEffect(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("glowGreen");

        if (team == null) {
            team = scoreboard.registerNewTeam("glowGreen");
            team.setColor(org.bukkit.ChatColor.GREEN);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }

        final Team finalTeam = team;
        team.addEntry(player.getName());
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0, false, false, true));

        Bukkit.getScheduler().runTaskLater(AywenCraftPlugin.getInstance(), () -> finalTeam.removeEntry(player.getName()), 600L);
    }

    private Material getBoatType(Boat boat){
        switch(boat.getBoatType()){
            case OAK -> {return Material.OAK_BOAT;}
            case BIRCH -> {return Material.BIRCH_BOAT;}
            case ACACIA -> {return Material.ACACIA_BOAT;}
            case BAMBOO -> {return Material.BAMBOO_RAFT;}
            case CHERRY -> {return Material.CHERRY_BOAT;}
            case JUNGLE -> {return Material.JUNGLE_BOAT;}
            case SPRUCE -> {return Material.SPRUCE_BOAT;}
            case DARK_OAK -> {return Material.DARK_OAK_BOAT;}
            case MANGROVE -> {return  Material.MANGROVE_BOAT;}
            default -> {return Material.OAK_BOAT;}
        }
    }
}
