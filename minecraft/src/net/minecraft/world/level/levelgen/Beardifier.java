package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Beardifier implements DensityFunctions.BeardifierOrMarker {
	public static final int BEARD_KERNEL_RADIUS = 12;
	private static final int BEARD_KERNEL_SIZE = 24;
	private static final float[] BEARD_KERNEL = Util.make(new float[13824], fs -> {
		for (int i = 0; i < 24; i++) {
			for (int j = 0; j < 24; j++) {
				for (int k = 0; k < 24; k++) {
					fs[i * 24 * 24 + j * 24 + k] = (float)computeBeardContribution(j - 12, k - 12, i - 12);
				}
			}
		}
	});
	private final ObjectListIterator<Beardifier.Rigid> pieceIterator;
	private final ObjectListIterator<JigsawJunction> junctionIterator;

	public static Beardifier forStructuresInChunk(StructureManager structureManager, ChunkPos chunkPos) {
		int i = chunkPos.getMinBlockX();
		int j = chunkPos.getMinBlockZ();
		ObjectList<Beardifier.Rigid> objectList = new ObjectArrayList<>(10);
		ObjectList<JigsawJunction> objectList2 = new ObjectArrayList<>(32);
		structureManager.startsForStructure(chunkPos, structure -> structure.terrainAdaptation() != TerrainAdjustment.NONE).forEach(structureStart -> {
			TerrainAdjustment terrainAdjustment = structureStart.getStructure().terrainAdaptation();

			for (StructurePiece structurePiece : structureStart.getPieces()) {
				if (structurePiece.isCloseToChunk(chunkPos, 12)) {
					if (structurePiece instanceof PoolElementStructurePiece) {
						PoolElementStructurePiece poolElementStructurePiece = (PoolElementStructurePiece)structurePiece;
						StructureTemplatePool.Projection projection = poolElementStructurePiece.getElement().getProjection();
						if (projection == StructureTemplatePool.Projection.RIGID) {
							objectList.add(new Beardifier.Rigid(poolElementStructurePiece.getBoundingBox(), terrainAdjustment, poolElementStructurePiece.getGroundLevelDelta()));
						}

						for (JigsawJunction jigsawJunction : poolElementStructurePiece.getJunctions()) {
							int k = jigsawJunction.getSourceX();
							int l = jigsawJunction.getSourceZ();
							if (k > i - 12 && l > j - 12 && k < i + 15 + 12 && l < j + 15 + 12) {
								objectList2.add(jigsawJunction);
							}
						}
					} else {
						objectList.add(new Beardifier.Rigid(structurePiece.getBoundingBox(), terrainAdjustment, 0));
					}
				}
			}
		});
		return new Beardifier(objectList.iterator(), objectList2.iterator());
	}

	@VisibleForTesting
	public Beardifier(ObjectListIterator<Beardifier.Rigid> objectListIterator, ObjectListIterator<JigsawJunction> objectListIterator2) {
		this.pieceIterator = objectListIterator;
		this.junctionIterator = objectListIterator2;
	}

	@Override
	public double compute(DensityFunction.FunctionContext functionContext) {
		int i = functionContext.blockX();
		int j = functionContext.blockY();
		int k = functionContext.blockZ();
		double d = 0.0;

		while (this.pieceIterator.hasNext()) {
			Beardifier.Rigid rigid = (Beardifier.Rigid)this.pieceIterator.next();
			BoundingBox boundingBox = rigid.box();
			int l = rigid.groundLevelDelta();
			int m = Math.max(0, Math.max(boundingBox.minX() - i, i - boundingBox.maxX()));
			int n = Math.max(0, Math.max(boundingBox.minZ() - k, k - boundingBox.maxZ()));
			int o = boundingBox.minY() + l;
			int p = j - o;

			int q = switch (rigid.terrainAdjustment()) {
				case NONE -> 0;
				case BURY, BEARD_THIN -> p;
				case BURY_PROPER -> Math.max(0, Math.max(boundingBox.minY() - j, j - boundingBox.maxY()));
				case BEARD_BOX -> Math.max(0, Math.max(o - j, j - boundingBox.maxY()));
			};

			d += switch (rigid.terrainAdjustment()) {
				case NONE -> 0.0;
				case BURY -> getBuryContribution(m, q, n);
				case BEARD_THIN, BEARD_BOX -> getBeardContribution(m, q, n, p) * 0.8;
				case BURY_PROPER -> getBuryXContribution(m, q, n);
			};
		}

		this.pieceIterator.back(Integer.MAX_VALUE);

		while (this.junctionIterator.hasNext()) {
			JigsawJunction jigsawJunction = (JigsawJunction)this.junctionIterator.next();
			int r = i - jigsawJunction.getSourceX();
			int l = j - jigsawJunction.getSourceGroundY();
			int m = k - jigsawJunction.getSourceZ();
			d += getBeardContribution(r, l, m, l) * 0.4;
		}

		this.junctionIterator.back(Integer.MAX_VALUE);
		return d;
	}

	@Override
	public double minValue() {
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public double maxValue() {
		return Double.POSITIVE_INFINITY;
	}

	private static double getBuryContribution(int i, int j, int k) {
		double d = Mth.length((double)i, (double)j / 2.0, (double)k);
		return Mth.clampedMap(d, 0.0, 6.0, 1.0, 0.0);
	}

	private static double getBuryXContribution(int i, int j, int k) {
		double d = Mth.length((double)i, (double)j, (double)k);
		return Mth.clampedMap(d, 0.0, 6.0, 1.0, 0.0);
	}

	private static double getBeardContribution(int i, int j, int k, int l) {
		int m = i + 12;
		int n = j + 12;
		int o = k + 12;
		if (isInKernelRange(m) && isInKernelRange(n) && isInKernelRange(o)) {
			double d = (double)l + 0.5;
			double e = Mth.lengthSquared((double)i, d, (double)k);
			double f = -d * Mth.fastInvSqrt(e / 2.0) / 2.0;
			return f * (double)BEARD_KERNEL[o * 24 * 24 + m * 24 + n];
		} else {
			return 0.0;
		}
	}

	private static boolean isInKernelRange(int i) {
		return i >= 0 && i < 24;
	}

	private static double computeBeardContribution(int i, int j, int k) {
		return computeBeardContribution(i, (double)j + 0.5, k);
	}

	private static double computeBeardContribution(int i, double d, int j) {
		double e = Mth.lengthSquared((double)i, d, (double)j);
		return Math.pow(Math.E, -e / 16.0);
	}

	@VisibleForTesting
	public static record Rigid(BoundingBox box, TerrainAdjustment terrainAdjustment, int groundLevelDelta) {
	}
}
