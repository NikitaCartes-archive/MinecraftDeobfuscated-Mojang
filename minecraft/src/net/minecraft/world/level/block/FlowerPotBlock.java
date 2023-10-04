package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		Item item = itemStack.getItem();
		BlockState blockState2 = (item instanceof BlockItem ? (Block)POTTED_BY_CONTENT.getOrDefault(((BlockItem)item).getBlock(), Blocks.AIR) : Blocks.AIR)
			.defaultBlockState();
		boolean bl = blockState2.is(Blocks.AIR);
		boolean bl2 = this.isEmpty();
		if (bl != bl2) {
			if (bl2) {
				level.setBlock(blockPos, blockState2, 3);
				player.awardStat(Stats.POT_FLOWER);
				if (!player.getAbilities().instabuild) {
					itemStack.shrink(1);
				}
			} else {
				ItemStack itemStack2 = new ItemStack(this.potted);
				if (itemStack.isEmpty()) {
					player.setItemInHand(interactionHand, itemStack2);
				} else if (!player.addItem(itemStack2)) {
					player.drop(itemStack2, false);
				}

				level.setBlock(blockPos, Blocks.FLOWER_POT.defaultBlockState(), 3);
			}

			level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return InteractionResult.CONSUME;
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
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	public Block getPotted() {
		return this.potted;
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
