package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public abstract class StructureStart {
	public static final StructureStart INVALID_START = new StructureStart(Feature.MINESHAFT, 0, 0, BoundingBox.getUnknownBox(), 0, 0L) {
		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
		}
	};
	private final StructureFeature<?> feature;
	protected final List<StructurePiece> pieces = Lists.<StructurePiece>newArrayList();
	protected BoundingBox boundingBox;
	private final int chunkX;
	private final int chunkZ;
	private int references;
	protected final WorldgenRandom random;

	public StructureStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
		this.feature = structureFeature;
		this.chunkX = i;
		this.chunkZ = j;
		this.references = k;
		this.random = new WorldgenRandom();
		this.random.setLargeFeatureSeed(l, i, j);
		this.boundingBox = boundingBox;
	}

	public abstract void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome);

	public BoundingBox getBoundingBox() {
		return this.boundingBox;
	}

	public List<StructurePiece> getPieces() {
		return this.pieces;
	}

	public void postProcess(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos) {
		synchronized (this.pieces) {
			Iterator<StructurePiece> iterator = this.pieces.iterator();

			while (iterator.hasNext()) {
				StructurePiece structurePiece = (StructurePiece)iterator.next();
				if (structurePiece.getBoundingBox().intersects(boundingBox) && !structurePiece.postProcess(levelAccessor, chunkGenerator, random, boundingBox, chunkPos)) {
					iterator.remove();
				}
			}

			this.calculateBoundingBox();
		}
	}

	protected void calculateBoundingBox() {
		this.boundingBox = BoundingBox.getUnknownBox();

		for (StructurePiece structurePiece : this.pieces) {
			this.boundingBox.expand(structurePiece.getBoundingBox());
		}
	}

	public CompoundTag createTag(int i, int j) {
		CompoundTag compoundTag = new CompoundTag();
		if (this.isValid()) {
			compoundTag.putString("id", Registry.STRUCTURE_FEATURE.getKey(this.getFeature()).toString());
			compoundTag.putInt("ChunkX", i);
			compoundTag.putInt("ChunkZ", j);
			compoundTag.putInt("references", this.references);
			compoundTag.put("BB", this.boundingBox.createTag());
			ListTag listTag = new ListTag();
			synchronized (this.pieces) {
				for (StructurePiece structurePiece : this.pieces) {
					listTag.add(structurePiece.createTag());
				}
			}

			compoundTag.put("Children", listTag);
			return compoundTag;
		} else {
			compoundTag.putString("id", "INVALID");
			return compoundTag;
		}
	}

	protected void moveBelowSeaLevel(int i, Random random, int j) {
		int k = i - j;
		int l = this.boundingBox.getYSpan() + 1;
		if (l < k) {
			l += random.nextInt(k - l);
		}

		int m = l - this.boundingBox.y1;
		this.boundingBox.move(0, m, 0);

		for (StructurePiece structurePiece : this.pieces) {
			structurePiece.move(0, m, 0);
		}
	}

	protected void moveInsideHeights(Random random, int i, int j) {
		int k = j - i + 1 - this.boundingBox.getYSpan();
		int l;
		if (k > 1) {
			l = i + random.nextInt(k);
		} else {
			l = i;
		}

		int m = l - this.boundingBox.y0;
		this.boundingBox.move(0, m, 0);

		for (StructurePiece structurePiece : this.pieces) {
			structurePiece.move(0, m, 0);
		}
	}

	public boolean isValid() {
		return !this.pieces.isEmpty();
	}

	public int getChunkX() {
		return this.chunkX;
	}

	public int getChunkZ() {
		return this.chunkZ;
	}

	public BlockPos getLocatePos() {
		return new BlockPos(this.chunkX << 4, 0, this.chunkZ << 4);
	}

	public boolean canBeReferenced() {
		return this.references < this.getMaxReferences();
	}

	public void addReference() {
		this.references++;
	}

	protected int getMaxReferences() {
		return 1;
	}

	public StructureFeature<?> getFeature() {
		return this.feature;
	}
}
