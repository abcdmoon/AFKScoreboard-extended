package space.gorogoro.afkscoreboard;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConfigManager {

    private final AFKScoreboard plugin;
    private FileConfiguration config;

    public enum PrefixMode {team,tab}

    public ConfigManager(AFKScoreboard plugin) {
        this.plugin = plugin;
        initialProcess();
        AFKScoreboard.addOnDisableTask(this::finalProcess);

        //1時間毎に情報をファイルに保存
        AFKScoreboard.registerTaskTimer(this::periodicProcess,20*60*60,20*60*60);
    }

    private void initialProcess(){
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    private void periodicProcess(){
        plugin.saveConfig();
    }

    private void finalProcess(){
        saveWelcomedPlayers();
        saveHiddenPlayers();
        requestSaveConfig();
        plugin.saveConfig();
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
        String yaml = getConfig().saveToString();
        if (pendingConfigYaml.getAndSet(yaml) != null) {
            // すでに書き込み待ちがあるので、その書き込みで最新の内容が使われる
            return;
        }
        File configFile = new File(getDataFolder(), "config.yml");
        saveExecutor.execute(() -> {
            String latestYaml = pendingConfigYaml.getAndSet(null);
            try {
                Files.writeString(configFile.toPath(), latestYaml, StandardCharsets.UTF_8);
            } catch (IOException e) {
                getLogger().severe("config.yml の書き込みに失敗しました: " + e.getMessage());
            }
        });
    }

    /**
     * config.yml からメッセージ既読プレイヤーのUUIDを読み込む
     */
    private void loadWelcomedPlayers() {
        welcomedPlayers.clear();
        List<String> uuidStrings = getConfig().getStringList("welcomed-players");
        for (String s : uuidStrings) {
            try {
                welcomedPlayers.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    /**
     * メッセージ既読プレイヤーのUUIDを config.yml へ保存する
     */
    private void saveWelcomedPlayers() {
        List<String> uuidStrings = welcomedPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        getConfig().set("welcomed-players", uuidStrings);
        requestSaveConfig();
    }

    /**
     * config.yml から非表示プレイヤーのUUIDを読み込む
     */
    private void loadHiddenPlayers() {
        hiddenPlayers.clear();
        List<String> uuidStrings = getConfig().getStringList("hidden-players");
        for (String s : uuidStrings) {
            try {
                hiddenPlayers.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    /**
     * 非表示プレイヤーのUUIDを config.yml へ保存する
     */
    private void saveHiddenPlayers() {
        List<String> uuidStrings = hiddenPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        getConfig().set("hidden-players", uuidStrings);
        requestSaveConfig();
    }

}
