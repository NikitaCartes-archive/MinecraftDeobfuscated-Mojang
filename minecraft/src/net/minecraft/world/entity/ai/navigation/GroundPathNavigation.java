package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class GroundPathNavigation extends PathNavigation {
	private boolean avoidSun;

	public GroundPathNavigation(Mob mob, Level level) {
		super(mob, level);
	}

	@Override
	protected PathFinder createPathFinder(int i) {
		this.nodeEvaluator = new WalkNodeEvaluator();
		this.nodeEvaluator.setCanPassDoors(true);
		return new PathFinder(this.nodeEvaluator, i);
	}

	@Override
	protected boolean canUpdatePath() {
		return this.mob.isOnGround() || this.isInLiquid() || this.mob.isPassenger();
	}

	@Override
	protected Vec3 getTempMobPos() {
		return new Vec3(this.mob.getX(), (double)this.getSurfaceY(), this.mob.getZ());
	}

	@Override
	public Path createPath(BlockPos blockPos, int i) {
		if (this.level.getBlockState(blockPos).isAir()) {
			BlockPos blockPos2 = blockPos.below();

			while (blockPos2.getY() > 0 && this.level.getBlockState(blockPos2).isAir()) {
				blockPos2 = blockPos2.below();
			}

			if (blockPos2.getY() > 0) {
				return super.createPath(blockPos2.above(), i);
			}

			while (blockPos2.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockPos2).isAir()) {
				blockPos2 = blockPos2.above();
			}

			blockPos = blockPos2;
		}

		if (!this.level.getBlockState(blockPos).getMaterial().isSolid()) {
			return super.createPath(blockPos, i);
		} else {
			BlockPos blockPos2 = blockPos.above();

			while (blockPos2.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockPos2).getMaterial().isSolid()) {
				blockPos2 = blockPos2.above();
			}

			return super.createPath(blockPos2, i);
		}
	}

	@Override
	public Path createPath(Entity entity, int i) {
		return this.createPath(entity.blockPosition(), i);
	}

	private int getSurfaceY() {
		if (this.mob.isInWater() && this.canFloat()) {
			int i = Mth.floor(this.mob.getY());
			Block block = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)i, this.mob.getZ())).getBlock();
			int j = 0;

			while (block == Blocks.WATER) {
				block = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)(++i), this.mob.getZ())).getBlock();
				if (++j > 16) {
					return Mth.floor(this.mob.getY());
				}
			}

			return i;
		} else {
			return Mth.floor(this.mob.getY() + 0.5);
		}
	}

	@Override
	protected void trimPath() {
		super.trimPath();
		if (this.avoidSun) {
			if (this.level.canSeeSky(new BlockPos(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ()))) {
				return;
			}

			for (int i = 0; i < this.path.getNodeCount(); i++) {
				Node node = this.path.getNode(i);
				if (this.level.canSeeSky(new BlockPos(node.x, node.y, node.z))) {
					this.path.truncateNodes(i);
					return;
				}
			}
		}
	}

	@Override
	protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec32, int i, int j, int k) {
		int l = Mth.floor(vec3.x);
		int m = Mth.floor(vec3.z);
		double d = vec32.x - vec3.x;
		double e = vec32.z - vec3.z;
		double f = d * d + e * e;
		if (f < 1.0E-8) {
			return false;
		} else {
			double g = 1.0 / Math.sqrt(f);
			d *= g;
			e *= g;
			i += 2;
			k += 2;
			if (!this.canWalkOn(l, Mth.floor(vec3.y), m, i, j, k, vec3, d, e)) {
				return false;
			} else {
				i -= 2;
				k -= 2;
				double h = 1.0 / Math.abs(d);
				double n = 1.0 / Math.abs(e);
				double o = (double)l - vec3.x;
				double p = (double)m - vec3.z;
				if (d >= 0.0) {
					o++;
				}

				if (e >= 0.0) {
					p++;
				}

				o /= d;
				p /= e;
				int q = d < 0.0 ? -1 : 1;
				int r = e < 0.0 ? -1 : 1;
				int s = Mth.floor(vec32.x);
				int t = Mth.floor(vec32.z);
				int u = s - l;
				int v = t - m;

				while (u * q > 0 || v * r > 0) {
					if (o < p) {
						o += h;
						l += q;
						u = s - l;
					} else {
						p += n;
						m += r;
						v = t - m;
					}

					if (!this.canWalkOn(l, Mth.floor(vec3.y), m, i, j, k, vec3, d, e)) {
						return false;
					}
				}

				return true;
			}
		}
	}

	private boolean canWalkOn(int i, int j, int k, int l, int m, int n, Vec3 vec3, double d, double e) {
		int o = i - l / 2;
		int p = k - n / 2;
		if (!this.canWalkAbove(o, j, p, l, m, n, vec3, d, e)) {
			return false;
		} else {
			for (int q = o; q < o + l; q++) {
				for (int r = p; r < p + n; r++) {
					double f = (double)q + 0.5 - vec3.x;
					double g = (double)r + 0.5 - vec3.z;
					if (!(f * d + g * e < 0.0)) {
						BlockPathTypes blockPathTypes = this.nodeEvaluator.getBlockPathType(this.level, q, j - 1, r, this.mob, l, m, n, true, true);
						if (!this.hasValidPathType(blockPathTypes)) {
							return false;
						}

						blockPathTypes = this.nodeEvaluator.getBlockPathType(this.level, q, j, r, this.mob, l, m, n, true, true);
						float h = this.mob.getPathfindingMalus(blockPathTypes);
						if (h < 0.0F || h >= 8.0F) {
							return false;
						}

						if (blockPathTypes == BlockPathTypes.DAMAGE_FIRE || blockPathTypes == BlockPathTypes.DANGER_FIRE || blockPathTypes == BlockPathTypes.DAMAGE_OTHER) {
							return false;
						}
					}
				}
			}

			return true;
		}
	}

	protected boolean hasValidPathType(BlockPathTypes blockPathTypes) {
		if (blockPathTypes == BlockPathTypes.WATER) {
			return false;
		} else {
			return blockPathTypes == BlockPathTypes.LAVA ? false : blockPathTypes != BlockPathTypes.OPEN;
		}
	}

	private boolean canWalkAbove(int i, int j, int k, int l, int m, int n, Vec3 vec3, double d, double e) {
		for (BlockPos blockPos : BlockPos.betweenClosed(new BlockPos(i, j, k), new BlockPos(i + l - 1, j + m - 1, k + n - 1))) {
			double f = (double)blockPos.getX() + 0.5 - vec3.x;
			double g = (double)blockPos.getZ() + 0.5 - vec3.z;
			if (!(f * d + g * e < 0.0) && !this.level.getBlockState(blockPos).isPathfindable(this.level, blockPos, PathComputationType.LAND)) {
				return false;
			}
		}

		return true;
	}

	public void setCanOpenDoors(boolean bl) {
		this.nodeEvaluator.setCanOpenDoors(bl);
	}

	public boolean canOpenDoors() {
		return this.nodeEvaluator.canPassDoors();
	}

	public void setAvoidSun(boolean bl) {
		this.avoidSun = bl;
	}
}
