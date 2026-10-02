package me.dragonwhale7.afkscoreboard.prefix;

import me.dragonwhale7.afkscoreboard.AFKScoreboard;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class PrefixConfigManager {
    private final AFKScoreboard plugin;
    private final File file;

    public PrefixConfigManager(AFKScoreboard plugin) {
        this.plugin = plugin;

        file = new File(plugin.getDataFolder(), "prefix.yml");
    }

    public Set<Prefix> loadPrefixes(){
        Set<Prefix> prefixes = new HashSet<>();

        if (!file.exists()||!file.isFile()) {
            plugin.saveResource("prefix.yml", false);
        }

        YamlConfiguration config = new YamlConfiguration();
        try{
            config.load(file);
        } catch (Exception e) {
            AFKScoreboard.warn("An error occurred while loading prefix.yml: " + e.getMessage());
            return Set.of();
        }

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
