/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ResourceOrTagLocationArgument<T>
implements ArgumentType<Result<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
    private static final DynamicCommandExceptionType ERROR_INVALID_BIOME = new DynamicCommandExceptionType(object -> Component.translatable("commands.locatebiome.invalid", object));
    private static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE = new DynamicCommandExceptionType(object -> Component.translatable("commands.locate.invalid", object));
    final ResourceKey<? extends Registry<T>> registryKey;

    public ResourceOrTagLocationArgument(ResourceKey<? extends Registry<T>> resourceKey) {
        this.registryKey = resourceKey;
    }

    public static <T> ResourceOrTagLocationArgument<T> resourceOrTag(ResourceKey<? extends Registry<T>> resourceKey) {
        return new ResourceOrTagLocationArgument<T>(resourceKey);
    }

    private static <T> Result<T> getRegistryType(CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey, DynamicCommandExceptionType dynamicCommandExceptionType) throws CommandSyntaxException {
        Result result = commandContext.getArgument(string, Result.class);
        Optional<Result<T>> optional = result.cast(resourceKey);
        return optional.orElseThrow(() -> dynamicCommandExceptionType.create(result));
    }

    public static Result<Biome> getBiome(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ResourceOrTagLocationArgument.getRegistryType(commandContext, string, Registry.BIOME_REGISTRY, ERROR_INVALID_BIOME);
    }

    public static Result<Structure> getStructure(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ResourceOrTagLocationArgument.getRegistryType(commandContext, string, Registry.STRUCTURE_REGISTRY, ERROR_INVALID_STRUCTURE);
    }

    @Override
    public Result<T> parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '#') {
            int i = stringReader.getCursor();
            try {
                stringReader.skip();
                ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
                return new TagResult(TagKey.create(this.registryKey, resourceLocation));
            } catch (CommandSyntaxException commandSyntaxException) {
                stringReader.setCursor(i);
                throw commandSyntaxException;
            }
        }
        ResourceLocation resourceLocation2 = ResourceLocation.read(stringReader);
        return new ResourceResult(ResourceKey.create(this.registryKey, resourceLocation2));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        S s = commandContext.getSource();
        if (s instanceof SharedSuggestionProvider) {
            SharedSuggestionProvider sharedSuggestionProvider = (SharedSuggestionProvider)s;
            return sharedSuggestionProvider.suggestRegistryElements(this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ALL, suggestionsBuilder, commandContext);
        }
        return suggestionsBuilder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static interface Result<T>
    extends Predicate<Holder<T>> {
        public Either<ResourceKey<T>, TagKey<T>> unwrap();

        public <E> Optional<Result<E>> cast(ResourceKey<? extends Registry<E>> var1);

        public String asPrintable();
    }

    record TagResult<T>(TagKey<T> key) implements Result<T>
    {
        @Override
        public Either<ResourceKey<T>, TagKey<T>> unwrap() {
            return Either.right(this.key);
        }

        @Override
        public <E> Optional<Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
            return this.key.cast(resourceKey).map(TagResult::new);
        }

        @Override
        public boolean test(Holder<T> holder) {
            return holder.is(this.key);
        }

        @Override
        public String asPrintable() {
            return "#" + this.key.location();
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Holder)object);
        }
    }

    record ResourceResult<T>(ResourceKey<T> key) implements Result<T>
    {
        @Override
        public Either<ResourceKey<T>, TagKey<T>> unwrap() {
            return Either.left(this.key);
        }

        @Override
        public <E> Optional<Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
            return this.key.cast(resourceKey).map(ResourceResult::new);
        }

        @Override
        public boolean test(Holder<T> holder) {
            return holder.is(this.key);
        }

        @Override
        public String asPrintable() {
            return this.key.location().toString();
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Holder)object);
        }
    }

    public static class Info<T>
    implements ArgumentTypeInfo<ResourceOrTagLocationArgument<T>, Template> {
        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeResourceLocation(template.registryKey.location());
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
            return new Template(ResourceKey.createRegistryKey(resourceLocation));
        }

        @Override
        public void serializeToJson(Template template, JsonObject jsonObject) {
            jsonObject.addProperty("registry", template.registryKey.location().toString());
        }

        @Override
        public Template unpack(ResourceOrTagLocationArgument<T> resourceOrTagLocationArgument) {
            return new Template(resourceOrTagLocationArgument.registryKey);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<ResourceOrTagLocationArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> resourceKey) {
                this.registryKey = resourceKey;
            }

            @Override
            public ResourceOrTagLocationArgument<T> instantiate(CommandBuildContext commandBuildContext) {
                return new ResourceOrTagLocationArgument(this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceOrTagLocationArgument<T>, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

