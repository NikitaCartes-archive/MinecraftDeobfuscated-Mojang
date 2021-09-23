package net.minecraft.world.level.levelgen.structure;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class StructureStart<C extends FeatureConfiguration> {
	public static final String INVALID_START_ID = "INVALID";
	public static final StructureStart<?> INVALID_START = new StructureStart(null, new ChunkPos(0, 0), 0, new PiecesContainer(List.of()));
	private final StructureFeature<C> feature;
	private final PiecesContainer pieceContainer;
	private final ChunkPos chunkPos;
	private int references;
	@Nullable
	private volatile BoundingBox cachedBoundingBox;

	public StructureStart(StructureFeature<C> structureFeature, ChunkPos chunkPos, int i, PiecesContainer piecesContainer) {
		this.feature = structureFeature;
		this.chunkPos = chunkPos;
		this.references = i;
		this.pieceContainer = piecesContainer;
	}

	public BoundingBox getBoundingBox() {
		BoundingBox boundingBox = this.cachedBoundingBox;
		if (boundingBox == null) {
			boundingBox = this.feature.adjustBoundingBox(this.pieceContainer.calculateBoundingBox());
			this.cachedBoundingBox = boundingBox;
		}

		return boundingBox;
	}

	public void placeInChunk(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BoundingBox boundingBox,
		ChunkPos chunkPos
	) {
		List<StructurePiece> list = this.pieceContainer.pieces();
		if (!list.isEmpty()) {
			BoundingBox boundingBox2 = ((StructurePiece)list.get(0)).boundingBox;
			BlockPos blockPos = boundingBox2.getCenter();
			BlockPos blockPos2 = new BlockPos(blockPos.getX(), boundingBox2.minY(), blockPos.getZ());

			for (StructurePiece structurePiece : list) {
				if (structurePiece.getBoundingBox().intersects(boundingBox)) {
					structurePiece.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos2);
				}
			}

			this.feature
				.getPostPlacementProcessor()
				.afterPlace(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, this.pieceContainer);
		}
	}

	public CompoundTag createTag(StructurePieceSerializationContext structurePieceSerializationContext, ChunkPos chunkPos) {
		CompoundTag compoundTag = new CompoundTag();
		if (this.isValid()) {
			compoundTag.putString("id", Registry.STRUCTURE_FEATURE.getKey(this.getFeature()).toString());
			compoundTag.putInt("ChunkX", chunkPos.x);
			compoundTag.putInt("ChunkZ", chunkPos.z);
			compoundTag.putInt("references", this.references);
			compoundTag.put("Children", this.pieceContainer.save(structurePieceSerializationContext));
			return compoundTag;
		} else {
			compoundTag.putString("id", "INVALID");
			return compoundTag;
		}
	}

	public boolean isValid() {
		return !this.pieceContainer.isEmpty();
	}

	public ChunkPos getChunkPos() {
		return this.chunkPos;
	}

	public boolean canBeReferenced() {
		return this.references < this.getMaxReferences();
	}

	public void addReference() {
		this.references++;
	}

	public int getReferences() {
		return this.references;
	}

	protected int getMaxReferences() {
		return 1;
	}

	public StructureFeature<?> getFeature() {
		return this.feature;
	}

	public List<StructurePiece> getPieces() {
		return this.pieceContainer.pieces();
	}
}
