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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class ResourceKeyArgument<T> implements ArgumentType<ResourceKey<T>> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_ATTRIBUTE = new DynamicCommandExceptionType(
		object -> Component.translatable("attribute.unknown", object)
	);
	private static final DynamicCommandExceptionType ERROR_INVALID_FEATURE = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.placefeature.invalid", object)
	);
	final ResourceKey<? extends Registry<T>> registryKey;

	public ResourceKeyArgument(ResourceKey<? extends Registry<T>> resourceKey) {
		this.registryKey = resourceKey;
	}

	public static <T> ResourceKeyArgument<T> key(ResourceKey<? extends Registry<T>> resourceKey) {
		return new ResourceKeyArgument<>(resourceKey);
	}

	private static <T> ResourceKey<T> getRegistryType(
		CommandContext<CommandSourceStack> commandContext,
		String string,
		ResourceKey<Registry<T>> resourceKey,
		DynamicCommandExceptionType dynamicCommandExceptionType
	) throws CommandSyntaxException {
		ResourceKey<?> resourceKey2 = commandContext.getArgument(string, ResourceKey.class);
		Optional<ResourceKey<T>> optional = resourceKey2.cast(resourceKey);
		return (ResourceKey<T>)optional.orElseThrow(() -> dynamicCommandExceptionType.create(resourceKey2));
	}

	private static <T> Registry<T> getRegistry(CommandContext<CommandSourceStack> commandContext, ResourceKey<? extends Registry<T>> resourceKey) {
		return commandContext.getSource().getServer().registryAccess().registryOrThrow(resourceKey);
	}

	public static Attribute getAttribute(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		ResourceKey<Attribute> resourceKey = getRegistryType(commandContext, string, Registry.ATTRIBUTE_REGISTRY, ERROR_UNKNOWN_ATTRIBUTE);
		return (Attribute)getRegistry(commandContext, Registry.ATTRIBUTE_REGISTRY)
			.getOptional(resourceKey)
			.orElseThrow(() -> ERROR_UNKNOWN_ATTRIBUTE.create(resourceKey.location()));
	}

	public static Holder<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		ResourceKey<ConfiguredFeature<?, ?>> resourceKey = getRegistryType(commandContext, string, Registry.CONFIGURED_FEATURE_REGISTRY, ERROR_INVALID_FEATURE);
		return (Holder<ConfiguredFeature<?, ?>>)getRegistry(commandContext, Registry.CONFIGURED_FEATURE_REGISTRY)
			.getHolder(resourceKey)
			.orElseThrow(() -> ERROR_INVALID_FEATURE.create(resourceKey.location()));
	}

	public ResourceKey<T> parse(StringReader stringReader) throws CommandSyntaxException {
		ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
		return ResourceKey.create(this.registryKey, resourceLocation);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return commandContext.getSource() instanceof SharedSuggestionProvider sharedSuggestionProvider
			? sharedSuggestionProvider.suggestRegistryElements(
				this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS, suggestionsBuilder, commandContext
			)
			: suggestionsBuilder.buildFuture();
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static class Info<T> implements ArgumentTypeInfo<ResourceKeyArgument<T>, ResourceKeyArgument.Info<T>.Template> {
		public void serializeToNetwork(ResourceKeyArgument.Info.Template template, FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeResourceLocation(template.registryKey.location());
		}

		public ResourceKeyArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
			ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
			return new ResourceKeyArgument.Info.Template(ResourceKey.createRegistryKey(resourceLocation));
		}

		public void serializeToJson(ResourceKeyArgument.Info.Template template, JsonObject jsonObject) {
			jsonObject.addProperty("registry", template.registryKey.location().toString());
		}

		public ResourceKeyArgument.Info<T>.Template unpack(ResourceKeyArgument<T> resourceKeyArgument) {
			return new ResourceKeyArgument.Info.Template(resourceKeyArgument.registryKey);
		}

		public final class Template implements ArgumentTypeInfo.Template<ResourceKeyArgument<T>> {
			final ResourceKey<? extends Registry<T>> registryKey;

			Template(ResourceKey<? extends Registry<T>> resourceKey) {
				this.registryKey = resourceKey;
			}

			public ResourceKeyArgument<T> instantiate(CommandBuildContext commandBuildContext) {
				return new ResourceKeyArgument<>(this.registryKey);
			}

			@Override
			public ArgumentTypeInfo<ResourceKeyArgument<T>, ?> type() {
				return Info.this;
			}
		}
	}
}
