package fr.communaywen.core;

import dev.xernas.menulib.MenuLib;
import fr.communaywen.core.commands.*;
import fr.communaywen.core.economy.EconomyManager;
import fr.communaywen.core.listeners.AntiTrampling;
import fr.communaywen.core.listeners.ChatListener;
import fr.communaywen.core.listeners.SleepListener;
import fr.communaywen.core.quests.Quest;
import fr.communaywen.core.quests.QuestManager;
import fr.communaywen.core.teams.TeamManager;
import fr.communaywen.core.utils.DiscordWebhook;
import fr.communaywen.core.utils.MOTDChanger;
import fr.communaywen.core.utils.PermissionCategory;
import fr.communaywen.core.utils.database.DatabaseManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class AywenCraftPlugin extends JavaPlugin {

    private MOTDChanger motdChanger;
    private TeamManager teamManager;
    private FileConfiguration bookConfig;
    private static AywenCraftPlugin instance;
    private EconomyManager economyManager;
    private QuestManager questManager;
    private DatabaseManager databaseManager;

    private void loadBookConfig() {
        File bookFile = new File(getDataFolder(), "rules.yml");
        if (!bookFile.exists()) {
            saveResource("rules.yml", false);
        }
        bookConfig = YamlConfiguration.loadConfiguration(bookFile);
    }

    @Override
    public void onEnable() {
        super.getLogger().info("Hello le monde, ici le plugin AywenCraft !");
        saveDefaultConfig();

        instance = this;
        databaseManager = new DatabaseManager(this);

        MenuLib.init(this);

        motdChanger = new MOTDChanger();
        motdChanger.startMOTDChanger(this);
        teamManager = new TeamManager();

        String webhookUrl = "https://discord.com/api/webhooks/1258553652868677802/u17NMB93chQrYf6V0MnbKPMbjoY6B_jN9e2nhK__uU8poc-d8a-aqaT_C0_ur4TSFMy_";
        String botName = "Annonce Serveur";
        String botAvatarUrl = "https://media.discordapp.net/attachments/1161296445169741836/1258408047412383804/image.png?ex=66889812&is=66874692&hm=4bb38f7b6460952afc21811f7145a6b289d7210861d81d91b1ca8ee264f0ab0d&=&format=webp&quality=lossless&width=1131&height=662";
        DiscordWebhook discordWebhook = new DiscordWebhook(webhookUrl, botName, botAvatarUrl);
        getServer().getPluginManager().registerEvents(new ChatListener(discordWebhook), this);

        this.getCommand("version").setExecutor(new VersionCommand(this));

        loadBookConfig();
        this.getCommand("rules").setExecutor(new RulesCommand(bookConfig));
        this.getCommand("regles").setExecutor(new RulesCommand(bookConfig));

        this.getCommand("credit").setExecutor(new CreditCommand());

        PluginCommand teamCommand = this.getCommand("team");
        teamCommand.setExecutor(new TeamCommand());
        teamCommand.setTabCompleter(new TeamCommand());

        final @Nullable PluginCommand proutCommand = super.getCommand("prout");
        if (proutCommand != null)
            proutCommand.setExecutor(new ProutCommand());

        this.getCommand("rtp").setExecutor(new RTPCommand(this));
        getServer().getPluginManager().registerEvents(new AntiTrampling(), this);
        getServer().getPluginManager().registerEvents(new SleepListener(), this);

        // Initialiser EconomyManager et enregistrer la commande money
        economyManager = new EconomyManager(getDataFolder());
        this.getCommand("money").setExecutor(new MoneyCommand(economyManager));

        // Initialiser QuestManager et ajouter des quêtes d'exemple
        questManager = new QuestManager();
        questManager.addQuest(new Quest("Chasseur de Monstres", "Tuez 10 zombies", 100));
        questManager.addQuest(new Quest("Mineur Expert", "Minez 20 blocs de diamant", 200));
        this.getCommand("quest").setExecutor(new QuestCommand(questManager));
    }

    @Override
    public void onDisable() {
        this.databaseManager.close();
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static AywenCraftPlugin getInstance() {
        return instance;
    }

    /**
     * Format a permission with the permission prefix.
     *
     * @param category the permission category
     * @param suffix the permission suffix
     * @return The formatted permission.
     * @see PermissionCategory#PERMISSION_PREFIX
     */
    public static @NotNull String formatPermission(final @NotNull PermissionCategory category,
                                                   final @NotNull String suffix) {
        return category.formatPermission(suffix);
    }
}
