package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class PlacedFeature {
	public static final Codec<PlacedFeature> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ConfiguredFeature.CODEC.fieldOf("feature").forGetter(placedFeature -> placedFeature.feature),
					PlacementModifier.CODEC.listOf().fieldOf("placement").forGetter(placedFeature -> placedFeature.placement)
				)
				.apply(instance, PlacedFeature::new)
	);
	public static final Codec<Supplier<PlacedFeature>> CODEC = RegistryFileCodec.create(Registry.PLACED_FEATURE_REGISTRY, DIRECT_CODEC);
	public static final Codec<List<Supplier<PlacedFeature>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.PLACED_FEATURE_REGISTRY, DIRECT_CODEC);
	private final Supplier<ConfiguredFeature<?, ?>> feature;
	private final List<PlacementModifier> placement;

	public PlacedFeature(Supplier<ConfiguredFeature<?, ?>> supplier, List<PlacementModifier> list) {
		this.feature = supplier;
		this.placement = list;
	}

	public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
		return this.placeWithContext(new PlacementContext(worldGenLevel, chunkGenerator, Optional.empty()), random, blockPos);
	}

	public boolean placeWithBiomeCheck(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
		return this.placeWithContext(new PlacementContext(worldGenLevel, chunkGenerator, Optional.of(this)), random, blockPos);
	}

	private boolean placeWithContext(PlacementContext placementContext, Random random, BlockPos blockPos) {
		Stream<BlockPos> stream = Stream.of(blockPos);

		for (PlacementModifier placementModifier : this.placement) {
			stream = stream.flatMap(blockPosx -> placementModifier.getPositions(placementContext, random, blockPosx));
		}

		ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)this.feature.get();
		MutableBoolean mutableBoolean = new MutableBoolean();
		stream.forEach(blockPosx -> {
			if (configuredFeature.place(placementContext.getLevel(), placementContext.generator(), random, blockPosx)) {
				mutableBoolean.setTrue();
			}
		});
		return mutableBoolean.isTrue();
	}

	public Stream<ConfiguredFeature<?, ?>> getFeatures() {
		return ((ConfiguredFeature)this.feature.get()).getFeatures();
	}

	public String toString() {
		return "Placed " + Registry.FEATURE.getKey(((ConfiguredFeature)this.feature.get()).feature());
	}
}
