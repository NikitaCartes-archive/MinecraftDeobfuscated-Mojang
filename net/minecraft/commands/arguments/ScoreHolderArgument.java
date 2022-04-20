/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class ScoreHolderArgument
implements ArgumentType<Result> {
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_SCORE_HOLDERS = (commandContext, suggestionsBuilder2) -> {
        StringReader stringReader = new StringReader(suggestionsBuilder2.getInput());
        stringReader.setCursor(suggestionsBuilder2.getStart());
        EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader);
        try {
            entitySelectorParser.parse();
        } catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return entitySelectorParser.fillSuggestions(suggestionsBuilder2, suggestionsBuilder -> SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getOnlinePlayerNames(), suggestionsBuilder));
    };
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "*", "@e");
    private static final SimpleCommandExceptionType ERROR_NO_RESULTS = new SimpleCommandExceptionType(Component.translatable("argument.scoreHolder.empty"));
    final boolean multiple;

    public ScoreHolderArgument(boolean bl) {
        this.multiple = bl;
    }

    public static String getName(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ScoreHolderArgument.getNames(commandContext, string).iterator().next();
    }

    public static Collection<String> getNames(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ScoreHolderArgument.getNames(commandContext, string, Collections::emptyList);
    }

    public static Collection<String> getNamesWithDefaultWildcard(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ScoreHolderArgument.getNames(commandContext, string, commandContext.getSource().getServer().getScoreboard()::getTrackedPlayers);
    }

    public static Collection<String> getNames(CommandContext<CommandSourceStack> commandContext, String string, Supplier<Collection<String>> supplier) throws CommandSyntaxException {
        Collection<String> collection = commandContext.getArgument(string, Result.class).getNames(commandContext.getSource(), supplier);
        if (collection.isEmpty()) {
            throw EntityArgument.NO_ENTITIES_FOUND.create();
        }
        return collection;
    }

    public static ScoreHolderArgument scoreHolder() {
        return new ScoreHolderArgument(false);
    }

    public static ScoreHolderArgument scoreHolders() {
        return new ScoreHolderArgument(true);
    }

    @Override
    public Result parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '@') {
            EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader);
            EntitySelector entitySelector = entitySelectorParser.parse();
            if (!this.multiple && entitySelector.getMaxResults() > 1) {
                throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
            }
            return new SelectorResult(entitySelector);
        }
        int i = stringReader.getCursor();
        while (stringReader.canRead() && stringReader.peek() != ' ') {
            stringReader.skip();
        }
        String string = stringReader.getString().substring(i, stringReader.getCursor());
        if (string.equals("*")) {
            return (commandSourceStack, supplier) -> {
                Collection collection = (Collection)supplier.get();
                if (collection.isEmpty()) {
                    throw ERROR_NO_RESULTS.create();
                }
                return collection;
            };
        }
        Set<String> collection = Collections.singleton(string);
        return (commandSourceStack, supplier) -> collection;
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
        public Collection<String> getNames(CommandSourceStack var1, Supplier<Collection<String>> var2) throws CommandSyntaxException;
    }

    public static class SelectorResult
    implements Result {
        private final EntitySelector selector;

        public SelectorResult(EntitySelector entitySelector) {
            this.selector = entitySelector;
        }

        @Override
        public Collection<String> getNames(CommandSourceStack commandSourceStack, Supplier<Collection<String>> supplier) throws CommandSyntaxException {
            List<? extends Entity> list = this.selector.findEntities(commandSourceStack);
            if (list.isEmpty()) {
                throw EntityArgument.NO_ENTITIES_FOUND.create();
            }
            ArrayList<String> list2 = Lists.newArrayList();
            for (Entity entity : list) {
                list2.add(entity.getScoreboardName());
            }
            return list2;
        }
    }

    public static class Info
    implements ArgumentTypeInfo<ScoreHolderArgument, Template> {
        private static final byte FLAG_MULTIPLE = 1;

        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
            int i = 0;
            if (template.multiple) {
                i |= 1;
            }
            friendlyByteBuf.writeByte(i);
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            byte b = friendlyByteBuf.readByte();
            boolean bl = (b & 1) != 0;
            return new Template(bl);
        }

        @Override
        public void serializeToJson(Template template, JsonObject jsonObject) {
            jsonObject.addProperty("amount", template.multiple ? "multiple" : "single");
        }

        @Override
        public Template unpack(ScoreHolderArgument scoreHolderArgument) {
            return new Template(scoreHolderArgument.multiple);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<ScoreHolderArgument> {
            final boolean multiple;

            Template(boolean bl) {
                this.multiple = bl;
            }

            @Override
            public ScoreHolderArgument instantiate(CommandBuildContext commandBuildContext) {
                return new ScoreHolderArgument(this.multiple);
            }

            @Override
            public ArgumentTypeInfo<ScoreHolderArgument, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

