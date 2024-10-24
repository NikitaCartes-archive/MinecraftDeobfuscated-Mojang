package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlastFurnaceBlock extends AbstractFurnaceBlock {
	public static final MapCodec<BlastFurnaceBlock> CODEC = simpleCodec(BlastFurnaceBlock::new);

	@Override
	public MapCodec<BlastFurnaceBlock> codec() {
		return CODEC;
	}

	protected BlastFurnaceBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new BlastFurnaceBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return createFurnaceTicker(level, blockEntityType, BlockEntityType.BLAST_FURNACE);
	}

	@Override
	protected void openContainer(Level level, BlockPos blockPos, Player player) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof BlastFurnaceBlockEntity) {
			player.openMenu((MenuProvider)blockEntity);
			player.awardStat(Stats.INTERACT_WITH_BLAST_FURNACE);
		}
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if ((Boolean)blockState.getValue(LIT)) {
			double d = (double)blockPos.getX() + 0.5;
			double e = (double)blockPos.getY();
			double f = (double)blockPos.getZ() + 0.5;
			if (randomSource.nextDouble() < 0.1) {
				level.playLocalSound(d, e, f, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
			}

			Direction direction = blockState.getValue(FACING);
			Direction.Axis axis = direction.getAxis();
			double g = 0.52;
			double h = randomSource.nextDouble() * 0.6 - 0.3;
			double i = axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52 : h;
			double j = randomSource.nextDouble() * 9.0 / 16.0;
			double k = axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52 : h;
			level.addParticle(ParticleTypes.SMOKE, d + i, e + j, f + k, 0.0, 0.0, 0.0);
		}
	}
}
