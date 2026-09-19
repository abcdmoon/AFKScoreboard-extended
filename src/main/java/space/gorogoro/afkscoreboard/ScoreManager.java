package space.gorogoro.afkscoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 名前が紛らわしいけど、プレイヤーの放置時間を司るクラス
 */
public class ScoreManager {
    private final ZoneManager zoneManager;


    // プレイヤーの「現在の連続放置時間（秒）」を保持するマップ
    private final Map<UUID, Integer> currentSessionTimes = new HashMap<>();
    // ログアウトしたプレイヤーのデータを一時保存するマップ（UUID -> 放置秒数）
    private final Map<UUID, Integer> disconnectedSessionTimes = new HashMap<>();
    // ログアウトした時刻を保存するマップ（UUID -> エポックミリ秒）
    private final Map<UUID, Long> disconnectTimes = new HashMap<>();
    //領域内のプレイヤーのセット
    private final Set<Player> currentAFKPlayers = new HashSet<>();

    public ScoreManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
        init();
    }
    private void init(){
        AFKScoreboard.registerTaskTimer(this::incrementTimeEverySecond,0,20L);
    }

    /**
     * 1秒ごとに、ゾーンにいるプレイヤーの時間（連続）を加算
     */
    private void incrementTimeEverySecond() {

        for(Player p : currentAFKPlayers) {
            UUID uuid = p.getUniqueId();
            currentSessionTimes.put(uuid, currentSessionTimes.getOrDefault(uuid, 0) + 1);
        }
    }

    public void onPlayerEnterZone(Player player){
        currentAFKPlayers.add(player);
        currentSessionTimes.put(player.getUniqueId(),0);

    }
    public void onPlayerLeaveZone(Player player){
        currentAFKPlayers.remove(player);
        currentSessionTimes.remove(player.getUniqueId());
    }
}
