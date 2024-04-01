package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PotatoBlock extends CropBlock {
	public static final MapCodec<PotatoBlock> CODEC = simpleCodec(PotatoBlock::new);
	public static final IntegerProperty TATER_BOOST = IntegerProperty.create("tater_boost", 0, 2);
	private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
		Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0)
	};

	@Override
	public MapCodec<PotatoBlock> codec() {
		return CODEC;
	}

	public PotatoBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(TATER_BOOST);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Level level = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos().step(Direction.DOWN);
		BlockState blockState = level.getBlockState(blockPos);
		BlockState blockState2 = super.getStateForPlacement(blockPlaceContext);
		return blockState2 == null ? null : withCorrectTaterBoost(blockState2, blockState);
	}

	@Override
	protected ItemLike getBaseSeedId() {
		return Items.POTATO;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE_BY_AGE[this.getAge(blockState)];
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(BlockTags.GROWS_POTATOES);
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
		if (blockPos.step(Direction.DOWN).equals(blockPos2)) {
			BlockState blockState2 = level.getBlockState(blockPos2);
			BlockState blockState3 = level.getBlockState(blockPos);
			if (blockState3.getBlock() instanceof PotatoBlock) {
				level.setBlock(blockPos, withCorrectTaterBoost(blockState3, blockState2), 3);
			}
		}
	}

	@Override
	public BlockState getStateForAge(int i, BlockState blockState) {
		return blockState.setValue(this.getAgeProperty(), Integer.valueOf(i));
	}

	public static BlockState withCorrectTaterBoost(BlockState blockState, BlockState blockState2) {
		return blockState.setValue(TATER_BOOST, Integer.valueOf(calculateTaterBoost(blockState2)));
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		if (levelReader.isPotato()) {
			BlockPos blockPos2 = blockPos.below();
			return this.mayPlaceOn(levelReader.getBlockState(blockPos2), levelReader, blockPos2);
		} else {
			return super.canSurvive(blockState, levelReader, blockPos);
		}
	}

	private static int calculateTaterBoost(BlockState blockState) {
		if (blockState.is(Blocks.PEELGRASS_BLOCK)) {
			return 1;
		} else {
			return blockState.is(Blocks.CORRUPTED_PEELGRASS_BLOCK) ? 2 : 0;
		}
	}
}
