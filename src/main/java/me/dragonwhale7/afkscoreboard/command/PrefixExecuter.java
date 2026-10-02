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
import me.dragonwhale7.afkscoreboard.PlayerDataManager;
import me.dragonwhale7.afkscoreboard.command.argument.OwnedPrefixArgument;
import me.dragonwhale7.afkscoreboard.prefix.Prefix;
import me.dragonwhale7.afkscoreboard.prefix.PrefixManager;
import me.dragonwhale7.afkscoreboard.prefix.PrefixRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

public class PrefixExecuter {

    private final PrefixRegistry prefixRegistry;
    private final PlayerDataManager playerDataManager;
    private final PrefixManager prefixManager;

    public PrefixExecuter(PrefixManager prefixManager, PlayerDataManager playerDataManager, PrefixRegistry prefixRegistry) {
        this.prefixManager = prefixManager;
        this.playerDataManager = playerDataManager;
        this.prefixRegistry = prefixRegistry;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(PrefixExecuter prefixExecuter, PlayerDataManager playerDataManager, PrefixRegistry prefixRegistry) {
        return Commands.literal("prefix")
                .then(Commands.argument("text",new OwnedPrefixArgument(playerDataManager,prefixRegistry))
                        .executes(prefixExecuter::showPrefix))
                .then(Commands.literal("hide")
                        .executes(prefixExecuter::hidePrefix));
    }

    private int showPrefix(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(ctx.getSource().getSender() instanceof Player player){
            Prefix prefix = ctx.getArgument("text",Prefix.class);
            if(playerDataManager.getPlayerPrefixes(player.getUniqueId()).contains(prefix.key())){
                prefixManager.changePrefix(player.getUniqueId(),prefix);
                player.sendMessage(Component.text("表示する称号を").append(Component.text(prefix.prefixText()).decorate(TextDecoration.BOLD).color(prefix.color())).append(Component.text("にしました")));
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
