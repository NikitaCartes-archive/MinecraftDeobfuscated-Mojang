/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.providers.score;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.FixedScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;

public class ScoreboardNameProviders {
    public static final LootScoreProviderType FIXED = ScoreboardNameProviders.register("fixed", new FixedScoreboardNameProvider.Serializer());
    public static final LootScoreProviderType CONTEXT = ScoreboardNameProviders.register("context", new ContextScoreboardNameProvider.Serializer());

    private static LootScoreProviderType register(String string, Serializer<? extends ScoreboardNameProvider> serializer) {
        return Registry.register(Registry.LOOT_SCORE_PROVIDER_TYPE, new ResourceLocation(string), new LootScoreProviderType(serializer));
    }

    public static Object createGsonAdapter() {
        return GsonAdapterFactory.builder(Registry.LOOT_SCORE_PROVIDER_TYPE, "provider", "type", ScoreboardNameProvider::getType).withInlineSerializer(CONTEXT, new ContextScoreboardNameProvider.InlineSerializer()).build();
    }
}

