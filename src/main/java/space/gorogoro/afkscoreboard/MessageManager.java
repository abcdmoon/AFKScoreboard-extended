package space.gorogoro.afkscoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import space.gorogoro.afkscoreboard.data.ConfigManager;
import space.gorogoro.afkscoreboard.data.PlayerDataManager;

public class MessageManager {

    private final PlayerDataManager playerDataManager;


    public MessageManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
        init();
    }

    private void init(){
    }

    public void onPlayerEnterZone(Player player) {
        // 放置エリアに足を踏み入れたプレイヤーへの通知

        if (!playerDataManager.isWelcomed(player.getUniqueId())) {
            playerDataManager.setWelcomed(player.getUniqueId(), true);

            // メッセージを送信
            player.sendMessage(Component.text("/afkscore hidescore").color(NamedTextColor.AQUA).append(Component.text(" で放置ランキングから自分を表示/非表示できます").color(NamedTextColor.WHITE)));
            player.sendMessage(Component.text("/afkscore hideprefix").color(NamedTextColor.AQUA).append(Component.text(" で自分の称号を表示/非表示できます").color(NamedTextColor.WHITE)));

        }else{
            //入ったことがある場合
        }
    }
}
