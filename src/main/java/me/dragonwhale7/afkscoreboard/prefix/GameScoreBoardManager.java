package me.dragonwhale7.afkscoreboard.prefix;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.function.Consumer;

public class GameScoreBoardManager {
    private ScoreboardManager scoreboardManager;
    private Scoreboard mainScoreboard;
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
        mainScoreboard = scoreboardManager.getMainScoreboard();

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
                return mainScoreboard;
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

    public void addTeamToAll(String key) {
        try{
            if(mainScoreboard.getTeam(key) != null){
                mainScoreboard.getTeam(key).unregister();
                Bukkit.getLogger().warning(key+" Team already exists!");
            }
            mainScoreboard.registerNewTeam(key);
            if(afkScoreboard.getTeam(key) != null){
                afkScoreboard.getTeam(key).unregister();
                Bukkit.getLogger().warning(key+" Team already exists!");
            }
            afkScoreboard.registerNewTeam(key);
            if(highScoreScoreboard.getTeam(key) != null){
                highScoreScoreboard.getTeam(key).unregister();
                Bukkit.getLogger().warning(key+" Team already exists!");
            }
            highScoreScoreboard.registerNewTeam(key);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Team key are already in use");
        }
    }

    public void modifyAllTeam(String key, Consumer<Team> consumer) {
        consumer.accept(mainScoreboard.getTeam(key));
        consumer.accept(afkScoreboard.getTeam(key));
        consumer.accept(highScoreScoreboard.getTeam(key));
    }

    public void removeTeamFromAll(String key) {
        Team team = mainScoreboard.getTeam(key);
        if (team != null) {
            team.unregister();
        }
        team = afkScoreboard.getTeam(key);
        if (team != null) {
            team.unregister();
        }
        team = highScoreScoreboard.getTeam(key);
        if (team != null) {
            team.unregister();
        }
    }

    public void addPlayerToAllTeam(String name, String key) {
        Team team = mainScoreboard.getTeam(key);
        if (team == null) {
            throw new IllegalStateException("Team has not been added");
        }
        team.addEntry(name);
        team = afkScoreboard.getTeam(key);
        if (team == null) {
            throw new IllegalStateException("Team has not been added");
        }
        team.addEntry(name);
        team = highScoreScoreboard.getTeam(key);
        if (team == null) {
            throw new IllegalStateException("Team has not been added");
        }
        team.addEntry(name);
    }

    public  void removePlayerFromAllTeam(String name) {
        Team team = mainScoreboard.getEntryTeam(name);
        if (team != null) {
            team.removeEntry(name);
        }
        team = afkScoreboard.getEntryTeam(name);
        if (team != null) {
            team.removeEntry(name);
        }
        team = highScoreScoreboard.getEntryTeam(name);
        if (team != null) {
            team.removeEntry(name);
        }
    }
}
