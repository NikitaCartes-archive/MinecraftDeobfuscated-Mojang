package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class PillagerOutpostFeature extends JigsawFeature {
	public PillagerOutpostFeature(Codec<JigsawConfiguration> codec) {
		super(codec, 0, true, true, PillagerOutpostFeature::checkLocation);
	}

	private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
		ChunkPos chunkPos = context.chunkPos();
		int i = chunkPos.x >> 4;
		int j = chunkPos.z >> 4;
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setSeed((long)(i ^ j << 4) ^ context.seed());
		worldgenRandom.nextInt();
		return worldgenRandom.nextInt(5) != 0 ? false : !context.chunkGenerator().hasFeatureChunkInRange(BuiltinStructureSets.VILLAGES, chunkPos.x, chunkPos.z, 10);
	}
}
