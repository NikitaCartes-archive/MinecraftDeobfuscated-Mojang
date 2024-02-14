package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public abstract class NodeEvaluator {
	protected PathNavigationRegion level;
	protected Mob mob;
	protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<>();
	protected int entityWidth;
	protected int entityHeight;
	protected int entityDepth;
	protected boolean canPassDoors;
	protected boolean canOpenDoors;
	protected boolean canFloat;
	protected boolean canWalkOverFences;

	public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
		this.level = pathNavigationRegion;
		this.mob = mob;
		this.nodes.clear();
		this.entityWidth = Mth.floor(mob.getBbWidth() + 1.0F);
		this.entityHeight = Mth.floor(mob.getBbHeight() + 1.0F);
		this.entityDepth = Mth.floor(mob.getBbWidth() + 1.0F);
	}

	public void done() {
		this.level = null;
		this.mob = null;
	}

	protected Node getNode(BlockPos blockPos) {
		return this.getNode(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	protected Node getNode(int i, int j, int k) {
		return this.nodes.computeIfAbsent(Node.createHash(i, j, k), (Int2ObjectFunction<? extends Node>)(l -> new Node(i, j, k)));
	}

	public abstract Node getStart();

	public abstract Target getTarget(double d, double e, double f);

	protected Target getTargetNodeAt(double d, double e, double f) {
		return new Target(this.getNode(Mth.floor(d), Mth.floor(e), Mth.floor(f)));
	}

	public abstract int getNeighbors(Node[] nodes, Node node);

	public abstract PathType getPathTypeOfMob(BlockGetter blockGetter, int i, int j, int k, Mob mob);

	public abstract PathType getPathType(BlockGetter blockGetter, int i, int j, int k);

	public void setCanPassDoors(boolean bl) {
		this.canPassDoors = bl;
	}

	public void setCanOpenDoors(boolean bl) {
		this.canOpenDoors = bl;
	}

	public void setCanFloat(boolean bl) {
		this.canFloat = bl;
	}

	public void setCanWalkOverFences(boolean bl) {
		this.canWalkOverFences = bl;
	}

	public boolean canPassDoors() {
		return this.canPassDoors;
	}

	public boolean canOpenDoors() {
		return this.canOpenDoors;
	}

	public boolean canFloat() {
		return this.canFloat;
	}

	public boolean canWalkOverFences() {
		return this.canWalkOverFences;
	}

	public static boolean isBurningBlock(BlockState blockState) {
		return blockState.is(BlockTags.FIRE)
			|| blockState.is(Blocks.LAVA)
			|| blockState.is(Blocks.MAGMA_BLOCK)
			|| CampfireBlock.isLitCampfire(blockState)
			|| blockState.is(Blocks.LAVA_CAULDRON);
	}
}
