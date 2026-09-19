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

import java.util.List;

public class CommandManager {
    private CommandManager(){}

    public static void registerCommands(Commands registrar, RankingManager rankingManager){
        HideExecuter hideExecuter = new HideExecuter(rankingManager);
        registrar.register(build(hideExecuter));
        registrar.register(oldbuild(hideExecuter));
    }

    public static LiteralCommandNode<CommandSourceStack> build(HideExecuter hideExecuter){
        return Commands.literal("afkscore")
                .then(HideExecuter.create(hideExecuter))
                .build();

    }

    public static LiteralCommandNode<CommandSourceStack> oldbuild(HideExecuter hideExecuter){
        return HideExecuter.oldCreate(hideExecuter).build();
    }

}
