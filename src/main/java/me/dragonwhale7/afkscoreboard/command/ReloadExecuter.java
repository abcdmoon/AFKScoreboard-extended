package me.dragonwhale7.afkscoreboard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.dragonwhale7.afkscoreboard.ZoneManager;
import me.dragonwhale7.afkscoreboard.event.EventManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ReloadExecuter {

    private final ZoneManager zoneManager;

    public ReloadExecuter(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(ReloadExecuter reloadExecuter) {
        return Commands.literal("reload")
                .requires(ctx->ctx.getSender().isOp())
                .executes(reloadExecuter::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        zoneManager.reloadAxAFKZones();
        if(ctx.getSource().getSender() instanceof Player player) {
            player.sendMessage(Component.text("放置エリアの情報を再読込しました"));
        }
        return Command.SINGLE_SUCCESS;
    }
}
