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
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class SuggestionProviders {
	private static final Map<ResourceLocation, SuggestionProvider<SharedSuggestionProvider>> PROVIDERS_BY_NAME = Maps.<ResourceLocation, SuggestionProvider<SharedSuggestionProvider>>newHashMap();
	private static final ResourceLocation DEFAULT_NAME = new ResourceLocation("ask_server");
	public static final SuggestionProvider<SharedSuggestionProvider> ASK_SERVER = register(
		DEFAULT_NAME, (commandContext, suggestionsBuilder) -> commandContext.getSource().customSuggestion(commandContext, suggestionsBuilder)
	);
	public static final SuggestionProvider<CommandSourceStack> ALL_RECIPES = register(
		new ResourceLocation("all_recipes"),
		(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(commandContext.getSource().getRecipeNames(), suggestionsBuilder)
	);
	public static final SuggestionProvider<CommandSourceStack> AVAILABLE_SOUNDS = register(
		new ResourceLocation("available_sounds"),
		(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(commandContext.getSource().getAvailableSoundEvents(), suggestionsBuilder)
	);
	public static final SuggestionProvider<CommandSourceStack> AVAILABLE_BIOMES = register(
		new ResourceLocation("available_biomes"),
		(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(
				commandContext.getSource().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).keySet(), suggestionsBuilder
			)
	);
	public static final SuggestionProvider<CommandSourceStack> SUMMONABLE_ENTITIES = register(
		new ResourceLocation("summonable_entities"),
		(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(
				Registry.ENTITY_TYPE.stream().filter(EntityType::canSummon),
				suggestionsBuilder,
				EntityType::getKey,
				entityType -> new TranslatableComponent(Util.makeDescriptionId("entity", EntityType.getKey(entityType)))
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

	public static class Wrapper implements SuggestionProvider<SharedSuggestionProvider> {
		private final SuggestionProvider<SharedSuggestionProvider> delegate;
		private final ResourceLocation name;

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
