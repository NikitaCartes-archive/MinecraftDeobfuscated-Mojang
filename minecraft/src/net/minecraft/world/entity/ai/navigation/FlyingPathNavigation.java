package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class FlyingPathNavigation extends PathNavigation {
	public FlyingPathNavigation(Mob mob, Level level) {
		super(mob, level);
	}

	@Override
	protected PathFinder createPathFinder(int i) {
		this.nodeEvaluator = new FlyNodeEvaluator();
		this.nodeEvaluator.setCanPassDoors(true);
		return new PathFinder(this.nodeEvaluator, i);
	}

	@Override
	protected boolean canUpdatePath() {
		return this.canFloat() && this.isInLiquid() || !this.mob.isPassenger();
	}

	@Override
	protected Vec3 getTempMobPos() {
		return this.mob.position();
	}

	@Override
	public Path createPath(Entity entity, int i) {
		return this.createPath(entity.blockPosition(), i);
	}

	@Override
	public void tick() {
		this.tick++;
		if (this.hasDelayedRecomputation) {
			this.recomputePath();
		}

		if (!this.isDone()) {
			if (this.canUpdatePath()) {
				this.followThePath();
			} else if (this.path != null && !this.path.isDone()) {
				Vec3 vec3 = this.path.getNextEntityPos(this.mob);
				if (this.mob.getBlockX() == Mth.floor(vec3.x) && this.mob.getBlockY() == Mth.floor(vec3.y) && this.mob.getBlockZ() == Mth.floor(vec3.z)) {
					this.path.advance();
				}
			}

			DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
			if (!this.isDone()) {
				Vec3 vec3 = this.path.getNextEntityPos(this.mob);
				this.mob.getMoveControl().setWantedPosition(vec3.x, vec3.y, vec3.z, this.speedModifier);
			}
		}
	}

	@Override
	protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec32, int i, int j, int k) {
		int l = Mth.floor(vec3.x);
		int m = Mth.floor(vec3.y);
		int n = Mth.floor(vec3.z);
		double d = vec32.x - vec3.x;
		double e = vec32.y - vec3.y;
		double f = vec32.z - vec3.z;
		double g = d * d + e * e + f * f;
		if (g < 1.0E-8) {
			return false;
		} else {
			double h = 1.0 / Math.sqrt(g);
			d *= h;
			e *= h;
			f *= h;
			double o = 1.0 / Math.abs(d);
			double p = 1.0 / Math.abs(e);
			double q = 1.0 / Math.abs(f);
			double r = (double)l - vec3.x;
			double s = (double)m - vec3.y;
			double t = (double)n - vec3.z;
			if (d >= 0.0) {
				r++;
			}

			if (e >= 0.0) {
				s++;
			}

			if (f >= 0.0) {
				t++;
			}

			r /= d;
			s /= e;
			t /= f;
			int u = d < 0.0 ? -1 : 1;
			int v = e < 0.0 ? -1 : 1;
			int w = f < 0.0 ? -1 : 1;
			int x = Mth.floor(vec32.x);
			int y = Mth.floor(vec32.y);
			int z = Mth.floor(vec32.z);
			int aa = x - l;
			int ab = y - m;
			int ac = z - n;

			while (aa * u > 0 || ab * v > 0 || ac * w > 0) {
				if (r < t && r <= s) {
					r += o;
					l += u;
					aa = x - l;
				} else if (s < r && s <= t) {
					s += p;
					m += v;
					ab = y - m;
				} else {
					t += q;
					n += w;
					ac = z - n;
				}
			}

			return true;
		}
	}

	public void setCanOpenDoors(boolean bl) {
		this.nodeEvaluator.setCanOpenDoors(bl);
	}

	public boolean canPassDoors() {
		return this.nodeEvaluator.canPassDoors();
	}

	public void setCanPassDoors(boolean bl) {
		this.nodeEvaluator.setCanPassDoors(bl);
	}

	public boolean canOpenDoors() {
		return this.nodeEvaluator.canPassDoors();
	}

	@Override
	public boolean isStableDestination(BlockPos blockPos) {
		return this.level.getBlockState(blockPos).entityCanStandOn(this.level, blockPos, this.mob);
	}
}
