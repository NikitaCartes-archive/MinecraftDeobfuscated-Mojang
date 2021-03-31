package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public abstract class HeightProvider {
	private static final Codec<Either<VerticalAnchor, HeightProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(
		VerticalAnchor.CODEC, Registry.HEIGHT_PROVIDER_TYPES.dispatch(HeightProvider::getType, HeightProviderType::codec)
	);
	public static final Codec<HeightProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(
		either -> either.map(ConstantHeight::of, heightProvider -> heightProvider),
		heightProvider -> heightProvider.getType() == HeightProviderType.CONSTANT
				? Either.left(((ConstantHeight)heightProvider).getValue())
				: Either.right(heightProvider)
	);

	public abstract int sample(Random random, WorldGenerationContext worldGenerationContext);

	public abstract HeightProviderType<?> getType();
}
