package me.dragonwhale7.afkscoreboard;

import me.dragonwhale7.afkscoreboard.prefix.Prefix;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ConfigManager {


    private final AFKScoreboard plugin;
    private final FileConfiguration config;

    public ConfigManager(AFKScoreboard plugin) {
        this.plugin = plugin;
        config = plugin.getConfig();
        initialProcess();
        AFKScoreboard.addOnDisableTask(this::finalProcess);

        //1時間毎に情報をファイルに保存
        AFKScoreboard.registerTaskTimer(this::periodicProcess,20*60*60,20*60*60);
    }

    private void initialProcess(){
        plugin.saveDefaultConfig();
    }

    private void periodicProcess(){
        plugin.saveConfig();
    }

    private void finalProcess(){
        plugin.saveConfig();
    }




}
