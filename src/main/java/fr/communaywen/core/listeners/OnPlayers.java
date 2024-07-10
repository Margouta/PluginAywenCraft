package fr.communaywen.core.listeners;

import fr.communaywen.core.AywenCraftPlugin;
import fr.communaywen.core.utils.DraftAPI;
import fr.communaywen.core.utils.LinkerAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Attr;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class OnPlayers implements Listener {

    private LuckPerms luckPerms;
    private LinkerAPI linkerAPI;

    public void setLuckPerms(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    public void setLinkerAPI(LinkerAPI linkerAPI) {
        this.linkerAPI = linkerAPI;
    }

    public void addPermission(User user, String permission) {
        // Add the permission
        user.data().add(Node.builder(permission).build());

        // Now we need to save changes.
        luckPerms.getUserManager().saveUser(user);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) { // Donne une permissions en fonction du niveau
        Player player = event.getPlayer();

        System.out.println(player.getUniqueId().toString());
        if (player.getUniqueId().toString().equals("1581225d-e6a2-44e8-af37-c71702c60665")){
            Bukkit.getServer().dispatchCommand(player, "attribute "+player.getUniqueId().toString()+" minecraft:generic.scale base set 0.0625");
        }

        Bukkit.getScheduler().runTaskAsynchronously(AywenCraftPlugin.getInstance(), ()->{
            DraftAPI draftAPI = new DraftAPI();

            JSONObject data = null;
            String discordPlayerId = null;
            JSONArray users = null;
            try {
                data = new JSONObject(draftAPI.getTop());

                users = data.getJSONArray("users");

                discordPlayerId = this.linkerAPI.getUserId(player);
            } catch (IOException | SQLException e) {
                throw new RuntimeException(e);
            }

            if (discordPlayerId.isEmpty()){
                player.sendMessage("Profitez de récompenses en liant votre compte Discord à Minecraft");
                return;
            }

            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                String discordId = user.getString("id");

                if (discordPlayerId.equals(discordId)){
                    User lpPlayer = this.luckPerms.getPlayerAdapter(Player.class).getUser(player);

                    int level = user.getInt("level");
                    if (level < 10){ break; }

                    addPermission(lpPlayer, "ayw.levels.10");
                    if (level >= 20){
                        addPermission(lpPlayer, "ayw.levels.20");
                    }
                    if (level >= 30){
                        addPermission(lpPlayer, "ayw.levels.30");
                    }
                    if (level >= 40){
                        addPermission(lpPlayer, "ayw.levels.40");
                    }
                    if (level >= 50) {
                        addPermission(lpPlayer, "ayw.levels.50");
                    }
                    return;
                }
            }
        });

    }
}
