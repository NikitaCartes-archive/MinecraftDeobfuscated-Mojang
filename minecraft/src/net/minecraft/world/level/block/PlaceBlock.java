package net.minecraft.world.level.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.PlaceBlockBlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;

public class PlaceBlock extends FacingTriggerableBlock {
	protected PlaceBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected TickPriority getTickPriority() {
		return TickPriority.EXTREMELY_LOW;
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (Rules.PLACE_BLOCK.get()) {
			Direction direction = blockState.getValue(FACING);
			Direction direction2 = direction.getOpposite();
			BlockPos blockPos2 = blockPos.relative(direction2);
			BlockPos blockPos3 = blockPos.relative(direction);
			getItemAndDoThings(
				serverLevel,
				blockPos2,
				direction2,
				itemStack -> {
					if (itemStack.isEmpty()) {
						return false;
					} else {
						boolean var10000;
						label19: {
							if (itemStack.getItem() instanceof BlockItem blockItem
								&& blockItem.place(
										new PlaceBlockBlockPlaceContext(
											serverLevel, InteractionHand.MAIN_HAND, itemStack, new BlockHitResult(blockPos3.getCenter(), direction2, blockPos3, false)
										)
									)
									.consumesAction()) {
								var10000 = true;
								break label19;
							}

							var10000 = false;
						}

						boolean bl = var10000;
						if (!bl) {
							double d = (double)EntityType.ITEM.getHeight() / 2.0;
							double e = (double)blockPos3.getX() + 0.5;
							double f = (double)blockPos3.getY() + 0.5 - d;
							double g = (double)blockPos3.getZ() + 0.5;
							ItemEntity itemEntity = new ItemEntity(serverLevel, e, f, g, itemStack);
							itemEntity.setDefaultPickUpDelay();
							serverLevel.addFreshEntity(itemEntity);
						}

						return true;
					}
				}
			);
		}
	}

	public static boolean pushItemToContainer(Level level, BlockPos blockPos, Direction direction, ItemStack itemStack) {
		for (Container container : getContainersAt(level, blockPos)) {
			ItemStack itemStack2 = HopperBlockEntity.addItem(null, container, itemStack, direction);
			if (itemStack2.isEmpty()) {
				return true;
			}
		}

		return false;
	}

	public static boolean getItemAndDoThings(Level level, BlockPos blockPos, Direction direction, Function<ItemStack, Boolean> function) {
		for (Container container : getContainersAt(level, blockPos)) {
			boolean bl = HopperBlockEntity.getSlots(container, direction).anyMatch(i -> {
				ItemStack itemStackx = container.removeItem(i, 1);
				if (!itemStackx.isEmpty()) {
					boolean blx = (Boolean)function.apply(itemStackx.copy());
					if (blx) {
						container.setChanged();
					} else {
						container.setItem(i, itemStackx);
					}

					return true;
				} else {
					return false;
				}
			});
			if (bl) {
				return true;
			}
		}

		ItemEntity itemEntity = getItemAt(level, blockPos);
		if (itemEntity != null) {
			ItemStack itemStack = itemEntity.getItem();
			if (!itemStack.isEmpty()) {
				boolean bl = (Boolean)function.apply(itemStack.copyWithCount(1));
				if (bl) {
					itemStack.shrink(1);
					if (itemStack.getCount() <= 0) {
						itemEntity.discard();
					}
				}

				return true;
			}
		}

		return false;
	}

	public static List<Container> getContainersAt(Level level, BlockPos blockPos) {
		BlockState blockState = level.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (block instanceof WorldlyContainerHolder) {
			WorldlyContainer worldlyContainer = ((WorldlyContainerHolder)block).getContainer(blockState, level, blockPos);
			if (worldlyContainer != null) {
				return List.of(worldlyContainer);
			}
		} else if (blockState.hasBlockEntity()) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof Container) {
				if (!(blockEntity instanceof ChestBlockEntity) || !(block instanceof ChestBlock)) {
					return List.of((Container)blockEntity);
				}

				Container container = ChestBlock.getContainer((ChestBlock)block, blockState, level, blockPos, true);
				if (container != null) {
					return List.of(container);
				}
			}
		}

		List<Container> list = new ArrayList();

		for (Entity entity : level.getEntities((Entity)null, blockAABB(blockPos), EntitySelector.CONTAINER_ENTITY_SELECTOR)) {
			if (entity instanceof Container container2) {
				list.add(container2);
			}
		}

		return list;
	}

	@Nullable
	public static ItemEntity getItemAt(Level level, BlockPos blockPos) {
		List<ItemEntity> list = level.getEntitiesOfClass(ItemEntity.class, blockAABB(blockPos), EntitySelector.ENTITY_STILL_ALIVE);
		return list.size() < 1 ? null : (ItemEntity)list.get(0);
	}

	private static AABB blockAABB(BlockPos blockPos) {
		double d = 0.9999999;
		return AABB.ofSize(blockPos.getCenter(), 0.9999999, 0.9999999, 0.9999999);
	}
}
