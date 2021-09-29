package net.minecraft.world.entity.ai.navigation;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public abstract class PathNavigation {
	private static final int MAX_TIME_RECOMPUTE = 20;
	protected final Mob mob;
	protected final Level level;
	@Nullable
	protected Path path;
	protected double speedModifier;
	protected int tick;
	protected int lastStuckCheck;
	protected Vec3 lastStuckCheckPos = Vec3.ZERO;
	protected Vec3i timeoutCachedNode = Vec3i.ZERO;
	protected long timeoutTimer;
	protected long lastTimeoutCheck;
	protected double timeoutLimit;
	protected float maxDistanceToWaypoint = 0.5F;
	protected boolean hasDelayedRecomputation;
	protected long timeLastRecompute;
	protected NodeEvaluator nodeEvaluator;
	@Nullable
	private BlockPos targetPos;
	private int reachRange;
	private float maxVisitedNodesMultiplier = 1.0F;
	private final PathFinder pathFinder;
	private boolean isStuck;

	public PathNavigation(Mob mob, Level level) {
		this.mob = mob;
		this.level = level;
		int i = Mth.floor(mob.getAttributeValue(Attributes.FOLLOW_RANGE) * 16.0);
		this.pathFinder = this.createPathFinder(i);
	}

	public void resetMaxVisitedNodesMultiplier() {
		this.maxVisitedNodesMultiplier = 1.0F;
	}

	public void setMaxVisitedNodesMultiplier(float f) {
		this.maxVisitedNodesMultiplier = f;
	}

	@Nullable
	public BlockPos getTargetPos() {
		return this.targetPos;
	}

	protected abstract PathFinder createPathFinder(int i);

	public void setSpeedModifier(double d) {
		this.speedModifier = d;
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
	public Path createPath(Set<BlockPos> set, int i) {
		return this.createPath(set, 8, false, i);
	}

	@Nullable
	public Path createPath(BlockPos blockPos, int i) {
		return this.createPath(ImmutableSet.of(blockPos), 8, false, i);
	}

	@Nullable
	public Path createPath(BlockPos blockPos, int i, int j) {
		return this.createPath(ImmutableSet.of(blockPos), 8, false, i, (float)j);
	}

	@Nullable
	public Path createPath(Entity entity, int i) {
		return this.createPath(ImmutableSet.of(entity.blockPosition()), 16, true, i);
	}

	@Nullable
	protected Path createPath(Set<BlockPos> set, int i, boolean bl, int j) {
		return this.createPath(set, i, bl, j, (float)this.mob.getAttributeValue(Attributes.FOLLOW_RANGE));
	}

	@Nullable
	protected Path createPath(Set<BlockPos> set, int i, boolean bl, int j, float f) {
		if (set.isEmpty()) {
			return null;
		} else if (this.mob.getY() < (double)this.level.getMinBuildHeight()) {
			return null;
		} else if (!this.canUpdatePath()) {
			return null;
		} else if (this.path != null && !this.path.isDone() && set.contains(this.targetPos)) {
			return this.path;
		} else {
			this.level.getProfiler().push("pathfind");
			BlockPos blockPos = bl ? this.mob.blockPosition().above() : this.mob.blockPosition();
			int k = (int)(f + (float)i);
			PathNavigationRegion pathNavigationRegion = new PathNavigationRegion(this.level, blockPos.offset(-k, -k, -k), blockPos.offset(k, k, k));
			Path path = this.pathFinder.findPath(pathNavigationRegion, this.mob, set, f, j, this.maxVisitedNodesMultiplier);
			this.level.getProfiler().pop();
			if (path != null && path.getTarget() != null) {
				this.targetPos = path.getTarget();
				this.reachRange = j;
				this.resetStuckTimeout();
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

			if (this.isDone()) {
				return false;
			} else {
				this.trimPath();
				if (this.path.getNodeCount() <= 0) {
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
				this.followThePath();
			} else if (this.path != null && !this.path.isDone()) {
				Vec3 vec3 = this.getTempMobPos();
				Vec3 vec32 = this.path.getNextEntityPos(this.mob);
				if (vec3.y > vec32.y && !this.mob.isOnGround() && Mth.floor(vec3.x) == Mth.floor(vec32.x) && Mth.floor(vec3.z) == Mth.floor(vec32.z)) {
					this.path.advance();
				}
			}

			DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
			if (!this.isDone()) {
				Vec3 vec3 = this.path.getNextEntityPos(this.mob);
				this.mob.getMoveControl().setWantedPosition(vec3.x, this.getGroundY(vec3), vec3.z, this.speedModifier);
			}
		}
	}

	protected double getGroundY(Vec3 vec3) {
		BlockPos blockPos = new BlockPos(vec3);
		return this.level.getBlockState(blockPos.below()).isAir() ? vec3.y : WalkNodeEvaluator.getFloorLevel(this.level, blockPos);
	}

	protected void followThePath() {
		Vec3 vec3 = this.getTempMobPos();
		this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
		Vec3i vec3i = this.path.getNextNodePos();
		double d = Math.abs(this.mob.getX() - ((double)vec3i.getX() + 0.5));
		double e = Math.abs(this.mob.getY() - (double)vec3i.getY());
		double f = Math.abs(this.mob.getZ() - ((double)vec3i.getZ() + 0.5));
		boolean bl = d < (double)this.maxDistanceToWaypoint && f < (double)this.maxDistanceToWaypoint && e < 1.0;
		if (bl || this.mob.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(vec3)) {
			this.path.advance();
		}

		this.doStuckDetection(vec3);
	}

	private boolean shouldTargetNextNodeInDirection(Vec3 vec3) {
		if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
			return false;
		} else {
			Vec3 vec32 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
			if (!vec3.closerThan(vec32, 2.0)) {
				return false;
			} else if (this.canMoveDirectly(vec3, this.path.getNextEntityPos(this.mob))) {
				return true;
			} else {
				Vec3 vec33 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
				Vec3 vec34 = vec33.subtract(vec32);
				Vec3 vec35 = vec3.subtract(vec32);
				return vec34.dot(vec35) > 0.0;
			}
		}
	}

	protected void doStuckDetection(Vec3 vec3) {
		if (this.tick - this.lastStuckCheck > 100) {
			if (vec3.distanceToSqr(this.lastStuckCheckPos) < 2.25) {
				this.isStuck = true;
				this.stop();
			} else {
				this.isStuck = false;
			}

			this.lastStuckCheck = this.tick;
			this.lastStuckCheckPos = vec3;
		}

		if (this.path != null && !this.path.isDone()) {
			Vec3i vec3i = this.path.getNextNodePos();
			if (vec3i.equals(this.timeoutCachedNode)) {
				this.timeoutTimer = this.timeoutTimer + (Util.getMillis() - this.lastTimeoutCheck);
			} else {
				this.timeoutCachedNode = vec3i;
				double d = vec3.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode));
				this.timeoutLimit = this.mob.getSpeed() > 0.0F ? d / (double)this.mob.getSpeed() * 1000.0 : 0.0;
			}

			if (this.timeoutLimit > 0.0 && (double)this.timeoutTimer > this.timeoutLimit * 3.0) {
				this.timeoutPath();
			}

			this.lastTimeoutCheck = Util.getMillis();
		}
	}

	private void timeoutPath() {
		this.resetStuckTimeout();
		this.stop();
	}

	private void resetStuckTimeout() {
		this.timeoutCachedNode = Vec3i.ZERO;
		this.timeoutTimer = 0L;
		this.timeoutLimit = 0.0;
		this.isStuck = false;
	}

	public boolean isDone() {
		return this.path == null || this.path.isDone();
	}

	public boolean isInProgress() {
		return !this.isDone();
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
			for (int i = 0; i < this.path.getNodeCount(); i++) {
				Node node = this.path.getNode(i);
				Node node2 = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
				BlockState blockState = this.level.getBlockState(new BlockPos(node.x, node.y, node.z));
				if (blockState.is(BlockTags.CAULDRONS)) {
					this.path.replaceNode(i, node.cloneAndMove(node.x, node.y + 1, node.z));
					if (node2 != null && node.y >= node2.y) {
						this.path.replaceNode(i + 1, node.cloneAndMove(node2.x, node.y + 1, node2.z));
					}
				}
			}
		}
	}

	protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec32) {
		return false;
	}

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
		if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
			Node node = this.path.getEndNode();
			Vec3 vec3 = new Vec3(((double)node.x + this.mob.getX()) / 2.0, ((double)node.y + this.mob.getY()) / 2.0, ((double)node.z + this.mob.getZ()) / 2.0);
			if (blockPos.closerThan(vec3, (double)(this.path.getNodeCount() - this.path.getNextNodeIndex()))) {
				this.recomputePath();
			}
		}
	}

	public float getMaxDistanceToWaypoint() {
		return this.maxDistanceToWaypoint;
	}

	public boolean isStuck() {
		return this.isStuck;
	}
}
