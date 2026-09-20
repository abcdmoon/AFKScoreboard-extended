package me.dragonwhale7.afkscoreboard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import me.dragonwhale7.afkscoreboard.command.argument.OwnedPrefixArgument;
import me.dragonwhale7.afkscoreboard.prefix.Prefix;
import me.dragonwhale7.afkscoreboard.prefix.PrefixManager;
import me.dragonwhale7.afkscoreboard.prefix.PrefixRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class RoleExecuter {

    private final PrefixManager prefixManager;
    private final PrefixRegistry prefixRegistry;

    public RoleExecuter(PrefixManager prefixManager,PrefixRegistry prefixRegistry) {
        this.prefixManager = prefixManager;
        this.prefixRegistry = prefixRegistry;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(RoleExecuter roleExecuter, PrefixManager prefixManager, PrefixRegistry prefixRegistry) {
        return Commands.literal("role")
                .then(Commands.argument("text",new OwnedPrefixArgument(prefixManager,prefixRegistry))
                        .executes(roleExecuter::showRole))
                .then(Commands.literal("hide")
                        .executes(roleExecuter::hideRole))
                .then(Commands.literal("load")
                        .requires(ctx->ctx.getSender().isOp())
                        .executes(roleExecuter::loadRole));
    }

    private int showRole(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(ctx.getSource().getSender() instanceof Player player){
            Prefix prefix = ctx.getArgument("text",Prefix.class);
            prefixManager.changePrefix(player.getUniqueId(),prefix);
            return Command.SINGLE_SUCCESS;
        }else {
            final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
            throw new SimpleCommandExceptionType(message).create();
        }
    }

    private int hideRole(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(ctx.getSource().getSender() instanceof Player player){
            prefixManager.toggleHidden(player.getUniqueId());
            return Command.SINGLE_SUCCESS;
        }else {
            final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
            throw new SimpleCommandExceptionType(message).create();
        }
    }

    private int loadRole(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        prefixRegistry.loadPrefixes();
        prefixManager.reload();
        return Command.SINGLE_SUCCESS;
    }
}
