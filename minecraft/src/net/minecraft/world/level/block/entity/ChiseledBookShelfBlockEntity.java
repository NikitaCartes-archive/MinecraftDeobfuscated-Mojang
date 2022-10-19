package net.minecraft.world.level.block.entity;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ChiseledBookShelfBlockEntity extends BlockEntity implements Clearable {
	public static final int MAX_BOOKS_IN_STORAGE = 6;
	private final Deque<ItemStack> items = new ArrayDeque(6);

	public ChiseledBookShelfBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.CHISELED_BOOKSHELF, blockPos, blockState);
	}

	public ItemStack removeBook() {
		return (ItemStack)Objects.requireNonNullElse((ItemStack)this.items.poll(), ItemStack.EMPTY);
	}

	public void addBook(ItemStack itemStack) {
		if (itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
			this.items.addFirst(itemStack);
		}
	}

	@Override
	public void load(CompoundTag compoundTag) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(6, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compoundTag, nonNullList);
		this.items.clear();

		for (ItemStack itemStack : nonNullList) {
			if (itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
				this.items.add(itemStack);
			}
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		ContainerHelper.saveAllItems(compoundTag, asNonNullList(this.items), true);
	}

	@NotNull
	private static NonNullList<ItemStack> asNonNullList(Collection<ItemStack> collection) {
		NonNullList<ItemStack> nonNullList = NonNullList.createWithCapacity(collection.size());
		nonNullList.addAll(collection);
		return nonNullList;
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag compoundTag = new CompoundTag();
		ContainerHelper.saveAllItems(compoundTag, asNonNullList(this.items), true);
		return compoundTag;
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	public int bookCount() {
		return this.items.size();
	}

	public boolean isFull() {
		return this.bookCount() == 6;
	}

	public boolean isEmpty() {
		return this.items.isEmpty();
	}
}
