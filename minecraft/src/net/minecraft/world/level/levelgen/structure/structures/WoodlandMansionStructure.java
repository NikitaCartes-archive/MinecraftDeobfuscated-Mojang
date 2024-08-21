package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class WoodlandMansionStructure extends Structure {
	public static final MapCodec<WoodlandMansionStructure> CODEC = simpleCodec(WoodlandMansionStructure::new);

	public WoodlandMansionStructure(Structure.StructureSettings structureSettings) {
		super(structureSettings);
	}

	@Override
	public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
		Rotation rotation = Rotation.getRandom(generationContext.random());
		BlockPos blockPos = this.getLowestYIn5by5BoxOffset7Blocks(generationContext, rotation);
		return blockPos.getY() < 60
			? Optional.empty()
			: Optional.of(
				new Structure.GenerationStub(blockPos, structurePiecesBuilder -> this.generatePieces(structurePiecesBuilder, generationContext, blockPos, rotation))
			);
	}

	private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext, BlockPos blockPos, Rotation rotation) {
		List<WoodlandMansionPieces.WoodlandMansionPiece> list = Lists.<WoodlandMansionPieces.WoodlandMansionPiece>newLinkedList();
		WoodlandMansionPieces.generateMansion(generationContext.structureTemplateManager(), blockPos, rotation, list, generationContext.random());
		list.forEach(structurePiecesBuilder::addPiece);
	}

	@Override
	public void afterPlace(
		WorldGenLevel worldGenLevel,
		StructureManager structureManager,
		ChunkGenerator chunkGenerator,
		RandomSource randomSource,
		BoundingBox boundingBox,
		ChunkPos chunkPos,
		PiecesContainer piecesContainer
	) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int i = worldGenLevel.getMinY();
		BoundingBox boundingBox2 = piecesContainer.calculateBoundingBox();
		int j = boundingBox2.minY();

		for (int k = boundingBox.minX(); k <= boundingBox.maxX(); k++) {
			for (int l = boundingBox.minZ(); l <= boundingBox.maxZ(); l++) {
				mutableBlockPos.set(k, j, l);
				if (!worldGenLevel.isEmptyBlock(mutableBlockPos) && boundingBox2.isInside(mutableBlockPos) && piecesContainer.isInsidePiece(mutableBlockPos)) {
					for (int m = j - 1; m > i; m--) {
						mutableBlockPos.setY(m);
						if (!worldGenLevel.isEmptyBlock(mutableBlockPos) && !worldGenLevel.getBlockState(mutableBlockPos).liquid()) {
							break;
						}

						worldGenLevel.setBlock(mutableBlockPos, Blocks.COBBLESTONE.defaultBlockState(), 2);
					}
				}
			}
		}
	}

	@Override
	public StructureType<?> type() {
		return StructureType.WOODLAND_MANSION;
	}
}
