package net.minecraft.world.level.levelgen;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

public class Beardifier {
	public static final Beardifier NO_BEARDS = new Beardifier();
	private static final float[] BEARD_KERNEL = Util.make(new float[13824], fs -> {
		for (int i = 0; i < 24; i++) {
			for (int j = 0; j < 24; j++) {
				for (int k = 0; k < 24; k++) {
					fs[i * 24 * 24 + j * 24 + k] = (float)computeBeardContribution(j - 12, k - 12, i - 12);
				}
			}
		}
	});
	private final ObjectList<StructurePiece> rigids;
	private final ObjectList<JigsawJunction> junctions;
	private final ObjectListIterator<StructurePiece> pieceIterator;
	private final ObjectListIterator<JigsawJunction> junctionIterator;

	protected Beardifier(StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		int i = chunkPos.x;
		int j = chunkPos.z;
		int k = chunkPos.getMinBlockX();
		int l = chunkPos.getMinBlockZ();
		this.junctions = new ObjectArrayList<>(32);
		this.rigids = new ObjectArrayList<>(10);

		for (StructureFeature<?> structureFeature : StructureFeature.NOISE_AFFECTING_FEATURES) {
			structureFeatureManager.startsForFeature(SectionPos.bottomOf(chunkAccess), structureFeature).forEach(structureStart -> {
				for (StructurePiece structurePiece : structureStart.getPieces()) {
					if (structurePiece.isCloseToChunk(chunkPos, 12)) {
						if (structurePiece instanceof PoolElementStructurePiece) {
							PoolElementStructurePiece poolElementStructurePiece = (PoolElementStructurePiece)structurePiece;
							StructureTemplatePool.Projection projection = poolElementStructurePiece.getElement().getProjection();
							if (projection == StructureTemplatePool.Projection.RIGID) {
								this.rigids.add(poolElementStructurePiece);
							}

							for (JigsawJunction jigsawJunction : poolElementStructurePiece.getJunctions()) {
								int kx = jigsawJunction.getSourceX();
								int lx = jigsawJunction.getSourceZ();
								if (kx > k - 12 && lx > l - 12 && kx < k + 15 + 12 && lx < l + 15 + 12) {
									this.junctions.add(jigsawJunction);
								}
							}
						} else {
							this.rigids.add(structurePiece);
						}
					}
				}
			});
		}

		this.pieceIterator = this.rigids.iterator();
		this.junctionIterator = this.junctions.iterator();
	}

	private Beardifier() {
		this.junctions = new ObjectArrayList<>();
		this.rigids = new ObjectArrayList<>();
		this.pieceIterator = this.rigids.iterator();
		this.junctionIterator = this.junctions.iterator();
	}

	protected double beardify(int i, int j, int k) {
		double d = 0.0;

		while (this.pieceIterator.hasNext()) {
			StructurePiece structurePiece = (StructurePiece)this.pieceIterator.next();
			BoundingBox boundingBox = structurePiece.getBoundingBox();
			int l = Math.max(0, Math.max(boundingBox.x0 - i, i - boundingBox.x1));
			int m = j - (boundingBox.y0 + (structurePiece instanceof PoolElementStructurePiece ? ((PoolElementStructurePiece)structurePiece).getGroundLevelDelta() : 0));
			int n = Math.max(0, Math.max(boundingBox.z0 - k, k - boundingBox.z1));
			d += getContribution(l, m, n) * 0.8;
		}

		this.pieceIterator.back(this.rigids.size());

		while (this.junctionIterator.hasNext()) {
			JigsawJunction jigsawJunction = (JigsawJunction)this.junctionIterator.next();
			int o = i - jigsawJunction.getSourceX();
			int l = j - jigsawJunction.getSourceGroundY();
			int m = k - jigsawJunction.getSourceZ();
			d += getContribution(o, l, m) * 0.4;
		}

		this.junctionIterator.back(this.junctions.size());
		return d;
	}

	private static double getContribution(int i, int j, int k) {
		int l = i + 12;
		int m = j + 12;
		int n = k + 12;
		if (l < 0 || l >= 24) {
			return 0.0;
		} else if (m < 0 || m >= 24) {
			return 0.0;
		} else {
			return n >= 0 && n < 24 ? (double)BEARD_KERNEL[n * 24 * 24 + l * 24 + m] : 0.0;
		}
	}

	private static double computeBeardContribution(int i, int j, int k) {
		double d = (double)(i * i + k * k);
		double e = (double)j + 0.5;
		double f = e * e;
		double g = Math.pow(Math.E, -(f / 16.0 + d / 16.0));
		double h = -e * Mth.fastInvSqrt(f / 2.0 + d / 2.0) / 2.0;
		return h * g;
	}
}
