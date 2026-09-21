package me.dragonwhale7.afkscoreboard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.dragonwhale7.afkscoreboard.ZoneManager;
import me.dragonwhale7.afkscoreboard.event.EventManager;

public class ReloadExecuter {

    private final ZoneManager zoneManager;
    private final EventManager eventManager;

    public ReloadExecuter(ZoneManager zoneManager, EventManager eventManager) {
        this.zoneManager = zoneManager;
        this.eventManager = eventManager;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(ReloadExecuter reloadExecuter) {
        return Commands.literal("reload")
                .requires(ctx->ctx.getSender().isOp())
                .executes(reloadExecuter::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        zoneManager.reloadAxAFKZones(eventManager);
        return Command.SINGLE_SUCCESS;
    }
}
