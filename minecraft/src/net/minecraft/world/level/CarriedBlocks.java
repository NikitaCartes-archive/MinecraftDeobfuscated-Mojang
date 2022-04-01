package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GenericItemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class CarriedBlocks {
	public static Optional<FallingBlockEntity> createEntityFromItemStack(Level level, double d, double e, double f, ItemStack itemStack) {
		return getBlockFromItemStack(itemStack).map(blockState -> new FallingBlockEntity(level, d, e, f, blockState));
	}

	public static BlockState normalizeBlockState(BlockState blockState) {
		BlockState blockState2 = blockState;

		while (true) {
			BlockState blockState3 = GenericItemBlock.unwrap(blockState2);
			if (blockState3 == null || blockState3 == blockState2) {
				return blockState2;
			}

			blockState2 = blockState3;
		}
	}

	public static ItemStack getItemStackFromBlock(BlockState blockState) {
		if (blockState.isAir()) {
			return ItemStack.EMPTY;
		} else {
			Item item = GenericItemBlock.itemFromGenericBlock(blockState);
			if (item != null) {
				return item.getDefaultInstance();
			} else {
				Block block = blockState.getBlock();
				ItemStack itemStack = block.asItem().getDefaultInstance();
				itemStack.addTagElement("BlockStateTag", serializeStateProperties(blockState));
				return itemStack;
			}
		}
	}

	public static Optional<BlockState> getBlockFromItemStack(ItemStack itemStack) {
		return itemStack.isEmpty() ? Optional.empty() : getBlockFromItem(itemStack.getItem()).map(blockState -> deserializeStateProperties(itemStack, blockState));
	}

	private static Optional<BlockState> getBlockFromItem(Item item) {
		if (item == Items.AIR) {
			return Optional.empty();
		} else {
			return item instanceof BlockItem blockItem
				? Optional.of(blockItem.getBlock().defaultBlockState())
				: Optional.ofNullable(GenericItemBlock.genericBlockFromItem(item));
		}
	}

	private static CompoundTag serializeStateProperties(BlockState blockState) {
		CompoundTag compoundTag = new CompoundTag();

		for (Property<?> property : blockState.getProperties()) {
			compoundTag.putString(property.getName(), serializeStateProperty(blockState, property));
		}

		return compoundTag;
	}

	private static <T extends Comparable<T>> String serializeStateProperty(BlockState blockState, Property<T> property) {
		T comparable = blockState.getValue(property);
		return property.getName(comparable);
	}

	private static BlockState deserializeStateProperties(ItemStack itemStack, BlockState blockState) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && compoundTag.contains("BlockStateTag")) {
			BlockState blockState2 = blockState;
			CompoundTag compoundTag2 = compoundTag.getCompound("BlockStateTag");
			StateDefinition<Block, BlockState> stateDefinition = blockState.getBlock().getStateDefinition();

			for (String string : compoundTag2.getAllKeys()) {
				Property<?> property = stateDefinition.getProperty(string);
				if (property != null) {
					String string2 = compoundTag2.get(string).getAsString();

					try {
						blockState2 = deserializeStateProperty(blockState2, property, string2);
					} catch (Throwable var11) {
						var11.printStackTrace();
					}
				}
			}

			return blockState2;
		} else {
			return blockState;
		}
	}

	private static <T extends Comparable<T>> BlockState deserializeStateProperty(BlockState blockState, Property<T> property, String string) {
		return (BlockState)property.getValue(string).map(comparable -> blockState.setValue(property, comparable)).orElse(blockState);
	}
}
