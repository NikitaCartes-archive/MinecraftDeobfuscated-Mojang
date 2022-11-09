package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StackInventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.slf4j.Logger;

public class ChiseledBookShelfBlockEntity extends BlockEntity implements Container {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int MAX_BOOKS_IN_STORAGE = 6;
	private final StackInventory books = new StackInventory(6);

	public ChiseledBookShelfBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.CHISELED_BOOKSHELF, blockPos, blockState);
	}

	private void updateBlockState() {
		int i = (Integer)this.getBlockState().getValue(BlockStateProperties.BOOKS_STORED);
		if (i != this.books.size()) {
			((Level)Objects.requireNonNull(this.level))
				.setBlock(
					this.worldPosition,
					this.getBlockState()
						.setValue(BlockStateProperties.BOOKS_STORED, Integer.valueOf(this.books.size()))
						.setValue(BlockStateProperties.LAST_INTERACTION_BOOK_SLOT, Integer.valueOf(i > this.books.size() ? this.books.size() + 1 : this.books.size())),
					3
				);
		}
	}

	public ItemStack removeBook() {
		ItemStack itemStack = this.books.pop();
		if (!itemStack.isEmpty()) {
			this.updateBlockState();
		}

		return itemStack;
	}

	public List<ItemStack> removeAllBooksWithoutBlockStateUpdate() {
		return this.books.clear();
	}

	public boolean addBook(ItemStack itemStack) {
		if (this.isFull()) {
			return false;
		} else if (itemStack.getCount() > 1) {
			LOGGER.warn("tried to add a stack with more than one items {} at {}", itemStack, this.worldPosition);
			return false;
		} else if (!itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
			LOGGER.warn("tried to add a non book: {} at {}", itemStack, this.worldPosition);
			return false;
		} else if (!this.books.push(itemStack)) {
			LOGGER.warn("failed to add {} at {}", itemStack, this.worldPosition);
			return false;
		} else {
			this.updateBlockState();
			return true;
		}
	}

	@Override
	public void load(CompoundTag compoundTag) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(6, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compoundTag, nonNullList);
		this.books.clear();

		for (int i = 0; i < nonNullList.size(); i++) {
			ItemStack itemStack = nonNullList.get(i);
			if (!itemStack.isEmpty()) {
				this.books.pushWithSlot(itemStack, i);
			}
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		ContainerHelper.saveAllItems(compoundTag, asNonNullList(this.books), true);
	}

	private static NonNullList<ItemStack> asNonNullList(StackInventory stackInventory) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(6, ItemStack.EMPTY);

		for (int i = 0; i < 6; i++) {
			nonNullList.set(i, stackInventory.get(i));
		}

		return nonNullList;
	}

	@Override
	public void clearContent() {
		this.books.clear();
	}

	public int bookCount() {
		return this.books.size();
	}

	public boolean isFull() {
		return this.books.isFull();
	}

	@Override
	public int getContainerSize() {
		return 6;
	}

	@Override
	public void setChanged() {
		this.books.flatten();
		this.updateBlockState();
	}

	@Override
	public boolean isEmpty() {
		return this.books.isEmpty();
	}

	@Override
	public ItemStack getItem(int i) {
		return this.books.get(i);
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		ItemStack itemStack = this.removeItemNoUpdate(i);
		this.updateBlockState();
		return itemStack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		return this.books.remove(i);
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		if (this.books.set(itemStack, i)) {
			this.updateBlockState();
		}
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean stillValid(Player player) {
		if (this.level == null) {
			return false;
		} else {
			return this.level.getBlockEntity(this.worldPosition) != this
				? false
				: player.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5) <= 64.0;
		}
	}

	@Override
	public boolean canPlaceItem(int i, ItemStack itemStack) {
		return !this.isFull() && itemStack.is(ItemTags.BOOKSHELF_BOOKS) && this.books.canSet(i);
	}

	@Override
	public int countItem(Item item) {
		return (int)this.books.view().stream().filter(itemStack -> itemStack.is(item)).count();
	}

	@Override
	public boolean hasAnyOf(Set<Item> set) {
		return this.books.view().stream().anyMatch(itemStack -> set.contains(itemStack.getItem()));
	}

	@Override
	public boolean hasAnyMatching(Predicate<ItemStack> predicate) {
		return this.books.view().stream().anyMatch(predicate);
	}
}
