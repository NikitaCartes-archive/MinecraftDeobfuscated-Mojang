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
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class ResourceKeyArgument<T>
implements ArgumentType<ResourceKey<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ATTRIBUTE = new DynamicCommandExceptionType(object -> new TranslatableComponent("attribute.unknown", object));
    private static final DynamicCommandExceptionType ERROR_INVALID_FEATURE = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.placefeature.invalid", object));
    final ResourceKey<? extends Registry<T>> registryKey;

    public ResourceKeyArgument(ResourceKey<? extends Registry<T>> resourceKey) {
        this.registryKey = resourceKey;
    }

    public static <T> ResourceKeyArgument<T> key(ResourceKey<? extends Registry<T>> resourceKey) {
        return new ResourceKeyArgument<T>(resourceKey);
    }

    private static <T> ResourceKey<T> getRegistryType(CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey, DynamicCommandExceptionType dynamicCommandExceptionType) throws CommandSyntaxException {
        ResourceKey resourceKey2 = commandContext.getArgument(string, ResourceKey.class);
        Optional<ResourceKey<T>> optional = resourceKey2.cast(resourceKey);
        return optional.orElseThrow(() -> dynamicCommandExceptionType.create(resourceKey2));
    }

    private static <T> Registry<T> getRegistry(CommandContext<CommandSourceStack> commandContext, ResourceKey<? extends Registry<T>> resourceKey) {
        return commandContext.getSource().getServer().registryAccess().registryOrThrow(resourceKey);
    }

    public static Attribute getAttribute(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        ResourceKey resourceKey = ResourceKeyArgument.getRegistryType(commandContext, string, Registry.ATTRIBUTE_REGISTRY, ERROR_UNKNOWN_ATTRIBUTE);
        return ResourceKeyArgument.getRegistry(commandContext, Registry.ATTRIBUTE_REGISTRY).getOptional(resourceKey).orElseThrow(() -> ERROR_UNKNOWN_ATTRIBUTE.create(resourceKey.location()));
    }

    public static Holder<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        ResourceKey resourceKey = ResourceKeyArgument.getRegistryType(commandContext, string, Registry.CONFIGURED_FEATURE_REGISTRY, ERROR_INVALID_FEATURE);
        return ResourceKeyArgument.getRegistry(commandContext, Registry.CONFIGURED_FEATURE_REGISTRY).getHolder(resourceKey).orElseThrow(() -> ERROR_INVALID_FEATURE.create(resourceKey.location()));
    }

    @Override
    public ResourceKey<T> parse(StringReader stringReader) throws CommandSyntaxException {
        ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
        return ResourceKey.create(this.registryKey, resourceLocation);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        S s = commandContext.getSource();
        if (s instanceof SharedSuggestionProvider) {
            SharedSuggestionProvider sharedSuggestionProvider = (SharedSuggestionProvider)s;
            return sharedSuggestionProvider.suggestRegistryElements(this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS, suggestionsBuilder, commandContext);
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

    public static class Info<T>
    implements ArgumentTypeInfo<ResourceKeyArgument<T>, Template> {
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
        public Template unpack(ResourceKeyArgument<T> resourceKeyArgument) {
            return new Template(resourceKeyArgument.registryKey);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<ResourceKeyArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> resourceKey) {
                this.registryKey = resourceKey;
            }

            @Override
            public ResourceKeyArgument<T> instantiate(CommandBuildContext commandBuildContext) {
                return new ResourceKeyArgument(this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceKeyArgument<T>, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

