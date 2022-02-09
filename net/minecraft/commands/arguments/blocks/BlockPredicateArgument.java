/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class BlockPredicateArgument
implements ArgumentType<Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
    static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(object -> new TranslatableComponent("arguments.block.tag.unknown", object));

    public static BlockPredicateArgument blockPredicate() {
        return new BlockPredicateArgument();
    }

    @Override
    public Result parse(StringReader stringReader) throws CommandSyntaxException {
        final BlockStateParser blockStateParser = new BlockStateParser(stringReader, true).parse(true);
        if (blockStateParser.getState() != null) {
            final BlockPredicate blockPredicate = new BlockPredicate(blockStateParser.getState(), blockStateParser.getProperties().keySet(), blockStateParser.getNbt());
            return new Result(){

                @Override
                public Predicate<BlockInWorld> create(Registry<Block> registry) {
                    return blockPredicate;
                }

                @Override
                public boolean requiresNbt() {
                    return blockPredicate.requiresNbt();
                }
            };
        }
        final TagKey<Block> tagKey = blockStateParser.getTag();
        return new Result(){

            @Override
            public Predicate<BlockInWorld> create(Registry<Block> registry) throws CommandSyntaxException {
                if (!registry.isKnownTagName(tagKey)) {
                    throw ERROR_UNKNOWN_TAG.create(tagKey);
                }
                return new TagPredicate(tagKey, blockStateParser.getVagueProperties(), blockStateParser.getNbt());
            }

            @Override
            public boolean requiresNbt() {
                return blockStateParser.getNbt() != null;
            }
        };
    }

    public static Predicate<BlockInWorld> getBlockPredicate(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return commandContext.getArgument(string, Result.class).create(commandContext.getSource().getServer().registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
        stringReader.setCursor(suggestionsBuilder.getStart());
        BlockStateParser blockStateParser = new BlockStateParser(stringReader, true);
        try {
            blockStateParser.parse(true);
        } catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return blockStateParser.fillSuggestions(suggestionsBuilder, Registry.BLOCK);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    static class BlockPredicate
    implements Predicate<BlockInWorld> {
        private final BlockState state;
        private final Set<Property<?>> properties;
        @Nullable
        private final CompoundTag nbt;

        public BlockPredicate(BlockState blockState, Set<Property<?>> set, @Nullable CompoundTag compoundTag) {
            this.state = blockState;
            this.properties = set;
            this.nbt = compoundTag;
        }

        @Override
        public boolean test(BlockInWorld blockInWorld) {
            BlockState blockState = blockInWorld.getState();
            if (!blockState.is(this.state.getBlock())) {
                return false;
            }
            for (Property<?> property : this.properties) {
                if (blockState.getValue(property) == this.state.getValue(property)) continue;
                return false;
            }
            if (this.nbt != null) {
                BlockEntity blockEntity = blockInWorld.getEntity();
                return blockEntity != null && NbtUtils.compareNbt(this.nbt, blockEntity.saveWithFullMetadata(), true);
            }
            return true;
        }

        public boolean requiresNbt() {
            return this.nbt != null;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((BlockInWorld)object);
        }
    }

    public static interface Result {
        public Predicate<BlockInWorld> create(Registry<Block> var1) throws CommandSyntaxException;

        public boolean requiresNbt();
    }

    static class TagPredicate
    implements Predicate<BlockInWorld> {
        private final TagKey<Block> tag;
        @Nullable
        private final CompoundTag nbt;
        private final Map<String, String> vagueProperties;

        TagPredicate(TagKey<Block> tagKey, Map<String, String> map, @Nullable CompoundTag compoundTag) {
            this.tag = tagKey;
            this.vagueProperties = map;
            this.nbt = compoundTag;
        }

        @Override
        public boolean test(BlockInWorld blockInWorld) {
            BlockState blockState = blockInWorld.getState();
            if (!blockState.is(this.tag)) {
                return false;
            }
            for (Map.Entry<String, String> entry : this.vagueProperties.entrySet()) {
                Property<?> property = blockState.getBlock().getStateDefinition().getProperty(entry.getKey());
                if (property == null) {
                    return false;
                }
                Comparable comparable = property.getValue(entry.getValue()).orElse(null);
                if (comparable == null) {
                    return false;
                }
                if (blockState.getValue(property) == comparable) continue;
                return false;
            }
            if (this.nbt != null) {
                BlockEntity blockEntity = blockInWorld.getEntity();
                return blockEntity != null && NbtUtils.compareNbt(this.nbt, blockEntity.saveWithFullMetadata(), true);
            }
            return true;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((BlockInWorld)object);
        }
    }
}

