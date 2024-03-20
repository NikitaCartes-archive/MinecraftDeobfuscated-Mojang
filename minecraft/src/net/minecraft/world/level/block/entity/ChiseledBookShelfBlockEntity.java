package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import org.slf4j.Logger;

public class ChiseledBookShelfBlockEntity extends BlockEntity implements Container {
	public static final int MAX_BOOKS_IN_STORAGE = 6;
	private static final Logger LOGGER = LogUtils.getLogger();
	private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
	private int lastInteractedSlot = -1;

	public ChiseledBookShelfBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.CHISELED_BOOKSHELF, blockPos, blockState);
	}

	private void updateState(int i) {
		if (i >= 0 && i < 6) {
			this.lastInteractedSlot = i;
			BlockState blockState = this.getBlockState();

			for (int j = 0; j < ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); j++) {
				boolean bl = !this.getItem(j).isEmpty();
				BooleanProperty booleanProperty = (BooleanProperty)ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(j);
				blockState = blockState.setValue(booleanProperty, Boolean.valueOf(bl));
			}

			((Level)Objects.requireNonNull(this.level)).setBlock(this.worldPosition, blockState, 3);
			this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.worldPosition, GameEvent.Context.of(blockState));
		} else {
			LOGGER.error("Expected slot 0-5, got {}", i);
		}
	}

	@Override
	public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		this.items.clear();
		ContainerHelper.loadAllItems(compoundTag, this.items, provider);
		this.lastInteractedSlot = compoundTag.getInt("last_interacted_slot");
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		ContainerHelper.saveAllItems(compoundTag, this.items, true, provider);
		compoundTag.putInt("last_interacted_slot", this.lastInteractedSlot);
	}

	public int count() {
		return (int)this.items.stream().filter(Predicate.not(ItemStack::isEmpty)).count();
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	public int getContainerSize() {
		return 6;
	}

	@Override
	public boolean isEmpty() {
		return this.items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getItem(int i) {
		return this.items.get(i);
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		ItemStack itemStack = (ItemStack)Objects.requireNonNullElse(this.items.get(i), ItemStack.EMPTY);
		this.items.set(i, ItemStack.EMPTY);
		if (!itemStack.isEmpty()) {
			this.updateState(i);
		}

		return itemStack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		return this.removeItem(i, 1);
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		if (itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
			this.items.set(i, itemStack);
			this.updateState(i);
		} else if (itemStack.isEmpty()) {
			this.removeItem(i, 1);
		}
	}

	@Override
	public boolean canTakeItem(Container container, int i, ItemStack itemStack) {
		return container.hasAnyMatching(
			itemStack2 -> itemStack2.isEmpty()
					? true
					: ItemStack.isSameItemSameComponents(itemStack, itemStack2) && itemStack2.getCount() + itemStack.getCount() <= container.getMaxStackSize(itemStack2)
		);
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public boolean canPlaceItem(int i, ItemStack itemStack) {
		return itemStack.is(ItemTags.BOOKSHELF_BOOKS) && this.getItem(i).isEmpty() && itemStack.getCount() == this.getMaxStackSize();
	}

	public int getLastInteractedSlot() {
		return this.lastInteractedSlot;
	}

	@Override
	public void applyComponents(DataComponentMap dataComponentMap) {
		dataComponentMap.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.items);
	}

	@Override
	public void collectComponents(DataComponentMap.Builder builder) {
		builder.set(DataComponents.CONTAINER, ItemContainerContents.copyOf(this.items));
	}

	@Override
	public void removeComponentsFromTag(CompoundTag compoundTag) {
		compoundTag.remove("Items");
	}
}
