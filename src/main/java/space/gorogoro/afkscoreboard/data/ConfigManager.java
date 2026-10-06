package space.gorogoro.afkscoreboard.data;

import org.bukkit.configuration.file.FileConfiguration;
import space.gorogoro.afkscoreboard.AFKScoreboard;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ConfigManager {

    private final AFKScoreboard plugin;
    private FileConfiguration config;

    public enum PrefixMode {team,tab}

    private final AtomicReference<String> pendingConfigYaml = new AtomicReference<>();
    // config.yml の書き込み専用スレッド（1本なので書き込みは必ず順番に行われる）
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "AFKScoreboard-Save"));

    public ConfigManager(AFKScoreboard plugin) {
        this.plugin = plugin;
        initialProcess();
        AFKScoreboard.addOnDisableTask(this::finalProcess);
    }

    private void initialProcess(){
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    private void finalProcess(){
        requestSaveConfig();
        saveExecutor.shutdown();
        try {
            if (!saveExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                AFKScoreboard.warn("config.ymlの書き込みが時間内に終わりませんでした");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public PrefixMode loadPrefixMode(){
        String value = config.getString("prefix-mode", "team");

        return Arrays.stream(PrefixMode.values())
                .filter(mode -> mode.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseGet(() -> {
                    AFKScoreboard.warn("Prefix mode is invalid: " + value);
                    return PrefixMode.team;
                });
    }

    /**
     * 現在の config.yml の内容を専用スレッドで書き込む
     * getConfig() へのアクセスはメインスレッドで行い、ファイルの書き込みだけを専用スレッドに任せる
     * 書き込み待ちが残っている間に呼ばれた場合は、最新の内容で 1 回にまとめて書き込む
     */
    private void requestSaveConfig() {
        String yaml = plugin.getConfig().saveToString();
        if (pendingConfigYaml.getAndSet(yaml) != null) {
            // すでに書き込み待ちがあるので、その書き込みで最新の内容が使われる
            return;
        }
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        saveExecutor.execute(() -> {
            String latestYaml = pendingConfigYaml.getAndSet(null);
            try {
                Files.writeString(configFile.toPath(), latestYaml, StandardCharsets.UTF_8);
            } catch (IOException e) {
                plugin.getLogger().severe("config.yml の書き込みに失敗しました: " + e.getMessage());
            }
        });
    }

    /**
     * config.yml からメッセージ既読プレイヤーのUUIDを読み込む
     */
    Set<UUID> loadWelcomedPlayers() {
        Set<UUID> welcomedPlayers = new HashSet<>();
        List<String> uuidStrings = plugin.getConfig().getStringList("welcomed-players");
        for (String s : uuidStrings) {
            try {
                welcomedPlayers.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return welcomedPlayers;
    }

    /**
     * メッセージ既読プレイヤーのUUIDを config.yml へ保存する
     */
    void saveWelcomedPlayers(Set<UUID> welcomedPlayers) {
        List<String> uuidStrings = welcomedPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        plugin.getConfig().set("welcomed-players", uuidStrings);
        requestSaveConfig();
    }

    void addWelcomedPlayer(UUID uuid){
        Set<UUID> welcomedPlayers = loadWelcomedPlayers();
        welcomedPlayers.add(uuid);
        saveWelcomedPlayers(welcomedPlayers);
    }

    void removeWelcomedPlayer(UUID uuid){
        Set<UUID> welcomedPlayers = loadWelcomedPlayers();
        welcomedPlayers.remove(uuid);
        saveWelcomedPlayers(welcomedPlayers);
    }

    /**
     * config.yml から非表示プレイヤーのUUIDを読み込む
     */
    Set<UUID> loadHiddenPlayers() {
        Set<UUID> hiddenPlayers = new HashSet<>();
        List<String> uuidStrings = plugin.getConfig().getStringList("hidden-players");
        for (String s : uuidStrings) {
            try {
                hiddenPlayers.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return hiddenPlayers;
    }

    /**
     * 非表示プレイヤーのUUIDを config.yml へ保存する
     */
    void saveHiddenPlayers(Set<UUID> hiddenPlayers) {
        List<String> uuidStrings = hiddenPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        plugin.getConfig().set("hidden-players", uuidStrings);
        requestSaveConfig();
    }

    void addHiddenPlayer(UUID uuid) {
        Set<UUID> hiddenPlayers = loadHiddenPlayers();
        hiddenPlayers.add(uuid);
        saveHiddenPlayers(hiddenPlayers);
    }

    void removeHiddenPlayer(UUID uuid) {
        Set<UUID> hiddenPlayers = loadHiddenPlayers();
        hiddenPlayers.remove(uuid);
        saveHiddenPlayers(hiddenPlayers);
    }
}
