package space.gorogoro.afkscoreboard.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.gorogoro.afkscoreboard.AFKScoreboard;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerDataManager {


    private final Map<UUID, PlayerData> playerData = new HashMap<>();
    private final ConfigManager configManager;
    //元のファイルの読み込みに異常があった場合立つフラグ メモリ上では初期値から扱い、ファイルには書き込まない
    private boolean configError = false;
    //データが読み込めなかったUUID メモリ上では初期値から扱い、ファイルには書き込まない
    private final Set<UUID> errorUUIDs = new HashSet<>();
    private final File file;
    private final AFKScoreboard plugin;
    private final ExecutorService saveExecutor =  Executors.newSingleThreadExecutor();
    private final AtomicReference<String> pendingYml = new AtomicReference<>();
    private YamlConfiguration config;
    private boolean isDirty = false;

    public PlayerDataManager(AFKScoreboard plugin,ConfigManager configManager) {
        this.plugin = plugin;

        this.configManager = configManager;

        file = new File(plugin.getDataFolder(), "playerdata.yml");
        if ((!file.exists())||!file.isFile()) {
            plugin.saveResource("playerdata.yml", false);
        }

        reloadPlayerData();
        AFKScoreboard.addOnDisableTask(this::onDisable);
    }
    private void onDisable(){
        savePlayerData();
        saveExecutor.shutdown();
        try {
            if (!saveExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                AFKScoreboard.warn("playerdata.ymlの書き込みが時間内に終わりませんでした");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void reloadPlayerData() {
        config = new YamlConfiguration();
        try{
            config.load(file);
            configError = false;
        } catch (Exception e) {
            AFKScoreboard.warn("playerdata.ymlの読み込みに失敗しました");
            configError = true;
        }
        playerData.clear();
        errorUUIDs.clear();

        for (String key : config.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                AFKScoreboard.warn("無効なUUIDのデータをスキップしました:" + key);
                continue;
            }
            loadPlayerData(uuid);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            if (!playerData.containsKey(uuid)) {
                loadPlayerData(uuid);
            }
        }
    }

    private void loadPlayerData(UUID uuid) {
        ConfigurationSection section = config.getConfigurationSection(uuid.toString());

        Set<UUID> hiddenPlayers = configManager.loadHiddenPlayers();
        Set<UUID> welcomedPlayers = configManager.loadWelcomedPlayers();

        if(section == null) {
            PlayerData data = PlayerData.getDefault(uuid);
            config.set(uuid.toString(), data);
            playerData.put(uuid, data);
        }else{
            try{
                boolean isWelcomed = welcomedPlayers.contains(uuid);
                boolean isHiddenInRank = hiddenPlayers.contains(uuid);
                boolean isHidingPrefix = section.getBoolean("isHidingPrefix");

                ConfigurationSection highScoresSection = section.getConfigurationSection("highScores");
                Map<String, Integer> highScores = new HashMap<>();
                if(highScoresSection != null) {
                    highScoresSection.getKeys(false).forEach(key -> {
                        highScores.put(key, highScoresSection.getInt(key));
                    });
                }

                String showedPrefix = section.getString("showedPrefix");
                if(showedPrefix == null){showedPrefix = "";}

                Set<String> prefixes = new HashSet<>(section.getStringList("prefixes"));
                playerData.put(uuid,new PlayerData(uuid,isWelcomed,isHiddenInRank,isHidingPrefix,highScores,showedPrefix,prefixes));
            }catch(Exception e){
                String name = Bukkit.getOfflinePlayer(uuid).getName();
                if(name==null){
                    AFKScoreboard.warn(uuid+" からプレイヤーの名前が取得できませんでした: "+e.getMessage());
                }else{
                    AFKScoreboard.warn(name+"("+uuid+")のデータの読み込みに失敗しました: "+e.getMessage());
                }
                playerData.put(uuid,PlayerData.getDefault(uuid));
                errorUUIDs.add(uuid);
            }
        }
    }

    public void savePlayerData() {
        if(configError){return;}
        if(!isDirty){return;}
        isDirty = false;
        
        String ymlText = config.saveToString();
        if(pendingYml.getAndSet(ymlText) != null){
            return;
        }
        saveExecutor.execute(() -> {
            String yml = pendingYml.getAndSet(null);
            Path tempPath = null;
            try {
                file.getParentFile().mkdirs();
                tempPath = Files.createTempFile(file.getParentFile().toPath(),"playerdata",".tmp");
                // 一時ファイルへ書き込む
                Files.writeString(
                        tempPath,
                        yml,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );

                // 元ファイルを原子的に置き換える
                Files.move(
                        tempPath,
                        file.toPath(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException e) {
                AFKScoreboard.warn("playerdata.yml の書き込みに失敗しました: " + e.getMessage());
                AFKScoreboard.runTask(() -> isDirty = true);
            }finally {
                if (tempPath != null) {
                    try {
                        Files.deleteIfExists(tempPath);
                    } catch (IOException e) {
                        AFKScoreboard.warn(
                                "一時ファイルの削除に失敗しました: " + e.getMessage()
                        );
                    }
                }
            }
        });
    }
    
    private void saveValue(UUID uuid,Object value,String... path){
        if(errorUUIDs.contains(uuid)){return;}

        StringBuilder sb = new StringBuilder(uuid.toString());
        for(String s : path){
            sb.append(".").append(s);
        }
        config.set(sb.toString(), value);
        isDirty = true;
    }

    public void onPlayerJoin(Player player){
        loadPlayerData(player.getUniqueId());
    }

    public boolean isWelcomed(UUID uuid){
        return playerData.get(uuid).isWelcomed;
    }
    public void setWelcomed(UUID uuid, boolean isWelcomed){
        playerData.get(uuid).isWelcomed = isWelcomed;
        if(isWelcomed){
            configManager.addWelcomedPlayer(uuid);
        }else{
            configManager.removeWelcomedPlayer(uuid);
        }
        saveValue(uuid,isWelcomed,"isWelcomed");
    }
    public boolean isHiddenInRank(UUID uuid){
        return playerData.get(uuid).isHiddenInRank;
    }
    public void setHiddenInRank(UUID uuid, boolean isHiddenInRank){
        playerData.get(uuid).isHiddenInRank = isHiddenInRank;
        if(isHiddenInRank){
            configManager.addHiddenPlayer(uuid);
        }else{
            configManager.removeHiddenPlayer(uuid);
        }
        saveValue(uuid,isHiddenInRank,"isHiddenInRank");
    }
    public boolean isHidingPrefix(UUID uuid){
        return playerData.get(uuid).isHidingPrefix;
    }
    public void setHidingPrefix(UUID uuid, boolean isHidingPrefix){
        playerData.get(uuid).isHidingPrefix = isHidingPrefix;
        saveValue(uuid,isHidingPrefix,"isHidingPrefix");
    }

    public int getHighestScore(UUID uuid){
        return playerData.get(uuid).highScores.values().stream().max(Integer::compareTo).orElse(0);
    }

    public int getHighScore(UUID uuid,String zoneName) {
        return playerData.get(uuid).highScores.getOrDefault(zoneName, 0);
    }

    public void setHighScore(UUID uuid, int newScore,String zoneName) {
        playerData.get(uuid).highScores.put(zoneName, newScore);
        saveValue(uuid,newScore,"highScores",zoneName);
    }
    
    public String getShowedPrefix(UUID uuid){
        return playerData.get(uuid).showedPrefix;
    }
    public void setShowedPrefix(UUID uuid, String showedPrefix){
        playerData.get(uuid).showedPrefix = showedPrefix;
        saveValue(uuid,showedPrefix,"showedPrefix");
    }

    public Set<String> getPlayerPrefixes(UUID uuid){
        if(!playerData.containsKey(uuid)){
            return Set.of();
        }
        return Set.copyOf(playerData.get(uuid).prefixes);
    }
    public void addPlayerPrefix(UUID uuid,String prefix){
        playerData.get(uuid).prefixes.add(prefix);
        saveValue(uuid,List.copyOf(playerData.get(uuid).prefixes),"prefixes");
    }


    public List<Map.Entry<UUID, Integer>> getSortedList() {
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>();
        for(PlayerData playerData : playerData.values()){
            list.add(Map.entry(playerData.uuid, playerData.highScores.values().stream().max(Integer::compareTo).orElse(0)));
        }
        return list.stream()
                .sorted(Map.Entry.<UUID,Integer>comparingByValue().reversed())
                .toList();
    }

    private static class PlayerData {

        private final UUID uuid;
        private boolean isWelcomed;
        private boolean isHiddenInRank;
        private boolean isHidingPrefix;
        private final HashMap<String, Integer> highScores;
        private String showedPrefix;
        private final Set<String> prefixes;

        public PlayerData(
                UUID uuid,
                boolean isWelcomed,
                boolean isHiddenInRank,
                boolean isHidingPrefix,
                Map<String,Integer> highScores,
                String showedPrefix,
                Set<String> prefixes
        ) {
            this.uuid = uuid;
            this.isWelcomed = isWelcomed;
            this.isHiddenInRank = isHiddenInRank;
            this.isHidingPrefix = isHidingPrefix;
            this.highScores = new HashMap<>(highScores);
            this.showedPrefix = showedPrefix;
            this.prefixes = new HashSet<>(prefixes);
        }

        public static PlayerData getDefault(UUID uuid) {
            return new PlayerData(uuid,false,false,false,Map.of(),"",Set.of());
        }

        @NotNull
        public Map<String, Object> serialize() {
            return Map.of(
                    "isWelcomed", isWelcomed,
                    "isHiddenInRank",isHiddenInRank,
                    "isHidingPrefix",isHidingPrefix,
                    "highScores",highScores,
                    "showedPrefix",showedPrefix,
                    "prefixes",List.copyOf(prefixes));
        }
    }
}
