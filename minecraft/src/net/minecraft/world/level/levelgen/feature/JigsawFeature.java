package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureStart;
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
		return (structureFeature, chunkPos, i, l) -> new JigsawFeature.FeatureStart(this, chunkPos, i, l);
	}

	public static class FeatureStart extends NoiseAffectingStructureStart<JigsawConfiguration> {
		private final JigsawFeature feature;

		public FeatureStart(JigsawFeature jigsawFeature, ChunkPos chunkPos, int i, long l) {
			super(jigsawFeature, chunkPos, i, l);
			this.feature = jigsawFeature;
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			Biome biome,
			JigsawConfiguration jigsawConfiguration,
			LevelHeightAccessor levelHeightAccessor
		) {
			BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), this.feature.startY, chunkPos.getMinBlockZ());
			Pools.bootstrap();
			JigsawPlacement.addPieces(
				registryAccess,
				jigsawConfiguration,
				PoolElementStructurePiece::new,
				chunkGenerator,
				structureManager,
				blockPos,
				this,
				this.random,
				this.feature.doExpansionHack,
				this.feature.projectStartToHeightmap,
				levelHeightAccessor
			);
		}
	}
}
