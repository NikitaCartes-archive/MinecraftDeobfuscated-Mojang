package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public record RandomSpreadStructurePlacement(int spacing, int separation, RandomSpreadType spreadType, int salt, Vec3i locateOffset)
	implements StructurePlacement {
	public static final Codec<RandomSpreadStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing),
						Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation),
						RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(RandomSpreadStructurePlacement::salt),
						Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(RandomSpreadStructurePlacement::locateOffset)
					)
					.apply(instance, RandomSpreadStructurePlacement::new)
		)
		.<RandomSpreadStructurePlacement>flatXmap(
			randomSpreadStructurePlacement -> randomSpreadStructurePlacement.spacing <= randomSpreadStructurePlacement.separation
					? DataResult.error("Spacing has to be larger than separation")
					: DataResult.success(randomSpreadStructurePlacement),
			DataResult::success
		)
		.codec();

	public RandomSpreadStructurePlacement(int i, int j, RandomSpreadType randomSpreadType, int k) {
		this(i, j, randomSpreadType, k, Vec3i.ZERO);
	}

	public ChunkPos getPotentialFeatureChunk(long l, int i, int j) {
		int k = this.spacing();
		int m = this.separation();
		int n = Math.floorDiv(i, k);
		int o = Math.floorDiv(j, k);
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setLargeFeatureWithSalt(l, n, o, this.salt());
		int p = k - m;
		int q = this.spreadType().evaluate(worldgenRandom, p);
		int r = this.spreadType().evaluate(worldgenRandom, p);
		return new ChunkPos(n * k + q, o * k + r);
	}

	@Override
	public boolean isFeatureChunk(ChunkGenerator chunkGenerator, long l, int i, int j) {
		ChunkPos chunkPos = this.getPotentialFeatureChunk(l, i, j);
		return chunkPos.x == i && chunkPos.z == j;
	}

	@Override
	public StructurePlacementType<?> type() {
		return StructurePlacementType.RANDOM_SPREAD;
	}
}
