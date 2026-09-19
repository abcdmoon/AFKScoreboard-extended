package space.gorogoro.afkscoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;

public class RankingManager {

    private final ConfigManager configManager;
    private final ScoreManager scoreManager;
    private final HighScoreManager highScoreManager;
    private ScoreboardManager scoreboardManager;
    private Scoreboard afkScoreboard;
    private Scoreboard highScoreScoreboard;
    private Objective afkObjective;
    private Objective highScoreObjective;

    // ランキングから自分を非表示にしているプレイヤーのUUIDを保持するセット
    private final Set<UUID> hiddenPlayers = new HashSet<>();
    public boolean isHidden(UUID uuid) {
        return hiddenPlayers.contains(uuid);
    }
    public void toggleHidden(UUID uuid) {
        if(hiddenPlayers.contains(uuid)) {
            hiddenPlayers.remove(uuid);
        }else {
            hiddenPlayers.add(uuid);
        }
        configManager.saveHiddenPlayers(hiddenPlayers);
    }

    public RankingManager(ConfigManager configManager,ScoreManager scoreManager,HighScoreManager highScoreManager) {
        this.configManager = configManager;
        this.scoreManager = scoreManager;
        this.highScoreManager = highScoreManager;

        init();
    }
    private void init(){
        scoreboardManager = Bukkit.getScoreboardManager();

        //スコア表示用
        afkScoreboard = scoreboardManager.getNewScoreboard();
        // タイトル (Paper推奨の形式に修正)
        this.afkObjective = afkScoreboard.registerNewObjective(
                "afk_top10",
                Criteria.DUMMY,
                LegacyComponentSerializer.legacySection().deserialize("§e§l放置時間ランキング"),
                RenderType.INTEGER
        );
        this.afkObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // スコアのフォーマットを「空白（Blank）」に設定することで、右側の数字を完全に非表示
        this.afkObjective.numberFormat(NumberFormat.blank());

        //ハイスコア表示用
        highScoreScoreboard = scoreboardManager.getNewScoreboard();
        // タイトル (Paper推奨の形式に修正)
        this.highScoreObjective = highScoreScoreboard.registerNewObjective(
                "afkHighScore_top10",
                Criteria.DUMMY,
                LegacyComponentSerializer.legacySection().deserialize("§6§l最高放置時間ランキング"),
                RenderType.INTEGER
        );
        this.highScoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // スコアのフォーマットを「空白（Blank）」に設定することで、右側の数字を完全に非表示
        this.highScoreObjective.numberFormat(NumberFormat.blank());

        hiddenPlayers.clear();
        hiddenPlayers.addAll(configManager.loadHiddenPlayers());

        AFKScoreboard.registerTaskTimer(this::updateLeaderboard,0,100L);
    }

    public void onPlayerEnterZone(Player player){
        if (!player.getScoreboard().equals(afkScoreboard)) {
            player.setScoreboard(afkScoreboard);
        }
    }

    public void onPlayerLeaveZone(Player player){
        if (player.getScoreboard().equals(afkScoreboard)) {
            player.setScoreboard(scoreboardManager.getMainScoreboard());
        }
    }

    /**
     * ランキングを計算してスコアボードを更新
     */
    private void updateLeaderboard() {
        for (String entry : afkScoreboard.getEntries()) {
            afkScoreboard.resetScores(entry);
        }

        // 現在放置中の上位10人を取得
        List<Map.Entry<UUID, Integer>> sortedTop10 = scoreManager.getSortedList();
        sortedTop10 = sortedTop10.stream()
                .filter(e->!isHidden(e.getKey()))
                .limit(10)
                .toList();

        // 初期値の動的計算: ヘッダー2行 ＋ ランクインしている人数
        // 誰もおらず「誰も放置していません」の1行を表示する場合は「2行 + 1行 = 3」になります
        int scoreValue = 2 + (sortedTop10.isEmpty() ? 1 : sortedTop10.size());

        // ヘッダー部分の設定
        afkObjective.getScore("§7位 プレイヤー §b連続放置時間").setScore(scoreValue--);
        afkObjective.getScore("§8----------------------").setScore(scoreValue--);

        if (sortedTop10.isEmpty()) {
            afkObjective.getScore("§7 現在、誰も放置していません").setScore(scoreValue);
            return;
        }

        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : sortedTop10) {
            UUID uuid = entry.getKey();
            Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                continue;
            }

            String playerName = player.getName();
            int sessionSeconds = entry.getValue();

            String currentStr = Util.formatTimeCompact(sessionSeconds);
            String scoreLine = String.format("§7#%d §f%s §b%s", rank, playerName, currentStr);

            afkObjective.getScore(scoreLine).setScore(scoreValue--);
            rank++;
        }
    }

    public void showMainScoreBoard(Player player){
        player.setScoreboard(scoreboardManager.getMainScoreboard());
    }

    public void showAfkScoreBoard(Player player){
        player.setScoreboard(afkScoreboard);
    }

    public void showHighScoreScoreBoard(Player player){
        player.setScoreboard(highScoreScoreboard);
    }
    public boolean isShownHighScoreScoreBoard(Player player){
        return player.getScoreboard().equals(highScoreScoreboard);
    }

    /**
     * ハイスコアのランキングを計算して反映
     */
    public void updateHighScoreScoreBoard(){
        for (String entry : highScoreScoreboard.getEntries()) {
            highScoreScoreboard.resetScores(entry);
        }

        // 現在放置中の上位10人を取得
        List<Map.Entry<UUID, Integer>> sortedTop10 = highScoreManager.getSortedList();
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
            Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                continue;
            }

            String playerName = player.getName();
            int sessionSeconds = entry.getValue();

            String currentStr = Util.formatTimeCompact(sessionSeconds);
            String scoreLine = String.format("§7#%d §f%s §6%s", rank, playerName, currentStr);

            highScoreObjective.getScore(scoreLine).setScore(scoreValue--);
            rank++;
        }
    }

}
