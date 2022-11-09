package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.apache.commons.lang3.mutable.MutableBoolean;

public record PlacedFeature(Holder<ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placement) {
	public static final Codec<PlacedFeature> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ConfiguredFeature.CODEC.fieldOf("feature").forGetter(placedFeature -> placedFeature.feature),
					PlacementModifier.CODEC.listOf().fieldOf("placement").forGetter(placedFeature -> placedFeature.placement)
				)
				.apply(instance, PlacedFeature::new)
	);
	public static final Codec<Holder<PlacedFeature>> CODEC = RegistryFileCodec.create(Registries.PLACED_FEATURE, DIRECT_CODEC);
	public static final Codec<HolderSet<PlacedFeature>> LIST_CODEC = RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE, DIRECT_CODEC);
	public static final Codec<List<HolderSet<PlacedFeature>>> LIST_OF_LISTS_CODEC = RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE, DIRECT_CODEC, true)
		.listOf();

	public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RandomSource randomSource, BlockPos blockPos) {
		return this.placeWithContext(new PlacementContext(worldGenLevel, chunkGenerator, Optional.empty()), randomSource, blockPos);
	}

	public boolean placeWithBiomeCheck(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RandomSource randomSource, BlockPos blockPos) {
		return this.placeWithContext(new PlacementContext(worldGenLevel, chunkGenerator, Optional.of(this)), randomSource, blockPos);
	}

	private boolean placeWithContext(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		Stream<BlockPos> stream = Stream.of(blockPos);

		for (PlacementModifier placementModifier : this.placement) {
			stream = stream.flatMap(blockPosx -> placementModifier.getPositions(placementContext, randomSource, blockPosx));
		}

		ConfiguredFeature<?, ?> configuredFeature = this.feature.value();
		MutableBoolean mutableBoolean = new MutableBoolean();
		stream.forEach(blockPosx -> {
			if (configuredFeature.place(placementContext.getLevel(), placementContext.generator(), randomSource, blockPosx)) {
				mutableBoolean.setTrue();
			}
		});
		return mutableBoolean.isTrue();
	}

	public Stream<ConfiguredFeature<?, ?>> getFeatures() {
		return this.feature.value().getFeatures();
	}

	public String toString() {
		return "Placed " + this.feature;
	}
}
