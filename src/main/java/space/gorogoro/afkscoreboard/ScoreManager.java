package space.gorogoro.afkscoreboard;

import org.bukkit.entity.Player;

import java.util.*;

/**
 * 名前が紛らわしいけど、プレイヤーの放置時間を司るクラス
 */
public class ScoreManager {

    // 救済猶予時間（5分 = 300,000ミリ秒）
    private static final long RECOVERY_GRACE_PERIOD_MS = 5 * 60 * 1000L;

    // プレイヤーの「現在の連続放置時間（秒）」を保持するマップ
    private final Map<UUID, Integer> currentSessionTimes = new HashMap<>();

    // ログアウトしたプレイヤーのデータを一時保存するマップ（UUID -> 放置秒数）
    private final Map<UUID, Integer> disconnectedSessionTimes = new HashMap<>();
    // ログアウトした時刻を保存するマップ（UUID -> エポックミリ秒）
    private final Map<UUID, Long> disconnectTimes = new HashMap<>();
    //領域内のプレイヤーのセット
    private final Set<UUID> currentAFKPlayers = new HashSet<>();

    public ScoreManager() {
        init();
    }
    private void init(){
        currentAFKPlayers.clear();
        AFKScoreboard.registerTaskTimer(this::incrementTimeEverySecond,0,20L);
    }

    /**
     * 1秒ごとに、ゾーンにいるプレイヤーの時間（連続）を加算
     */
    private void incrementTimeEverySecond() {

        for(UUID uuid : currentAFKPlayers) {
            currentSessionTimes.put(uuid, currentSessionTimes.getOrDefault(uuid, 0) + 1);
        }
    }

    public void onPlayerEnterZone(Player player){
        currentAFKPlayers.add(player.getUniqueId());
        currentSessionTimes.put(player.getUniqueId(),0);

    }
    public void onPlayerLeaveZone(Player player){
        currentAFKPlayers.remove(player.getUniqueId());
        currentSessionTimes.remove(player.getUniqueId());
    }

    public void onPlayerConnect(Player player){
        UUID uuid = player.getUniqueId();
        currentAFKPlayers.add(uuid);
        long quitTime = disconnectTimes.getOrDefault(uuid,0L);
        if ((System.currentTimeMillis() - quitTime) < RECOVERY_GRACE_PERIOD_MS) {
            currentSessionTimes.put(uuid,disconnectedSessionTimes.remove(uuid));
            disconnectTimes.remove(uuid);
        }
    }

    public void onPlayerDisconnect(Player player){
        currentAFKPlayers.remove(player.getUniqueId());
        disconnectedSessionTimes.put(player.getUniqueId(),currentSessionTimes.remove(player.getUniqueId()));
        disconnectTimes.put(player.getUniqueId(),System.currentTimeMillis());
    }

    public List<Map.Entry<UUID, Integer>> getSortedList() {
        return currentSessionTimes.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .toList();
    }

}
