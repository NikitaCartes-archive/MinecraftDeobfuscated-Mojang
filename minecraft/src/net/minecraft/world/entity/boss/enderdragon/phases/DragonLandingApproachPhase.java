package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class DragonLandingApproachPhase extends AbstractDragonPhaseInstance {
	private static final TargetingConditions NEAR_EGG_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight();
	@Nullable
	private Path currentPath;
	@Nullable
	private Vec3 targetLocation;

	public DragonLandingApproachPhase(EnderDragon enderDragon) {
		super(enderDragon);
	}

	@Override
	public EnderDragonPhase<DragonLandingApproachPhase> getPhase() {
		return EnderDragonPhase.LANDING_APPROACH;
	}

	@Override
	public void begin() {
		this.currentPath = null;
		this.targetLocation = null;
	}

	@Override
	public void doServerTick() {
		double d = this.targetLocation == null ? 0.0 : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
		if (d < 100.0 || d > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
			this.findNewTarget();
		}
	}

	@Nullable
	@Override
	public Vec3 getFlyTargetLocation() {
		return this.targetLocation;
	}

	private void findNewTarget() {
		if (this.currentPath == null || this.currentPath.isDone()) {
			int i = this.dragon.findClosestNode();
			BlockPos blockPos = this.dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
			Player player = this.dragon
				.level()
				.getNearestPlayer(NEAR_EGG_TARGETING, this.dragon, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
			int j;
			if (player != null) {
				Vec3 vec3 = new Vec3(player.getX(), 0.0, player.getZ()).normalize();
				j = this.dragon.findClosestNode(-vec3.x * 40.0, 105.0, -vec3.z * 40.0);
			} else {
				j = this.dragon.findClosestNode(40.0, (double)blockPos.getY(), 0.0);
			}

			Node node = new Node(blockPos.getX(), blockPos.getY(), blockPos.getZ());
			this.currentPath = this.dragon.findPath(i, j, node);
			if (this.currentPath != null) {
				this.currentPath.advance();
			}
		}

		this.navigateToNextPathNode();
		if (this.currentPath != null && this.currentPath.isDone()) {
			this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING);
		}
	}

	private void navigateToNextPathNode() {
		if (this.currentPath != null && !this.currentPath.isDone()) {
			Vec3i vec3i = this.currentPath.getNextNodePos();
			this.currentPath.advance();
			double d = (double)vec3i.getX();
			double e = (double)vec3i.getZ();

			double f;
			do {
				f = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
			} while (f < (double)vec3i.getY());

			this.targetLocation = new Vec3(d, f, e);
		}
	}
}
