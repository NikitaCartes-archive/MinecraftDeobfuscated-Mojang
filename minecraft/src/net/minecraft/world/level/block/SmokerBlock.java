package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SmokerBlock extends AbstractFurnaceBlock {
	public static final MapCodec<SmokerBlock> CODEC = simpleCodec(SmokerBlock::new);

	@Override
	public MapCodec<SmokerBlock> codec() {
		return CODEC;
	}

	protected SmokerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new SmokerBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return createFurnaceTicker(level, blockEntityType, BlockEntityType.SMOKER);
	}

	@Override
	protected void openContainer(Level level, BlockPos blockPos, Player player) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof SmokerBlockEntity) {
			player.openMenu((MenuProvider)blockEntity);
			player.awardStat(Stats.INTERACT_WITH_SMOKER);
		}
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if ((Boolean)blockState.getValue(LIT)) {
			double d = (double)blockPos.getX() + 0.5;
			double e = (double)blockPos.getY();
			double f = (double)blockPos.getZ() + 0.5;
			if (randomSource.nextDouble() < 0.1) {
				level.playLocalSound(d, e, f, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
			}

			level.addParticle(ParticleTypes.SMOKE, d, e + 1.1, f, 0.0, 0.0, 0.0);
		}
	}
}
