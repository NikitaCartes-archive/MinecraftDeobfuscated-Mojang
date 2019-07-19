package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFortressFeature extends StructureFeature<NoneFeatureConfiguration> {
	private static final List<Biome.SpawnerData> FORTRESS_ENEMIES = Lists.<Biome.SpawnerData>newArrayList(
		new Biome.SpawnerData(EntityType.BLAZE, 10, 2, 3),
		new Biome.SpawnerData(EntityType.ZOMBIE_PIGMAN, 5, 4, 4),
		new Biome.SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5),
		new Biome.SpawnerData(EntityType.SKELETON, 2, 5, 5),
		new Biome.SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4)
	);

	public NetherFortressFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	public boolean isFeatureChunk(ChunkGenerator<?> chunkGenerator, Random random, int i, int j) {
		int k = i >> 4;
		int l = j >> 4;
		random.setSeed((long)(k ^ l << 4) ^ chunkGenerator.getSeed());
		random.nextInt();
		if (random.nextInt(3) != 0) {
			return false;
		} else if (i != (k << 4) + 4 + random.nextInt(8)) {
			return false;
		} else if (j != (l << 4) + 4 + random.nextInt(8)) {
			return false;
		} else {
			Biome biome = chunkGenerator.getBiomeSource().getBiome(new BlockPos((i << 4) + 9, 0, (j << 4) + 9));
			return chunkGenerator.isBiomeValidStartForStructure(biome, Feature.NETHER_BRIDGE);
		}
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return NetherFortressFeature.NetherBridgeStart::new;
	}

	@Override
	public String getFeatureName() {
		return "Fortress";
	}

	@Override
	public int getLookupRange() {
		return 8;
	}

	@Override
	public List<Biome.SpawnerData> getSpecialEnemies() {
		return FORTRESS_ENEMIES;
	}

	public static class NetherBridgeStart extends StructureStart {
		public NetherBridgeStart(StructureFeature<?> structureFeature, int i, int j, Biome biome, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, biome, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
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
