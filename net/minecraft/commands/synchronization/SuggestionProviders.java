/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
    private static final Map<ResourceLocation, SuggestionProvider<SharedSuggestionProvider>> PROVIDERS_BY_NAME = Maps.newHashMap();
    private static final ResourceLocation DEFAULT_NAME = new ResourceLocation("ask_server");
    public static final SuggestionProvider<SharedSuggestionProvider> ASK_SERVER = SuggestionProviders.register(DEFAULT_NAME, (commandContext, suggestionsBuilder) -> ((SharedSuggestionProvider)commandContext.getSource()).customSuggestion(commandContext));
    public static final SuggestionProvider<CommandSourceStack> ALL_RECIPES = SuggestionProviders.register(new ResourceLocation("all_recipes"), (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(((SharedSuggestionProvider)commandContext.getSource()).getRecipeNames(), suggestionsBuilder));
    public static final SuggestionProvider<CommandSourceStack> AVAILABLE_SOUNDS = SuggestionProviders.register(new ResourceLocation("available_sounds"), (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(((SharedSuggestionProvider)commandContext.getSource()).getAvailableSoundEvents(), suggestionsBuilder));
    public static final SuggestionProvider<CommandSourceStack> SUMMONABLE_ENTITIES = SuggestionProviders.register(new ResourceLocation("summonable_entities"), (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.stream().filter(EntityType::canSummon), suggestionsBuilder, EntityType::getKey, entityType -> new TranslatableComponent(Util.makeDescriptionId("entity", EntityType.getKey(entityType)))));

    public static <S extends SharedSuggestionProvider> SuggestionProvider<S> register(ResourceLocation resourceLocation, SuggestionProvider<SharedSuggestionProvider> suggestionProvider) {
        if (PROVIDERS_BY_NAME.containsKey(resourceLocation)) {
            throw new IllegalArgumentException("A command suggestion provider is already registered with the name " + resourceLocation);
        }
        PROVIDERS_BY_NAME.put(resourceLocation, suggestionProvider);
        return new Wrapper(resourceLocation, suggestionProvider);
    }

    public static SuggestionProvider<SharedSuggestionProvider> getProvider(ResourceLocation resourceLocation) {
        return PROVIDERS_BY_NAME.getOrDefault(resourceLocation, ASK_SERVER);
    }

    public static ResourceLocation getName(SuggestionProvider<SharedSuggestionProvider> suggestionProvider) {
        if (suggestionProvider instanceof Wrapper) {
            return ((Wrapper)suggestionProvider).name;
        }
        return DEFAULT_NAME;
    }

    public static SuggestionProvider<SharedSuggestionProvider> safelySwap(SuggestionProvider<SharedSuggestionProvider> suggestionProvider) {
        if (suggestionProvider instanceof Wrapper) {
            return suggestionProvider;
        }
        return ASK_SERVER;
    }

    protected static class Wrapper
    implements SuggestionProvider<SharedSuggestionProvider> {
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

