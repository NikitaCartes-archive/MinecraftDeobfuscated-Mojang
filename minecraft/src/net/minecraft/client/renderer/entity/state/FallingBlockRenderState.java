package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

@Environment(EnvType.CLIENT)
public class FallingBlockRenderState extends EntityRenderState implements BlockAndTintGetter {
	public BlockPos startBlockPos = BlockPos.ZERO;
	public BlockPos blockPos = BlockPos.ZERO;
	public BlockState blockState = Blocks.SAND.defaultBlockState();
	@Nullable
	public Holder<Biome> biome;
	public BlockAndTintGetter level = EmptyBlockAndTintGetter.INSTANCE;

	@Override
	public float getShade(Direction direction, boolean bl) {
		return this.level.getShade(direction, bl);
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return this.level.getLightEngine();
	}

	@Override
	public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
		return this.biome == null ? -1 : colorResolver.getColor(this.biome.value(), (double)blockPos.getX(), (double)blockPos.getZ());
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		return blockPos.equals(this.blockPos) ? this.blockState : Blocks.AIR.defaultBlockState();
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		return this.getBlockState(blockPos).getFluidState();
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@Override
	public int getMinBuildHeight() {
		return this.blockPos.getY();
	}
}
