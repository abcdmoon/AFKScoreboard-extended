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
import space.gorogoro.afkscoreboard.data.PlayerDataManager;
import space.gorogoro.afkscoreboard.command.argument.OwnedPrefixArgument;
import space.gorogoro.afkscoreboard.prefix.Prefix;
import space.gorogoro.afkscoreboard.prefix.PrefixManager;
import space.gorogoro.afkscoreboard.prefix.PrefixRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

public class PrefixExecuter {

    private final PlayerDataManager playerDataManager;
    private final PrefixManager prefixManager;

    public PrefixExecuter(PrefixManager prefixManager, PlayerDataManager playerDataManager) {
        this.prefixManager = prefixManager;
        this.playerDataManager = playerDataManager;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(PrefixExecuter prefixExecuter, PlayerDataManager playerDataManager, PrefixRegistry prefixRegistry) {
        return Commands.literal("prefix")
                .then(Commands.argument("text",new OwnedPrefixArgument(playerDataManager,prefixRegistry))
                        .executes(prefixExecuter::showPrefix));
    }

    private int showPrefix(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(ctx.getSource().getSender() instanceof Player player){
            Prefix prefix = ctx.getArgument("text",Prefix.class);
            if(playerDataManager.getPlayerPrefixes(player.getUniqueId()).contains(prefix.key())){
                prefixManager.changePrefix(player.getUniqueId(),prefix);
                if(!playerDataManager.isHidingPrefix(player.getUniqueId())){
                    player.sendMessage(Component.text("表示する称号を").append(Component.text(prefix.prefixText()).decorate(TextDecoration.BOLD).color(prefix.color())).append(Component.text("にしました")));
                }
            }else{
                final Message message = MessageComponentSerializer.message().serialize(Component.text("その称号は所持していません").color(NamedTextColor.RED));
                throw new SimpleCommandExceptionType(message).create();
            }
            return Command.SINGLE_SUCCESS;
        }else {
            final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
            throw new SimpleCommandExceptionType(message).create();
        }
    }

}
