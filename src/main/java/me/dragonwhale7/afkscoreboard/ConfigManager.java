package me.dragonwhale7.afkscoreboard;

import me.dragonwhale7.afkscoreboard.prefix.Prefix;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    /**
     * config.yml から肩書非表示プレイヤーのUUIDを読み込む
     */
    public Set<UUID> loadPrefixHiddenPlayers() {
        Set<UUID> hiddenPlayers = new HashSet<>();
        List<String> uuidStrings = config.getStringList("prefix-hidden-players");
        for (String s : uuidStrings) {
            try {
                hiddenPlayers.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return hiddenPlayers;
    }

    /**
     * 肩書非表示プレイヤーのUUIDを config.yml へ保存する
     */
    public void savePrefixHiddenPlayers(Set<UUID> hiddenPlayers) {

        List<String> uuidStrings = hiddenPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        config.set("prefix-hidden-players", uuidStrings);
        plugin.saveConfig();
    }

    public Set<Prefix> loadPrefixes(){
        Set<Prefix> prefixes = new HashSet<>();
        File file = new File(plugin.getDataFolder(), "prefix.yml");

        if (!file.exists()) {
            plugin.saveResource("prefix.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection section = config.getConfigurationSection("prefixes");

        if (section == null) {
            return Set.of();
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection prefixSection = section.getConfigurationSection(key);

            if (prefixSection == null) {
                continue;
            }

            Prefix prefix = new Prefix(
                    key,
                    prefixSection.getInt("requireScore",0),
                    prefixSection.getString("prefixText", ""),
                    prefixSection.getString("color", "white")
            );
            prefixes.add(prefix);
        }
        return prefixes;
    }

}
