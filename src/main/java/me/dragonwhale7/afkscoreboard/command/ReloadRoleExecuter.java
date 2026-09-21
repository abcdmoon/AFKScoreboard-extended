package me.dragonwhale7.afkscoreboard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.dragonwhale7.afkscoreboard.prefix.Prefix;
import me.dragonwhale7.afkscoreboard.prefix.PrefixManager;
import me.dragonwhale7.afkscoreboard.prefix.PrefixRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ReloadRoleExecuter {
    private final PrefixRegistry prefixRegistry;
    private final PrefixManager prefixManager;


    public ReloadRoleExecuter(PrefixRegistry prefixRegistry, PrefixManager prefixManager) {
        this.prefixRegistry = prefixRegistry;
        this.prefixManager = prefixManager;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(ReloadRoleExecuter reloadRoleExecuter) {
        return Commands.literal("reloadrole")
                .requires(ctx->ctx.getSender().isOp())
                .executes(reloadRoleExecuter::execute);
    }


    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Set<Prefix> oldPrefixes = new HashSet<>(prefixRegistry.getAllPrefixes());
        prefixRegistry.loadPrefixes();
        prefixManager.reload(oldPrefixes);
        if(ctx.getSource().getSender() instanceof Player player){
            player.sendMessage(Component.text("称号の情報をファイルから再読込しました"));
        }
        return Command.SINGLE_SUCCESS;
    }
}
