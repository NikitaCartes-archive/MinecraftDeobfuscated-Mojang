package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class HeightmapPlacement extends PlacementModifier {
	public static final Codec<HeightmapPlacement> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(heightmapPlacement -> heightmapPlacement.heightmap))
				.apply(instance, HeightmapPlacement::new)
	);
	private final Heightmap.Types heightmap;

	private HeightmapPlacement(Heightmap.Types types) {
		this.heightmap = types;
	}

	public static HeightmapPlacement onHeightmap(Heightmap.Types types) {
		return new HeightmapPlacement(types);
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		int i = blockPos.getX();
		int j = blockPos.getZ();
		int k = placementContext.getHeight(this.heightmap, i, j);
		return k > placementContext.getMinBuildHeight() ? Stream.of(new BlockPos(i, k, j)) : Stream.of();
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.HEIGHTMAP;
	}
}
