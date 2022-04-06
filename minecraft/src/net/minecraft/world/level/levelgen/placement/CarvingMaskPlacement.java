package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.GenerationStep;

public class CarvingMaskPlacement extends PlacementModifier {
	public static final Codec<CarvingMaskPlacement> CODEC = GenerationStep.Carving.CODEC
		.fieldOf("step")
		.<CarvingMaskPlacement>xmap(CarvingMaskPlacement::new, carvingMaskPlacement -> carvingMaskPlacement.step)
		.codec();
	private final GenerationStep.Carving step;

	private CarvingMaskPlacement(GenerationStep.Carving carving) {
		this.step = carving;
	}

	public static CarvingMaskPlacement forStep(GenerationStep.Carving carving) {
		return new CarvingMaskPlacement(carving);
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		ChunkPos chunkPos = new ChunkPos(blockPos);
		return placementContext.getCarvingMask(chunkPos, this.step).stream(chunkPos);
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.CARVING_MASK_PLACEMENT;
	}
}
