package me.dragonwhale7.afkscoreboard.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.dragonwhale7.afkscoreboard.PlayerDataManager;
import me.dragonwhale7.afkscoreboard.RankingManager;
import me.dragonwhale7.afkscoreboard.ZoneManager;
import me.dragonwhale7.afkscoreboard.GameScoreBoardManager;
import me.dragonwhale7.afkscoreboard.event.EventManager;
import me.dragonwhale7.afkscoreboard.prefix.PrefixManager;
import me.dragonwhale7.afkscoreboard.prefix.PrefixRegistry;

public class CommandManager {
    private CommandManager(){}

    public static void registerCommands(Commands registrar, RankingManager rankingManager, ZoneManager zoneManager, PlayerDataManager playerDataManager, GameScoreBoardManager gameScoreBoardManager, PrefixManager prefixManager, PrefixRegistry prefixRegistry, EventManager eventManager) {
        HideExecuter hideExecuter = new HideExecuter(rankingManager);
        ReloadExecuter reloadExecuter = new ReloadExecuter(zoneManager,eventManager,prefixRegistry);
        HighScoreExecuter highScoreExecuter = new HighScoreExecuter(playerDataManager,rankingManager,zoneManager,gameScoreBoardManager);
        PrefixExecuter prefixExecuter = new PrefixExecuter(prefixManager,playerDataManager,prefixRegistry);
        ReloadPrefixExecuter reloadPrefixExecuter = new ReloadPrefixExecuter(prefixRegistry,prefixManager);
        registrar.register(build(hideExecuter,reloadExecuter,highScoreExecuter, prefixExecuter,playerDataManager,prefixRegistry, reloadPrefixExecuter));
        registrar.register(oldbuild(hideExecuter));
    }

    public static LiteralCommandNode<CommandSourceStack> build(HideExecuter hideExecuter, ReloadExecuter reloadExecuter, HighScoreExecuter highScoreExecuter, PrefixExecuter prefixExecuter, PlayerDataManager playerDataManager, PrefixRegistry prefixRegistry, ReloadPrefixExecuter reloadPrefixExecuter) {
        return Commands.literal("afkscore")
                .then(HideExecuter.create(hideExecuter))
                .then(ReloadExecuter.create(reloadExecuter))
                .then(HighScoreExecuter.create(highScoreExecuter))
                .then(PrefixExecuter.create(prefixExecuter,playerDataManager,prefixRegistry))
                .then(ReloadPrefixExecuter.create(reloadPrefixExecuter))
                .build();

    }

    public static LiteralCommandNode<CommandSourceStack> oldbuild(HideExecuter hideExecuter){
        return HideExecuter.oldCreate(hideExecuter).build();
    }

}
