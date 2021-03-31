package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class LecternBlockEntity extends BlockEntity implements Clearable, MenuProvider {
	public static final int DATA_PAGE = 0;
	public static final int NUM_DATA = 1;
	public static final int SLOT_BOOK = 0;
	public static final int NUM_SLOTS = 1;
	private final Container bookAccess = new Container() {
		@Override
		public int getContainerSize() {
			return 1;
		}

		@Override
		public boolean isEmpty() {
			return LecternBlockEntity.this.book.isEmpty();
		}

		@Override
		public ItemStack getItem(int i) {
			return i == 0 ? LecternBlockEntity.this.book : ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeItem(int i, int j) {
			if (i == 0) {
				ItemStack itemStack = LecternBlockEntity.this.book.split(j);
				if (LecternBlockEntity.this.book.isEmpty()) {
					LecternBlockEntity.this.onBookItemRemove();
				}

				return itemStack;
			} else {
				return ItemStack.EMPTY;
			}
		}

		@Override
		public ItemStack removeItemNoUpdate(int i) {
			if (i == 0) {
				ItemStack itemStack = LecternBlockEntity.this.book;
				LecternBlockEntity.this.book = ItemStack.EMPTY;
				LecternBlockEntity.this.onBookItemRemove();
				return itemStack;
			} else {
				return ItemStack.EMPTY;
			}
		}

		@Override
		public void setItem(int i, ItemStack itemStack) {
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}

		@Override
		public void setChanged() {
			LecternBlockEntity.this.setChanged();
		}

		@Override
		public boolean stillValid(Player player) {
			if (LecternBlockEntity.this.level.getBlockEntity(LecternBlockEntity.this.worldPosition) != LecternBlockEntity.this) {
				return false;
			} else {
				return player.distanceToSqr(
							(double)LecternBlockEntity.this.worldPosition.getX() + 0.5,
							(double)LecternBlockEntity.this.worldPosition.getY() + 0.5,
							(double)LecternBlockEntity.this.worldPosition.getZ() + 0.5
						)
						> 64.0
					? false
					: LecternBlockEntity.this.hasBook();
			}
		}

		@Override
		public boolean canPlaceItem(int i, ItemStack itemStack) {
			return false;
		}

		@Override
		public void clearContent() {
		}
	};
	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int i) {
			return i == 0 ? LecternBlockEntity.this.page : 0;
		}

		@Override
		public void set(int i, int j) {
			if (i == 0) {
				LecternBlockEntity.this.setPage(j);
			}
		}

		@Override
		public int getCount() {
			return 1;
		}
	};
	private ItemStack book = ItemStack.EMPTY;
	private int page;
	private int pageCount;

	public LecternBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.LECTERN, blockPos, blockState);
	}

	public ItemStack getBook() {
		return this.book;
	}

	public boolean hasBook() {
		return this.book.is(Items.WRITABLE_BOOK) || this.book.is(Items.WRITTEN_BOOK);
	}

	public void setBook(ItemStack itemStack) {
		this.setBook(itemStack, null);
	}

	private void onBookItemRemove() {
		this.page = 0;
		this.pageCount = 0;
		LecternBlock.resetBookState(this.getLevel(), this.getBlockPos(), this.getBlockState(), false);
	}

	public void setBook(ItemStack itemStack, @Nullable Player player) {
		this.book = this.resolveBook(itemStack, player);
		this.page = 0;
		this.pageCount = WrittenBookItem.getPageCount(this.book);
		this.setChanged();
	}

	private void setPage(int i) {
		int j = Mth.clamp(i, 0, this.pageCount - 1);
		if (j != this.page) {
			this.page = j;
			this.setChanged();
			LecternBlock.signalPageChange(this.getLevel(), this.getBlockPos(), this.getBlockState());
		}
	}

	public int getPage() {
		return this.page;
	}

	public int getRedstoneSignal() {
		float f = this.pageCount > 1 ? (float)this.getPage() / ((float)this.pageCount - 1.0F) : 1.0F;
		return Mth.floor(f * 14.0F) + (this.hasBook() ? 1 : 0);
	}

	private ItemStack resolveBook(ItemStack itemStack, @Nullable Player player) {
		if (this.level instanceof ServerLevel && itemStack.is(Items.WRITTEN_BOOK)) {
			WrittenBookItem.resolveBookComponents(itemStack, this.createCommandSourceStack(player), player);
		}

		return itemStack;
	}

	private CommandSourceStack createCommandSourceStack(@Nullable Player player) {
		String string;
		Component component;
		if (player == null) {
			string = "Lectern";
			component = new TextComponent("Lectern");
		} else {
			string = player.getName().getString();
			component = player.getDisplayName();
		}

		Vec3 vec3 = Vec3.atCenterOf(this.worldPosition);
		return new CommandSourceStack(CommandSource.NULL, vec3, Vec2.ZERO, (ServerLevel)this.level, 2, string, component, this.level.getServer(), player);
	}

	@Override
	public boolean onlyOpCanSetNbt() {
		return true;
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		if (compoundTag.contains("Book", 10)) {
			this.book = this.resolveBook(ItemStack.of(compoundTag.getCompound("Book")), null);
		} else {
			this.book = ItemStack.EMPTY;
		}

		this.pageCount = WrittenBookItem.getPageCount(this.book);
		this.page = Mth.clamp(compoundTag.getInt("Page"), 0, this.pageCount - 1);
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		if (!this.getBook().isEmpty()) {
			compoundTag.put("Book", this.getBook().save(new CompoundTag()));
			compoundTag.putInt("Page", this.page);
		}

		return compoundTag;
	}

	@Override
	public void clearContent() {
		this.setBook(ItemStack.EMPTY);
	}

	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		return new LecternMenu(i, this.bookAccess, this.dataAccess);
	}

	@Override
	public Component getDisplayName() {
		return new TranslatableComponent("container.lectern");
	}
}
