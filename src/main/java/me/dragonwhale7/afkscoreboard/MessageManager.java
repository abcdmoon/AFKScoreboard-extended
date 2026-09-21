package me.dragonwhale7.afkscoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MessageManager {

    private final ConfigManager configManager;

    // 過去に一度でも放置ゾーンに入ったことがあるプレイヤーを記憶するセット
    private final Set<UUID> welcomedPlayers = new HashSet<>();


    public MessageManager(ConfigManager configManager) {
        this.configManager = configManager;
        init();
    }

    private void init(){
        welcomedPlayers.clear();
        welcomedPlayers.addAll(configManager.loadWelcomedPlayers());
    }

    public void onPlayerEnterZone(Player player) {
        // 放置エリアに足を踏み入れたプレイヤーへの通知

        if (!welcomedPlayers.contains(player.getUniqueId())) {
            //初めて入った場合

            welcomedPlayers.add(player.getUniqueId());
            // メッセージを送信
            player.sendMessage(Component.text("/afkhide").color(NamedTextColor.AQUA).append(Component.text(" で放置ランキングから自分を表示/非表示できます").color(NamedTextColor.WHITE)));

            Set<UUID> welcomedPlayersSet = Set.copyOf(welcomedPlayers);
            configManager.saveWelcomedPlayers(welcomedPlayersSet);
        }else{
            //入ったことがある場合
        }
    }
}
