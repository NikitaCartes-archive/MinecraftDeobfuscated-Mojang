/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EntityArgument
implements ArgumentType<EntitySelector> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "@e", "@e[type=foo]", "dd12be42-52a9-4a91-a8a1-11c01849e498");
    public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_ENTITY = new SimpleCommandExceptionType(Component.translatable("argument.entity.toomany"));
    public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_PLAYER = new SimpleCommandExceptionType(Component.translatable("argument.player.toomany"));
    public static final SimpleCommandExceptionType ERROR_ONLY_PLAYERS_ALLOWED = new SimpleCommandExceptionType(Component.translatable("argument.player.entities"));
    public static final SimpleCommandExceptionType NO_ENTITIES_FOUND = new SimpleCommandExceptionType(Component.translatable("argument.entity.notfound.entity"));
    public static final SimpleCommandExceptionType NO_PLAYERS_FOUND = new SimpleCommandExceptionType(Component.translatable("argument.entity.notfound.player"));
    public static final SimpleCommandExceptionType ERROR_SELECTORS_NOT_ALLOWED = new SimpleCommandExceptionType(Component.translatable("argument.entity.selector.not_allowed"));
    final boolean single;
    final boolean playersOnly;

    protected EntityArgument(boolean bl, boolean bl2) {
        this.single = bl;
        this.playersOnly = bl2;
    }

    public static EntityArgument entity() {
        return new EntityArgument(true, false);
    }

    public static Entity getEntity(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return commandContext.getArgument(string, EntitySelector.class).findSingleEntity(commandContext.getSource());
    }

    public static EntityArgument entities() {
        return new EntityArgument(false, false);
    }

    public static Collection<? extends Entity> getEntities(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        Collection<? extends Entity> collection = EntityArgument.getOptionalEntities(commandContext, string);
        if (collection.isEmpty()) {
            throw NO_ENTITIES_FOUND.create();
        }
        return collection;
    }

    public static Collection<? extends Entity> getOptionalEntities(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return commandContext.getArgument(string, EntitySelector.class).findEntities(commandContext.getSource());
    }

    public static Collection<ServerPlayer> getOptionalPlayers(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return commandContext.getArgument(string, EntitySelector.class).findPlayers(commandContext.getSource());
    }

    public static EntityArgument player() {
        return new EntityArgument(true, true);
    }

    public static ServerPlayer getPlayer(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return commandContext.getArgument(string, EntitySelector.class).findSinglePlayer(commandContext.getSource());
    }

    public static EntityArgument players() {
        return new EntityArgument(false, true);
    }

    public static Collection<ServerPlayer> getPlayers(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        List<ServerPlayer> list = commandContext.getArgument(string, EntitySelector.class).findPlayers(commandContext.getSource());
        if (list.isEmpty()) {
            throw NO_PLAYERS_FOUND.create();
        }
        return list;
    }

    @Override
    public EntitySelector parse(StringReader stringReader) throws CommandSyntaxException {
        boolean i = false;
        EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader);
        EntitySelector entitySelector = entitySelectorParser.parse();
        if (entitySelector.getMaxResults() > 1 && this.single) {
            if (this.playersOnly) {
                stringReader.setCursor(0);
                throw ERROR_NOT_SINGLE_PLAYER.createWithContext(stringReader);
            }
            stringReader.setCursor(0);
            throw ERROR_NOT_SINGLE_ENTITY.createWithContext(stringReader);
        }
        if (entitySelector.includesEntities() && this.playersOnly && !entitySelector.isSelfSelector()) {
            stringReader.setCursor(0);
            throw ERROR_ONLY_PLAYERS_ALLOWED.createWithContext(stringReader);
        }
        return entitySelector;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder2) {
        S s = commandContext.getSource();
        if (s instanceof SharedSuggestionProvider) {
            SharedSuggestionProvider sharedSuggestionProvider = (SharedSuggestionProvider)s;
            StringReader stringReader = new StringReader(suggestionsBuilder2.getInput());
            stringReader.setCursor(suggestionsBuilder2.getStart());
            EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader, sharedSuggestionProvider.hasPermission(2));
            try {
                entitySelectorParser.parse();
            } catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
            return entitySelectorParser.fillSuggestions(suggestionsBuilder2, suggestionsBuilder -> {
                Collection<String> collection = sharedSuggestionProvider.getOnlinePlayerNames();
                Collection<String> iterable = this.playersOnly ? collection : Iterables.concat(collection, sharedSuggestionProvider.getSelectedEntities());
                SharedSuggestionProvider.suggest(iterable, suggestionsBuilder);
            });
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

    public static class Info
    implements ArgumentTypeInfo<EntityArgument, Template> {
        private static final byte FLAG_SINGLE = 1;
        private static final byte FLAG_PLAYERS_ONLY = 2;

        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
            int i = 0;
            if (template.single) {
                i |= 1;
            }
            if (template.playersOnly) {
                i |= 2;
            }
            friendlyByteBuf.writeByte(i);
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            byte b = friendlyByteBuf.readByte();
            return new Template((b & 1) != 0, (b & 2) != 0);
        }

        @Override
        public void serializeToJson(Template template, JsonObject jsonObject) {
            jsonObject.addProperty("amount", template.single ? "single" : "multiple");
            jsonObject.addProperty("type", template.playersOnly ? "players" : "entities");
        }

        @Override
        public Template unpack(EntityArgument entityArgument) {
            return new Template(entityArgument.single, entityArgument.playersOnly);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<EntityArgument> {
            final boolean single;
            final boolean playersOnly;

            Template(boolean bl, boolean bl2) {
                this.single = bl;
                this.playersOnly = bl2;
            }

            @Override
            public EntityArgument instantiate(CommandBuildContext commandBuildContext) {
                return new EntityArgument(this.single, this.playersOnly);
            }

            @Override
            public ArgumentTypeInfo<EntityArgument, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

