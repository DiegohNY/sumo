package dev.diegoh.sumo;

import dev.diegoh.sumo.arena.ArenaRepository;
import dev.diegoh.sumo.arena.ArenaService;
import dev.diegoh.sumo.command.SumoCommand;
import dev.diegoh.sumo.command.sub.JoinSub;
import dev.diegoh.sumo.command.sub.LeaveSub;
import dev.diegoh.sumo.command.sub.ListSub;
import dev.diegoh.sumo.command.sub.MenuSub;
import dev.diegoh.sumo.command.sub.ReloadSub;
import dev.diegoh.sumo.command.sub.StatsSub;
import dev.diegoh.sumo.command.sub.admin.ArenaCreateSub;
import dev.diegoh.sumo.command.sub.admin.ArenaDeleteSub;
import dev.diegoh.sumo.command.sub.admin.ArenaSetBoundsSub;
import dev.diegoh.sumo.command.sub.admin.ArenaSetKnockbackSub;
import dev.diegoh.sumo.command.sub.admin.ArenaSetLobbySub;
import dev.diegoh.sumo.command.sub.admin.ArenaSetPlayersSub;
import dev.diegoh.sumo.command.sub.admin.ArenaSetSpawnSub;
import dev.diegoh.sumo.command.sub.admin.ForceStartSub;
import dev.diegoh.sumo.command.sub.admin.ForceStopSub;
import dev.diegoh.sumo.config.ConfigLoader;
import dev.diegoh.sumo.config.PluginConfig;
import dev.diegoh.sumo.game.GameOrchestrator;
import dev.diegoh.sumo.gui.ArenaSelectorGui;
import dev.diegoh.sumo.gui.MenuListener;
import dev.diegoh.sumo.i18n.LocaleResolver;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.listener.BoundsListener;
import dev.diegoh.sumo.listener.CombatListener;
import dev.diegoh.sumo.listener.ConnectionListener;
import dev.diegoh.sumo.listener.ProtectionListener;
import dev.diegoh.sumo.player.InventoryStore;
import dev.diegoh.sumo.player.SessionRegistry;
import dev.diegoh.sumo.queue.QueueService;
import dev.diegoh.sumo.stats.SqlStatsRepository;
import dev.diegoh.sumo.stats.StatsRecorder;
import dev.diegoh.sumo.stats.StatsRepository;
import dev.diegoh.sumo.stats.StatsService;
import dev.diegoh.sumo.ui.SessionUiPresenter;
import dev.diegoh.sumo.util.AdventureUtil;
import java.nio.file.Path;
import java.util.Locale;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

public class SumoPlugin extends JavaPlugin {
  // bStats service id (https://bstats.org/plugin/bukkit/Sumo/31843). 0 disables metrics.
  private static final int BSTATS_PLUGIN_ID = 31843;

  private AdventureUtil adventure;
  private StatsRepository statsRepository;
  private GameOrchestrator orchestrator;

  @Override
  public void onEnable() {
    saveDefaultConfig();
    PluginConfig pluginConfig = ConfigLoader.load(getConfig());

    adventure = new AdventureUtil(this);
    Messages messages = new Messages(this, pluginConfig.defaultLocale());
    LocaleResolver localeResolver =
        new LocaleResolver(pluginConfig.defaultLocale(), pluginConfig.followPlayerLocale());

    statsRepository = createStatsRepository(pluginConfig);
    statsRepository.init();
    StatsService statsService = new StatsService(statsRepository);

    Path arenasDir = getDataFolder().toPath().resolve("arenas");
    ArenaRepository arenaRepository = new ArenaRepository(arenasDir, getServer());
    ArenaService arenaService = new ArenaService(arenaRepository);

    InventoryStore inventoryStore = new InventoryStore();
    SessionRegistry registry = new SessionRegistry();
    orchestrator =
        new GameOrchestrator(
            this,
            inventoryStore,
            registry,
            pluginConfig.matchCountdownSeconds(),
            pluginConfig.endDelaySeconds());

    orchestrator.configureAutoStart(
        pluginConfig.autoStartEnabled(),
        pluginConfig.joinPeriodSeconds(),
        pluginConfig.autoStartWhenFull());

    Locale defaultLocale = parseLocale(pluginConfig.defaultLocale());
    StatsRecorder statsRecorder = new StatsRecorder(statsService, getLogger());
    orchestrator.setOnSessionCreated(
        session -> {
          SessionUiPresenter presenter =
              new SessionUiPresenter(
                  session, messages, defaultLocale, adventure, pluginConfig.scoreboardEnabled());
          presenter.attach();
          statsRecorder.attach(session);
        });

    // QueueService initialized but not yet wired into command flow (planned for future iteration).
    new QueueService();

    ArenaSelectorGui gui = new ArenaSelectorGui(arenaService, orchestrator);

    SumoCommand root =
        new SumoCommand(messages, localeResolver, adventure)
            .register(new JoinSub(arenaService, orchestrator, messages, localeResolver, adventure))
            .register(new LeaveSub(orchestrator, messages, localeResolver, adventure))
            .register(new ListSub(arenaService, orchestrator, messages, localeResolver, adventure))
            .register(new StatsSub(statsService, messages, localeResolver, adventure))
            .register(
                new MenuSub(gui, pluginConfig.guiEnabled(), messages, localeResolver, adventure))
            .register(new ReloadSub(messages, arenaRepository, this, localeResolver, adventure))
            .register(
                new ArenaCreateSub(arenaService, pluginConfig, messages, localeResolver, adventure))
            .register(new ArenaDeleteSub(arenaService, messages, localeResolver, adventure))
            .register(new ArenaSetSpawnSub(arenaService, messages, localeResolver, adventure))
            .register(new ArenaSetLobbySub(arenaService, messages, localeResolver, adventure))
            .register(new ArenaSetBoundsSub(arenaService, messages, localeResolver, adventure))
            .register(new ArenaSetKnockbackSub(arenaService, messages, localeResolver, adventure))
            .register(new ArenaSetPlayersSub(arenaService, messages, localeResolver, adventure))
            .register(
                new ForceStartSub(orchestrator, arenaService, messages, localeResolver, adventure))
            .register(
                new ForceStopSub(orchestrator, arenaService, messages, localeResolver, adventure));
    getCommand("sumo").setExecutor(root);
    getCommand("sumo").setTabCompleter(root);

    getServer()
        .getPluginManager()
        .registerEvents(new ConnectionListener(orchestrator, inventoryStore), this);
    getServer().getPluginManager().registerEvents(new ProtectionListener(orchestrator), this);
    getServer().getPluginManager().registerEvents(new BoundsListener(orchestrator), this);
    getServer().getPluginManager().registerEvents(new CombatListener(orchestrator), this);
    if (pluginConfig.guiEnabled()) {
      getServer()
          .getPluginManager()
          .registerEvents(new MenuListener(gui, arenaService, orchestrator), this);
    }

    initMetrics(pluginConfig, arenaService);

    adventure.audiences().console().sendMessage(messages.get(Locale.US, MessageKey.PLUGIN_ENABLED));
  }

  private void initMetrics(PluginConfig config, ArenaService arenaService) {
    if (BSTATS_PLUGIN_ID <= 0) return;
    try {
      Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
      metrics.addCustomChart(
          new SimplePie("database", () -> config.databaseDriver().name().toLowerCase(Locale.ROOT)));
      metrics.addCustomChart(
          new SimplePie("scoreboard", () -> String.valueOf(config.scoreboardEnabled())));
      metrics.addCustomChart(new SingleLineChart("arenas", () -> arenaService.all().size()));
    } catch (Throwable t) {
      getLogger().warning("bStats metrics could not start: " + t.getMessage());
    }
  }

  @Override
  public void onDisable() {
    if (orchestrator != null) orchestrator.shutdownAll();
    if (statsRepository != null) statsRepository.close();
    if (adventure != null) adventure.close();
  }

  private static Locale parseLocale(String code) {
    String[] parts = code.split("_");
    return parts.length >= 2 ? new Locale(parts[0], parts[1]) : new Locale(parts[0]);
  }

  private StatsRepository createStatsRepository(PluginConfig config) {
    return switch (config.databaseDriver()) {
      case SQLITE ->
          SqlStatsRepository.sqlite(
              "jdbc:sqlite:" + getDataFolder().toPath().resolve(config.sqliteFile()));
      case MYSQL ->
          SqlStatsRepository.mysql(
              "jdbc:mariadb://"
                  + config.mysqlHost()
                  + ":"
                  + config.mysqlPort()
                  + "/"
                  + config.mysqlDatabase()
                  + "?useSsl="
                  + config.mysqlUseSsl(),
              config.mysqlUsername(),
              config.mysqlPassword(),
              config.poolMaxSize(),
              config.poolMinIdle(),
              config.poolConnectionTimeoutMs());
    };
  }
}
