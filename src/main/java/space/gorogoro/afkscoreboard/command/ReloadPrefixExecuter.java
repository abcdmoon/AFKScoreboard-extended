package space.gorogoro.afkscoreboard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import space.gorogoro.afkscoreboard.prefix.Prefix;
import space.gorogoro.afkscoreboard.prefix.PrefixManager;
import space.gorogoro.afkscoreboard.prefix.PrefixRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ReloadPrefixExecuter {
    private final PrefixRegistry prefixRegistry;
    private final PrefixManager prefixManager;


    public ReloadPrefixExecuter(PrefixRegistry prefixRegistry, PrefixManager prefixManager) {
        this.prefixRegistry = prefixRegistry;
        this.prefixManager = prefixManager;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(ReloadPrefixExecuter reloadPrefixExecuter) {
        return Commands.literal("reloadprefix")
                .requires(ctx->ctx.getSender().isOp())
                .executes(reloadPrefixExecuter::execute);
    }


    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Set<Prefix> oldPrefixes = new HashSet<>(prefixRegistry.getAllPrefixes());
        prefixRegistry.loadPrefixes();
        prefixManager.recreatePrefixes(oldPrefixes);
        if(ctx.getSource().getSender() instanceof Player player){
            player.sendMessage(Component.text("称号の情報をファイルから再読込しました"));
        }
        return Command.SINGLE_SUCCESS;
    }
}
