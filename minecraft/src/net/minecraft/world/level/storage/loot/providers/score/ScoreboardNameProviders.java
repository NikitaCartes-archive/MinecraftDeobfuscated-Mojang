package net.minecraft.world.level.storage.loot.providers.score;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;

public class ScoreboardNameProviders {
	public static final LootScoreProviderType FIXED = register("fixed", new FixedScoreboardNameProvider.Serializer());
	public static final LootScoreProviderType CONTEXT = register("context", new ContextScoreboardNameProvider.Serializer());

	private static LootScoreProviderType register(String string, Serializer<? extends ScoreboardNameProvider> serializer) {
		return Registry.register(Registry.LOOT_SCORE_PROVIDER_TYPE, new ResourceLocation(string), new LootScoreProviderType(serializer));
	}

	public static Object createGsonAdapter() {
		return GsonAdapterFactory.<ScoreboardNameProvider, LootScoreProviderType>builder(
				Registry.LOOT_SCORE_PROVIDER_TYPE, "provider", "type", ScoreboardNameProvider::getType
			)
			.withDefaultSerializer(CONTEXT, new ContextScoreboardNameProvider.DefaultSerializer())
			.build();
	}
}
