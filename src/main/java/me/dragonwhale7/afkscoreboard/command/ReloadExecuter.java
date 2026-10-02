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
import me.dragonwhale7.afkscoreboard.ZoneManager;
import me.dragonwhale7.afkscoreboard.event.EventManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class ReloadExecuter {

    private final ZoneManager zoneManager;
    private final EventManager eventManager;

    public ReloadExecuter(ZoneManager zoneManager,EventManager eventManager) {
        this.zoneManager = zoneManager;
        this.eventManager = eventManager;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(ReloadExecuter reloadExecuter) {
        return Commands.literal("reload")
                .requires(ctx->ctx.getSender().isOp())
                .executes(reloadExecuter::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(zoneManager.reloadAxAFKZones(eventManager)){
            if(ctx.getSource().getSender() instanceof Player player) {
                player.sendMessage(Component.text("放置エリアの情報を再読込しました"));
            }
            return Command.SINGLE_SUCCESS;
        }else {
            final Message message = MessageComponentSerializer.message().serialize(Component.text("放置エリアの情報の読み込みに失敗しました").color(NamedTextColor.RED));
            throw new SimpleCommandExceptionType(message).create();
        }
    }
}
