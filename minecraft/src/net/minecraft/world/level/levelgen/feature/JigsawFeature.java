package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class JigsawFeature extends NoiseAffectingStructureFeature<JigsawConfiguration> {
	private static final Random REMOVE_ME_RANDOM = new Random();

	public JigsawFeature(
		Codec<JigsawConfiguration> codec, int i, boolean bl, boolean bl2, Predicate<PieceGeneratorSupplier.Context<JigsawConfiguration>> predicate
	) {
		this(codec, PoolElementStructurePiece::new, random -> i, bl, bl2, predicate, 80);
	}

	public JigsawFeature(
		Codec<JigsawConfiguration> codec,
		JigsawPlacement.PieceFactory pieceFactory,
		Function<Random, Integer> function,
		boolean bl,
		boolean bl2,
		Predicate<PieceGeneratorSupplier.Context<JigsawConfiguration>> predicate,
		int i
	) {
		super(codec, context -> {
			if (!predicate.test(context)) {
				return Optional.empty();
			} else {
				BlockPos blockPos = new BlockPos(context.chunkPos().getMinBlockX(), (Integer)function.apply(REMOVE_ME_RANDOM), context.chunkPos().getMinBlockZ());
				Pools.bootstrap();
				return JigsawPlacement.addPieces(context, pieceFactory, blockPos, bl, bl2, i);
			}
		});
	}
}
