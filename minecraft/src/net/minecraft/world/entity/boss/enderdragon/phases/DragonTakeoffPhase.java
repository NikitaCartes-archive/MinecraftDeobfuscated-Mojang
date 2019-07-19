package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class DragonTakeoffPhase extends AbstractDragonPhaseInstance {
	private boolean firstTick;
	private Path currentPath;
	private Vec3 targetLocation;

	public DragonTakeoffPhase(EnderDragon enderDragon) {
		super(enderDragon);
	}

	@Override
	public void doServerTick() {
		if (!this.firstTick && this.currentPath != null) {
			BlockPos blockPos = this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
			if (!blockPos.closerThan(this.dragon.position(), 10.0)) {
				this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
			}
		} else {
			this.firstTick = false;
			this.findNewTarget();
		}
	}

	@Override
	public void begin() {
		this.firstTick = true;
		this.currentPath = null;
		this.targetLocation = null;
	}

	private void findNewTarget() {
		int i = this.dragon.findClosestNode();
		Vec3 vec3 = this.dragon.getHeadLookVector(1.0F);
		int j = this.dragon.findClosestNode(-vec3.x * 40.0, 105.0, -vec3.z * 40.0);
		if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() > 0) {
			j %= 12;
			if (j < 0) {
				j += 12;
			}
		} else {
			j -= 12;
			j &= 7;
			j += 12;
		}

		this.currentPath = this.dragon.findPath(i, j, null);
		this.navigateToNextPathNode();
	}

	private void navigateToNextPathNode() {
		if (this.currentPath != null) {
			this.currentPath.next();
			if (!this.currentPath.isDone()) {
				Vec3 vec3 = this.currentPath.currentPos();
				this.currentPath.next();

				double d;
				do {
					d = vec3.y + (double)(this.dragon.getRandom().nextFloat() * 20.0F);
				} while (d < vec3.y);

				this.targetLocation = new Vec3(vec3.x, d, vec3.z);
			}
		}
	}

	@Nullable
	@Override
	public Vec3 getFlyTargetLocation() {
		return this.targetLocation;
	}

	@Override
	public EnderDragonPhase<DragonTakeoffPhase> getPhase() {
		return EnderDragonPhase.TAKEOFF;
	}
}
