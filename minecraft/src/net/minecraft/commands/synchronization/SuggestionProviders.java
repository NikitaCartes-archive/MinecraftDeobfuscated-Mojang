package net.minecraft.commands.synchronization;

import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class SuggestionProviders {
	private static final Map<ResourceLocation, SuggestionProvider<SharedSuggestionProvider>> PROVIDERS_BY_NAME = Maps.<ResourceLocation, SuggestionProvider<SharedSuggestionProvider>>newHashMap();
	private static final ResourceLocation DEFAULT_NAME = ResourceLocation.withDefaultNamespace("ask_server");
	public static final SuggestionProvider<SharedSuggestionProvider> ASK_SERVER = register(
		DEFAULT_NAME, (commandContext, suggestionsBuilder) -> commandContext.getSource().customSuggestion(commandContext)
	);
	public static final SuggestionProvider<CommandSourceStack> AVAILABLE_SOUNDS = register(
		ResourceLocation.withDefaultNamespace("available_sounds"),
		(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(commandContext.getSource().getAvailableSounds(), suggestionsBuilder)
	);
	public static final SuggestionProvider<CommandSourceStack> SUMMONABLE_ENTITIES = register(
		ResourceLocation.withDefaultNamespace("summonable_entities"),
		(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(
				BuiltInRegistries.ENTITY_TYPE.stream().filter(entityType -> entityType.isEnabled(commandContext.getSource().enabledFeatures()) && entityType.canSummon()),
				suggestionsBuilder,
				EntityType::getKey,
				entityType -> Component.translatable(Util.makeDescriptionId("entity", EntityType.getKey(entityType)))
			)
	);

	public static <S extends SharedSuggestionProvider> SuggestionProvider<S> register(
		ResourceLocation resourceLocation, SuggestionProvider<SharedSuggestionProvider> suggestionProvider
	) {
		if (PROVIDERS_BY_NAME.containsKey(resourceLocation)) {
			throw new IllegalArgumentException("A command suggestion provider is already registered with the name " + resourceLocation);
		} else {
			PROVIDERS_BY_NAME.put(resourceLocation, suggestionProvider);
			return new SuggestionProviders.Wrapper(resourceLocation, suggestionProvider);
		}
	}

	public static SuggestionProvider<SharedSuggestionProvider> getProvider(ResourceLocation resourceLocation) {
		return (SuggestionProvider<SharedSuggestionProvider>)PROVIDERS_BY_NAME.getOrDefault(resourceLocation, ASK_SERVER);
	}

	public static ResourceLocation getName(SuggestionProvider<SharedSuggestionProvider> suggestionProvider) {
		return suggestionProvider instanceof SuggestionProviders.Wrapper ? ((SuggestionProviders.Wrapper)suggestionProvider).name : DEFAULT_NAME;
	}

	public static SuggestionProvider<SharedSuggestionProvider> safelySwap(SuggestionProvider<SharedSuggestionProvider> suggestionProvider) {
		return suggestionProvider instanceof SuggestionProviders.Wrapper ? suggestionProvider : ASK_SERVER;
	}

	protected static class Wrapper implements SuggestionProvider<SharedSuggestionProvider> {
		private final SuggestionProvider<SharedSuggestionProvider> delegate;
		final ResourceLocation name;

		public Wrapper(ResourceLocation resourceLocation, SuggestionProvider<SharedSuggestionProvider> suggestionProvider) {
			this.delegate = suggestionProvider;
			this.name = resourceLocation;
		}

		@Override
		public CompletableFuture<Suggestions> getSuggestions(CommandContext<SharedSuggestionProvider> commandContext, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
			return this.delegate.getSuggestions(commandContext, suggestionsBuilder);
		}
	}
}
