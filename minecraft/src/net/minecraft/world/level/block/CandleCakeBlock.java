package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CandleCakeBlock extends AbstractCandleBlock {
	public static final MapCodec<CandleCakeBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					BuiltInRegistries.BLOCK.byNameCodec().fieldOf("candle").forGetter(candleCakeBlock -> candleCakeBlock.candleBlock), propertiesCodec()
				)
				.apply(instance, CandleCakeBlock::new)
	);
	public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
	protected static final float AABB_OFFSET = 1.0F;
	protected static final VoxelShape CAKE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 8.0, 15.0);
	protected static final VoxelShape CANDLE_SHAPE = Block.box(7.0, 8.0, 7.0, 9.0, 14.0, 9.0);
	protected static final VoxelShape SHAPE = Shapes.or(CAKE_SHAPE, CANDLE_SHAPE);
	private static final Map<CandleBlock, CandleCakeBlock> BY_CANDLE = Maps.<CandleBlock, CandleCakeBlock>newHashMap();
	private static final Iterable<Vec3> PARTICLE_OFFSETS = ImmutableList.<Vec3>of(new Vec3(0.5, 1.0, 0.5));
	private final CandleBlock candleBlock;

	@Override
	public MapCodec<CandleCakeBlock> codec() {
		return CODEC;
	}

	protected CandleCakeBlock(Block block, BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.valueOf(false)));
		if (block instanceof CandleBlock candleBlock) {
			BY_CANDLE.put(candleBlock, this);
			this.candleBlock = candleBlock;
		} else {
			throw new IllegalArgumentException("Expected block to be of " + CandleBlock.class + " was " + block.getClass());
		}
	}

	@Override
	protected Iterable<Vec3> getParticleOffsets(BlockState blockState) {
		return PARTICLE_OFFSETS;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected ItemInteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (itemStack.is(Items.FLINT_AND_STEEL) || itemStack.is(Items.FIRE_CHARGE)) {
			return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
		} else if (candleHit(blockHitResult) && itemStack.isEmpty() && (Boolean)blockState.getValue(LIT)) {
			extinguish(player, blockState, level, blockPos);
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		InteractionResult interactionResult = CakeBlock.eat(level, blockPos, Blocks.CAKE.defaultBlockState(), player);
		if (interactionResult.consumesAction()) {
			dropResources(blockState, level, blockPos);
		}

		return interactionResult;
	}

	private static boolean candleHit(BlockHitResult blockHitResult) {
		return blockHitResult.getLocation().y - (double)blockHitResult.getBlockPos().getY() > 0.5;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(Blocks.CAKE);
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return levelReader.getBlockState(blockPos.below()).isSolid();
	}

	@Override
	protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return CakeBlock.FULL_CAKE_SIGNAL;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}

	public static BlockState byCandle(CandleBlock candleBlock) {
		return ((CandleCakeBlock)BY_CANDLE.get(candleBlock)).defaultBlockState();
	}

	public static boolean canLight(BlockState blockState) {
		return blockState.is(BlockTags.CANDLE_CAKES, blockStateBase -> blockStateBase.hasProperty(LIT) && !(Boolean)blockState.getValue(LIT));
	}
}
