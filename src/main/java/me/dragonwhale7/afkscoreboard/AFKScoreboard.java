package me.dragonwhale7.afkscoreboard;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.dragonwhale7.afkscoreboard.prefix.GameScoreBoardManager;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import me.dragonwhale7.afkscoreboard.command.CommandManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AFKScoreboard extends JavaPlugin {

    private static AFKScoreboard instance;

    private ConfigManager configManager;
    private EventManager eventManager;
    private ZoneManager zoneManager;
    private MessageManager messageManager;
    private RankingManager rankingManager;
    private ScoreManager scoreManager;
    private HighScoreManager highScoreManager;
    private GameScoreBoardManager gameScoreBoardManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        zoneManager = new ZoneManager(this);
        gameScoreBoardManager = new GameScoreBoardManager();
        messageManager = new MessageManager(configManager);
        highScoreManager = new HighScoreManager();
        scoreManager = new ScoreManager(highScoreManager);
        rankingManager = new RankingManager(configManager,scoreManager,highScoreManager,gameScoreBoardManager);
        eventManager = new EventManager(zoneManager, messageManager, rankingManager, scoreManager);

        getServer().getPluginManager().registerEvents(eventManager, this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, registrarEvent->{
            CommandManager.registerCommands(registrarEvent.registrar(),rankingManager,zoneManager,highScoreManager,gameScoreBoardManager);
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

    public static void registerTaskLater(Runnable runnable, long delay) {
        if(instance==null){return;}
        Bukkit.getScheduler().runTaskLater(instance,runnable,delay);
    }

}
