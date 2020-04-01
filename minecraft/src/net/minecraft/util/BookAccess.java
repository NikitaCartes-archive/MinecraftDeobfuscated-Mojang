package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;

public interface BookAccess {
	BookAccess EMPTY_ACCESS = new BookAccess() {
		@Override
		public int getPageCount() {
			return 0;
		}

		@Override
		public Component getPageRaw(int i) {
			return new TextComponent("");
		}
	};

	static List<String> convertPages(CompoundTag compoundTag) {
		ListTag listTag = compoundTag.getList("pages", 8).copy();
		Builder<String> builder = ImmutableList.builder();

		for (int i = 0; i < listTag.size(); i++) {
			builder.add(listTag.getString(i));
		}

		return builder.build();
	}

	int getPageCount();

	Component getPageRaw(int i);

	default Component getPage(int i) {
		return (Component)(i >= 0 && i < this.getPageCount() ? this.getPageRaw(i) : new TextComponent(""));
	}

	static BookAccess fromItem(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item == Items.WRITTEN_BOOK) {
			return new BookAccess.WrittenBookAccess(itemStack);
		} else {
			return (BookAccess)(item == Items.WRITABLE_BOOK ? new BookAccess.WritableBookAccess(itemStack) : EMPTY_ACCESS);
		}
	}

	public static class WritableBookAccess implements BookAccess {
		private final List<String> pages;

		public WritableBookAccess(ItemStack itemStack) {
			this.pages = readPages(itemStack);
		}

		private static List<String> readPages(ItemStack itemStack) {
			CompoundTag compoundTag = itemStack.getTag();
			return (List<String>)(compoundTag != null ? BookAccess.convertPages(compoundTag) : ImmutableList.of());
		}

		@Override
		public int getPageCount() {
			return this.pages.size();
		}

		@Override
		public Component getPageRaw(int i) {
			return new TextComponent((String)this.pages.get(i));
		}
	}

	public static class WrittenBookAccess implements BookAccess {
		private final List<String> pages;

		public WrittenBookAccess(ItemStack itemStack) {
			this.pages = readPages(itemStack);
		}

		private static List<String> readPages(ItemStack itemStack) {
			CompoundTag compoundTag = itemStack.getTag();
			return (List<String>)(compoundTag != null && WrittenBookItem.makeSureTagIsValid(compoundTag)
				? BookAccess.convertPages(compoundTag)
				: ImmutableList.of(new TranslatableComponent("book.invalid.tag").withStyle(ChatFormatting.DARK_RED).getColoredString()));
		}

		@Override
		public int getPageCount() {
			return this.pages.size();
		}

		@Override
		public Component getPageRaw(int i) {
			String string = (String)this.pages.get(i);

			try {
				Component component = Component.Serializer.fromJson(string);
				if (component != null) {
					return component;
				}
			} catch (Exception var4) {
			}

			return new TextComponent(string);
		}
	}
}
