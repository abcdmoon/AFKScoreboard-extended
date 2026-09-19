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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import space.gorogoro.afkscoreboard.ScoreManager;

public class HideExecuter {

    private final ScoreManager scoreManager;

    HideExecuter(ScoreManager scoreManager) {
        this.scoreManager = scoreManager;
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(HideExecuter hideExecuter) {
        return Commands.literal("hide").executes(hideExecuter::execute);
    }

    /**
     * 既存プラグインと同様のコマンドの実装を返す
     */
    static LiteralArgumentBuilder<CommandSourceStack> oldCreate(HideExecuter hideExecuter) {
        return Commands.literal("afkhide").executes(hideExecuter::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(!(ctx.getSource().getSender() instanceof Player)){
            final Message message = MessageComponentSerializer.message().serialize(Component.text("このコマンドはプレイヤーのみ実行できます。").color(NamedTextColor.RED));
            throw new SimpleCommandExceptionType(message).create();
        }
    }
}
