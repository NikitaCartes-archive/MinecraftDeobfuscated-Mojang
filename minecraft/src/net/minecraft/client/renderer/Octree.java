package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public class Octree {
	private final Octree.Branch root;
	final BlockPos cameraSectionCenter;

	public Octree(SectionPos sectionPos, int i, int j, int k) {
		int l = i * 2 + 1;
		int m = Mth.smallestEncompassingPowerOfTwo(l);
		int n = i * 16;
		BlockPos blockPos = sectionPos.origin();
		this.cameraSectionCenter = sectionPos.center();
		int o = blockPos.getX() - n;
		int p = o + m * 16 - 1;
		int q = m >= j ? k : blockPos.getY() - n;
		int r = q + m * 16 - 1;
		int s = blockPos.getZ() - n;
		int t = s + m * 16 - 1;
		this.root = new Octree.Branch(new BoundingBox(o, q, s, p, r, t));
	}

	public boolean add(SectionRenderDispatcher.RenderSection renderSection) {
		return this.root.add(renderSection);
	}

	public void visitNodes(Octree.OctreeVisitor octreeVisitor, Frustum frustum, int i) {
		this.root.visitNodes(octreeVisitor, false, frustum, 0, i, true);
	}

	boolean isClose(double d, double e, double f, double g, double h, double i, int j) {
		int k = this.cameraSectionCenter.getX();
		int l = this.cameraSectionCenter.getY();
		int m = this.cameraSectionCenter.getZ();
		return (double)k > d - (double)j
			&& (double)k < g + (double)j
			&& (double)l > e - (double)j
			&& (double)l < h + (double)j
			&& (double)m > f - (double)j
			&& (double)m < i + (double)j;
	}

	@Environment(EnvType.CLIENT)
	static enum AxisSorting {
		XYZ(4, 2, 1),
		XZY(4, 1, 2),
		YXZ(2, 4, 1),
		YZX(1, 4, 2),
		ZXY(2, 1, 4),
		ZYX(1, 2, 4);

		final int xShift;
		final int yShift;
		final int zShift;

		private AxisSorting(final int j, final int k, final int l) {
			this.xShift = j;
			this.yShift = k;
			this.zShift = l;
		}

		public static Octree.AxisSorting getAxisSorting(int i, int j, int k) {
			if (i > j && i > k) {
				return j > k ? XYZ : XZY;
			} else if (j > i && j > k) {
				return i > k ? YXZ : YZX;
			} else {
				return i > j ? ZXY : ZYX;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class Branch implements Octree.Node {
		private final Octree.Node[] nodes = new Octree.Node[8];
		private final BoundingBox boundingBox;
		private final int bbCenterX;
		private final int bbCenterY;
		private final int bbCenterZ;
		private final Octree.AxisSorting sorting;
		private final boolean cameraXDiffNegative;
		private final boolean cameraYDiffNegative;
		private final boolean cameraZDiffNegative;

		public Branch(final BoundingBox boundingBox) {
			this.boundingBox = boundingBox;
			this.bbCenterX = this.boundingBox.minX() + this.boundingBox.getXSpan() / 2;
			this.bbCenterY = this.boundingBox.minY() + this.boundingBox.getYSpan() / 2;
			this.bbCenterZ = this.boundingBox.minZ() + this.boundingBox.getZSpan() / 2;
			int i = Octree.this.cameraSectionCenter.getX() - this.bbCenterX;
			int j = Octree.this.cameraSectionCenter.getY() - this.bbCenterY;
			int k = Octree.this.cameraSectionCenter.getZ() - this.bbCenterZ;
			this.sorting = Octree.AxisSorting.getAxisSorting(Math.abs(i), Math.abs(j), Math.abs(k));
			this.cameraXDiffNegative = i < 0;
			this.cameraYDiffNegative = j < 0;
			this.cameraZDiffNegative = k < 0;
		}

		public boolean add(SectionRenderDispatcher.RenderSection renderSection) {
			boolean bl = renderSection.getOrigin().getX() - this.bbCenterX < 0;
			boolean bl2 = renderSection.getOrigin().getY() - this.bbCenterY < 0;
			boolean bl3 = renderSection.getOrigin().getZ() - this.bbCenterZ < 0;
			boolean bl4 = bl != this.cameraXDiffNegative;
			boolean bl5 = bl2 != this.cameraYDiffNegative;
			boolean bl6 = bl3 != this.cameraZDiffNegative;
			int i = getNodeIndex(this.sorting, bl4, bl5, bl6);
			if (this.areChildrenLeaves()) {
				boolean bl7 = this.nodes[i] != null;
				this.nodes[i] = Octree.this.new Leaf(renderSection);
				return !bl7;
			} else if (this.nodes[i] != null) {
				Octree.Branch branch = (Octree.Branch)this.nodes[i];
				return branch.add(renderSection);
			} else {
				BoundingBox boundingBox = this.createChildBoundingBox(bl, bl2, bl3);
				Octree.Branch branch2 = Octree.this.new Branch(boundingBox);
				this.nodes[i] = branch2;
				return branch2.add(renderSection);
			}
		}

		private static int getNodeIndex(Octree.AxisSorting axisSorting, boolean bl, boolean bl2, boolean bl3) {
			int i = 0;
			if (bl) {
				i += axisSorting.xShift;
			}

			if (bl2) {
				i += axisSorting.yShift;
			}

			if (bl3) {
				i += axisSorting.zShift;
			}

			return i;
		}

		private boolean areChildrenLeaves() {
			return this.boundingBox.getXSpan() == 32;
		}

		private BoundingBox createChildBoundingBox(boolean bl, boolean bl2, boolean bl3) {
			int i;
			int j;
			if (bl) {
				i = this.boundingBox.minX();
				j = this.bbCenterX - 1;
			} else {
				i = this.bbCenterX;
				j = this.boundingBox.maxX();
			}

			int k;
			int l;
			if (bl2) {
				k = this.boundingBox.minY();
				l = this.bbCenterY - 1;
			} else {
				k = this.bbCenterY;
				l = this.boundingBox.maxY();
			}

			int m;
			int n;
			if (bl3) {
				m = this.boundingBox.minZ();
				n = this.bbCenterZ - 1;
			} else {
				m = this.bbCenterZ;
				n = this.boundingBox.maxZ();
			}

			return new BoundingBox(i, k, m, j, l, n);
		}

		@Override
		public void visitNodes(Octree.OctreeVisitor octreeVisitor, boolean bl, Frustum frustum, int i, int j, boolean bl2) {
			boolean bl3 = bl;
			if (!bl) {
				int k = frustum.cubeInFrustum(this.boundingBox);
				bl = k == -2;
				bl3 = k == -2 || k == -1;
			}

			if (bl3) {
				bl2 = bl2
					&& Octree.this.isClose(
						(double)this.boundingBox.minX(),
						(double)this.boundingBox.minY(),
						(double)this.boundingBox.minZ(),
						(double)this.boundingBox.maxX(),
						(double)this.boundingBox.maxY(),
						(double)this.boundingBox.maxZ(),
						j
					);
				octreeVisitor.visit(this, bl, i, bl2);

				for (Octree.Node node : this.nodes) {
					if (node != null) {
						node.visitNodes(octreeVisitor, bl, frustum, i + 1, j, bl2);
					}
				}
			}
		}

		@Nullable
		@Override
		public SectionRenderDispatcher.RenderSection getSection() {
			return null;
		}

		@Override
		public AABB getAABB() {
			return new AABB(
				(double)this.boundingBox.minX(),
				(double)this.boundingBox.minY(),
				(double)this.boundingBox.minZ(),
				(double)(this.boundingBox.maxX() + 1),
				(double)(this.boundingBox.maxY() + 1),
				(double)(this.boundingBox.maxZ() + 1)
			);
		}
	}

	@Environment(EnvType.CLIENT)
	final class Leaf implements Octree.Node {
		private final SectionRenderDispatcher.RenderSection section;

		Leaf(final SectionRenderDispatcher.RenderSection renderSection) {
			this.section = renderSection;
		}

		@Override
		public void visitNodes(Octree.OctreeVisitor octreeVisitor, boolean bl, Frustum frustum, int i, int j, boolean bl2) {
			AABB aABB = this.section.getBoundingBox();
			if (bl || frustum.isVisible(this.getSection().getBoundingBox())) {
				bl2 = bl2 && Octree.this.isClose(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, j);
				octreeVisitor.visit(this, bl, i, bl2);
			}
		}

		@Override
		public SectionRenderDispatcher.RenderSection getSection() {
			return this.section;
		}

		@Override
		public AABB getAABB() {
			return this.section.getBoundingBox();
		}
	}

	@Environment(EnvType.CLIENT)
	public interface Node {
		void visitNodes(Octree.OctreeVisitor octreeVisitor, boolean bl, Frustum frustum, int i, int j, boolean bl2);

		@Nullable
		SectionRenderDispatcher.RenderSection getSection();

		AABB getAABB();
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface OctreeVisitor {
		void visit(Octree.Node node, boolean bl, int i, boolean bl2);
	}
}
