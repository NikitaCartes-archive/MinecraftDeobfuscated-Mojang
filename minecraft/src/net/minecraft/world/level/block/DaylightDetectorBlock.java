package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DaylightDetectorBlock extends BaseEntityBlock {
	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);

	public DaylightDetectorBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)).setValue(INVERTED, Boolean.valueOf(false)));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return (Integer)blockState.getValue(POWER);
	}

	public static void updateSignalStrength(BlockState blockState, Level level, BlockPos blockPos) {
		if (level.dimensionType().hasSkyLight()) {
			int i = level.getBrightness(LightLayer.SKY, blockPos) - level.getSkyDarken();
			float f = level.getSunAngle(1.0F);
			boolean bl = (Boolean)blockState.getValue(INVERTED);
			if (bl) {
				i = 15 - i;
			} else if (i > 0) {
				float g = f < (float) Math.PI ? 0.0F : (float) (Math.PI * 2);
				f += (g - f) * 0.2F;
				i = Math.round((float)i * Mth.cos(f));
			}

			i = Mth.clamp(i, 0, 15);
			if ((Integer)blockState.getValue(POWER) != i) {
				level.setBlock(blockPos, blockState.setValue(POWER, Integer.valueOf(i)), 3);
			}
		}
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (player.mayBuild()) {
			if (level.isClientSide) {
				return InteractionResult.SUCCESS;
			} else {
				BlockState blockState2 = blockState.cycle(INVERTED);
				level.setBlock(blockPos, blockState2, 4);
				updateSignalStrength(blockState2, level, blockPos);
				return InteractionResult.CONSUME;
			}
		} else {
			return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
		}
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	public BlockEntity newBlockEntity(BlockGetter blockGetter) {
		return new DaylightDetectorBlockEntity();
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWER, INVERTED);
	}
}
