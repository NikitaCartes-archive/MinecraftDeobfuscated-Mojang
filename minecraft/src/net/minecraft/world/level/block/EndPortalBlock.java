package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EndPortalBlock extends BaseEntityBlock {
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

	protected EndPortalBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockGetter blockGetter) {
		return new TheEndPortalBlockEntity();
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!level.isClientSide
			&& !entity.isPassenger()
			&& !entity.isVehicle()
			&& entity.canChangeDimensions()
			&& Shapes.joinIsNotEmpty(
				Shapes.create(entity.getBoundingBox().move((double)(-blockPos.getX()), (double)(-blockPos.getY()), (double)(-blockPos.getZ()))),
				blockState.getShape(level, blockPos),
				BooleanOp.AND
			)) {
			entity.changeDimension(level.dimension.getType() == DimensionType.THE_END ? DimensionType.OVERWORLD : DimensionType.THE_END);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		double d = (double)((float)blockPos.getX() + random.nextFloat());
		double e = (double)((float)blockPos.getY() + 0.8F);
		double f = (double)((float)blockPos.getZ() + random.nextFloat());
		double g = 0.0;
		double h = 0.0;
		double i = 0.0;
		level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, Fluid fluid) {
		return false;
	}
}
