package space.gorogoro.afkscoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import me.dragonwhale7.afkscoreboard.AFKScoreboard;
import space.gorogoro.afkscoreboard.prefix.PrefixRegistry;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.function.Consumer;

public class GameScoreBoardManager {
    private final PrefixRegistry prefixRegistry;
    private ScoreboardManager scoreboardManager;
    private Scoreboard mainScoreboard;
    private Scoreboard afkScoreboard;
    private Scoreboard highScoreScoreboard;
    private Objective afkObjective;
    private Objective highScoreObjective;

    public enum ScoreboardType {
        MAIN,SCORE,HIGHSCORE
    }

    public GameScoreBoardManager(PrefixRegistry prefixRegistry) {
        this.prefixRegistry = prefixRegistry;
        init();
        AFKScoreboard.addOnDisableTask(this::fin);
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

    private void fin(){
        for(String teamKey : prefixRegistry.getTeamKeys()) {
            removeTeamFromAll(teamKey);
        }

        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player.getScoreboard().equals(afkScoreboard)||player.getScoreboard().equals(highScoreScoreboard)){
                player.setScoreboard(main);
            }
        }
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
        if (mainScoreboard.getTeam(key) == null) {
            mainScoreboard.registerNewTeam(key);
        }
        if (afkScoreboard.getTeam(key) == null) {
            afkScoreboard.registerNewTeam(key);
        }
        if (highScoreScoreboard.getTeam(key) == null) {
            highScoreScoreboard.registerNewTeam(key);
        }
    }

    public void modifyAllTeam(String key, Consumer<Team> consumer) {
        if(mainScoreboard.getTeam(key) != null) {
            consumer.accept(mainScoreboard.getTeam(key));
        }
        if(afkScoreboard.getTeam(key) != null) {
            consumer.accept(afkScoreboard.getTeam(key));
        }
        if(highScoreScoreboard.getTeam(key) != null) {
            consumer.accept(highScoreScoreboard.getTeam(key));
        }
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
        if (team != null&&team.getName().startsWith("afs_")) {
            team.removeEntry(name);
        }
        team = afkScoreboard.getEntryTeam(name);
        if (team != null&&team.getName().startsWith("afs_")) {
            team.removeEntry(name);
        }
        team = highScoreScoreboard.getEntryTeam(name);
        if (team != null&&team.getName().startsWith("afs_")) {
            team.removeEntry(name);
        }
    }
}
