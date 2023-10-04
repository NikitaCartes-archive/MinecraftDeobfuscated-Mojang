package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class EndGatewayBlock extends BaseEntityBlock {
	public static final MapCodec<EndGatewayBlock> CODEC = simpleCodec(EndGatewayBlock::new);

	@Override
	public MapCodec<EndGatewayBlock> codec() {
		return CODEC;
	}

	protected EndGatewayBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new TheEndGatewayBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return createTickerHelper(
			blockEntityType, BlockEntityType.END_GATEWAY, level.isClientSide ? TheEndGatewayBlockEntity::beamAnimationTick : TheEndGatewayBlockEntity::teleportTick
		);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof TheEndGatewayBlockEntity) {
			int i = ((TheEndGatewayBlockEntity)blockEntity).getParticleAmount();

			for (int j = 0; j < i; j++) {
				double d = (double)blockPos.getX() + randomSource.nextDouble();
				double e = (double)blockPos.getY() + randomSource.nextDouble();
				double f = (double)blockPos.getZ() + randomSource.nextDouble();
				double g = (randomSource.nextDouble() - 0.5) * 0.5;
				double h = (randomSource.nextDouble() - 0.5) * 0.5;
				double k = (randomSource.nextDouble() - 0.5) * 0.5;
				int l = randomSource.nextInt(2) * 2 - 1;
				if (randomSource.nextBoolean()) {
					f = (double)blockPos.getZ() + 0.5 + 0.25 * (double)l;
					k = (double)(randomSource.nextFloat() * 2.0F * (float)l);
				} else {
					d = (double)blockPos.getX() + 0.5 + 0.25 * (double)l;
					g = (double)(randomSource.nextFloat() * 2.0F * (float)l);
				}

				level.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, k);
			}
		}
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, Fluid fluid) {
		return false;
	}
}
