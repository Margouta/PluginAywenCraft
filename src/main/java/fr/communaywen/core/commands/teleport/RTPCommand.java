package fr.communaywen.core.commands.teleport;

import dev.lone.itemsadder.api.CustomStack;
import fr.communaywen.core.AywenCraftPlugin;
import fr.communaywen.core.credit.Credit;
import fr.communaywen.core.credit.Feature;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.HashMap;
import java.util.UUID;


@Feature("RTP")
@Credit("misieur")
@Command({"rtp", "randomteleportation"})
@Description("Téléportation aléatoire")
@CommandPermission("ayw.command.rtp")
public class RTPCommand implements Listener {
    private final String RTP_WAND_NAME;
    private final AywenCraftPlugin plugin;

    // Configuration values
    private final int COOLDOWN_TIME;
    private final int COOLDOWN_ERROR;
    private final int MIN_X;
    private final int MAX_X;
    private final int MIN_Z;
    private final int MAX_Z;
    private int MAX_TRY;
    private String MINE_NAME;

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    private final HashMap<UUID, Location> loc = new HashMap<>();

    public RTPCommand(AywenCraftPlugin plugin) {
        this.plugin = plugin;

        // Load configuration values
        COOLDOWN_TIME = plugin.getConfig().getInt("rtp.cooldown");
        COOLDOWN_ERROR = plugin.getConfig().getInt("rtp.cooldown-error");
        MIN_X = plugin.getConfig().getInt("rtp.minx");
        MAX_X = plugin.getConfig().getInt("rtp.maxx");
        MIN_Z = plugin.getConfig().getInt("rtp.minz");
        MAX_Z = plugin.getConfig().getInt("rtp.maxz");
        MAX_TRY = plugin.getConfig().getInt("rtp.max_try");
        RTP_WAND_NAME = plugin.getConfig().getString("rtp.rtp_wand");
        MINE_NAME = plugin.getConfig().getString("mine.name");

    }


    @DefaultFor("~")
    public void onCommand(Player player) {
        if (player.getWorld().getName().equals(MINE_NAME)) {
            UUID playerId = player.getUniqueId();
            long Time = System.currentTimeMillis() / 1000;
            long ExactTime = System.currentTimeMillis();

            if (cooldowns.containsKey(playerId)) {
                long lastUsed = cooldowns.get(playerId);
                long timeSinceLastUse = Time - lastUsed;

                if (timeSinceLastUse < COOLDOWN_TIME) {
                    long timeLeft = COOLDOWN_TIME - timeSinceLastUse;
                    player.sendActionBar("Veyez attendre "+timeLeft+"s");
                    return;
                }
            }
            World world = player.getWorld();
            cooldowns.put(playerId, Time - COOLDOWN_TIME + COOLDOWN_ERROR);
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (int i = 1; i <= MAX_TRY; i++) {
                        int x = (int) (Math.random() * (MAX_X - MIN_X) + MIN_X);
                        int z = (int) (Math.random() * (MAX_Z - MIN_Z) + MIN_Z);
                        if (!world.getBiome(new Location(world, x, 64, z)).equals(Biome.RIVER) || !world.getBiome(new Location(world, x, 64, z)).toString().contains("OCEAN")) {
                            int y = world.getHighestBlockAt(new Location(world, x, 64, z)).getY();
                            Location location = new Location(world, x, y + 1, z);
                            if (new Location(world, x, y, z).getBlock().getType().isSolid()) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        player.teleport(location);
                                        player.sendMessage(" §aRTP réussi x: §f" + x + " §ay: §f" + y + " §az: §f" + z);
                                        player.sendTitle(" §aRTP réussi", "x: " + x + " y: " + y + " z: " + z);
                                        cooldowns.put(playerId, Time);
                                    }
                                }.runTask(plugin);
                                return;
                            }
                        } else if (i >= 3) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    player.sendTitle(" §cErreur", "");
                                    cooldowns.put(playerId, Time - COOLDOWN_TIME + COOLDOWN_ERROR);
                                }
                            }.runTask(plugin);
                            return;
                        }
                    }
                }
            }.runTaskAsynchronously(plugin);
        }
        else{
            player.sendMessage( Component.text("§c❌§7Le /rtp n'es disponible que dans le monde minage pour utiliser le rtp dans l'overworld veyez utiliser le RTPWand [§f§nvoir le craft§r§7]")
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,"/rtp craft"))
                    .hoverEvent(HoverEvent.showText(Component.text("Clique pour voir le craft"))));
        }
    }
    @Subcommand("craft")
    @Description("Afficher le craft")
    public void craft(Player player) {
        player.openInventory(getInventory());
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getOpenInventory().getTitle().toString().contains("§f\uF809\uE016\uF801\uF829\uF80C\uF80A\uF808\uF807§0RTP Wand\uF82C\uF82A\uF828\uF827")) {
            e.setCancelled(true);
        }
    }
    public Inventory getInventory(){
        Inventory inv = Bukkit.createInventory(null, 54, "§f\uF809\uE016\uF801\uF829\uF80C\uF80A\uF808\uF807§0RTP Wand\uF82C\uF82A\uF828\uF827");
        inv.setItem(2, new ItemStack(Material.ENDER_PEARL,1));
        inv.setItem(11, new ItemStack(Material.STICK,1));
        inv.setItem(20, new ItemStack(Material.STICK,1));
        inv.setItem(16, CustomStack.getInstance(RTP_WAND_NAME).getItemStack());
        return inv;
    }
}
