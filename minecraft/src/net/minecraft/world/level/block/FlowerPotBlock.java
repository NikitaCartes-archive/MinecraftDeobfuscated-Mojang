package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerPotBlock extends Block {
	public static final MapCodec<FlowerPotBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("potted").forGetter(flowerPotBlock -> flowerPotBlock.potted), propertiesCodec())
				.apply(instance, FlowerPotBlock::new)
	);
	private static final Map<Block, Block> POTTED_BY_CONTENT = Maps.<Block, Block>newHashMap();
	public static final float AABB_SIZE = 3.0F;
	protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);
	private final Block potted;

	@Override
	public MapCodec<FlowerPotBlock> codec() {
		return CODEC;
	}

	public FlowerPotBlock(Block block, BlockBehaviour.Properties properties) {
		super(properties);
		this.potted = block;
		POTTED_BY_CONTENT.put(block, this);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected InteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		BlockState blockState2 = (itemStack.getItem() instanceof BlockItem blockItem
				? (Block)POTTED_BY_CONTENT.getOrDefault(blockItem.getBlock(), Blocks.AIR)
				: Blocks.AIR)
			.defaultBlockState();
		if (blockState2.isAir()) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		} else if (!this.isEmpty()) {
			return InteractionResult.CONSUME;
		} else {
			level.setBlock(blockPos, blockState2, 3);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
			player.awardStat(Stats.POT_FLOWER);
			itemStack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (this.isEmpty()) {
			return InteractionResult.CONSUME;
		} else {
			ItemStack itemStack = new ItemStack(this.potted);
			if (!player.addItem(itemStack)) {
				player.drop(itemStack, false);
			}

			level.setBlock(blockPos, Blocks.FLOWER_POT.defaultBlockState(), 3);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return this.isEmpty() ? super.getCloneItemStack(levelReader, blockPos, blockState) : new ItemStack(this.potted);
	}

	private boolean isEmpty() {
		return this.potted == Blocks.AIR;
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState,
		LevelReader levelReader,
		ScheduledTickAccess scheduledTickAccess,
		BlockPos blockPos,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		RandomSource randomSource
	) {
		return direction == Direction.DOWN && !blockState.canSurvive(levelReader, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
	}

	public Block getPotted() {
		return this.potted;
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}
}
