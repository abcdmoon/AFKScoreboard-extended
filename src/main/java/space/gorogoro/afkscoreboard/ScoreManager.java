package space.gorogoro.afkscoreboard;

import space.gorogoro.afkscoreboard.prefix.PrefixManager;
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

    private final PlayerDataManager playerDataManager;
    private final PrefixManager prefixManager;
    private final ZoneManager zoneManager;

    public ScoreManager(PlayerDataManager playerDataManager, PrefixManager prefixManager, ZoneManager zoneManager) {
        this.playerDataManager = playerDataManager;
        this.prefixManager = prefixManager;
        this.zoneManager = zoneManager;
    }

    /**
     * 1秒ごとに、ゾーンにいるプレイヤーの時間（連続）を加算
     */
    public void incrementTimeEverySecond() {
        for(ZoneManager.ZoneArea zone : zoneManager.getAllZones()){
            for(UUID uuid : zone.getAfkPlayers()){
                int score = currentSessionTimes.getOrDefault(uuid,0)+1;
                currentSessionTimes.put(uuid, score);
                prefixManager.onScoreChange(uuid,zone.getName(), score);
                if(playerDataManager.getHighScore(uuid,zone.getName())<score){
                    playerDataManager.setHighScore(uuid,score,zone.getName());
                }
            }

        }
    }

    public void onPlayerEnterZone(Player player){
        UUID uuid = player.getUniqueId();
        Integer disconnectedSessionTime = disconnectedSessionTimes.get(uuid);
        if(disconnectedSessionTime != null){
            long quitTime = disconnectTimes.getOrDefault(uuid,0L);
            if ((System.currentTimeMillis() - quitTime) < RECOVERY_GRACE_PERIOD_MS) {
                currentSessionTimes.put(uuid,disconnectedSessionTimes.remove(uuid));
            }else{
                disconnectedSessionTimes.remove(uuid);
            }
            disconnectTimes.remove(uuid);
        }
        currentSessionTimes.putIfAbsent(uuid, 0);

    }
    public void onPlayerLeaveZone(Player player){
        currentSessionTimes.remove(player.getUniqueId());
    }

    /*
    public void onPlayerConnect(Player player){
        UUID uuid = player.getUniqueId();
        Integer disconnectedSessionTime = disconnectedSessionTimes.get(uuid);
        if(disconnectedSessionTime != null){
            long quitTime = disconnectTimes.getOrDefault(uuid,0L);
            if ((System.currentTimeMillis() - quitTime) < RECOVERY_GRACE_PERIOD_MS&&zoneManager.getLocZone(player.getLocation())!=null) {
                currentSessionTimes.put(uuid,disconnectedSessionTimes.remove(uuid));
            }else{
                disconnectedSessionTimes.remove(uuid);
            }
            disconnectTimes.remove(uuid);
        }
    }

     */

    public void preparePlayerDisconnect(Player player){
        UUID uuid = player.getUniqueId();
        Integer sessionTime = currentSessionTimes.remove(uuid);

        if (sessionTime != null) {
            disconnectedSessionTimes.put(uuid, sessionTime);
            disconnectTimes.put(uuid, System.currentTimeMillis());
        }
    }


    public List<Map.Entry<UUID, Integer>> getSortedList() {
        return currentSessionTimes.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .toList();
    }


}
