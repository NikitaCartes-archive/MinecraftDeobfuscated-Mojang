package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class ScoreboardNameProviders {
	private static final Codec<ScoreboardNameProvider> TYPED_CODEC = BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE
		.byNameCodec()
		.dispatch(ScoreboardNameProvider::getType, LootScoreProviderType::codec);
	public static final Codec<ScoreboardNameProvider> CODEC = ExtraCodecs.lazyInitializedCodec(
		() -> Codec.either(ContextScoreboardNameProvider.INLINE_CODEC, TYPED_CODEC)
				.xmap(
					either -> either.map(Function.identity(), Function.identity()),
					scoreboardNameProvider -> scoreboardNameProvider instanceof ContextScoreboardNameProvider contextScoreboardNameProvider
							? Either.left(contextScoreboardNameProvider)
							: Either.right(scoreboardNameProvider)
				)
	);
	public static final LootScoreProviderType FIXED = register("fixed", FixedScoreboardNameProvider.CODEC);
	public static final LootScoreProviderType CONTEXT = register("context", ContextScoreboardNameProvider.CODEC);

	private static LootScoreProviderType register(String string, Codec<? extends ScoreboardNameProvider> codec) {
		return Registry.register(BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE, new ResourceLocation(string), new LootScoreProviderType(codec));
	}
}
