package space.gorogoro.afkscoreboard.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import space.gorogoro.afkscoreboard.data.PlayerDataManager;
import space.gorogoro.afkscoreboard.prefix.Prefix;
import space.gorogoro.afkscoreboard.prefix.PrefixRegistry;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class OwnedPrefixArgument implements CustomArgumentType.Converted<Prefix,String> {

    private static final DynamicCommandExceptionType invalidPrefixOption = new DynamicCommandExceptionType(o-> MessageComponentSerializer.message().serialize(Component.text(o+"は無効なオプションです")));


    private final PrefixRegistry prefixRegistry;
    private final PlayerDataManager playerDataManager;

    public OwnedPrefixArgument(PlayerDataManager playerDataManager, PrefixRegistry prefixRegistry) {
        this.playerDataManager = playerDataManager;
        this.prefixRegistry = prefixRegistry;
    }

    @Override
    public Prefix convert(String nativeType) throws CommandSyntaxException {
        Prefix prefix = prefixRegistry.getPrefix(nativeType);
        if (prefix == null) {
            throw invalidPrefixOption.create(nativeType);
        }
        return prefix;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if(context.getSource() instanceof CommandSourceStack ctx){
            if(ctx.getSender() instanceof Player p){
                for(Prefix prefix : playerDataManager.getPlayerPrefixes(p.getUniqueId()).stream().map(prefixRegistry::getPrefix).filter(Objects::nonNull).toList()){
                    builder.suggest(prefix.key());
                }
            }
        }

        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }
}
