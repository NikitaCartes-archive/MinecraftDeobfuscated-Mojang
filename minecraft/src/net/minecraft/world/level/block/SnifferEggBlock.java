package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SnifferEggBlock extends Block {
	public static final int MAX_HATCH_LEVEL = 2;
	public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
	private static final int REGULAR_HATCH_TIME_TICKS = 24000;
	private static final int BOOSTED_HATCH_TIME_TICKS = 12000;
	private static final int RANDOM_HATCH_OFFSET_TICKS = 300;
	private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 2.0, 15.0, 16.0, 14.0);

	public SnifferEggBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, Integer.valueOf(0)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HATCH);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	public int getHatchLevel(BlockState blockState) {
		return (Integer)blockState.getValue(HATCH);
	}

	private boolean isReadyToHatch(BlockState blockState) {
		return this.getHatchLevel(blockState) == 2;
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return levelReader.getBlockState(blockPos).getFluidState().is(Fluids.EMPTY);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (!this.isReadyToHatch(blockState)) {
			serverLevel.playSound(null, blockPos, SoundEvents.SNIFFER_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + randomSource.nextFloat() * 0.2F);
			serverLevel.setBlock(blockPos, blockState.setValue(HATCH, Integer.valueOf(this.getHatchLevel(blockState) + 1)), 2);
		} else {
			serverLevel.playSound(null, blockPos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + randomSource.nextFloat() * 0.2F);
			serverLevel.destroyBlock(blockPos, false);
			Sniffer sniffer = EntityType.SNIFFER.create(serverLevel);
			if (sniffer != null) {
				Vec3 vec3 = blockPos.getCenter();
				sniffer.setBaby(true);
				sniffer.moveTo(vec3.x(), vec3.y(), vec3.z(), Mth.wrapDegrees(serverLevel.random.nextFloat() * 360.0F), 0.0F);
				serverLevel.addFreshEntity(sniffer);
			}
		}
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		boolean bl2 = hatchBoost(level, blockPos);
		if (!level.isClientSide()) {
			level.levelEvent(3009, blockPos, bl2 ? 1 : 0);
		}

		int i = bl2 ? 12000 : 24000;
		int j = i / 3;
		level.gameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Context.of(blockState));
		level.scheduleTick(blockPos, this, j + level.random.nextInt(300));
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}

	public static boolean hatchBoost(BlockGetter blockGetter, BlockPos blockPos) {
		return blockGetter.getBlockState(blockPos.below()).is(BlockTags.SNIFFER_EGG_HATCH_BOOST);
	}
}
