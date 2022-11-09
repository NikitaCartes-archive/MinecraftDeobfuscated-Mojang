package net.minecraft.world.level.levelgen.structure;

import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import org.slf4j.Logger;

public final class StructureStart {
	public static final String INVALID_START_ID = "INVALID";
	public static final StructureStart INVALID_START = new StructureStart(null, new ChunkPos(0, 0), 0, new PiecesContainer(List.of()));
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Structure structure;
	private final PiecesContainer pieceContainer;
	private final ChunkPos chunkPos;
	private int references;
	@Nullable
	private volatile BoundingBox cachedBoundingBox;

	public StructureStart(Structure structure, ChunkPos chunkPos, int i, PiecesContainer piecesContainer) {
		this.structure = structure;
		this.chunkPos = chunkPos;
		this.references = i;
		this.pieceContainer = piecesContainer;
	}

	@Nullable
	public static StructureStart loadStaticStart(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l) {
		String string = compoundTag.getString("id");
		if ("INVALID".equals(string)) {
			return INVALID_START;
		} else {
			Registry<Structure> registry = structurePieceSerializationContext.registryAccess().registryOrThrow(Registries.STRUCTURE);
			Structure structure = registry.get(new ResourceLocation(string));
			if (structure == null) {
				LOGGER.error("Unknown stucture id: {}", string);
				return null;
			} else {
				ChunkPos chunkPos = new ChunkPos(compoundTag.getInt("ChunkX"), compoundTag.getInt("ChunkZ"));
				int i = compoundTag.getInt("references");
				ListTag listTag = compoundTag.getList("Children", 10);

				try {
					PiecesContainer piecesContainer = PiecesContainer.load(listTag, structurePieceSerializationContext);
					if (structure instanceof OceanMonumentStructure) {
						piecesContainer = OceanMonumentStructure.regeneratePiecesAfterLoad(chunkPos, l, piecesContainer);
					}

					return new StructureStart(structure, chunkPos, i, piecesContainer);
				} catch (Exception var11) {
					LOGGER.error("Failed Start with id {}", string, var11);
					return null;
				}
			}
		}
	}

	public BoundingBox getBoundingBox() {
		BoundingBox boundingBox = this.cachedBoundingBox;
		if (boundingBox == null) {
			boundingBox = this.structure.adjustBoundingBox(this.pieceContainer.calculateBoundingBox());
			this.cachedBoundingBox = boundingBox;
		}

		return boundingBox;
	}

	public void placeInChunk(
		WorldGenLevel worldGenLevel,
		StructureManager structureManager,
		ChunkGenerator chunkGenerator,
		RandomSource randomSource,
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
					structurePiece.postProcess(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, blockPos2);
				}
			}

			this.structure.afterPlace(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, this.pieceContainer);
		}
	}

	public CompoundTag createTag(StructurePieceSerializationContext structurePieceSerializationContext, ChunkPos chunkPos) {
		CompoundTag compoundTag = new CompoundTag();
		if (this.isValid()) {
			compoundTag.putString("id", structurePieceSerializationContext.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(this.structure).toString());
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

	public Structure getStructure() {
		return this.structure;
	}

	public List<StructurePiece> getPieces() {
		return this.pieceContainer.pieces();
	}
}
