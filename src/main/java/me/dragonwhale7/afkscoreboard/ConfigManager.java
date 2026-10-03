package me.dragonwhale7.afkscoreboard;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

public class ConfigManager {


    private final AFKScoreboard plugin;
    private final FileConfiguration config;

    public enum PrefixMode {team,tab}

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

    public PrefixMode loadPrefixMode(){
        if(Arrays.stream(PrefixMode.values()).anyMatch(m->m.toString().equalsIgnoreCase(config.getString("prefix.mode")))){
            return PrefixMode.valueOf(config.getString("PrefixMode"));
        }else{
            AFKScoreboard.warn("Prefix mode is invalid!");
            return PrefixMode.team;
        }
    }



}
