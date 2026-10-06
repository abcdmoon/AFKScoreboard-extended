package space.gorogoro.afkscoreboard;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import space.gorogoro.afkscoreboard.command.CommandManager;
import space.gorogoro.afkscoreboard.cosmetic.CosmeticService;
import space.gorogoro.afkscoreboard.cosmetic.CosmeticStore;
import space.gorogoro.afkscoreboard.prefix.PrefixConfigManager;
import space.gorogoro.afkscoreboard.prefix.PrefixManager;
import space.gorogoro.afkscoreboard.prefix.PrefixRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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

        this.weeklyStore = new WeeklyStore(this,zoneManager);
        this.weeklyStore.load();
        // 見た目は別タスク。パーティクルは既定 3 秒、追従チェックは 1 秒。乗客なので座標の毎 tick 更新はしない
        this.cosmetics = new CosmeticService(this,zoneManager,playerDataManager);
        long particleInterval = getConfig().getLong("particle-interval-ticks");
        if (particleInterval < 20L) {
            particleInterval = 60L;
        }

        playerDataManager = new PlayerDataManager(this,configManager);
        prefixConfigManager = new PrefixConfigManager(this);
        messageManager = new MessageManager(configManager,playerDataManager);
        prefixRegistry = new PrefixRegistry(prefixConfigManager);
        gameScoreBoardManager = new GameScoreBoardManager(prefixRegistry);
        prefixManager = new PrefixManager(gameScoreBoardManager, prefixRegistry,configManager, playerDataManager);
        scoreManager = new ScoreManager(playerDataManager,prefixManager,zoneManager);
        rankingManager = new RankingManager(configManager,scoreManager, playerDataManager,gameScoreBoardManager,zoneManager,weeklyStore);
        eventManager = new EventManager(zoneManager, messageManager, rankingManager, scoreManager,playerDataManager,prefixManager);
        zoneManager.reloadAxAFKZones(eventManager,prefixRegistry);

        getServer().getPluginManager().registerEvents(eventManager, this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, registrarEvent->{
            CommandManager.registerCommands(registrarEvent.registrar(),rankingManager,zoneManager, playerDataManager,gameScoreBoardManager,prefixManager,prefixRegistry,eventManager);
        });

        // スコアボードの更新頻度（5秒ごと = 100ティックス）
        Bukkit.getScheduler().runTaskTimer(this, this::updateFive, 0L, 100L);

        // 滞在時間のカウントタスク（1秒ごと = 20ティックス）
        Bukkit.getScheduler().runTaskTimer(this, this::update, 0L, 20L);

        // 週間累計の保存（60秒ごと）。書き込み自体は専用スレッド
        Bukkit.getScheduler().runTaskTimer(this, this::saveData, 1200L, 1200L);

        Bukkit.getScheduler().runTaskTimer(this, this::tick, particleInterval, particleInterval);
        // 座っている間は PlayerMoveEvent が来ないので、3 tick ごとに足元ブロックの高さと頭上の MOB の向きを合わせる（向きを送る間隔と同じ）
        Bukkit.getScheduler().runTaskTimer(this, this.cosmetics::tickSeated, 3L, 3L);

        PluginCommand debugCommand = getCommand("afkdebug");
        if (debugCommand != null) {
            debugCommand.setTabCompleter(this);
        }
        PluginCommand lookCommand = getCommand("afklook");
        if (lookCommand != null) {
            lookCommand.setTabCompleter(this);
        }

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

    private void updateFive(){
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

    /**
     * コマンドの処理ルーチン
     * /afkhide コマンドでランキングの表示/非表示を切り替えます
     */
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ実行できます。");
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (command.getName().equalsIgnoreCase("afkhide")) {
            // 見た目ボーナスは非表示中は付けない。切り替えたらその場で合わせる
            if (cosmetics != null) {
                cosmetics.refresh(player);
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("afkdebug")) {
            return handleDebugCommand(player, args);
        }
        if (command.getName().equalsIgnoreCase("afklook")) {
            return handleLookCommand(player, args);
        }
        return false;
    }

    /**
     * 見た目ボーナスを待たずに付与する。OP のみ。放置秒数は変えない。
     */
    private boolean handleDebugCommand(Player player, String[] args) {
        if (cosmetics == null) {
            player.sendMessage("§c見た目ボーナスはまだ準備できていません。");
            return true;
        }
        if (args.length != 1) {
            sendDebugUsage(player);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "particle", "30m", "30" -> sendDebugGrant(player, cosmetics.debugGrant(player, true, false, false));
            case "block", "1h", "60" -> sendDebugGrant(player, cosmetics.debugGrant(player, false, true, false));
            case "mount", "3h", "180" -> sendDebugGrant(player, cosmetics.debugGrant(player, false, false, true));
            case "all" -> sendDebugGrant(player, cosmetics.debugGrant(player, true, true, true));
            default -> sendDebugUsage(player);
        }
        return true;
    }

    private void sendDebugGrant(Player player, CosmeticService.DebugGrant grant) {
        sendDebugLine(player, "30分（パーティクル）", grant.particle, grant.particleNew);
        sendDebugLine(player, "1時間（ブロック）", grant.block, grant.blockNew);
        sendDebugLine(player, "3時間（頭MOB）", grant.mount, grant.mountNew);
        if (grant.hidden) {
            player.sendMessage("§7/afkhide で非表示中なので、見た目は表示しません。");
        } else if (grant.concealed) {
            player.sendMessage("§7スペクテイター・バニッシュ中なので、見た目は表示しません。");
        } else if (grant.inZone) {
            player.sendMessage("§7ゾーン内なので、この場に表示しました。");
        } else {
            player.sendMessage("§7今はゾーン外です。放置ゾーンに入ると表示されます。");
        }
    }

    private void sendDebugLine(Player player, String label, String kind, boolean fresh) {
        if (kind == null) {
            return;
        }
        if (fresh) {
            player.sendMessage("§f" + label + "を付与しました: §a" + kind);
        } else {
            player.sendMessage("§f" + label + "は付与済みです: §a" + kind);
        }
    }

    private void sendDebugUsage(Player player) {
        player.sendMessage("§f/afkdebug <particle|block|mount|all>");
        player.sendMessage("§7particle §f30分のパーティクル  §7block §f1時間のブロック  §7mount §f3時間の頭MOB  §7all §f3つまとめて");
        player.sendMessage("§7外すときは /afklook reset");
    }

    /**
     * /afklook。見た目の種類ごとに表示と非表示を切り替える。誰でも使える。設定は週をまたいで残る
     * reset は見た目をすべて外す(秒数は残すので、条件を満たしている見た目はすぐに引き直される)
     */
    private boolean handleLookCommand(Player player, String[] args) {
        if (cosmetics == null) {
            player.sendMessage("§c見た目ボーナスはまだ準備できていません。");
            return true;
        }
        if (args.length != 1) {
            sendLookUsage(player);
            return true;
        }
        List<CosmeticStore.Slot> slots;
        String label;
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "particle" -> {
                slots = List.of(CosmeticStore.Slot.PARTICLE);
                label = "パーティクル";
            }
            case "block" -> {
                slots = List.of(CosmeticStore.Slot.BLOCK);
                label = "ブロック";
            }
            case "mount" -> {
                slots = List.of(CosmeticStore.Slot.MOUNT);
                label = "頭MOB";
            }
            case "all" -> {
                slots = List.of(CosmeticStore.Slot.PARTICLE, CosmeticStore.Slot.BLOCK, CosmeticStore.Slot.MOUNT);
                label = "すべての見た目";
            }
            case "reset" -> {
                if (cosmetics.debugClear(player)) {
                    player.sendMessage("§f見た目を外しました。§7放置の秒数はそのままなので、条件を満たしている見た目はすぐに引き直されます。");
                } else {
                    player.sendMessage("§7付与されている見た目はありません。");
                }
                return true;
            }
            default -> {
                sendLookUsage(player);
                return true;
            }
        }
        if (cosmetics.toggleLook(player, slots)) {
            player.sendMessage("§f" + label + "を§a非表示§fにしました。§7もう一度実行すると表示に戻ります。");
        } else {
            player.sendMessage("§f" + label + "を§a表示§fするようにしました。");
        }
        return true;
    }

    private void sendLookUsage(Player player) {
        player.sendMessage("§f/afklook <particle|block|mount|all|reset>");
        player.sendMessage("§7見た目を種類ごとに表示/非表示にします。§7particle §fパーティクル  §7block §fブロック  §7mount §f頭MOB  §7all §fすべて");
        player.sendMessage("§7reset §f見た目をすべて外す（条件を満たしている見た目はすぐに引き直されます）");
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String @NonNull [] args) {
        if (args.length != 1) {
            return List.of();
        }
        List<String> options;
        if (command.getName().equalsIgnoreCase("afklook")) {
            options = List.of("particle", "block", "mount", "all", "reset");
        } else if (command.getName().equalsIgnoreCase("afkdebug")) {
            options = List.of("particle", "block", "mount", "all");
        } else {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(prefix)) {
                matches.add(option);
            }
        }
        return matches;
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
