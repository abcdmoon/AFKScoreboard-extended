package me.dragonwhale7.afkscoreboard.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.dragonwhale7.afkscoreboard.HighScoreManager;
import me.dragonwhale7.afkscoreboard.RankingManager;
import me.dragonwhale7.afkscoreboard.ZoneManager;
import me.dragonwhale7.afkscoreboard.event.EventManager;
import me.dragonwhale7.afkscoreboard.prefix.GameScoreBoardManager;
import me.dragonwhale7.afkscoreboard.prefix.PrefixManager;
import me.dragonwhale7.afkscoreboard.prefix.PrefixRegistry;

public class CommandManager {
    private CommandManager(){}

    public static void registerCommands(Commands registrar, RankingManager rankingManager, ZoneManager zoneManager, HighScoreManager highScoreManager, GameScoreBoardManager gameScoreBoardManager, PrefixManager prefixManager, PrefixRegistry prefixRegistry) {
        HideExecuter hideExecuter = new HideExecuter(rankingManager);
        ReloadExecuter reloadExecuter = new ReloadExecuter(zoneManager);
        HighScoreExecuter highScoreExecuter = new HighScoreExecuter(highScoreManager,rankingManager,zoneManager,gameScoreBoardManager);
        RoleExecuter roleExecuter = new RoleExecuter(prefixManager,prefixRegistry);
        ReloadRoleExecuter reloadRoleExecuter = new ReloadRoleExecuter(prefixRegistry,prefixManager);
        registrar.register(build(hideExecuter,reloadExecuter,highScoreExecuter,roleExecuter,prefixManager,prefixRegistry,reloadRoleExecuter));
        registrar.register(oldbuild(hideExecuter));
    }

    public static LiteralCommandNode<CommandSourceStack> build(HideExecuter hideExecuter, ReloadExecuter reloadExecuter, HighScoreExecuter highScoreExecuter, RoleExecuter roleExecuter, PrefixManager prefixManager, PrefixRegistry prefixRegistry,ReloadRoleExecuter reloadRoleExecuter) {
        return Commands.literal("afkscore")
                .then(HideExecuter.create(hideExecuter))
                .then(ReloadExecuter.create(reloadExecuter))
                .then(HighScoreExecuter.create(highScoreExecuter))
                .then(RoleExecuter.create(roleExecuter,prefixManager,prefixRegistry))
                .then(ReloadRoleExecuter.create(reloadRoleExecuter))
                .build();

    }

    public static LiteralCommandNode<CommandSourceStack> oldbuild(HideExecuter hideExecuter){
        return HideExecuter.oldCreate(hideExecuter).build();
    }

}
