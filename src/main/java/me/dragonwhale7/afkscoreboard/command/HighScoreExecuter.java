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
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.dragonwhale7.afkscoreboard.*;
import me.dragonwhale7.afkscoreboard.prefix.GameScoreBoardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class HighScoreExecuter {

    private final HighScoreManager highScoreManager;
    private final RankingManager rankingManager;
    private final ZoneManager zoneManager;
    private final GameScoreBoardManager gameScoreBoardManager;

    public HighScoreExecuter(HighScoreManager highScoreManager, RankingManager rankingManager, ZoneManager zoneManager, GameScoreBoardManager gameScoreBoardManager) {
        this.highScoreManager = highScoreManager;
        this.rankingManager = rankingManager;
        this.zoneManager = zoneManager;
        this.gameScoreBoardManager = gameScoreBoardManager;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(HighScoreExecuter highScoreExecuter) {
        return Commands.literal("highscore")
                .executes(highScoreExecuter::showOwnScore)
                .then(Commands.argument("target",ArgumentTypes.player())
                        .requires(ctx->ctx.getSender().isOp())
                        .executes(highScoreExecuter::showScore))
                .then(Commands.literal("rank")
                        .executes(highScoreExecuter::showRank));
    }

    private int showOwnScore(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(ctx.getSource().getSender() instanceof Player player){
            player.sendMessage(Component.text("現在のハイスコア:").append(Component.text(Util.formatTimeCompact(highScoreManager.getHighScore(player.getUniqueId()))).color(NamedTextColor.AQUA)));
            return Command.SINGLE_SUCCESS;
        }else{
            final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
            throw new SimpleCommandExceptionType(message).create();
        }
    }

    private int showScore(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(ctx.getSource().getSender() instanceof Player player){
            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
            Player target = targetResolver.resolve(ctx.getSource()).getFirst();
            player.sendMessage(Component.text("現在の"+target.getName()+"のハイスコア:").append(Component.text(Util.formatTimeCompact(highScoreManager.getHighScore(target.getUniqueId()))).color(NamedTextColor.AQUA)));
            return Command.SINGLE_SUCCESS;
        }else {
            final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
            throw new SimpleCommandExceptionType(message).create();
        }
    }

    private int showRank(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(ctx.getSource().getSender() instanceof Player player){

            //要するにハイスコアのスコアボードを見られて、コマンドまたは時間経過で今いる場所にあった表示に戻る
            if(player.getScoreboard().equals(gameScoreBoardManager.getScoreboard(GameScoreBoardManager.ScoreboardType.HIGHSCORE))){
                if(zoneManager.isLocInAnyZone(player.getLocation())){
                    gameScoreBoardManager.showScoreboard(player, GameScoreBoardManager.ScoreboardType.SCORE);
                }else{
                    gameScoreBoardManager.showScoreboard(player, GameScoreBoardManager.ScoreboardType.MAIN);
                }
            }else{
                rankingManager.updateHighScoreScoreBoard();
                gameScoreBoardManager.showScoreboard(player, GameScoreBoardManager.ScoreboardType.HIGHSCORE);
                AFKScoreboard.registerTaskLater(()->{
                    if(!player.isOnline()){return;}
                    if(player.getScoreboard().equals(gameScoreBoardManager.getScoreboard(GameScoreBoardManager.ScoreboardType.HIGHSCORE))){
                        if(zoneManager.isLocInAnyZone(player.getLocation())){
                            gameScoreBoardManager.showScoreboard(player, GameScoreBoardManager.ScoreboardType.SCORE);
                        }else{
                            gameScoreBoardManager.showScoreboard(player, GameScoreBoardManager.ScoreboardType.MAIN);
                        }
                    }
                },60L);
            }
            return Command.SINGLE_SUCCESS;
        }else {
            final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
            throw new SimpleCommandExceptionType(message).create();
        }
    }
}
