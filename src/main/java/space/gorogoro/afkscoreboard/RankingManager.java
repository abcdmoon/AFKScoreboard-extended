package space.gorogoro.afkscoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class RankingManager {

    private final ConfigManager configManager;
    private final ScoreManager scoreManager;
    private ScoreboardManager scoreboardManager;
    private Scoreboard afkScoreboard;
    private Objective afkObjective;

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

    public RankingManager(ConfigManager configManager,ScoreManager scoreManager) {
        this.configManager = configManager;
        this.scoreManager = scoreManager;

        init();
    }
    private void init(){
        scoreboardManager = Bukkit.getScoreboardManager();
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
        List<Map.Entry<UUID, Integer>> sortedTop10 = scoreManager.getSortedList(10);

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

            String currentStr = formatTimeCompact(sessionSeconds);
            String scoreLine = String.format("§7#%d §f%s §b%s", rank, playerName, currentStr);

            afkObjective.getScore(scoreLine).setScore(scoreValue--);
            rank++;
        }
    }


    /**
     * コンパクトな時間フォーマット
     */
    private String formatTimeCompact(int totalSeconds) {
        if (totalSeconds < 60) return totalSeconds + "s";

        int totalMinutes = totalSeconds / 60;
        if (totalMinutes < 60) return totalMinutes + "m";

        int totalHours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        if (totalHours < 24) {
            if (minutes == 0) return totalHours + "h";
            return totalHours + "h" + minutes + "m";
        }

        int days = totalHours / 24;
        int hours = totalHours % 24;

        if (hours == 0 && minutes == 0) return days + "d";
        if (minutes == 0) return days + "d" + hours + "h";
        if (hours == 0) return days + "d" + minutes + "m";

        return days + "d" + hours + "h" + minutes + "m";
    }
}
