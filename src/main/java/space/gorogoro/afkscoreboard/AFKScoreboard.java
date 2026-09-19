package space.gorogoro.afkscoreboard;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import space.gorogoro.afkscoreboard.command.CommandManager;

import java.util.*;
import java.util.function.Consumer;

public class AFKScoreboard extends JavaPlugin {

    private Scoreboard afkScoreboard;
    private Objective afkObjective;

    // 読み込んだ各ゾーンの座標範囲データを保持するマップ
    //private final Map<String, ZoneArea> loadedZones = new HashMap<>();

    // プレイヤーの「現在の連続放置時間（秒）」を保持するマップ
    //private final Map<UUID, Integer> currentSessionTimes = new HashMap<>();

    // ログアウトしたプレイヤーのデータを一時保存するマップ（UUID -> 放置秒数）
    //private final Map<UUID, Integer> disconnectedSessionTimes = new HashMap<>();
    // ログアウトした時刻を保存するマップ（UUID -> エポックミリ秒）
    //private final Map<UUID, Long> disconnectTimes = new HashMap<>();

    // ランキングから自分を非表示にしているプレイヤーのUUIDを保持するセット
    //private final Set<UUID> hiddenPlayers = new HashSet<>();

    // 過去に一度でも放置ゾーンに入ったことがあるプレイヤーを記憶するセット
    //private final Set<UUID> welcomedPlayers = new HashSet<>();




    private static AFKScoreboard instance;

    private ConfigManager configManager;
    private EventManager eventManager;
    private ZoneManager zoneManager;
    private MessageManager messageManager;
    private RankingManager rankingManager;
    private ScoreManager scoreManager;




    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        zoneManager = new ZoneManager(this);
        messageManager = new MessageManager(configManager);
        scoreManager = new ScoreManager();
        rankingManager = new RankingManager(configManager,scoreManager);
        eventManager = new EventManager(zoneManager, messageManager, rankingManager, scoreManager);


        getServer().getPluginManager().registerEvents(eventManager, this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, registrarEvent->{
            CommandManager.registerCommands(registrarEvent.registrar(),rankingManager);
        });
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
            runnable.run();
        }
        onDisableTasks.clear();

        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

    }

    /**
     * プラグイン名義で非同期でタスクを実行します
     */
    public static void runTaskAsynchronously(Consumer<BukkitTask> bukkitTaskConsumer){
        if(instance==null){return;}
        Bukkit.getScheduler().runTaskAsynchronously(instance,bukkitTaskConsumer);
    }

    /**
     * プラグイン名義でタスクを定期実行します
     */
    public static void registerTaskTimer(Runnable runnable, long delay, long period) {
        if(instance==null){return;}
        Bukkit.getScheduler().runTaskTimer(instance,runnable,delay,period);
    }

}
