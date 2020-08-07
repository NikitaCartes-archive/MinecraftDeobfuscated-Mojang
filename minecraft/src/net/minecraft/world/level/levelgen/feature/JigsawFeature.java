package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class JigsawFeature extends StructureFeature<JigsawConfiguration> {
	private final int startY;
	private final boolean doExpansionHack;
	private final boolean projectStartToHeightmap;

	public JigsawFeature(Codec<JigsawConfiguration> codec, int i, boolean bl, boolean bl2) {
		super(codec);
		this.startY = i;
		this.doExpansionHack = bl;
		this.projectStartToHeightmap = bl2;
	}

	@Override
	public StructureFeature.StructureStartFactory<JigsawConfiguration> getStartFactory() {
		return (structureFeature, i, j, boundingBox, k, l) -> new JigsawFeature.FeatureStart(this, i, j, boundingBox, k, l);
	}

	public static class FeatureStart extends BeardedStructureStart<JigsawConfiguration> {
		private final JigsawFeature feature;

		public FeatureStart(JigsawFeature jigsawFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(jigsawFeature, i, j, boundingBox, k, l);
			this.feature = jigsawFeature;
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			int i,
			int j,
			Biome biome,
			JigsawConfiguration jigsawConfiguration
		) {
			BlockPos blockPos = new BlockPos(i * 16, this.feature.startY, j * 16);
			Pools.bootstrap();
			JigsawPlacement.addPieces(
				registryAccess,
				jigsawConfiguration,
				PoolElementStructurePiece::new,
				chunkGenerator,
				structureManager,
				blockPos,
				this.pieces,
				this.random,
				this.feature.doExpansionHack,
				this.feature.projectStartToHeightmap
			);
			this.calculateBoundingBox();
		}
	}
}
