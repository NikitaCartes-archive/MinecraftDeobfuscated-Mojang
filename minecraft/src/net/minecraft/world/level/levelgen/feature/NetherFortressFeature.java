package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFortressFeature extends StructureFeature<NoneFeatureConfiguration> {
	private static final List<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = ImmutableList.of(
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
		int i,
		int j,
		Biome biome,
		ChunkPos chunkPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		return worldgenRandom.nextInt(5) < 2;
	}

	@Override
	public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return NetherFortressFeature.NetherBridgeStart::new;
	}

	@Override
	public List<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
		return FORTRESS_ENEMIES;
	}

	public static class NetherBridgeStart extends StructureStart<NoneFeatureConfiguration> {
		public NetherBridgeStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			int i,
			int j,
			Biome biome,
			NoneFeatureConfiguration noneFeatureConfiguration
		) {
			NetherBridgePieces.StartPiece startPiece = new NetherBridgePieces.StartPiece(this.random, (i << 4) + 2, (j << 4) + 2);
			this.pieces.add(startPiece);
			startPiece.addChildren(startPiece, this.pieces, this.random);
			List<StructurePiece> list = startPiece.pendingChildren;

			while (!list.isEmpty()) {
				int k = this.random.nextInt(list.size());
				StructurePiece structurePiece = (StructurePiece)list.remove(k);
				structurePiece.addChildren(startPiece, this.pieces, this.random);
			}

			this.calculateBoundingBox();
			this.moveInsideHeights(this.random, 48, 70);
		}
	}
}
