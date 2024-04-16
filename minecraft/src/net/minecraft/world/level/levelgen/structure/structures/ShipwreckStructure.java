package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class ShipwreckStructure extends Structure {
	public static final MapCodec<ShipwreckStructure> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(settingsCodec(instance), Codec.BOOL.fieldOf("is_beached").forGetter(shipwreckStructure -> shipwreckStructure.isBeached))
				.apply(instance, ShipwreckStructure::new)
	);
	public final boolean isBeached;

	public ShipwreckStructure(Structure.StructureSettings structureSettings, boolean bl) {
		super(structureSettings);
		this.isBeached = bl;
	}

	@Override
	public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
		Heightmap.Types types = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
		return onTopOfChunkCenter(generationContext, types, structurePiecesBuilder -> this.generatePieces(structurePiecesBuilder, generationContext));
	}

	private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
		Rotation rotation = Rotation.getRandom(generationContext.random());
		BlockPos blockPos = new BlockPos(generationContext.chunkPos().getMinBlockX(), 90, generationContext.chunkPos().getMinBlockZ());
		ShipwreckPieces.ShipwreckPiece shipwreckPiece = ShipwreckPieces.addRandomPiece(
			generationContext.structureTemplateManager(), blockPos, rotation, structurePiecesBuilder, generationContext.random(), this.isBeached
		);
		if (shipwreckPiece.isTooBigToFitInWorldGenRegion()) {
			BoundingBox boundingBox = shipwreckPiece.getBoundingBox();
			int j;
			if (this.isBeached) {
				int i = Structure.getLowestY(generationContext, boundingBox.minX(), boundingBox.getXSpan(), boundingBox.minZ(), boundingBox.getZSpan());
				j = shipwreckPiece.calculateBeachedPosition(i, generationContext.random());
			} else {
				j = Structure.getMeanFirstOccupiedHeight(generationContext, boundingBox.minX(), boundingBox.getXSpan(), boundingBox.minZ(), boundingBox.getZSpan());
			}

			shipwreckPiece.adjustPositionHeight(j);
		}
	}

	@Override
	public StructureType<?> type() {
		return StructureType.SHIPWRECK;
	}
}
