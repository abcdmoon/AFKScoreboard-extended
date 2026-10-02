package me.dragonwhale7.afkscoreboard;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class RankingManager {

    private final ConfigManager configManager;
    private final ScoreManager scoreManager;
    private final PlayerDataManager playerDataManager;
    private final GameScoreBoardManager gameScoreBoardManager;

    // ランキングから自分を非表示にしているプレイヤーのUUIDを保持するセット
    private final Set<UUID> hiddenPlayers = new HashSet<>();
    public boolean isHidden(UUID uuid) {
        return hiddenPlayers.contains(uuid);
    }
    public void toggleHidden(UUID uuid) {
        if(hiddenPlayers.contains(uuid)) {
            hiddenPlayers.remove(uuid);
            playerDataManager.setHiddenInRank(uuid, false);
        }else {
            hiddenPlayers.add(uuid);
            playerDataManager.setHiddenInRank(uuid, true);
        }
    }

    public RankingManager(ConfigManager configManager, ScoreManager scoreManager, PlayerDataManager playerDataManager, GameScoreBoardManager gameScoreBoardManager) {
        this.configManager = configManager;
        this.scoreManager = scoreManager;
        this.playerDataManager = playerDataManager;
        this.gameScoreBoardManager = gameScoreBoardManager;

        init();
    }
    private void init(){
        hiddenPlayers.clear();
        hiddenPlayers.addAll(playerDataManager.getHiddenInRankPlayers());

        AFKScoreboard.registerTaskTimer(this::updateLeaderboard,0,100L);
    }

    public void onPlayerEnterZone(Player player){
        gameScoreBoardManager.showScoreboard(player, GameScoreBoardManager.ScoreboardType.SCORE);
    }

    public void onPlayerLeaveZone(Player player){
        gameScoreBoardManager.showScoreboard(player, GameScoreBoardManager.ScoreboardType.MAIN);
    }

    /**
     * ランキングを計算してスコアボードを更新
     */
    private void updateLeaderboard() {
        Scoreboard afkScoreboard = gameScoreBoardManager.getScoreboard(GameScoreBoardManager.ScoreboardType.SCORE);
        Objective afkObjective = gameScoreBoardManager.getObjective(GameScoreBoardManager.ScoreboardType.SCORE);
        for (String entry : afkScoreboard.getEntries()) {
            afkScoreboard.resetScores(entry);
        }

        // 現在放置中の上位10人を取得
        List<Map.Entry<UUID, Integer>> scoreList = scoreManager.getSortedList();
        List<Map.Entry<Player,Integer>> sortedPlayerTop10 = scoreList.stream()
                .filter(e->!isHidden(e.getKey()))
                .map(entry->{
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if(player == null||!player.isOnline()) {
                        return null;
                    }else{
                        return Map.entry(player,entry.getValue());
                    }
                })
                .filter(Objects::nonNull)
                .limit(10)
                .toList();

        // 初期値の動的計算: ヘッダー2行 ＋ ランクインしている人数
        // 誰もおらず「誰も放置していません」の1行を表示する場合は「2行 + 1行 = 3」になります
        int scoreValue = 2 + (sortedPlayerTop10.isEmpty() ? 1 : sortedPlayerTop10.size());

        // ヘッダー部分の設定
        afkObjective.getScore("§7位 プレイヤー §b連続放置時間").setScore(scoreValue--);
        afkObjective.getScore("§8----------------------").setScore(scoreValue--);

        if (sortedPlayerTop10.isEmpty()) {
            afkObjective.getScore("§7 現在、誰も放置していません").setScore(scoreValue);
            return;
        }

        int rank = 1;
        for (Map.Entry<Player, Integer> entry : sortedPlayerTop10) {

            String playerName = entry.getKey().getName();
            int sessionSeconds = entry.getValue();

            String currentStr = Util.formatTimeCompact(sessionSeconds);
            String scoreLine = String.format("§7#%d §f%s §b%s", rank, playerName, currentStr);

            afkObjective.getScore(scoreLine).setScore(scoreValue--);
            rank++;
        }
    }

    /**
     * ハイスコアのランキングを計算して反映
     */
    public void updateHighScoreScoreBoard(){
        Scoreboard highScoreScoreboard = gameScoreBoardManager.getScoreboard(GameScoreBoardManager.ScoreboardType.HIGHSCORE);
        Objective highScoreObjective = gameScoreBoardManager.getObjective(GameScoreBoardManager.ScoreboardType.HIGHSCORE);
        for (String entry : highScoreScoreboard.getEntries()) {
            highScoreScoreboard.resetScores(entry);
        }

        // 現在放置中の上位10人を取得
        List<Map.Entry<UUID, Integer>> sortedTop10 = playerDataManager.getSortedList();
        sortedTop10 = sortedTop10.stream()
                .filter(e->!isHidden(e.getKey()))
                .limit(10)
                .toList();

        // 初期値の動的計算: ヘッダー2行 ＋ ランクインしている人数
        // 誰もおらず「誰も放置していません」の1行を表示する場合は「2行 + 1行 = 3」になります
        int scoreValue = 2 + (sortedTop10.isEmpty() ? 1 : sortedTop10.size());

        // ヘッダー部分の設定
        highScoreObjective.getScore("§7位 プレイヤー §b最高連続放置時間").setScore(scoreValue--);
        highScoreObjective.getScore("§8----------------------").setScore(scoreValue--);

        if (sortedTop10.isEmpty()) {
            highScoreObjective.getScore("§7 現在まで誰も放置していません").setScore(scoreValue);
            return;
        }

        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : sortedTop10) {
            UUID uuid = entry.getKey();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String playerName = offlinePlayer.getName();
            if(playerName == null){
                continue;
            }

            int sessionSeconds = entry.getValue();

            String currentStr = Util.formatTimeCompact(sessionSeconds);
            String scoreLine = String.format("§7#%d §f%s §6%s", rank, playerName, currentStr);

            highScoreObjective.getScore(scoreLine).setScore(scoreValue--);
            rank++;
        }
    }

}
