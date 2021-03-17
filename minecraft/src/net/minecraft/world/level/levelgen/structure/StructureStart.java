package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
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

public abstract class StructureStart<C extends FeatureConfiguration> {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final StructureStart<?> INVALID_START = new StructureStart<MineshaftConfiguration>(
		StructureFeature.MINESHAFT, new ChunkPos(0, 0), BoundingBox.getUnknownBox(), 0, 0L
	) {
		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			Biome biome,
			MineshaftConfiguration mineshaftConfiguration,
			LevelHeightAccessor levelHeightAccessor
		) {
		}
	};
	private final StructureFeature<C> feature;
	protected final List<StructurePiece> pieces = Lists.<StructurePiece>newArrayList();
	protected BoundingBox boundingBox;
	private final ChunkPos chunkPos;
	private int references;
	protected final WorldgenRandom random;

	public StructureStart(StructureFeature<C> structureFeature, ChunkPos chunkPos, BoundingBox boundingBox, int i, long l) {
		this.feature = structureFeature;
		this.chunkPos = chunkPos;
		this.references = i;
		this.random = new WorldgenRandom();
		this.random.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
		this.boundingBox = boundingBox;
	}

	public abstract void generatePieces(
		RegistryAccess registryAccess,
		ChunkGenerator chunkGenerator,
		StructureManager structureManager,
		ChunkPos chunkPos,
		Biome biome,
		C featureConfiguration,
		LevelHeightAccessor levelHeightAccessor
	);

	public BoundingBox getBoundingBox() {
		return this.boundingBox;
	}

	public List<StructurePiece> getPieces() {
		return this.pieces;
	}

	public void placeInChunk(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BoundingBox boundingBox,
		ChunkPos chunkPos
	) {
		synchronized (this.pieces) {
			if (!this.pieces.isEmpty()) {
				BoundingBox boundingBox2 = ((StructurePiece)this.pieces.get(0)).boundingBox;
				BlockPos blockPos = boundingBox2.getCenter();
				BlockPos blockPos2 = new BlockPos(blockPos.getX(), boundingBox2.y0, blockPos.getZ());
				Iterator<StructurePiece> iterator = this.pieces.iterator();

				while (iterator.hasNext()) {
					StructurePiece structurePiece = (StructurePiece)iterator.next();
					if (structurePiece.getBoundingBox().intersects(boundingBox)
						&& !structurePiece.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos2)) {
						iterator.remove();
					}
				}

				this.calculateBoundingBox();
			}
		}
	}

	protected void calculateBoundingBox() {
		this.boundingBox = BoundingBox.getUnknownBox();

		for (StructurePiece structurePiece : this.pieces) {
			this.boundingBox.expand(structurePiece.getBoundingBox());
		}
	}

	public CompoundTag createTag(ServerLevel serverLevel, ChunkPos chunkPos) {
		CompoundTag compoundTag = new CompoundTag();
		if (this.isValid()) {
			compoundTag.putString("id", Registry.STRUCTURE_FEATURE.getKey(this.getFeature()).toString());
			compoundTag.putInt("ChunkX", chunkPos.x);
			compoundTag.putInt("ChunkZ", chunkPos.z);
			compoundTag.putInt("references", this.references);
			BoundingBox.CODEC.encodeStart(NbtOps.INSTANCE, this.boundingBox).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("BB", tag));
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

	protected void moveBelowSeaLevel(int i, int j, Random random, int k) {
		int l = i - k;
		int m = this.boundingBox.getYSpan() + j + 1;
		if (m < l) {
			m += random.nextInt(l - m);
		}

		int n = m - this.boundingBox.y1;
		this.boundingBox.move(0, n, 0);

		for (StructurePiece structurePiece : this.pieces) {
			structurePiece.move(0, n, 0);
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
}
