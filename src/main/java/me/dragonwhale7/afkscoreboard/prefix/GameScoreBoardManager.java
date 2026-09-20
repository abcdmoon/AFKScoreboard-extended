package me.dragonwhale7.afkscoreboard.prefix;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class GameScoreBoardManager {
    private ScoreboardManager scoreboardManager;
    private Scoreboard afkScoreboard;
    private Scoreboard highScoreScoreboard;
    private Objective afkObjective;
    private Objective highScoreObjective;

    public enum ScoreboardType {
        MAIN,SCORE,HIGHSCORE
    }

    public GameScoreBoardManager() {
        init();
    }

    private void init() {
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
    }

    public Scoreboard getScoreboard(ScoreboardType type) {
        switch (type) {
            case SCORE:{
                return afkScoreboard;
            }
            case HIGHSCORE:{
                return highScoreScoreboard;
            }
            case MAIN:
            default:{
                return scoreboardManager.getMainScoreboard();
            }
        }
    }

    public Objective getObjective(ScoreboardType type) {
        switch (type) {
            case SCORE:{
                return afkObjective;
            }
            case HIGHSCORE:{
                return highScoreObjective;
            }
            default:{
                throw new IllegalArgumentException("Unknown objective type");
            }
        }
    }

    public void showScoreboard(Player player, ScoreboardType type) {
        player.setScoreboard(getScoreboard(type));
    }
}
