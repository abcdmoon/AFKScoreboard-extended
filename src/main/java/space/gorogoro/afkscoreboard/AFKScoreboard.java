package space.gorogoro.afkscoreboard;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import space.gorogoro.afkscoreboard.command.CommandManager;
import space.gorogoro.afkscoreboard.cosmetic.CosmeticService;
import space.gorogoro.afkscoreboard.data.ConfigManager;
import space.gorogoro.afkscoreboard.data.PlayerDataManager;
import space.gorogoro.afkscoreboard.data.WeeklyStore;
import space.gorogoro.afkscoreboard.prefix.PrefixConfigManager;
import space.gorogoro.afkscoreboard.prefix.PrefixManager;
import space.gorogoro.afkscoreboard.prefix.PrefixRegistry;

import java.util.ArrayList;
import java.util.List;

public class AFKScoreboard extends JavaPlugin {

    // 今週の累計秒数（data.yml）。ボードに出すのは、今ゾーンにいる人だけ
    private WeeklyStore weeklyStore;

    // ゾーン内だけの見た目。停止時に乗客を消す
    private CosmeticService cosmetics;

    private static AFKScoreboard instance;

    private ConfigManager configManager;
    private EventManager eventManager;
    private ZoneManager zoneManager;
    private MessageManager messageManager;
    private RankingManager rankingManager;
    private ScoreManager scoreManager;
    private PlayerDataManager playerDataManager;
    private GameScoreBoardManager gameScoreBoardManager;
    private PrefixRegistry prefixRegistry;
    private PrefixManager prefixManager;
    private PrefixConfigManager prefixConfigManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        zoneManager = new ZoneManager(this);
        playerDataManager = new PlayerDataManager(this,configManager);

        this.weeklyStore = new WeeklyStore(this,zoneManager);
        // 見た目は別タスク。パーティクルは既定 3 秒、追従チェックは 1 秒。乗客なので座標の毎 tick 更新はしない
        this.cosmetics = new CosmeticService(this,zoneManager,playerDataManager);
        long particleInterval = getConfig().getLong("particle-interval-ticks");
        if (particleInterval < 20L) {
            particleInterval = 60L;
        }

        prefixConfigManager = new PrefixConfigManager(this);
        messageManager = new MessageManager(playerDataManager);
        prefixRegistry = new PrefixRegistry(prefixConfigManager);
        gameScoreBoardManager = new GameScoreBoardManager(prefixRegistry);
        prefixManager = new PrefixManager(gameScoreBoardManager, prefixRegistry,configManager, playerDataManager);
        scoreManager = new ScoreManager(playerDataManager,prefixManager,zoneManager);
        rankingManager = new RankingManager(configManager,scoreManager, playerDataManager,gameScoreBoardManager,zoneManager,weeklyStore);
        eventManager = new EventManager(zoneManager, messageManager, rankingManager, scoreManager,playerDataManager,prefixManager,cosmetics);
        zoneManager.reloadAxAFKZones(eventManager,prefixRegistry);

        getServer().getPluginManager().registerEvents(eventManager, this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, registrarEvent->{
            CommandManager.registerCommands(registrarEvent.registrar(),rankingManager,zoneManager, playerDataManager,gameScoreBoardManager,prefixManager,prefixRegistry,eventManager,cosmetics);
        });

        // スコアボードの更新頻度（5秒ごと = 100ティックス）
        Bukkit.getScheduler().runTaskTimer(this, this::updatePerFiveSeconds, 0L, 100L);

        // 滞在時間のカウントタスク（1秒ごと = 20ティックス）
        Bukkit.getScheduler().runTaskTimer(this, this::update, 0L, 20L);

        // 週間累計の保存（60秒ごと）。書き込み自体は専用スレッド
        Bukkit.getScheduler().runTaskTimer(this, this::saveData, 1200L, 1200L);

        Bukkit.getScheduler().runTaskTimer(this, this::tick, particleInterval, particleInterval);
        // 座っている間は PlayerMoveEvent が来ないので、3 tick ごとに足元ブロックの高さと頭上の MOB の向きを合わせる（向きを送る間隔と同じ）
        Bukkit.getScheduler().runTaskTimer(this, this.cosmetics::tickSeated, 3L, 3L);

        getServer().getPluginManager().registerEvents(this.cosmetics, this);
    }

    private void tick(){
        cosmetics.tickParticles();
    }

    private void update(){
        eventManager.checkPlayerZone();
        scoreManager.incrementTimeEverySecond();

        cosmetics.maintain();
        weeklyStore.update();
    }

    private void updatePerFiveSeconds(){
        rankingManager.updateLeaderboard();
    }

    private void saveData(){
        weeklyStore.requestSave();
        cosmetics.requestSave();
        playerDataManager.savePlayerData();
    }

    private final List<Runnable> onDisableTasks = new ArrayList<>();
    /**
     * プラグインの機能終了時に実行するタスクを追加します
     * @param runnable 呼び出されるタスク
     */
    public static void addOnDisableTask(Runnable runnable) {
        instance.onDisableTasks.add(runnable);
    }

    @Override
    public void onDisable() {
        for(Runnable runnable : onDisableTasks) {
            try{
                runnable.run();
            }catch(Exception e){
                warn(e.getMessage());
            }
        }
        onDisableTasks.clear();

        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        getLogger().info("The Plugin Has Been Disabled!");
    }

    public static void registerTaskLater(Runnable runnable, long delay) {
        if(instance==null){return;}
        Bukkit.getScheduler().runTaskLater(instance,runnable,delay);
    }

    public static void warn(String message){
        if(instance==null){return;}
        instance.getLogger().warning(message);
    }

    public static void runTask(Runnable runnable){
        if(instance==null){return;}
        Bukkit.getScheduler().runTask(instance,runnable);
    }

}
