package space.gorogoro.afkscoreboard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import space.gorogoro.afkscoreboard.data.PlayerDataManager;
import space.gorogoro.afkscoreboard.prefix.PrefixManager;
import space.gorogoro.afkscoreboard.prefix.PrefixRegistry;

public class HidePrefixExecuter {
    private final PlayerDataManager playerDataManager;
    private final PrefixManager prefixManager;
    private final PrefixRegistry prefixRegistry;

    HidePrefixExecuter(PlayerDataManager playerDataManager, PrefixManager prefixManager, PrefixRegistry prefixRegistry) {
        this.playerDataManager = playerDataManager;
        this.prefixManager = prefixManager;
        this.prefixRegistry = prefixRegistry;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(HidePrefixExecuter hidePrefixExecuter) {
        return Commands.literal("hideprefix").executes(hidePrefixExecuter::hidePrefix);
    }

    private int hidePrefix(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(ctx.getSource().getSender() instanceof Player player){
            if(!playerDataManager.isHidingPrefix(player.getUniqueId())){
                playerDataManager.setHidingPrefix(player.getUniqueId(),true);
                player.sendMessage(Component.text("あなたの称号を").append(Component.text("非表示").color(NamedTextColor.GREEN)).append(Component.text("にしました")));
            }else{
                playerDataManager.setHidingPrefix(player.getUniqueId(),false);
                player.sendMessage(Component.text("あなたの称号を").append(Component.text("表示").color(NamedTextColor.GREEN)).append(Component.text("するようにしました")));
            }
            prefixManager.changePrefix(player.getUniqueId(),prefixRegistry.getPrefix(playerDataManager.getShowedPrefix(player.getUniqueId())));
            return Command.SINGLE_SUCCESS;
        }else {
            final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
            throw new SimpleCommandExceptionType(message).create();
        }
    }
}
