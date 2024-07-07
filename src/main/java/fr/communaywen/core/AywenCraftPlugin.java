package fr.communaywen.core;

import dev.xernas.menulib.MenuLib;
import fr.communaywen.core.commands.*;
import fr.communaywen.core.economy.EconomyManager;
import fr.communaywen.core.listeners.*;
import fr.communaywen.core.teams.TeamManager;
import fr.communaywen.core.utils.*;
import fr.communaywen.core.tpa.*;
import fr.communaywen.core.utils.database.DatabaseManager;
import fr.communaywen.core.staff.freeze.FreezeCommand;
import fr.communaywen.core.staff.freeze.FreezeListener;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AywenCraftPlugin extends JavaPlugin {
    private final Set<UUID> frozenPlayers = new HashSet<>();

    private MOTDChanger motdChanger;
    private TeamManager teamManager;
    private FileConfiguration bookConfig;
    private static AywenCraftPlugin instance;
    public EconomyManager economyManager;
    public LuckPerms api;

    private DatabaseManager databaseManager;
    public QuizManager quizManager;

    private void loadBookConfig() {
        File bookFile = new File(getDataFolder(), "rules.yml");
        if (!bookFile.exists()) {
            saveResource("rules.yml", false);
        }
        bookConfig = YamlConfiguration.loadConfiguration(bookFile);
    }

    private FileConfiguration loadWelcomeMessageConfig() {
        File welcomeMessageConfigFile = new File(getDataFolder(), "welcomeMessageConfig.yml");
        if (!welcomeMessageConfigFile.exists()) {
            saveResource("welcomeMessageConfig.yml", false);
        }
        return YamlConfiguration.loadConfiguration(welcomeMessageConfigFile);
    }

    private FileConfiguration loadQuizzes() {
        File quizzesFile = new File(getDataFolder(), "quizzes.yml");
        if (!quizzesFile.exists()) {
            saveResource("quizzes.yml", false);
        }
        return YamlConfiguration.loadConfiguration(quizzesFile);
    }

    @Override
    public void onEnable() {
        super.getLogger().info("Hello le monde, ici le plugin AywenCraft !");
        saveDefaultConfig();

        instance = this;

        /* UTILS */
        databaseManager = new DatabaseManager(this);
        LinkerAPI linkerAPI = new LinkerAPI(databaseManager);

        quizManager = new QuizManager(this, loadQuizzes());

        OnPlayers onPlayers = new OnPlayers();
        onPlayers.setLinkerAPI(linkerAPI);

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            api = provider.getProvider();
            onPlayers.setLuckPerms(api);
        }

        MenuLib.init(this);
        economyManager = new EconomyManager(getDataFolder());
        loadBookConfig();

        motdChanger = new MOTDChanger();
        motdChanger.startMOTDChanger(this);
        teamManager = new TeamManager();
        /* ----- */

        String webhookUrl = "https://discord.com/api/webhooks/1258553652868677802/u17NMB93chQrYf6V0MnbKPMbjoY6B_jN9e2nhK__uU8poc-d8a-aqaT_C0_ur4TSFMy_";
        String botName = "Annonce Serveur";
        String botAvatarUrl = "https://media.discordapp.net/attachments/1161296445169741836/1258408047412383804/image.png?ex=66889812&is=66874692&hm=4bb38f7b6460952afc21811f7145a6b289d7210861d81d91b1ca8ee264f0ab0d&=&format=webp&quality=lossless&width=1131&height=662";
        DiscordWebhook discordWebhook = new DiscordWebhook(webhookUrl, botName, botAvatarUrl);

        /*  COMMANDS  */
        this.getCommand("version").setExecutor(new VersionCommand(this));
        this.getCommand("rules").setExecutor(new RulesCommand(bookConfig));
        this.getCommand("regles").setExecutor(new RulesCommand(bookConfig));
        this.getCommand("link").setExecutor(new LinkCommand(linkerAPI));
        this.getCommand("manuallink").setExecutor(new ManualLinkCommand(linkerAPI));
        this.getCommand("credit").setExecutor(new CreditCommand());
        this.getCommand("rtp").setExecutor(new RTPCommand(this));
        this.getCommand("feed").setExecutor(new FeedCommand(this));
        this.getCommand("money").setExecutor(new MoneyCommand(economyManager));
        this.getCommand("money").setTabCompleter(new MoneyCommand(economyManager));

        this.getCommand("tpa").setExecutor(new CommandTPA());
        this.getCommand("tpaccept").setExecutor(new CommandTpaccept());
        this.getCommand("tpdeny").setExecutor(new CommandTpdeny());
        this.getCommand("tpcancel").setExecutor(new CommandTpcancel());
        this.getCommand("spawn").setExecutor(new CommandSpawn(this));

        this.getCommand("freeze").setExecutor(new FreezeCommand(this));
        this.getCommand("unfreeze").setExecutor(new FreezeCommand(this));

        PluginCommand teamCommand = this.getCommand("team");
        teamCommand.setExecutor(new TeamCommand());
        teamCommand.setTabCompleter(new TeamCommand());

        final @Nullable PluginCommand proutCommand = super.getCommand("prout");
        if (proutCommand != null)
            proutCommand.setExecutor(new ProutCommand());

        /* LISTENERS */
        getServer().getPluginManager().registerEvents(new AntiTrampling(),this);
        getServer().getPluginManager().registerEvents(new RTPWand(this), this);
        getServer().getPluginManager().registerEvents(onPlayers, this);
        getServer().getPluginManager().registerEvents(new SleepListener(),this);
        getServer().getPluginManager().registerEvents(new ChatListener(this, discordWebhook), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(this), this);
        getServer().getPluginManager().registerEvents(new WelcomeMessage(loadWelcomeMessageConfig()), this);
        /* --------- */

        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        this.databaseManager.close();
    }

    public Set<UUID> getFrozenPlayers() {
        return frozenPlayers;
    }

    public int getBanDuration() {
        return getConfig().getInt("deco_freeze_nombre_de_jours_ban", 30);
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
