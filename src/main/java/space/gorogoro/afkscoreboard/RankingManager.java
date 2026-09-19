package space.gorogoro.afkscoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RankingManager {

    private final ConfigManager configManager;
    private ScoreboardManager scoreboardManager;
    private Scoreboard afkScoreboard;
    private Objective afkObjective;

    // ランキングから自分を非表示にしているプレイヤーのUUIDを保持するセット
    private final Set<UUID> hiddenPlayers = new HashSet<>();

    public RankingManager(ConfigManager configManager) {
        this.configManager = configManager;

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
}
