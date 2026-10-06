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
import space.gorogoro.afkscoreboard.RankingManager;
import space.gorogoro.afkscoreboard.cosmetic.CosmeticService;

public class HideScoreExecuter {


    private final RankingManager rankingManager;
    private final PlayerDataManager playerDataManager;
    private final CosmeticService cosmeticService;

    HideScoreExecuter(RankingManager rankingManager, PlayerDataManager playerDataManager, CosmeticService cosmeticService) {
        this.rankingManager = rankingManager;
        this.playerDataManager = playerDataManager;
        this.cosmeticService = cosmeticService;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(HideScoreExecuter hideScoreExecuter) {
        return Commands.literal("hidescore").executes(hideScoreExecuter::execute);
    }

    /**
     * 既存プラグインと同様のコマンドの実装を返す
     */
    static LiteralArgumentBuilder<CommandSourceStack> oldCreate(HideScoreExecuter hideScoreExecuter) {
        return Commands.literal("afkhide").executes(hideScoreExecuter::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(ctx.getSource().getSender() instanceof Player player){
            rankingManager.toggleHidden(player.getUniqueId());
            cosmeticService.refresh(player);
            if(playerDataManager.isHiddenInRank(player.getUniqueId())){
                player.sendMessage(Component.text("放置ランキングからあなたを").append(Component.text("非表示").color(NamedTextColor.GREEN)).append(Component.text("にしました")));
            }else{
                player.sendMessage(Component.text("放置ランキングにあなたを").append(Component.text("表示").color(NamedTextColor.GREEN)).append(Component.text("するようにしました")));
            }
            return Command.SINGLE_SUCCESS;
        }else {
            final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
            throw new SimpleCommandExceptionType(message).create();
        }
    }
}
