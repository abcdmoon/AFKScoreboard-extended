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
import space.gorogoro.afkscoreboard.cosmetic.CosmeticStore;

import java.util.List;
import java.util.Locale;

public class AfkLookExecuter {
    private final CosmeticService cosmetics;

    AfkLookExecuter(CosmeticService cosmetics) {
        this.cosmetics = cosmetics;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(AfkLookExecuter afkLookExecuter) {
        return Commands.literal("afklook")
                .then(Commands.argument("option", StringArgumentType.word())
                        .suggests((ctx,builder)->{
                            List.of("particle", "block", "mount", "all", "reset").forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(afkLookExecuter::handleLookCommand))
                .executes(ctx->{
                    if(ctx.getSource().getSender() instanceof Player player){
                        player.sendMessage("§f/afklook <particle|block|mount|all|reset>");
                        player.sendMessage("§7見た目を種類ごとに表示/非表示にします。§7particle §fパーティクル  §7block §fブロック  §7mount §f頭MOB  §7all §fすべて");
                        player.sendMessage("§7reset §f見た目をすべて外す（条件を満たしている見た目はすぐに引き直されます）");
                        return Command.SINGLE_SUCCESS;
                    }else {
                        final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
                        throw new SimpleCommandExceptionType(message).create();
                    }
                });
    }

    /**
     * /afklook。見た目の種類ごとに表示と非表示を切り替える。誰でも使える。設定は週をまたいで残る
     * reset は見た目をすべて外す(秒数は残すので、条件を満たしている見た目はすぐに引き直される)
     */
    private int handleLookCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(ctx.getSource().getSender() instanceof Player player){
            if (cosmetics == null) {
                final Message message = MessageComponentSerializer.message().serialize(Component.text("見た目ボーナスはまだ準備できていません。").color(NamedTextColor.RED));
                throw new SimpleCommandExceptionType(message).create();
            }

            List<CosmeticStore.Slot> slots;
            String label;
            switch (ctx.getArgument("option", String.class).toLowerCase(Locale.ROOT)) {
                case "particle" -> {
                    slots = List.of(CosmeticStore.Slot.PARTICLE);
                    label = "パーティクル";
                }
                case "block" -> {
                    slots = List.of(CosmeticStore.Slot.BLOCK);
                    label = "ブロック";
                }
                case "mount" -> {
                    slots = List.of(CosmeticStore.Slot.MOUNT);
                    label = "頭MOB";
                }
                case "all" -> {
                    slots = List.of(CosmeticStore.Slot.PARTICLE, CosmeticStore.Slot.BLOCK, CosmeticStore.Slot.MOUNT);
                    label = "すべての見た目";
                }
                case "reset" -> {
                    if (cosmetics.debugClear(player)) {
                        player.sendMessage("§f見た目を外しました。§7放置の秒数はそのままなので、条件を満たしている見た目はすぐに引き直されます。");
                    } else {
                        player.sendMessage("§7付与されている見た目はありません。");
                    }
                    return Command.SINGLE_SUCCESS;
                }
                default -> {
                    player.sendMessage("§f/afklook <particle|block|mount|all|reset>");
                    player.sendMessage("§7見た目を種類ごとに表示/非表示にします。§7particle §fパーティクル  §7block §fブロック  §7mount §f頭MOB  §7all §fすべて");
                    player.sendMessage("§7reset §f見た目をすべて外す（条件を満たしている見た目はすぐに引き直されます）");
                    return Command.SINGLE_SUCCESS;
                }
            }
            if (cosmetics.toggleLook(player, slots)) {
                player.sendMessage("§f" + label + "を§a非表示§fにしました。§7もう一度実行すると表示に戻ります。");
            } else {
                player.sendMessage("§f" + label + "を§a表示§fするようにしました。");
            }
            return Command.SINGLE_SUCCESS;
        }else {
            final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
            throw new SimpleCommandExceptionType(message).create();
        }

    }

}
