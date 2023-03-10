/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class GameProfileArgument
implements ArgumentType<Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "dd12be42-52a9-4a91-a8a1-11c01849e498", "@e");
    public static final SimpleCommandExceptionType ERROR_UNKNOWN_PLAYER = new SimpleCommandExceptionType(Component.translatable("argument.player.unknown"));

    public static Collection<GameProfile> getGameProfiles(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return commandContext.getArgument(string, Result.class).getNames(commandContext.getSource());
    }

    public static GameProfileArgument gameProfile() {
        return new GameProfileArgument();
    }

    @Override
    public Result parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '@') {
            EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader);
            EntitySelector entitySelector = entitySelectorParser.parse();
            if (entitySelector.includesEntities()) {
                throw EntityArgument.ERROR_ONLY_PLAYERS_ALLOWED.create();
            }
            return new SelectorResult(entitySelector);
        }
        int i = stringReader.getCursor();
        while (stringReader.canRead() && stringReader.peek() != ' ') {
            stringReader.skip();
        }
        String string = stringReader.getString().substring(i, stringReader.getCursor());
        return commandSourceStack -> {
            Optional<GameProfile> optional = commandSourceStack.getServer().getProfileCache().get(string);
            return Collections.singleton(optional.orElseThrow(ERROR_UNKNOWN_PLAYER::create));
        };
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder2) {
        if (commandContext.getSource() instanceof SharedSuggestionProvider) {
            StringReader stringReader = new StringReader(suggestionsBuilder2.getInput());
            stringReader.setCursor(suggestionsBuilder2.getStart());
            EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader);
            try {
                entitySelectorParser.parse();
            } catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
            return entitySelectorParser.fillSuggestions(suggestionsBuilder2, suggestionsBuilder -> SharedSuggestionProvider.suggest(((SharedSuggestionProvider)commandContext.getSource()).getOnlinePlayerNames(), suggestionsBuilder));
        }
        return Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    @FunctionalInterface
    public static interface Result {
        public Collection<GameProfile> getNames(CommandSourceStack var1) throws CommandSyntaxException;
    }

    public static class SelectorResult
    implements Result {
        private final EntitySelector selector;

        public SelectorResult(EntitySelector entitySelector) {
            this.selector = entitySelector;
        }

        @Override
        public Collection<GameProfile> getNames(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
            List<ServerPlayer> list = this.selector.findPlayers(commandSourceStack);
            if (list.isEmpty()) {
                throw EntityArgument.NO_PLAYERS_FOUND.create();
            }
            ArrayList<GameProfile> list2 = Lists.newArrayList();
            for (ServerPlayer serverPlayer : list) {
                list2.add(serverPlayer.getGameProfile());
            }
            return list2;
        }
    }
}

