package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StructureStart<C extends FeatureConfiguration> implements StructurePieceAccessor {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final String INVALID_START_ID = "INVALID";
	public static final StructureStart<?> INVALID_START = new StructureStart<MineshaftConfiguration>(null, new ChunkPos(0, 0), 0, 0L) {
		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			MineshaftConfiguration mineshaftConfiguration,
			LevelHeightAccessor levelHeightAccessor,
			Predicate<Biome> predicate
		) {
		}

		@Override
		public boolean isValid() {
			return false;
		}
	};
	private final StructureFeature<C> feature;
	protected final List<StructurePiece> pieces = Lists.<StructurePiece>newArrayList();
	private final ChunkPos chunkPos;
	private int references;
	protected final WorldgenRandom random;
	@Nullable
	private BoundingBox cachedBoundingBox;

	public StructureStart(StructureFeature<C> structureFeature, ChunkPos chunkPos, int i, long l) {
		this.feature = structureFeature;
		this.chunkPos = chunkPos;
		this.references = i;
		this.random = new WorldgenRandom();
		this.random.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
	}

	public abstract void generatePieces(
		RegistryAccess registryAccess,
		ChunkGenerator chunkGenerator,
		StructureManager structureManager,
		ChunkPos chunkPos,
		C featureConfiguration,
		LevelHeightAccessor levelHeightAccessor,
		Predicate<Biome> predicate
	);

	public final BoundingBox getBoundingBox() {
		if (this.cachedBoundingBox == null) {
			this.cachedBoundingBox = this.createBoundingBox();
		}

		return this.cachedBoundingBox;
	}

	protected BoundingBox createBoundingBox() {
		synchronized (this.pieces) {
			return (BoundingBox)BoundingBox.encapsulatingBoxes(this.pieces.stream().map(StructurePiece::getBoundingBox)::iterator)
				.orElseThrow(() -> new IllegalStateException("Unable to calculate boundingbox without pieces"));
		}
	}

	public List<StructurePiece> getPieces() {
		return this.pieces;
	}

	public void placeInChunk(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		Predicate<Biome> predicate,
		BoundingBox boundingBox,
		ChunkPos chunkPos
	) {
		synchronized (this.pieces) {
			if (!this.pieces.isEmpty()) {
				BoundingBox boundingBox2 = ((StructurePiece)this.pieces.get(0)).boundingBox;
				BlockPos blockPos = boundingBox2.getCenter();
				BlockPos blockPos2 = new BlockPos(blockPos.getX(), boundingBox2.minY(), blockPos.getZ());
				Iterator<StructurePiece> iterator = this.pieces.iterator();

				while (iterator.hasNext()) {
					StructurePiece structurePiece = (StructurePiece)iterator.next();
					if (structurePiece.getBoundingBox().intersects(boundingBox)
						&& !structurePiece.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos2)) {
						iterator.remove();
					}
				}
			}
		}
	}

	public CompoundTag createTag(ServerLevel serverLevel, ChunkPos chunkPos) {
		CompoundTag compoundTag = new CompoundTag();
		if (this.isValid()) {
			compoundTag.putString("id", Registry.STRUCTURE_FEATURE.getKey(this.getFeature()).toString());
			compoundTag.putInt("ChunkX", chunkPos.x);
			compoundTag.putInt("ChunkZ", chunkPos.z);
			compoundTag.putInt("references", this.references);
			ListTag listTag = new ListTag();
			synchronized (this.pieces) {
				for (StructurePiece structurePiece : this.pieces) {
					listTag.add(structurePiece.createTag(serverLevel));
				}
			}

			compoundTag.put("Children", listTag);
			return compoundTag;
		} else {
			compoundTag.putString("id", "INVALID");
			return compoundTag;
		}
	}

	@Deprecated
	protected void moveBelowSeaLevel(int i, int j, Random random, int k) {
		int l = i - k;
		BoundingBox boundingBox = this.getBoundingBox();
		int m = boundingBox.getYSpan() + j + 1;
		if (m < l) {
			m += random.nextInt(l - m);
		}

		int n = m - boundingBox.maxY();
		this.offsetPiecesVertically(n);
	}

	@Deprecated
	protected void moveInsideHeights(Random random, int i, int j) {
		BoundingBox boundingBox = this.getBoundingBox();
		int k = j - i + 1 - boundingBox.getYSpan();
		int l;
		if (k > 1) {
			l = i + random.nextInt(k);
		} else {
			l = i;
		}

		int m = l - boundingBox.minY();
		this.offsetPiecesVertically(m);
	}

	@Deprecated
	protected void offsetPiecesVertically(int i) {
		for (StructurePiece structurePiece : this.pieces) {
			structurePiece.move(0, i, 0);
		}

		this.invalidateCache();
	}

	private void invalidateCache() {
		this.cachedBoundingBox = null;
	}

	public boolean isValid() {
		return !this.pieces.isEmpty();
	}

	public ChunkPos getChunkPos() {
		return this.chunkPos;
	}

	public BlockPos getLocatePos() {
		return new BlockPos(this.chunkPos.getMinBlockX(), 0, this.chunkPos.getMinBlockZ());
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

	@Override
	public void addPiece(StructurePiece structurePiece) {
		this.pieces.add(structurePiece);
		this.invalidateCache();
	}

	@Nullable
	@Override
	public StructurePiece findCollisionPiece(BoundingBox boundingBox) {
		return findCollisionPiece(this.pieces, boundingBox);
	}

	public void clearPieces() {
		this.pieces.clear();
		this.invalidateCache();
	}

	public boolean hasNoPieces() {
		return this.pieces.isEmpty();
	}

	@Nullable
	public static StructurePiece findCollisionPiece(List<StructurePiece> list, BoundingBox boundingBox) {
		for (StructurePiece structurePiece : list) {
			if (structurePiece.getBoundingBox().intersects(boundingBox)) {
				return structurePiece;
			}
		}

		return null;
	}

	protected boolean isInsidePiece(BlockPos blockPos) {
		for (StructurePiece structurePiece : this.pieces) {
			if (structurePiece.getBoundingBox().isInside(blockPos)) {
				return true;
			}
		}

		return false;
	}
}
