/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments.blocks;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class BlockStateParser {
    public static final SimpleCommandExceptionType ERROR_NO_TAGS_ALLOWED = new SimpleCommandExceptionType(new TranslatableComponent("argument.block.tag.disallowed"));
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_BLOCK = new DynamicCommandExceptionType(object -> new TranslatableComponent("argument.block.id.invalid", object));
    public static final Dynamic2CommandExceptionType ERROR_UNKNOWN_PROPERTY = new Dynamic2CommandExceptionType((object, object2) -> new TranslatableComponent("argument.block.property.unknown", object, object2));
    public static final Dynamic2CommandExceptionType ERROR_DUPLICATE_PROPERTY = new Dynamic2CommandExceptionType((object, object2) -> new TranslatableComponent("argument.block.property.duplicate", object2, object));
    public static final Dynamic3CommandExceptionType ERROR_INVALID_VALUE = new Dynamic3CommandExceptionType((object, object2, object3) -> new TranslatableComponent("argument.block.property.invalid", object, object3, object2));
    public static final Dynamic2CommandExceptionType ERROR_EXPECTED_VALUE = new Dynamic2CommandExceptionType((object, object2) -> new TranslatableComponent("argument.block.property.novalue", object, object2));
    public static final SimpleCommandExceptionType ERROR_EXPECTED_END_OF_PROPERTIES = new SimpleCommandExceptionType(new TranslatableComponent("argument.block.property.unclosed"));
    private static final char SYNTAX_START_PROPERTIES = '[';
    private static final char SYNTAX_START_NBT = '{';
    private static final char SYNTAX_END_PROPERTIES = ']';
    private static final char SYNTAX_EQUALS = '=';
    private static final char SYNTAX_PROPERTY_SEPARATOR = ',';
    private static final char SYNTAX_TAG = '#';
    private static final BiFunction<SuggestionsBuilder, TagCollection<Block>, CompletableFuture<Suggestions>> SUGGEST_NOTHING = (suggestionsBuilder, tagCollection) -> suggestionsBuilder.buildFuture();
    private final StringReader reader;
    private final boolean forTesting;
    private final Map<Property<?>, Comparable<?>> properties = Maps.newHashMap();
    private final Map<String, String> vagueProperties = Maps.newHashMap();
    private ResourceLocation id = new ResourceLocation("");
    private StateDefinition<Block, BlockState> definition;
    private BlockState state;
    @Nullable
    private CompoundTag nbt;
    private ResourceLocation tag = new ResourceLocation("");
    private int tagCursor;
    private BiFunction<SuggestionsBuilder, TagCollection<Block>, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

    public BlockStateParser(StringReader stringReader, boolean bl) {
        this.reader = stringReader;
        this.forTesting = bl;
    }

    public Map<Property<?>, Comparable<?>> getProperties() {
        return this.properties;
    }

    @Nullable
    public BlockState getState() {
        return this.state;
    }

    @Nullable
    public CompoundTag getNbt() {
        return this.nbt;
    }

    @Nullable
    public ResourceLocation getTag() {
        return this.tag;
    }

    public BlockStateParser parse(boolean bl) throws CommandSyntaxException {
        this.suggestions = this::suggestBlockIdOrTag;
        if (this.reader.canRead() && this.reader.peek() == '#') {
            this.readTag();
            this.suggestions = this::suggestOpenVaguePropertiesOrNbt;
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.readVagueProperties();
                this.suggestions = this::suggestOpenNbt;
            }
        } else {
            this.readBlock();
            this.suggestions = this::suggestOpenPropertiesOrNbt;
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.readProperties();
                this.suggestions = this::suggestOpenNbt;
            }
        }
        if (bl && this.reader.canRead() && this.reader.peek() == '{') {
            this.suggestions = SUGGEST_NOTHING;
            this.readNbt();
        }
        return this;
    }

    private CompletableFuture<Suggestions> suggestPropertyNameOrEnd(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection) {
        if (suggestionsBuilder.getRemaining().isEmpty()) {
            suggestionsBuilder.suggest(String.valueOf(']'));
        }
        return this.suggestPropertyName(suggestionsBuilder, tagCollection);
    }

    private CompletableFuture<Suggestions> suggestVaguePropertyNameOrEnd(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection) {
        if (suggestionsBuilder.getRemaining().isEmpty()) {
            suggestionsBuilder.suggest(String.valueOf(']'));
        }
        return this.suggestVaguePropertyName(suggestionsBuilder, tagCollection);
    }

    private CompletableFuture<Suggestions> suggestPropertyName(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (Property<?> property : this.state.getProperties()) {
            if (this.properties.containsKey(property) || !property.getName().startsWith(string)) continue;
            suggestionsBuilder.suggest(property.getName() + '=');
        }
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestVaguePropertyName(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection) {
        Tag<Block> tag;
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        if (this.tag != null && !this.tag.getPath().isEmpty() && (tag = tagCollection.getTag(this.tag)) != null) {
            for (Block block : tag.getValues()) {
                for (Property<?> property : block.getStateDefinition().getProperties()) {
                    if (this.vagueProperties.containsKey(property.getName()) || !property.getName().startsWith(string)) continue;
                    suggestionsBuilder.suggest(property.getName() + '=');
                }
            }
        }
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOpenNbt(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection) {
        if (suggestionsBuilder.getRemaining().isEmpty() && this.hasBlockEntity(tagCollection)) {
            suggestionsBuilder.suggest(String.valueOf('{'));
        }
        return suggestionsBuilder.buildFuture();
    }

    private boolean hasBlockEntity(TagCollection<Block> tagCollection) {
        Tag<Block> tag;
        if (this.state != null) {
            return this.state.hasBlockEntity();
        }
        if (this.tag != null && (tag = tagCollection.getTag(this.tag)) != null) {
            for (Block block : tag.getValues()) {
                if (!block.defaultBlockState().hasBlockEntity()) continue;
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<Suggestions> suggestEquals(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection) {
        if (suggestionsBuilder.getRemaining().isEmpty()) {
            suggestionsBuilder.suggest(String.valueOf('='));
        }
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestNextPropertyOrEnd(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection) {
        if (suggestionsBuilder.getRemaining().isEmpty()) {
            suggestionsBuilder.suggest(String.valueOf(']'));
        }
        if (suggestionsBuilder.getRemaining().isEmpty() && this.properties.size() < this.state.getProperties().size()) {
            suggestionsBuilder.suggest(String.valueOf(','));
        }
        return suggestionsBuilder.buildFuture();
    }

    private static <T extends Comparable<T>> SuggestionsBuilder addSuggestions(SuggestionsBuilder suggestionsBuilder, Property<T> property) {
        for (Comparable comparable : property.getPossibleValues()) {
            if (comparable instanceof Integer) {
                suggestionsBuilder.suggest((Integer)comparable);
                continue;
            }
            suggestionsBuilder.suggest(property.getName(comparable));
        }
        return suggestionsBuilder;
    }

    private CompletableFuture<Suggestions> suggestVaguePropertyValue(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection, String string) {
        Tag<Block> tag;
        boolean bl = false;
        if (this.tag != null && !this.tag.getPath().isEmpty() && (tag = tagCollection.getTag(this.tag)) != null) {
            block0: for (Block block : tag.getValues()) {
                Property<?> property = block.getStateDefinition().getProperty(string);
                if (property != null) {
                    BlockStateParser.addSuggestions(suggestionsBuilder, property);
                }
                if (bl) continue;
                for (Property<?> property2 : block.getStateDefinition().getProperties()) {
                    if (this.vagueProperties.containsKey(property2.getName())) continue;
                    bl = true;
                    continue block0;
                }
            }
        }
        if (bl) {
            suggestionsBuilder.suggest(String.valueOf(','));
        }
        suggestionsBuilder.suggest(String.valueOf(']'));
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOpenVaguePropertiesOrNbt(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection) {
        Tag<Block> tag;
        if (suggestionsBuilder.getRemaining().isEmpty() && (tag = tagCollection.getTag(this.tag)) != null) {
            Block block;
            boolean bl = false;
            boolean bl2 = false;
            Iterator<Block> iterator = tag.getValues().iterator();
            while (!(!iterator.hasNext() || (bl |= !(block = iterator.next()).getStateDefinition().getProperties().isEmpty()) && (bl2 |= block.defaultBlockState().hasBlockEntity()))) {
            }
            if (bl) {
                suggestionsBuilder.suggest(String.valueOf('['));
            }
            if (bl2) {
                suggestionsBuilder.suggest(String.valueOf('{'));
            }
        }
        return this.suggestTag(suggestionsBuilder, tagCollection);
    }

    private CompletableFuture<Suggestions> suggestOpenPropertiesOrNbt(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection) {
        if (suggestionsBuilder.getRemaining().isEmpty()) {
            if (!this.state.getBlock().getStateDefinition().getProperties().isEmpty()) {
                suggestionsBuilder.suggest(String.valueOf('['));
            }
            if (this.state.hasBlockEntity()) {
                suggestionsBuilder.suggest(String.valueOf('{'));
            }
        }
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection) {
        return SharedSuggestionProvider.suggestResource(tagCollection.getAvailableTags(), suggestionsBuilder.createOffset(this.tagCursor).add(suggestionsBuilder));
    }

    private CompletableFuture<Suggestions> suggestBlockIdOrTag(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection) {
        if (this.forTesting) {
            SharedSuggestionProvider.suggestResource(tagCollection.getAvailableTags(), suggestionsBuilder, String.valueOf('#'));
        }
        SharedSuggestionProvider.suggestResource(Registry.BLOCK.keySet(), suggestionsBuilder);
        return suggestionsBuilder.buildFuture();
    }

    public void readBlock() throws CommandSyntaxException {
        int i = this.reader.getCursor();
        this.id = ResourceLocation.read(this.reader);
        Block block = Registry.BLOCK.getOptional(this.id).orElseThrow(() -> {
            this.reader.setCursor(i);
            return ERROR_UNKNOWN_BLOCK.createWithContext(this.reader, this.id.toString());
        });
        this.definition = block.getStateDefinition();
        this.state = block.defaultBlockState();
    }

    public void readTag() throws CommandSyntaxException {
        if (!this.forTesting) {
            throw ERROR_NO_TAGS_ALLOWED.create();
        }
        this.suggestions = this::suggestTag;
        this.reader.expect('#');
        this.tagCursor = this.reader.getCursor();
        this.tag = ResourceLocation.read(this.reader);
    }

    public void readProperties() throws CommandSyntaxException {
        this.reader.skip();
        this.suggestions = this::suggestPropertyNameOrEnd;
        this.reader.skipWhitespace();
        while (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int i = this.reader.getCursor();
            String string = this.reader.readString();
            Property<?> property = this.definition.getProperty(string);
            if (property == null) {
                this.reader.setCursor(i);
                throw ERROR_UNKNOWN_PROPERTY.createWithContext(this.reader, this.id.toString(), string);
            }
            if (this.properties.containsKey(property)) {
                this.reader.setCursor(i);
                throw ERROR_DUPLICATE_PROPERTY.createWithContext(this.reader, this.id.toString(), string);
            }
            this.reader.skipWhitespace();
            this.suggestions = this::suggestEquals;
            if (!this.reader.canRead() || this.reader.peek() != '=') {
                throw ERROR_EXPECTED_VALUE.createWithContext(this.reader, this.id.toString(), string);
            }
            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = (suggestionsBuilder, tagCollection) -> BlockStateParser.addSuggestions(suggestionsBuilder, property).buildFuture();
            int j = this.reader.getCursor();
            this.setValue(property, this.reader.readString(), j);
            this.suggestions = this::suggestNextPropertyOrEnd;
            this.reader.skipWhitespace();
            if (!this.reader.canRead()) continue;
            if (this.reader.peek() == ',') {
                this.reader.skip();
                this.suggestions = this::suggestPropertyName;
                continue;
            }
            if (this.reader.peek() == ']') break;
            throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
        }
        if (!this.reader.canRead()) {
            throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
        }
        this.reader.skip();
    }

    public void readVagueProperties() throws CommandSyntaxException {
        this.reader.skip();
        this.suggestions = this::suggestVaguePropertyNameOrEnd;
        int i = -1;
        this.reader.skipWhitespace();
        while (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int j = this.reader.getCursor();
            String string = this.reader.readString();
            if (this.vagueProperties.containsKey(string)) {
                this.reader.setCursor(j);
                throw ERROR_DUPLICATE_PROPERTY.createWithContext(this.reader, this.id.toString(), string);
            }
            this.reader.skipWhitespace();
            if (!this.reader.canRead() || this.reader.peek() != '=') {
                this.reader.setCursor(j);
                throw ERROR_EXPECTED_VALUE.createWithContext(this.reader, this.id.toString(), string);
            }
            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = (suggestionsBuilder, tagCollection) -> this.suggestVaguePropertyValue((SuggestionsBuilder)suggestionsBuilder, (TagCollection<Block>)tagCollection, string);
            i = this.reader.getCursor();
            String string2 = this.reader.readString();
            this.vagueProperties.put(string, string2);
            this.reader.skipWhitespace();
            if (!this.reader.canRead()) continue;
            i = -1;
            if (this.reader.peek() == ',') {
                this.reader.skip();
                this.suggestions = this::suggestVaguePropertyName;
                continue;
            }
            if (this.reader.peek() == ']') break;
            throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
        }
        if (!this.reader.canRead()) {
            if (i >= 0) {
                this.reader.setCursor(i);
            }
            throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
        }
        this.reader.skip();
    }

    public void readNbt() throws CommandSyntaxException {
        this.nbt = new TagParser(this.reader).readStruct();
    }

    private <T extends Comparable<T>> void setValue(Property<T> property, String string, int i) throws CommandSyntaxException {
        Optional<T> optional = property.getValue(string);
        if (!optional.isPresent()) {
            this.reader.setCursor(i);
            throw ERROR_INVALID_VALUE.createWithContext(this.reader, this.id.toString(), property.getName(), string);
        }
        this.state = (BlockState)this.state.setValue(property, (Comparable)optional.get());
        this.properties.put(property, (Comparable<?>)optional.get());
    }

    public static String serialize(BlockState blockState) {
        StringBuilder stringBuilder = new StringBuilder(Registry.BLOCK.getKey(blockState.getBlock()).toString());
        if (!blockState.getProperties().isEmpty()) {
            stringBuilder.append('[');
            boolean bl = false;
            for (Map.Entry entry : blockState.getValues().entrySet()) {
                if (bl) {
                    stringBuilder.append(',');
                }
                BlockStateParser.appendProperty(stringBuilder, (Property)entry.getKey(), (Comparable)entry.getValue());
                bl = true;
            }
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    private static <T extends Comparable<T>> void appendProperty(StringBuilder stringBuilder, Property<T> property, Comparable<?> comparable) {
        stringBuilder.append(property.getName());
        stringBuilder.append('=');
        stringBuilder.append(property.getName(comparable));
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder, TagCollection<Block> tagCollection) {
        return this.suggestions.apply(suggestionsBuilder.createOffset(this.reader.getCursor()), tagCollection);
    }

    public Map<String, String> getVagueProperties() {
        return this.vagueProperties;
    }
}

