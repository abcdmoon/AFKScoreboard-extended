package space.gorogoro.afkscoreboard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import space.gorogoro.afkscoreboard.RankingManager;
import space.gorogoro.afkscoreboard.ScoreManager;
import space.gorogoro.afkscoreboard.ZoneManager;

import java.util.List;

public class CommandManager {
    private CommandManager(){}

    public static void registerCommands(Commands registrar, RankingManager rankingManager, ZoneManager zoneManager) {
        HideExecuter hideExecuter = new HideExecuter(rankingManager);
        ReloadExecuter reloadExecuter = new ReloadExecuter(zoneManager);
        registrar.register(build(hideExecuter,reloadExecuter));
        registrar.register(oldbuild(hideExecuter));
    }

    public static LiteralCommandNode<CommandSourceStack> build(HideExecuter hideExecuter,ReloadExecuter reloadExecuter) {
        return Commands.literal("afkscore")
                .then(HideExecuter.create(hideExecuter))
                .then(ReloadExecuter.create(reloadExecuter))
                .build();

    }

    public static LiteralCommandNode<CommandSourceStack> oldbuild(HideExecuter hideExecuter){
        return HideExecuter.oldCreate(hideExecuter).build();
    }

}
