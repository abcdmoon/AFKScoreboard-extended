package space.gorogoro.afkscoreboard.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import space.gorogoro.afkscoreboard.cosmetic.CosmeticService;

import java.util.List;
import java.util.Locale;

class AfkDebugExecuter {

    private final CosmeticService cosmetics;
    AfkDebugExecuter(CosmeticService cosmetics) {
        this.cosmetics = cosmetics;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(AfkDebugExecuter afkDebugExecuter) {
        return Commands.literal("afkdebug")
                .requires(ctx->ctx.getSender().isOp())
                .then(Commands.argument("option",StringArgumentType.word())
                        .suggests((ctx,builder)->{
                            List.of("particle", "block", "mount", "all").forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(afkDebugExecuter::handleDebugCommand))
                .executes(ctx->{
                    if(ctx.getSource().getSender() instanceof Player player){
                        player.sendMessage("§f/afklook <particle|block|mount|all|reset>");
                        player.sendMessage("§7見た目を種類ごとに表示/非表示にします。§7particle §fパーティクル  §7block §fブロック  §7mount §f頭MOB  §7all §fすべて");
                        player.sendMessage("§7reset §f見た目をすべて外す（条件を満たしている見た目はすぐに引き直されます）");
                        return 0;
                    }else {
                        final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
                        throw new SimpleCommandExceptionType(message).create();
                    }
                });
    }

    /**
     * 見た目ボーナスを待たずに付与する。OP のみ。放置秒数は変えない。
     */
    private int handleDebugCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(ctx.getSource().getSender() instanceof Player player){
            if (cosmetics == null) {
                final Message message = MessageComponentSerializer.message().serialize(Component.text("見た目ボーナスはまだ準備できていません。").color(NamedTextColor.RED));
                throw new SimpleCommandExceptionType(message).create();
            }

            switch (ctx.getArgument("option", String.class)) {
                case "particle", "30m", "30" -> sendDebugGrant(player, cosmetics.debugGrant(player, true, false, false));
                case "block", "1h", "60" -> sendDebugGrant(player, cosmetics.debugGrant(player, false, true, false));
                case "mount", "3h", "180" -> sendDebugGrant(player, cosmetics.debugGrant(player, false, false, true));
                case "all" -> sendDebugGrant(player, cosmetics.debugGrant(player, true, true, true));
                default ->{
                    final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
                    throw new SimpleCommandExceptionType(message).create();
                }
            }

            return Command.SINGLE_SUCCESS;
        }else {
            final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
            throw new SimpleCommandExceptionType(message).create();
        }

    }

    private void sendDebugGrant(Player player, CosmeticService.DebugGrant grant) {
        sendDebugLine(player, "30分（パーティクル）", grant.particle, grant.particleNew);
        sendDebugLine(player, "1時間（ブロック）", grant.block, grant.blockNew);
        sendDebugLine(player, "3時間（頭MOB）", grant.mount, grant.mountNew);
        if (grant.hidden) {
            player.sendMessage("§7/afkhide で非表示中なので、見た目は表示しません。");
        } else if (grant.concealed) {
            player.sendMessage("§7スペクテイター・バニッシュ中なので、見た目は表示しません。");
        } else if (grant.inZone) {
            player.sendMessage("§7ゾーン内なので、この場に表示しました。");
        } else {
            player.sendMessage("§7今はゾーン外です。放置ゾーンに入ると表示されます。");
        }
    }
    private void sendDebugLine(Player player, String label, String kind, boolean fresh) {
        if (kind == null) {
            return;
        }
        if (fresh) {
            player.sendMessage("§f" + label + "を付与しました: §a" + kind);
        } else {
            player.sendMessage("§f" + label + "は付与済みです: §a" + kind);
        }
    }

}
