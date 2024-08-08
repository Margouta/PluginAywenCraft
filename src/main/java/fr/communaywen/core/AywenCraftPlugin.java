package fr.communaywen.core;

import com.onarandombox.MultiverseCore.MultiverseCore;
import dev.xernas.menulib.Menu;
import dev.xernas.menulib.MenuLib;
import fr.communaywen.core.claim.ClaimConfigDataBase;
import fr.communaywen.core.claim.ClaimListener;
import fr.communaywen.core.claim.GamePlayer;
import fr.communaywen.core.claim.RegionManager;
import fr.communaywen.core.clockinfos.tasks.CompassClockTask;
import fr.communaywen.core.commands.credits.CreditCommand;
import fr.communaywen.core.commands.credits.FeatureCommand;
import fr.communaywen.core.commands.economy.AdminShopCommand;
import fr.communaywen.core.commands.economy.BaltopCommand;
import fr.communaywen.core.commands.economy.MoneyCommand;
import fr.communaywen.core.commands.economy.PayCommands;
import fr.communaywen.core.commands.explosion.ExplodeRandomCommand;
import fr.communaywen.core.commands.explosion.FBoomCommand;
import fr.communaywen.core.commands.fun.*;
import fr.communaywen.core.commands.link.LinkCommand;
import fr.communaywen.core.commands.link.ManualLinkCommand;
import fr.communaywen.core.commands.socials.DiscordCommand;
import fr.communaywen.core.commands.socials.GithubCommand;
import fr.communaywen.core.commands.staff.ReportCommands;
import fr.communaywen.core.commands.teams.TeamAdminCommand;
import fr.communaywen.core.commands.teams.TeamCommand;
import fr.communaywen.core.commands.teleport.RTPCommand;
import fr.communaywen.core.commands.teleport.SpawnCommand;
import fr.communaywen.core.commands.utils.*;
import fr.communaywen.core.customitems.commands.ShowCraftCommand;
import fr.communaywen.core.customitems.listeners.CIBreakBlockListener;
import fr.communaywen.core.customitems.listeners.CIEnchantListener;
import fr.communaywen.core.customitems.listeners.CIPrepareAnvilListener;
import fr.communaywen.core.fallblood.BandageRecipe;
import fr.communaywen.core.friends.commands.FriendsCommand;
import fr.communaywen.core.levels.LevelsCommand;
import fr.communaywen.core.levels.LevelsListeners;
import fr.communaywen.core.listeners.*;
import fr.communaywen.core.quests.PlayerQuests;
import fr.communaywen.core.quests.QuestsListener;
import fr.communaywen.core.quests.QuestsManager;
import fr.communaywen.core.quests.qenum.QUESTS;
import fr.communaywen.core.staff.freeze.FreezeCommand;
import fr.communaywen.core.staff.players.PlayersCommand;
import fr.communaywen.core.tab.TabList;
import fr.communaywen.core.tpa.TPACommand;
import fr.communaywen.core.tpa.TpacceptCommand;
import fr.communaywen.core.tpa.TpcancelCommand;
import fr.communaywen.core.tpa.TpdenyCommand;
import fr.communaywen.core.trade.TradeAcceptCommand;
import fr.communaywen.core.trade.TradeCommand;
import fr.communaywen.core.trade.TradeListener;
import fr.communaywen.core.utils.DiscordWebhook;
import fr.communaywen.core.utils.LinkerAPI;
import fr.communaywen.core.utils.MOTDChanger;
import fr.communaywen.core.utils.PermissionCategory;
import fr.communaywen.core.utils.command.InteractiveHelpMenu;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class AywenCraftPlugin extends JavaPlugin {
    public static ArrayList<Player> frozenPlayers = new ArrayList<>();
    public static ArrayList<Player> playerClaimsByPass = new ArrayList<>();

    @Getter
    private final Managers managers = new Managers();

    public EventsManager eventsManager; // TODO: include to Managers.java

    @Getter
    private static AywenCraftPlugin instance;
    public LuckPerms api;

    @Getter
    private BukkitAudiences adventure;
    @Getter
    private InteractiveHelpMenu interactiveHelpMenu;

    @Getter
    private BukkitCommandHandler handler;

    @Getter
    private TabList tabList;

    public List<RegionManager> regions;
    public MultiverseCore mvCore;
    @SneakyThrows
    @Override
    public void onEnable() {
        // Logs
        super.getLogger().info("Hello le monde, ici le plugin AywenCraft !");

        // Gardez les au début sinon ça pète tout
        instance = this;

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            api = provider.getProvider();
        }
        else {
            getLogger().severe("LuckPerms not found !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        MenuLib.init(this);
        managers.initConfig(this);
        managers.init(this);

        eventsManager = new EventsManager(this, loadEventsManager()); // TODO: include to Managers.java

        mvCore = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");

        LinkerAPI linkerAPI = new LinkerAPI(managers.getDatabaseManager());

        OnPlayers onPlayers = new OnPlayers();
        onPlayers.setLinkerAPI(linkerAPI);
        onPlayers.setLuckPerms(api);

        MOTDChanger motdChanger = new MOTDChanger();
        motdChanger.startMOTDChanger(this);

        this.adventure = BukkitAudiences.create(this);

        this.regions = new ArrayList<>();

        this.tabList = new TabList();

        /* ----- */

        String webhookUrl = getConfig().getString("discord.webhookURL");
        String botName = getConfig().getString("discord.webhookName");
        String botAvatarUrl = getConfig().getString("discord.webhookIconURL");
        DiscordWebhook discordWebhook = new DiscordWebhook(webhookUrl, botName, botAvatarUrl);

        /*  COMMANDS  */

        this.handler = BukkitCommandHandler.create(this);
        this.interactiveHelpMenu = InteractiveHelpMenu.create();
        this.handler.accept(interactiveHelpMenu);

        this.handler.getAutoCompleter().registerSuggestion("featureName", SuggestionProvider.of(managers.getWikiConfig().getKeys(false)));

        this.handler.register(
                new TeamAdminCommand(this),
                new SpawnCommand(this),
                new RulesCommand(managers.getBookConfig()),
                new TeamCommand(),
                new MoneyCommand(this),
                new ScoreboardCommand(),
                new ProutCommand(),
                new TPACommand(this),
                new TpacceptCommand(this),  // Pass the plugin instance
                new TpcancelCommand(),
                new TpdenyCommand(),
                new CreditCommand(),
                new ExplodeRandomCommand(),
                new LinkCommand(linkerAPI),
                new ManualLinkCommand(linkerAPI),
                new RTPCommand(this),
                new FreezeCommand(),
                new PlayersCommand(),
                new FBoomCommand(),
                new BaltopCommand(this),
                new FriendsCommand(managers.getFriendsManager(), this, adventure),
                new PrivacyCommand(this),
                new LevelsCommand(managers.getLevelsManager()),
                new TailleCommand(),
                new WikiCommand(managers.getWikiConfig()),
                new GithubCommand(this),
                new TradeCommand(this),
                new TradeAcceptCommand(this),
                new QuestsCommands(),
                new RewardCommand(this),
                new FeatureCommand(managers.getFeatureManager()),
                new MineCommand(),
                new AdminShopCommand(),
                new PayCommands(),
                new FallBloodCommand(),
                new DiscordCommand(this),
                new ShowCraftCommand(managers.getCustomItemsManager()),
                new ReportCommands(),
                new ChatChannelCMD()
        );

        /*  --------  */

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Menu openedMenu = hasMenuOpened(player);
                    if (openedMenu != null) {
                        openedMenu.open();
                    }
                }
            }
        }.runTaskTimer(this, 0L, 100L);

        new CompassClockTask().runTaskTimer(this, 0L, 5L);

        /* LISTENERS */
        registerEvents(
                new NoMoreLapins(),
                new KebabListener(this),
                new AntiTrampling(),
                new RTPWand(this),
                onPlayers,
                new ExplosionListener(),
                new SleepListener(),
                new ChatListener(this, discordWebhook),
                new FreezeListener(this),
                new WelcomeMessage(managers.getWelcomeMessageConfig()),
                new Insomnia(),
                new VpnListener(this),
                new ThorHammer(),
                new FriendsListener(managers.getFriendsManager()),
                new TablistListener(this),
                new LevelsListeners(managers.getLevelsManager()),
                new CorpseListener(managers.getCorpseManager()),
                new TradeListener(),
                new QuestsListener(),
                new PasFraisListener(this),
                new ClaimListener(),
                new FarineListener(),
                new FallBloodListener(),
                new RTPCommand(this),
                new CIBreakBlockListener(managers.getCustomItemsManager()),
                new CIEnchantListener(managers.getCustomItemsManager()),
                new CIPrepareAnvilListener(managers.getCustomItemsManager()),
                new BabyFuzeListener()
        );

        getServer().getPluginManager().registerEvents(eventsManager, this); // TODO: refactor
        
        /* --------- */

        saveDefaultConfig();

        createSandRecipe();
        createFarineRecipe();
        createCrazyPotion();

        for (Player player : Bukkit.getOnlinePlayers()) {
            new GamePlayer(player.getName());
            QuestsManager.loadPlayerData(player);
        }

        QuestsManager.initializeQuestsTable();
        ClaimConfigDataBase.loadAllClaims();
        new BandageRecipe();
    }

    @SneakyThrows
    @Override
    public void onDisable() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            for(QUESTS quests : QUESTS.values()) {
                PlayerQuests pq = QuestsManager.getPlayerQuests(player); // Load quest progress
                QuestsManager.savePlayerQuestProgress(player, quests, pq.getProgress(quests)); // Save quest progress
                player.closeInventory(); // Close inventory
            }
        }
        managers.cleanup();
    }

    public void registerEvents(Listener... args) {
        for (Listener listener : args) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public ArrayList<Player> getFrozenPlayers() {
        return frozenPlayers;
    }

    public int getBanDuration() {
        return getConfig().getInt("deco_freeze_nombre_de_jours_ban", 30);
    }


    // Farine pour fabriquer du pain

    private void createFarineRecipe() {
        ItemStack farine = new ItemStack(Material.SUGAR);
        ItemMeta meta = farine.getItemMeta();
        assert meta != null;
        meta.setDisplayName("Farine");
        farine.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "farine"), farine);
        recipe.shape(" A ", " B ", "   ");
        recipe.setIngredient('A', Material.GUNPOWDER);
        recipe.setIngredient('B', Material.WHEAT);

        Bukkit.addRecipe(recipe);
    }

    private void createCrazyPotion(){
        ItemStack crazyPotion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) crazyPotion.getItemMeta();

        meta.setDisplayName("§k NEW §r §4 Crazy Potion §r §k NEW");
        meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 4800, 9), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.HASTE, 4800, 9), true);

        crazyPotion.setItemMeta(meta);

        NamespacedKey nmKey = new NamespacedKey(this, "crazypotion_craft");
        ShapedRecipe recipe = new ShapedRecipe(nmKey, crazyPotion);

        recipe.shape("BBB", "WGW", "IEI");

        recipe.setIngredient('B', Material.DIAMOND_BLOCK);
        recipe.setIngredient('G', Material.GLASS_BOTTLE);
        recipe.setIngredient('W', Material.WATER_BUCKET);
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('I', Material.IRON_INGOT);

        getServer().addRecipe(recipe);

    }

    private void createSandRecipe() {
        ItemStack sand = new ItemStack(Material.SAND, 4);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "sand_craft"), sand);
        recipe.shape("A");
        recipe.setIngredient('A', Material.SANDSTONE);

        Bukkit.addRecipe(recipe);
    }

    private Menu hasMenuOpened(Player player) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (inv.getHolder() instanceof Menu invMenu) {
            return invMenu;
        }
        return null;
    }

    // TODO: include to Managers.java
    private FileConfiguration loadEventsManager() {
        File eventsFile = new File(getDataFolder(), "events.yml");
        if (!eventsFile.exists()) {
            saveResource("events.yml", false);
        }
        return YamlConfiguration.loadConfiguration(eventsFile);
    }

    /**
     * Format a permission with the permission prefix.
     *
     * @param category the permission category
     * @param suffix   the permission suffix
     * @return The formatted permission.
     * @see PermissionCategory #PERMISSION_PREFIX
     */
    public static @NotNull String formatPermission(final @NotNull PermissionCategory category,
                                                   final @NotNull String suffix) {
        return category.formatPermission(suffix);
    }
}
