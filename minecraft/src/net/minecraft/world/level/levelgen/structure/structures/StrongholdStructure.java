package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class StrongholdStructure extends Structure {
	public static final Codec<StrongholdStructure> CODEC = RecordCodecBuilder.create(instance -> codec(instance).apply(instance, StrongholdStructure::new));

	public StrongholdStructure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, boolean bl) {
		super(holderSet, map, decoration, bl);
	}

	@Override
	public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
		return Optional.of(
			new Structure.GenerationStub(
				generationContext.chunkPos().getWorldPosition(), structurePiecesBuilder -> generatePieces(structurePiecesBuilder, generationContext)
			)
		);
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
		int i = 0;

		StrongholdPieces.StartPiece startPiece;
		do {
			structurePiecesBuilder.clear();
			generationContext.random().setLargeFeatureSeed(generationContext.seed() + (long)(i++), generationContext.chunkPos().x, generationContext.chunkPos().z);
			StrongholdPieces.resetPieces();
			startPiece = new StrongholdPieces.StartPiece(
				generationContext.random(), generationContext.chunkPos().getBlockX(2), generationContext.chunkPos().getBlockZ(2)
			);
			structurePiecesBuilder.addPiece(startPiece);
			startPiece.addChildren(startPiece, structurePiecesBuilder, generationContext.random());
			List<StructurePiece> list = startPiece.pendingChildren;

			while (!list.isEmpty()) {
				int j = generationContext.random().nextInt(list.size());
				StructurePiece structurePiece = (StructurePiece)list.remove(j);
				structurePiece.addChildren(startPiece, structurePiecesBuilder, generationContext.random());
			}

			structurePiecesBuilder.moveBelowSeaLevel(
				generationContext.chunkGenerator().getSeaLevel(), generationContext.chunkGenerator().getMinY(), generationContext.random(), 10
			);
		} while (structurePiecesBuilder.isEmpty() || startPiece.portalRoomPiece == null);
	}

	@Override
	public StructureType<?> type() {
		return StructureType.STRONGHOLD;
	}
}
