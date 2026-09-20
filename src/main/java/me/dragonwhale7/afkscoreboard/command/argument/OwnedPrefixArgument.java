package me.dragonwhale7.afkscoreboard.command.argument;

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
import me.dragonwhale7.afkscoreboard.prefix.Prefix;
import me.dragonwhale7.afkscoreboard.prefix.PrefixManager;
import me.dragonwhale7.afkscoreboard.prefix.PrefixRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class OwnedPrefixArgument implements CustomArgumentType.Converted<Prefix,String> {

    private static final DynamicCommandExceptionType invalidPrefixOption = new DynamicCommandExceptionType(o-> MessageComponentSerializer.message().serialize(Component.text(o+"は無効なオプションです")));
    
    private final PrefixManager prefixManager;
    private final PrefixRegistry prefixRegistry;

    public OwnedPrefixArgument(PrefixManager prefixManager, PrefixRegistry prefixRegistry) {
        this.prefixManager = prefixManager;
        this.prefixRegistry = prefixRegistry;
    }

    @Override
    public Prefix convert(String nativeType) throws CommandSyntaxException {
        for(Prefix prefix:prefixRegistry.getAllPrefixes()){
            if(prefix.prefixText().equals(nativeType)){
                return prefix;
            }
        }
        throw invalidPrefixOption.create(nativeType);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if(context.getSource() instanceof CommandSourceStack ctx){
            if(ctx.getSender() instanceof Player p){
                for(Prefix prefix : prefixManager.getOwnedPrefixes(p.getUniqueId())){
                    builder.suggest(prefix.prefixText());
                }
            }
        }

        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}
