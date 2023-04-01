package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CheeseBlock extends Block {
	public static final int SLICE_COUNT = 8;
	private static final int TABLE_SIZE = 256;
	public static final int FULL = 255;
	private static final int EMPTY = 0;
	public static final IntegerProperty SLICES = IntegerProperty.create("slices", 1, 255);
	public static final VoxelShape[] SHAPES_BY_SLICE = Util.make(new VoxelShape[8], voxelShapes -> {
		voxelShapes[0] = Shapes.box(0.0, 0.0, 0.0, 0.5, 0.5, 0.5);
		voxelShapes[1] = Shapes.box(0.5, 0.0, 0.0, 1.0, 0.5, 0.5);
		voxelShapes[2] = Shapes.box(0.0, 0.0, 0.5, 0.5, 0.5, 1.0);
		voxelShapes[3] = Shapes.box(0.5, 0.0, 0.5, 1.0, 0.5, 1.0);
		voxelShapes[4] = Shapes.box(0.0, 0.5, 0.0, 0.5, 1.0, 0.5);
		voxelShapes[5] = Shapes.box(0.5, 0.5, 0.0, 1.0, 1.0, 0.5);
		voxelShapes[6] = Shapes.box(0.0, 0.5, 0.5, 0.5, 1.0, 1.0);
		voxelShapes[7] = Shapes.box(0.5, 0.5, 0.5, 1.0, 1.0, 1.0);
	});
	public static final VoxelShape[] SHAPES_BY_STATE = Util.make(new VoxelShape[256], voxelShapes -> {
		for (int i = 0; i < voxelShapes.length; i++) {
			VoxelShape voxelShape = Shapes.empty();

			for (int j = 0; j < 8; j++) {
				if (hasSlice(i, j)) {
					voxelShape = Shapes.or(voxelShape, SHAPES_BY_SLICE[j]);
				}
			}

			voxelShapes[i] = voxelShape.optimize();
		}
	});
	private static final int SLICELESS = -1;

	protected CheeseBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(SLICES, Integer.valueOf(255)));
	}

	private static boolean hasSlice(int i, int j) {
		return (i & sliceMask(j)) != 0;
	}

	private static int sliceMask(int i) {
		return 1 << i;
	}

	private static int removeSlice(int i, int j) {
		return i & ~sliceMask(j);
	}

	private static boolean isFull(BlockState blockState) {
		return (Integer)blockState.getValue(SLICES) == 255;
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (!player.getItemInHand(interactionHand).isEmpty()) {
			return InteractionResult.FAIL;
		} else {
			Vec3 vec3 = blockHitResult.getLocation().subtract((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
			int i = getSliceAt(blockState, vec3);
			if (i == -1) {
				return InteractionResult.FAIL;
			} else {
				int j = removeSlice((Integer)blockState.getValue(SLICES), i);
				if (j != 0) {
					level.setBlockAndUpdate(blockPos, blockState.setValue(SLICES, Integer.valueOf(j)));
				} else {
					level.removeBlock(blockPos, false);
					level.gameEvent(player, GameEvent.BLOCK_DESTROY, blockPos);
				}

				if (!level.isClientSide) {
					level.levelEvent(2010, blockPos, i);
					player.getFoodData().eat(1, 0.1F);
					if (player.getAirSupply() < player.getMaxAirSupply()) {
						player.setAirSupply(player.getAirSupply() + 10);
					}

					level.gameEvent(player, GameEvent.EAT, blockPos);
				}

				return InteractionResult.SUCCESS;
			}
		}
	}

	private static int getSliceAt(BlockState blockState, Vec3 vec3) {
		int i = (Integer)blockState.getValue(SLICES);
		double d = Double.MAX_VALUE;
		int j = -1;

		for (int k = 0; k < SHAPES_BY_SLICE.length; k++) {
			if (hasSlice(i, k)) {
				VoxelShape voxelShape = SHAPES_BY_SLICE[k];
				Optional<Vec3> optional = voxelShape.closestPointTo(vec3);
				if (optional.isPresent()) {
					double e = ((Vec3)optional.get()).distanceToSqr(vec3);
					if (e < d) {
						d = e;
						j = k;
					}
				}
			}
		}

		return j;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPES_BY_STATE[blockState.getValue(SLICES)];
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return isFull(blockState) ? 0.2F : 1.0F;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SLICES);
	}
}
