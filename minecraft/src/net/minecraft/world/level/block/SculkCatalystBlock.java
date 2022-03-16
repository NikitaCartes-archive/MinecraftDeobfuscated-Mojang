package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEventListener;

public class SculkCatalystBlock extends BaseEntityBlock {
	public static final int PULSE_TICKS = 8;
	public static final BooleanProperty PULSE = BlockStateProperties.BLOOM;
	private final IntProvider xpRange = ConstantInt.of(20);

	public SculkCatalystBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(PULSE, Boolean.valueOf(false)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PULSE);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if ((Boolean)blockState.getValue(PULSE)) {
			serverLevel.setBlock(blockPos, blockState.setValue(PULSE, Boolean.valueOf(false)), 3);
		}
	}

	public static void bloom(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, Random random) {
		serverLevel.setBlock(blockPos, blockState.setValue(PULSE, Boolean.valueOf(true)), 3);
		serverLevel.scheduleTick(blockPos, blockState.getBlock(), 8);
		serverLevel.sendParticles(
			ParticleTypes.SCULK_SOUL, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.15, (double)blockPos.getZ() + 0.5, 2, 0.2, 0.0, 0.2, 0.0
		);
		serverLevel.playSound(null, blockPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + random.nextFloat() * 0.4F);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new SculkCatalystBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> GameEventListener getListener(Level level, T blockEntity) {
		return blockEntity instanceof SculkCatalystBlockEntity ? (SculkCatalystBlockEntity)blockEntity : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return level.isClientSide ? null : createTickerHelper(blockEntityType, BlockEntityType.SCULK_CATALYST, SculkCatalystBlockEntity::serverTick);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
		super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack);
		this.tryDropExperience(serverLevel, blockPos, itemStack, this.xpRange);
	}
}
