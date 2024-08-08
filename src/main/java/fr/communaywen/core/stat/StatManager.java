package fr.communaywen.core.stat;

import fr.communaywen.core.quests.qenum.QUESTS;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

@Getter
public class StatManager {
    private final Map<UUID,Stats> mapstat;

    public StatManager() {
        mapstat = StatData.loadStats();
        System.out.println("StatManager loaded");
    }


    public void add(Player player, Stats.StatList statList, Number value) {
        mapstat.put(player.getUniqueId(),getStats(player).add(statList,value));
    }




    public Stats getStats(Player player) {
        UUID id = player.getUniqueId();
        if(!mapstat.containsKey(id)) {
            mapstat.put(id,new Stats());
        }
        return mapstat.get(id);
    }

    public void saveStat(Player player) {
        StatData.saveStats(player);
    }
}
