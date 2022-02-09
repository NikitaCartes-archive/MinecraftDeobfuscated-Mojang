package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class PillagerOutpostFeature extends JigsawFeature {
	public static final WeightedRandomList<MobSpawnSettings.SpawnerData> OUTPOST_ENEMIES = WeightedRandomList.create(
		new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1)
	);

	public PillagerOutpostFeature(Codec<JigsawConfiguration> codec) {
		super(codec, 0, true, true, PillagerOutpostFeature::checkLocation);
	}

	private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
		int i = context.chunkPos().x >> 4;
		int j = context.chunkPos().z >> 4;
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setSeed((long)(i ^ j << 4) ^ context.seed());
		worldgenRandom.nextInt();
		return worldgenRandom.nextInt(5) != 0 ? false : !isNearVillage(context.chunkGenerator(), context.seed(), context.chunkPos());
	}

	private static boolean isNearVillage(ChunkGenerator chunkGenerator, long l, ChunkPos chunkPos) {
		StructurePlacement structurePlacement = chunkGenerator.getSettings().getConfig(StructureFeature.VILLAGE);
		if (structurePlacement != null) {
			int i = chunkPos.x;
			int j = chunkPos.z;

			for (int k = i - 10; k <= i + 10; k++) {
				for (int m = j - 10; m <= j + 10; m++) {
					if (structurePlacement.isFeatureChunk(chunkGenerator, k, m)) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
