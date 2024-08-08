package fr.communaywen.core.stat;

import fr.communaywen.core.AywenCraftPlugin;
import fr.communaywen.core.quests.qenum.QUESTS;
import fr.communaywen.core.utils.database.DatabaseConnector;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatData extends DatabaseConnector {

    private static final String TABLE_NAME = "stats";
    private static final String PLAYER_COLUMN = "player";

    public static void initializeStatsTable() throws SQLException {
        if (!tableExists()) {
            createStatsTable();
        } else {
            updateStatsTable();
        }
    }

    private static void createStatsTable() throws SQLException {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + PLAYER_COLUMN + " VARCHAR(36) PRIMARY KEY");

        for (Stats.StatList stat : Stats.StatList.values()) {
            sql.append(", ").append(stat.getName()).append(" INT DEFAULT 0");
        }

        sql.append(")");

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql.toString());
        }
        System.out.println("Table '" + TABLE_NAME + "' created successfully.");
    }

    private static void updateStatsTable() throws SQLException {
        for (Stats.StatList stat : Stats.StatList.values()) {
            if (!columnExists(stat.getName())) {
                addColumn(stat.getName());
            }
        }
    }

    private static boolean tableExists() throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet tables = meta.getTables(null, null, TABLE_NAME, null)) {
            return tables.next();
        }
    }

    private static boolean columnExists(String columnName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, TABLE_NAME, columnName)) {
            return rs.next();
        }
    }

    private static void addColumn(String columnName) throws SQLException {
        String sql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + columnName + " INT DEFAULT 0";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public static Map<UUID, Stats> loadStats() {
        try {
            Map<UUID, Stats> stats = new HashMap<>();

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM stats");
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                Map<Stats.StatList,Number> map = new HashMap<>();
                for (Stats.StatList value : Stats.StatList.values()) {
                    map.put(value, (Number) rs.getObject(value.getName()));
                }
                stats.put(UUID.fromString(rs.getString("player")),new Stats(map));
            }
            System.out.println("data loaded");
            return stats;

        } catch (SQLException ignored) {}

        return null;
    }

    public static Map<UUID, Stats> loadStats(Player player) {
        try {
            Map<UUID, Stats> stats = new HashMap<>();

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM stats");
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                Map<Stats.StatList,Number> map = new HashMap<>();
                for (Stats.StatList value : Stats.StatList.values()) {
                    map.put(value, (Number) rs.getObject(value.getName()));
                }
                stats.put(UUID.fromString(rs.getString("player")),new Stats(map));
            }

            return stats;

        } catch (SQLException ignored) {}

        return null;
    }
    public static void saveStats(Player player) {
        Stats stats = AywenCraftPlugin.getInstance().getManagers().getStatsManager().getStats(player);
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM stats WHERE player = ?");
            statement.setString(1, player.getUniqueId().toString());

            ResultSet rs = statement.executeQuery();
            PreparedStatement stm;
            //update if exist else insert
            if (rs.next()) {
                StringBuilder builder = new StringBuilder("UPDATE stats SET ");
                for (Stats.StatList value : stats.getValues().keySet()) {
                    builder.append(value.getName()).append(" = ").append(stats.getValues().get(value)).append(", ");
                }
                builder.deleteCharAt(builder.length()-2);
                builder.append(" WHERE player = \"").append(player.getUniqueId()).append("\"");
                stm = connection.prepareStatement(
                        builder.toString());
                stm.executeUpdate();

            } else {
                StringBuilder mainBuilder = new StringBuilder("INSERT INTO stats (player");
                StringBuilder valueBuilder = new StringBuilder("( \"").append(player.getUniqueId()).append("\"");
                for (Stats.StatList value : stats.getValues().keySet()) {
                    mainBuilder.append(", ").append(value.getName());
                    valueBuilder.append(", ").append(stats.getValues().get(value));
                }
                valueBuilder.append(" )");
                mainBuilder.append(") VALUES (").append(valueBuilder);
                stm = connection.prepareStatement(
                        mainBuilder.toString());
                stm.executeUpdate();
            }
            System.out.println("data save");
            int affectedRows = stm.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Saving stats failed, no rows affected.");
            }

        } catch (SQLException ignored) {
           // System.out.println(ignored.toString());
        }
    }
}
