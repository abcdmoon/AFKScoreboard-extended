package me.dragonwhale7.afkscoreboard.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.dragonwhale7.afkscoreboard.HighScoreManager;
import me.dragonwhale7.afkscoreboard.RankingManager;
import me.dragonwhale7.afkscoreboard.ZoneManager;

public class CommandManager {
    private CommandManager(){}

    public static void registerCommands(Commands registrar, RankingManager rankingManager, ZoneManager zoneManager, HighScoreManager highScoreManager) {
        HideExecuter hideExecuter = new HideExecuter(rankingManager);
        ReloadExecuter reloadExecuter = new ReloadExecuter(zoneManager);
        HighScoreExecuter highScoreExecuter = new HighScoreExecuter(highScoreManager,rankingManager,zoneManager);
        registrar.register(build(hideExecuter,reloadExecuter,highScoreExecuter));
        registrar.register(oldbuild(hideExecuter));
    }

    public static LiteralCommandNode<CommandSourceStack> build(HideExecuter hideExecuter,ReloadExecuter reloadExecuter,HighScoreExecuter highScoreExecuter) {
        return Commands.literal("afkscore")
                .then(HideExecuter.create(hideExecuter))
                .then(ReloadExecuter.create(reloadExecuter))
                .then(HighScoreExecuter.create(highScoreExecuter))
                .build();

    }

    public static LiteralCommandNode<CommandSourceStack> oldbuild(HideExecuter hideExecuter){
        return HideExecuter.oldCreate(hideExecuter).build();
    }

}
