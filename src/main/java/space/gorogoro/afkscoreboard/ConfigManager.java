package space.gorogoro.afkscoreboard;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConfigManager {


    private final AFKScoreboard plugin;
    private final FileConfiguration config;

    public ConfigManager(AFKScoreboard plugin) {
        this.plugin = plugin;
        config = plugin.getConfig();
        initialProcess();
    }

    private void initialProcess(){
        plugin.saveDefaultConfig();
    }

    /**
     * config.yml からメッセージ既読プレイヤーのUUIDを読み込む
     */
    public Set<UUID> loadWelcomedPlayers(){
        Set<UUID> welcomedPlayers = new HashSet<>();
        List<String> uuidStrings = config.getStringList("welcomed-players");
        for (String s : uuidStrings) {
            try {
                welcomedPlayers.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return welcomedPlayers;
    }

    /**
     * メッセージ既読プレイヤーのUUIDを config.yml へ保存する
     */
    public void saveWelcomedPlayers(Set<UUID> welcomedPlayers) {
        List<String> uuidStrings = welcomedPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        config.set("welcomed-players", uuidStrings);
        plugin.saveConfig();
    }

    /**
     * config.yml から非表示プレイヤーのUUIDを読み込む
     */
    public Set<UUID> loadHiddenPlayers() {
        Set<UUID> hiddenPlayers = new HashSet<>();
        List<String> uuidStrings = config.getStringList("hidden-players");
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
    public void saveHiddenPlayers(Set<UUID> hiddenPlayers) {

        List<String> uuidStrings = hiddenPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        config.set("hidden-players", uuidStrings);
        plugin.saveConfig();
    }


}
