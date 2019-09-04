package net.minecraft.world.entity.ai.navigation;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public abstract class PathNavigation {
	protected final Mob mob;
	protected final Level level;
	@Nullable
	protected Path path;
	protected double speedModifier;
	private final AttributeInstance dist;
	protected int tick;
	protected int lastStuckCheck;
	protected Vec3 lastStuckCheckPos = Vec3.ZERO;
	protected Vec3 timeoutCachedNode = Vec3.ZERO;
	protected long timeoutTimer;
	protected long lastTimeoutCheck;
	protected double timeoutLimit;
	protected float maxDistanceToWaypoint = 0.5F;
	protected boolean hasDelayedRecomputation;
	protected long timeLastRecompute;
	protected NodeEvaluator nodeEvaluator;
	private BlockPos targetPos;
	private int reachRange;
	private PathFinder pathFinder;

	public PathNavigation(Mob mob, Level level) {
		this.mob = mob;
		this.level = level;
		this.dist = mob.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
		this.pathFinder = this.createPathFinder(Mth.floor(this.dist.getValue() * 16.0));
	}

	public BlockPos getTargetPos() {
		return this.targetPos;
	}

	protected abstract PathFinder createPathFinder(int i);

	public void setSpeedModifier(double d) {
		this.speedModifier = d;
	}

	public float getMaxDist() {
		return (float)this.dist.getValue();
	}

	public boolean hasDelayedRecomputation() {
		return this.hasDelayedRecomputation;
	}

	public void recomputePath() {
		if (this.level.getGameTime() - this.timeLastRecompute > 20L) {
			if (this.targetPos != null) {
				this.path = null;
				this.path = this.createPath(this.targetPos, this.reachRange);
				this.timeLastRecompute = this.level.getGameTime();
				this.hasDelayedRecomputation = false;
			}
		} else {
			this.hasDelayedRecomputation = true;
		}
	}

	@Nullable
	public final Path createPath(double d, double e, double f, int i) {
		return this.createPath(new BlockPos(d, e, f), i);
	}

	@Nullable
	public Path createPath(Stream<BlockPos> stream, int i) {
		return this.createPath((Set<BlockPos>)stream.collect(Collectors.toSet()), 8, false, i);
	}

	@Nullable
	public Path createPath(BlockPos blockPos, int i) {
		return this.createPath(ImmutableSet.of(blockPos), 8, false, i);
	}

	@Nullable
	public Path createPath(Entity entity, int i) {
		return this.createPath(ImmutableSet.of(new BlockPos(entity)), 16, true, i);
	}

	@Nullable
	protected Path createPath(Set<BlockPos> set, int i, boolean bl, int j) {
		if (set.isEmpty()) {
			return null;
		} else if (this.mob.y < 0.0) {
			return null;
		} else if (!this.canUpdatePath()) {
			return null;
		} else if (this.path != null && !this.path.isDone() && set.contains(this.targetPos)) {
			return this.path;
		} else {
			this.level.getProfiler().push("pathfind");
			float f = this.getMaxDist();
			BlockPos blockPos = bl ? new BlockPos(this.mob).above() : new BlockPos(this.mob);
			int k = (int)(f + (float)i);
			PathNavigationRegion pathNavigationRegion = new PathNavigationRegion(this.level, blockPos.offset(-k, -k, -k), blockPos.offset(k, k, k));
			Path path = this.pathFinder.findPath(pathNavigationRegion, this.mob, set, f, j);
			this.level.getProfiler().pop();
			if (path != null && path.getTarget() != null) {
				this.targetPos = path.getTarget();
				this.reachRange = j;
			}

			return path;
		}
	}

	public boolean moveTo(double d, double e, double f, double g) {
		return this.moveTo(this.createPath(d, e, f, 1), g);
	}

	public boolean moveTo(Entity entity, double d) {
		Path path = this.createPath(entity, 1);
		return path != null && this.moveTo(path, d);
	}

	public boolean moveTo(@Nullable Path path, double d) {
		if (path == null) {
			this.path = null;
			return false;
		} else {
			if (!path.sameAs(this.path)) {
				this.path = path;
			}

			this.trimPath();
			if (this.path.getSize() <= 0) {
				return false;
			} else {
				this.speedModifier = d;
				Vec3 vec3 = this.getTempMobPos();
				this.lastStuckCheck = this.tick;
				this.lastStuckCheckPos = vec3;
				return true;
			}
		}
	}

	@Nullable
	public Path getPath() {
		return this.path;
	}

	public void tick() {
		this.tick++;
		if (this.hasDelayedRecomputation) {
			this.recomputePath();
		}

		if (!this.isDone()) {
			if (this.canUpdatePath()) {
				this.updatePath();
			} else if (this.path != null && this.path.getIndex() < this.path.getSize()) {
				Vec3 vec3 = this.getTempMobPos();
				Vec3 vec32 = this.path.getPos(this.mob, this.path.getIndex());
				if (vec3.y > vec32.y && !this.mob.onGround && Mth.floor(vec3.x) == Mth.floor(vec32.x) && Mth.floor(vec3.z) == Mth.floor(vec32.z)) {
					this.path.setIndex(this.path.getIndex() + 1);
				}
			}

			DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
			if (!this.isDone()) {
				Vec3 vec3 = this.path.currentPos(this.mob);
				BlockPos blockPos = new BlockPos(vec3);
				this.mob
					.getMoveControl()
					.setWantedPosition(
						vec3.x, this.level.getBlockState(blockPos.below()).isAir() ? vec3.y : WalkNodeEvaluator.getFloorLevel(this.level, blockPos), vec3.z, this.speedModifier
					);
			}
		}
	}

	protected void updatePath() {
		Vec3 vec3 = this.getTempMobPos();
		this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
		Vec3 vec32 = this.path.currentPos();
		if (Math.abs(this.mob.x - (vec32.x + 0.5)) < (double)this.maxDistanceToWaypoint
			&& Math.abs(this.mob.z - (vec32.z + 0.5)) < (double)this.maxDistanceToWaypoint
			&& Math.abs(this.mob.y - vec32.y) < 1.0) {
			this.path.setIndex(this.path.getIndex() + 1);
		}

		this.doStuckDetection(vec3);
	}

	protected void doStuckDetection(Vec3 vec3) {
		if (this.tick - this.lastStuckCheck > 100) {
			if (vec3.distanceToSqr(this.lastStuckCheckPos) < 2.25) {
				this.stop();
			}

			this.lastStuckCheck = this.tick;
			this.lastStuckCheckPos = vec3;
		}

		if (this.path != null && !this.path.isDone()) {
			Vec3 vec32 = this.path.currentPos();
			if (vec32.equals(this.timeoutCachedNode)) {
				this.timeoutTimer = this.timeoutTimer + (Util.getMillis() - this.lastTimeoutCheck);
			} else {
				this.timeoutCachedNode = vec32;
				double d = vec3.distanceTo(this.timeoutCachedNode);
				this.timeoutLimit = this.mob.getSpeed() > 0.0F ? d / (double)this.mob.getSpeed() * 1000.0 : 0.0;
			}

			if (this.timeoutLimit > 0.0 && (double)this.timeoutTimer > this.timeoutLimit * 3.0) {
				this.timeoutCachedNode = Vec3.ZERO;
				this.timeoutTimer = 0L;
				this.timeoutLimit = 0.0;
				this.stop();
			}

			this.lastTimeoutCheck = Util.getMillis();
		}
	}

	public boolean isDone() {
		return this.path == null || this.path.isDone();
	}

	public void stop() {
		this.path = null;
	}

	protected abstract Vec3 getTempMobPos();

	protected abstract boolean canUpdatePath();

	protected boolean isInLiquid() {
		return this.mob.isInWaterOrBubble() || this.mob.isInLava();
	}

	protected void trimPath() {
		if (this.path != null) {
			for (int i = 0; i < this.path.getSize(); i++) {
				Node node = this.path.get(i);
				Node node2 = i + 1 < this.path.getSize() ? this.path.get(i + 1) : null;
				BlockState blockState = this.level.getBlockState(new BlockPos(node.x, node.y, node.z));
				Block block = blockState.getBlock();
				if (block == Blocks.CAULDRON) {
					this.path.set(i, node.cloneMove(node.x, node.y + 1, node.z));
					if (node2 != null && node.y >= node2.y) {
						this.path.set(i + 1, node2.cloneMove(node2.x, node.y + 1, node2.z));
					}
				}
			}
		}
	}

	protected abstract boolean canMoveDirectly(Vec3 vec3, Vec3 vec32, int i, int j, int k);

	public boolean isStableDestination(BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		return this.level.getBlockState(blockPos2).isSolidRender(this.level, blockPos2);
	}

	public NodeEvaluator getNodeEvaluator() {
		return this.nodeEvaluator;
	}

	public void setCanFloat(boolean bl) {
		this.nodeEvaluator.setCanFloat(bl);
	}

	public boolean canFloat() {
		return this.nodeEvaluator.canFloat();
	}

	public void recomputePath(BlockPos blockPos) {
		if (this.path != null && !this.path.isDone() && this.path.getSize() != 0) {
			Node node = this.path.last();
			Vec3 vec3 = new Vec3(((double)node.x + this.mob.x) / 2.0, ((double)node.y + this.mob.y) / 2.0, ((double)node.z + this.mob.z) / 2.0);
			if (blockPos.closerThan(vec3, (double)(this.path.getSize() - this.path.getIndex()))) {
				this.recomputePath();
			}
		}
	}
}
