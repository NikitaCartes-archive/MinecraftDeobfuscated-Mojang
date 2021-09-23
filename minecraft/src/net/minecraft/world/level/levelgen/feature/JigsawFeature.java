package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;

public class JigsawFeature extends NoiseAffectingStructureFeature<JigsawConfiguration> {
	public JigsawFeature(Codec<JigsawConfiguration> codec, int i, boolean bl, boolean bl2) {
		super(
			codec,
			(structurePiecesBuilder, jigsawConfiguration, context) -> {
				BlockPos blockPos = new BlockPos(context.chunkPos().getMinBlockX(), i, context.chunkPos().getMinBlockZ());
				Pools.bootstrap();
				JigsawPlacement.addPieces(
					context.registryAccess(),
					jigsawConfiguration,
					PoolElementStructurePiece::new,
					context.chunkGenerator(),
					context.structureManager(),
					blockPos,
					structurePiecesBuilder,
					context.random(),
					bl,
					bl2,
					context.heightAccessor(),
					context.validBiome()
				);
			}
		);
	}
}
