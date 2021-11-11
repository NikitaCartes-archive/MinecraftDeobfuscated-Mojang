package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.feature.configurations.RangeConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class NetherFossilFeature extends NoiseAffectingStructureFeature<RangeConfiguration> {
	public NetherFossilFeature(Codec<RangeConfiguration> codec) {
		super(codec, NetherFossilFeature::generatePieces);
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, RangeConfiguration rangeConfiguration, PieceGenerator.Context context) {
		int i = context.chunkPos().getMinBlockX() + context.random().nextInt(16);
		int j = context.chunkPos().getMinBlockZ() + context.random().nextInt(16);
		int k = context.chunkGenerator().getSeaLevel();
		WorldGenerationContext worldGenerationContext = new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor());
		int l = rangeConfiguration.height.sample(context.random(), worldGenerationContext);
		NoiseColumn noiseColumn = context.chunkGenerator().getBaseColumn(i, j, context.heightAccessor());
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, l, j);

		while (l > k) {
			BlockState blockState = noiseColumn.getBlock(l);
			BlockState blockState2 = noiseColumn.getBlock(--l);
			if (blockState.isAir() && (blockState2.is(Blocks.SOUL_SAND) || blockState2.isFaceSturdy(EmptyBlockGetter.INSTANCE, mutableBlockPos.setY(l), Direction.UP))) {
				break;
			}
		}

		if (l > k) {
			if (context.validBiome().test(context.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(l), QuartPos.fromBlock(j)))) {
				NetherFossilPieces.addPieces(context.structureManager(), structurePiecesBuilder, context.random(), new BlockPos(i, l, j));
			}
		}
	}
}
