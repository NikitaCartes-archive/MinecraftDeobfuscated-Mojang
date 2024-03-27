package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ScoreboardNameProviders {
	private static final Codec<ScoreboardNameProvider> TYPED_CODEC = BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE
		.byNameCodec()
		.dispatch(ScoreboardNameProvider::getType, LootScoreProviderType::codec);
	public static final Codec<ScoreboardNameProvider> CODEC = Codec.lazyInitialized(
		() -> Codec.either(ContextScoreboardNameProvider.INLINE_CODEC, TYPED_CODEC)
				.xmap(
					Either::unwrap,
					scoreboardNameProvider -> scoreboardNameProvider instanceof ContextScoreboardNameProvider contextScoreboardNameProvider
							? Either.left(contextScoreboardNameProvider)
							: Either.right(scoreboardNameProvider)
				)
	);
	public static final LootScoreProviderType FIXED = register("fixed", FixedScoreboardNameProvider.CODEC);
	public static final LootScoreProviderType CONTEXT = register("context", ContextScoreboardNameProvider.CODEC);

	private static LootScoreProviderType register(String string, MapCodec<? extends ScoreboardNameProvider> mapCodec) {
		return Registry.register(BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE, new ResourceLocation(string), new LootScoreProviderType(mapCodec));
	}
}
