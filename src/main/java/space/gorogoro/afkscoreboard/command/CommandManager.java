package space.gorogoro.afkscoreboard.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import space.gorogoro.afkscoreboard.cosmetic.CosmeticService;
import space.gorogoro.afkscoreboard.data.PlayerDataManager;
import space.gorogoro.afkscoreboard.RankingManager;
import space.gorogoro.afkscoreboard.ZoneManager;
import space.gorogoro.afkscoreboard.GameScoreBoardManager;
import space.gorogoro.afkscoreboard.EventManager;
import space.gorogoro.afkscoreboard.prefix.PrefixManager;
import space.gorogoro.afkscoreboard.prefix.PrefixRegistry;

public class CommandManager {
    private CommandManager(){}

    public static void registerCommands(Commands registrar, RankingManager rankingManager, ZoneManager zoneManager, PlayerDataManager playerDataManager, GameScoreBoardManager gameScoreBoardManager, PrefixManager prefixManager, PrefixRegistry prefixRegistry, EventManager eventManager, CosmeticService cosmeticService) {
        HideScoreExecuter hideScoreExecuter = new HideScoreExecuter(rankingManager,playerDataManager,cosmeticService);
        ReloadExecuter reloadExecuter = new ReloadExecuter(zoneManager,eventManager,prefixRegistry);
        HighScoreExecuter highScoreExecuter = new HighScoreExecuter(playerDataManager,rankingManager,zoneManager,gameScoreBoardManager);
        PrefixExecuter prefixExecuter = new PrefixExecuter(prefixManager,playerDataManager);
        HidePrefixExecuter hidePrefixExecuter = new HidePrefixExecuter(playerDataManager, prefixManager, prefixRegistry);
        ReloadPrefixExecuter reloadPrefixExecuter = new ReloadPrefixExecuter(prefixRegistry,prefixManager);
        AfkDebugExecuter afkDebugExecuter = new AfkDebugExecuter(cosmeticService);
        AfkLookExecuter afkLookExecuter = new AfkLookExecuter(cosmeticService);
        registrar.register(build(hideScoreExecuter,reloadExecuter,highScoreExecuter, prefixExecuter,playerDataManager,prefixRegistry, reloadPrefixExecuter,hidePrefixExecuter));
        registrar.register(oldbuild(hideScoreExecuter));
        registrar.register(AfkDebugExecuter.create(afkDebugExecuter).build());
        registrar.register(AfkLookExecuter.create(afkLookExecuter).build());
    }

    public static LiteralCommandNode<CommandSourceStack> build(HideScoreExecuter hideScoreExecuter, ReloadExecuter reloadExecuter, HighScoreExecuter highScoreExecuter, PrefixExecuter prefixExecuter, PlayerDataManager playerDataManager, PrefixRegistry prefixRegistry, ReloadPrefixExecuter reloadPrefixExecuter,HidePrefixExecuter hidePrefixExecuter) {
        return Commands.literal("afkscore")
                .then(HideScoreExecuter.create(hideScoreExecuter))
                .then(ReloadExecuter.create(reloadExecuter))
                .then(HighScoreExecuter.create(highScoreExecuter))
                .then(PrefixExecuter.create(prefixExecuter,playerDataManager,prefixRegistry))
                .then(HidePrefixExecuter.create(hidePrefixExecuter))
                .then(ReloadPrefixExecuter.create(reloadPrefixExecuter))
                .build();

    }

    public static LiteralCommandNode<CommandSourceStack> oldbuild(HideScoreExecuter hideScoreExecuter){
        return HideScoreExecuter.oldCreate(hideScoreExecuter).build();
    }

}
