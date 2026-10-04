package space.gorogoro.afkscoreboard;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

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



}
