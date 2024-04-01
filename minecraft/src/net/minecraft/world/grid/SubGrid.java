package net.minecraft.world.grid;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SubGrid implements BlockGetter, CollisionGetter {
	protected final Level level;
	protected final GridCarrier carrier;
	private SubGridBlocks blocks = new SubGridBlocks(0, 0, 0);
	protected Holder<Biome> biome;
	private AABB boundingBox = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

	public SubGrid(Level level, GridCarrier gridCarrier) {
		this.level = level;
		this.carrier = gridCarrier;
		this.biome = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
		this.updatePosition(gridCarrier.getX(), gridCarrier.getY(), gridCarrier.getZ());
	}

	public void updatePosition(double d, double e, double f) {
		this.boundingBox = new AABB(d, e, f, d + (double)this.blocks.sizeX() + 1.0, e + (double)this.blocks.sizeY() + 1.0, f + (double)this.blocks.sizeZ() + 1.0);
	}

	public void setBlocks(SubGridBlocks subGridBlocks) {
		this.blocks = subGridBlocks;
		this.updatePosition(this.carrier.getX(), this.carrier.getY(), this.carrier.getZ());
	}

	public void setBiome(Holder<Biome> holder) {
		this.biome = holder;
	}

	public Level level() {
		return this.level;
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		return this.blocks.getBlockState(blockPos);
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		return this.getBlockState(blockPos).getFluidState();
	}

	@Override
	public boolean isPotato() {
		return false;
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		return null;
	}

	@Override
	public int getHeight() {
		return this.blocks.sizeY();
	}

	@Override
	public int getMinBuildHeight() {
		return 0;
	}

	public UUID id() {
		return this.carrier.getUUID();
	}

	public GridCarrier carrier() {
		return this.carrier;
	}

	public SubGridBlocks getBlocks() {
		return this.blocks;
	}

	public Holder<Biome> getBiome() {
		return this.biome;
	}

	public AABB getNextBoundingBox() {
		return this.boundingBox;
	}

	public AABB getKnownBoundingBox() {
		Vec3 vec3 = this.getLastMovement();
		return this.boundingBox.move(-vec3.x, -vec3.y, -vec3.z);
	}

	@Override
	public WorldBorder getWorldBorder() {
		return this.level.getWorldBorder();
	}

	@Nullable
	@Override
	public BlockGetter getChunkForCollisions(int i, int j) {
		return this;
	}

	@Override
	public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB) {
		return List.of();
	}

	public Vec3 getLastMovement() {
		return new Vec3(this.carrier.getX() - this.carrier.xOld, this.carrier.getY() - this.carrier.yOld, this.carrier.getZ() - this.carrier.zOld);
	}
}
