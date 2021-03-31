package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFortressFeature extends StructureFeature<NoneFeatureConfiguration> {
	private static final WeightedRandomList<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = WeightedRandomList.create(
		new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 10, 2, 3),
		new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4),
		new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5),
		new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 2, 5, 5),
		new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4)
	);

	public NetherFortressFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	protected boolean isFeatureChunk(
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		long l,
		WorldgenRandom worldgenRandom,
		ChunkPos chunkPos,
		Biome biome,
		ChunkPos chunkPos2,
		NoneFeatureConfiguration noneFeatureConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		return worldgenRandom.nextInt(5) < 2;
	}

	@Override
	public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return NetherFortressFeature.NetherBridgeStart::new;
	}

	@Override
	public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
		return FORTRESS_ENEMIES;
	}

	public static class NetherBridgeStart extends StructureStart<NoneFeatureConfiguration> {
		public NetherBridgeStart(StructureFeature<NoneFeatureConfiguration> structureFeature, ChunkPos chunkPos, int i, long l) {
			super(structureFeature, chunkPos, i, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			Biome biome,
			NoneFeatureConfiguration noneFeatureConfiguration,
			LevelHeightAccessor levelHeightAccessor
		) {
			NetherBridgePieces.StartPiece startPiece = new NetherBridgePieces.StartPiece(this.random, chunkPos.getBlockX(2), chunkPos.getBlockZ(2));
			this.addPiece(startPiece);
			startPiece.addChildren(startPiece, this, this.random);
			List<StructurePiece> list = startPiece.pendingChildren;

			while (!list.isEmpty()) {
				int i = this.random.nextInt(list.size());
				StructurePiece structurePiece = (StructurePiece)list.remove(i);
				structurePiece.addChildren(startPiece, this, this.random);
			}

			this.moveInsideHeights(this.random, 48, 70);
		}
	}
}
