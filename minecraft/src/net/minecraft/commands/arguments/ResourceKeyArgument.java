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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ResourceKeyArgument<T> implements ArgumentType<ResourceKey<T>> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
	private static final DynamicCommandExceptionType ERROR_INVALID_FEATURE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.place.feature.invalid", object)
	);
	private static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.place.structure.invalid", object)
	);
	private static final DynamicCommandExceptionType ERROR_INVALID_TEMPLATE_POOL = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.place.jigsaw.invalid", object)
	);
	final ResourceKey<? extends Registry<T>> registryKey;

	public ResourceKeyArgument(ResourceKey<? extends Registry<T>> resourceKey) {
		this.registryKey = resourceKey;
	}

	public static <T> ResourceKeyArgument<T> key(ResourceKey<? extends Registry<T>> resourceKey) {
		return new ResourceKeyArgument<>(resourceKey);
	}

	private static <T> ResourceKey<T> getRegistryKey(
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

	private static <T> Holder.Reference<T> resolveKey(
		CommandContext<CommandSourceStack> commandContext,
		String string,
		ResourceKey<Registry<T>> resourceKey,
		DynamicCommandExceptionType dynamicCommandExceptionType
	) throws CommandSyntaxException {
		ResourceKey<T> resourceKey2 = getRegistryKey(commandContext, string, resourceKey, dynamicCommandExceptionType);
		return (Holder.Reference<T>)getRegistry(commandContext, resourceKey)
			.getHolder(resourceKey2)
			.orElseThrow(() -> dynamicCommandExceptionType.create(resourceKey2.location()));
	}

	public static Holder.Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return resolveKey(commandContext, string, Registries.CONFIGURED_FEATURE, ERROR_INVALID_FEATURE);
	}

	public static Holder.Reference<Structure> getStructure(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return resolveKey(commandContext, string, Registries.STRUCTURE, ERROR_INVALID_STRUCTURE);
	}

	public static Holder.Reference<StructureTemplatePool> getStructureTemplatePool(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return resolveKey(commandContext, string, Registries.TEMPLATE_POOL, ERROR_INVALID_TEMPLATE_POOL);
	}

	public ResourceKey<T> parse(StringReader stringReader) throws CommandSyntaxException {
		ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
		return ResourceKey.create(this.registryKey, resourceLocation);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		Object var4 = commandContext.getSource();
		return var4 instanceof SharedSuggestionProvider sharedSuggestionProvider
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
		public void serializeToNetwork(ResourceKeyArgument.Info<T>.Template template, FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeResourceKey(template.registryKey);
		}

		public ResourceKeyArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
			return new ResourceKeyArgument.Info.Template(friendlyByteBuf.readRegistryKey());
		}

		public void serializeToJson(ResourceKeyArgument.Info<T>.Template template, JsonObject jsonObject) {
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
