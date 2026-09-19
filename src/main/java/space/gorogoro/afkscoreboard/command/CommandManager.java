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

import java.util.List;

public class CommandManager {
    private CommandManager(){}

    public static void registerCommands(Commands registrar){
        registrar.register(build());
    }

    public static LiteralCommandNode<CommandSourceStack> build(){
        return Commands.literal("afkscore")
                .then(Commands.literal("hide").executes(ctx->{

                            return Command.SINGLE_SUCCESS;
                        }
                ))
                .build();

    }

}
