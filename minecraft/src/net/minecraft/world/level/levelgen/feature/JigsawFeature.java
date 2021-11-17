package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class JigsawFeature extends NoiseAffectingStructureFeature<JigsawConfiguration> {
	public JigsawFeature(
		Codec<JigsawConfiguration> codec, int i, boolean bl, boolean bl2, Predicate<PieceGeneratorSupplier.Context<JigsawConfiguration>> predicate
	) {
		super(codec, context -> {
			if (!predicate.test(context)) {
				return Optional.empty();
			} else {
				BlockPos blockPos = new BlockPos(context.chunkPos().getMinBlockX(), i, context.chunkPos().getMinBlockZ());
				Pools.bootstrap();
				return JigsawPlacement.addPieces(context, PoolElementStructurePiece::new, blockPos, bl, bl2);
			}
		});
	}
}
