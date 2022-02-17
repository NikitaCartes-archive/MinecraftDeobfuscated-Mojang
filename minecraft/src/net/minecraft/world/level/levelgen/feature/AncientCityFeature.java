package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePieceNoiseEffect;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class AncientCityFeature extends JigsawFeature {
	public static final int MAX_PIECE_SIZE = 128;

	public AncientCityFeature(Codec<JigsawConfiguration> codec) {
		super(
			codec,
			(structureManager, structurePoolElement, blockPos, i, rotation, boundingBox) -> new PoolElementStructurePieceNoiseEffect(
					structureManager, structurePoolElement, blockPos, i, rotation, boundingBox, NoiseEffect.BEARD_AND_SHAVE
				),
			random -> -40,
			false,
			false,
			AncientCityFeature::checkLocation,
			128
		);
	}

	private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
		return worldgenRandom.nextInt(5) >= 2;
	}
}
