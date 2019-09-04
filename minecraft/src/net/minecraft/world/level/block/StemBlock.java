package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StemBlock extends BushBlock implements BonemealableBlock {
	public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
	protected static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
		Block.box(7.0, 0.0, 7.0, 9.0, 2.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 4.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 6.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 8.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 10.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 12.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 14.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)
	};
	private final StemGrownBlock fruit;

	protected StemBlock(StemGrownBlock stemGrownBlock, Block.Properties properties) {
		super(properties);
		this.fruit = stemGrownBlock;
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE_BY_AGE[blockState.getValue(AGE)];
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.getBlock() == Blocks.FARMLAND;
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		super.tick(blockState, serverLevel, blockPos, random);
		if (serverLevel.getRawBrightness(blockPos, 0) >= 9) {
			float f = CropBlock.getGrowthSpeed(this, serverLevel, blockPos);
			if (random.nextInt((int)(25.0F / f) + 1) == 0) {
				int i = (Integer)blockState.getValue(AGE);
				if (i < 7) {
					blockState = blockState.setValue(AGE, Integer.valueOf(i + 1));
					serverLevel.setBlock(blockPos, blockState, 2);
				} else {
					Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
					BlockPos blockPos2 = blockPos.relative(direction);
					Block block = serverLevel.getBlockState(blockPos2.below()).getBlock();
					if (serverLevel.getBlockState(blockPos2).isAir()
						&& (block == Blocks.FARMLAND || block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.PODZOL || block == Blocks.GRASS_BLOCK)) {
						serverLevel.setBlockAndUpdate(blockPos2, this.fruit.defaultBlockState());
						serverLevel.setBlockAndUpdate(blockPos, this.fruit.getAttachedStem().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction));
					}
				}
			}
		}
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	protected Item getSeedItem() {
		if (this.fruit == Blocks.PUMPKIN) {
			return Items.PUMPKIN_SEEDS;
		} else {
			return this.fruit == Blocks.MELON ? Items.MELON_SEEDS : null;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		Item item = this.getSeedItem();
		return item == null ? ItemStack.EMPTY : new ItemStack(item);
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		return (Integer)blockState.getValue(AGE) != 7;
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		int i = Math.min(7, (Integer)blockState.getValue(AGE) + Mth.nextInt(serverLevel.random, 2, 5));
		BlockState blockState2 = blockState.setValue(AGE, Integer.valueOf(i));
		serverLevel.setBlock(blockPos, blockState2, 2);
		if (i == 7) {
			blockState2.tick(serverLevel, blockPos, serverLevel.random);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	public StemGrownBlock getFruit() {
		return this.fruit;
	}
}
