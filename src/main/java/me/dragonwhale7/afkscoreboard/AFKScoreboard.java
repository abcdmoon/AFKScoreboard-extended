package me.dragonwhale7.afkscoreboard;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.dragonwhale7.afkscoreboard.command.CommandManager;
import me.dragonwhale7.afkscoreboard.event.EventManager;
import me.dragonwhale7.afkscoreboard.prefix.PrefixManager;
import me.dragonwhale7.afkscoreboard.prefix.PrefixRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

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
    private PlayerDataManager playerDataManager;
    private GameScoreBoardManager gameScoreBoardManager;
    private PrefixRegistry prefixRegistry;
    private PrefixManager prefixManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        zoneManager = new ZoneManager(this);
        playerDataManager = new PlayerDataManager(this);
        messageManager = new MessageManager(configManager,playerDataManager);
        prefixRegistry = new PrefixRegistry(configManager);
        gameScoreBoardManager = new GameScoreBoardManager(prefixRegistry);
        prefixManager = new PrefixManager(gameScoreBoardManager, prefixRegistry, playerDataManager);
        scoreManager = new ScoreManager(playerDataManager,prefixManager,zoneManager);
        rankingManager = new RankingManager(configManager,scoreManager, playerDataManager,gameScoreBoardManager);
        eventManager = new EventManager(zoneManager, messageManager, rankingManager, scoreManager,playerDataManager,prefixManager);
        zoneManager.reloadAxAFKZones(eventManager);

        getServer().getPluginManager().registerEvents(eventManager, this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, registrarEvent->{
            CommandManager.registerCommands(registrarEvent.registrar(),rankingManager,zoneManager, playerDataManager,gameScoreBoardManager,prefixManager,prefixRegistry,eventManager);
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
            try{
                runnable.run();
            }catch(Exception e){
                warn(e.getMessage());
            }
        }
        onDisableTasks.clear();

        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

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

    public static void warn(String message){
        if(instance==null){return;}
        instance.getLogger().warning(message);
    }

    public static void runTask(Runnable runnable){
        if(instance==null){return;}
        Bukkit.getScheduler().runTask(instance,runnable);
    }

}
