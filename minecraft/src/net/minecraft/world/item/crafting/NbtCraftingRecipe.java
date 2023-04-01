package net.minecraft.world.item.crafting;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.NameItem;
import net.minecraft.world.item.TagContainerItem;
import net.minecraft.world.level.Level;

public class NbtCraftingRecipe extends CustomRecipe {
	public NbtCraftingRecipe(ResourceLocation resourceLocation, CraftingBookCategory craftingBookCategory) {
		super(resourceLocation, craftingBookCategory);
	}

	private static List<ItemStack> getNonEmpty(CraftingContainer craftingContainer) {
		List<ItemStack> list = new ArrayList();

		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack = craftingContainer.getItem(i);
			if (!itemStack.isEmpty()) {
				list.add(itemStack);
			}
		}

		return list;
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		return !Rules.NBT_CRAFTING.get() ? false : !assemble(getNonEmpty(craftingContainer)).isEmpty();
	}

	public ItemStack assembleRaw(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
		return !Rules.NBT_CRAFTING.get() ? ItemStack.EMPTY : assemble(getNonEmpty(craftingContainer));
	}

	private static int removeAndCount(List<ItemStack> list, Predicate<ItemStack> predicate, boolean bl) {
		int i = 0;
		Iterator<ItemStack> iterator = list.iterator();

		while (iterator.hasNext()) {
			ItemStack itemStack = (ItemStack)iterator.next();
			if (predicate.test(itemStack)) {
				iterator.remove();
				i += bl ? itemStack.getCount() : 1;
			}
		}

		return i;
	}

	@Nullable
	private static <T> T removeExactlyOne(List<ItemStack> list, Function<ItemStack, T> function) {
		T object = null;
		Iterator<ItemStack> iterator = list.iterator();

		while (iterator.hasNext()) {
			ItemStack itemStack = (ItemStack)iterator.next();
			T object2 = (T)function.apply(itemStack);
			if (object2 != null) {
				if (object != null) {
					return null;
				}

				iterator.remove();
				object = object2;
			}
		}

		return object;
	}

	private static <T extends Tag> ItemStack returnTag(Item item, T tag) {
		ItemStack itemStack = new ItemStack(item);
		((TagContainerItem)item).setTag(itemStack, tag);
		return itemStack;
	}

	private static ItemStack syntaxError(String string) {
		ItemStack itemStack = new ItemStack(Items.SYNTAX_ERROR);
		CompoundTag compoundTag = itemStack.getOrCreateTagElement("display");
		ListTag listTag = new ListTag();
		String string2 = Component.Serializer.toJson(Component.literal(string));
		listTag.add(StringTag.valueOf(string2));
		compoundTag.put("Lore", listTag);
		return itemStack;
	}

	public static ItemStack assemble(List<ItemStack> list) {
		if (list.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			int i = removeAndCount(list, itemStackx -> itemStackx.is(Items.TAG), false);
			if (i > 0) {
				return craftFromRawTag(list, i);
			} else {
				int j = removeAndCount(list, itemStackx -> itemStackx.is(ItemTags.BOATS), false);
				if (j > 0) {
					return craftFloats(list, j);
				} else {
					ItemStack itemStack = (ItemStack)list.get(0);
					if (itemStack.is(Items.LEFT_CURLY) && list.size() > 1) {
						return parseCompound(list);
					} else if (itemStack.is(Items.LEFT_SQUARE) && list.size() > 1) {
						return parseList(list);
					} else if (!itemStack.is(Items.RIGHT_CURLY) && !itemStack.is(Items.RIGHT_SQUARE) && !itemStack.is(Items.NAME)) {
						byte[] bs = concatAsBytes(list);
						return bs != null ? convertToNumberItem(bs) : concatSameTypes(list);
					} else {
						return syntaxError("Expected { or [");
					}
				}
			}
		}
	}

	private static ItemStack craftFromRawTag(List<ItemStack> list, int i) {
		if (i != 1) {
			return ItemStack.EMPTY;
		} else {
			int j = removeAndCount(list, itemStack -> itemStack.is(Items.STRING), false);
			if (j > 0) {
				return j == 1 && list.isEmpty() ? returnTag(Items.STRING_TAG, StringTag.valueOf("")) : ItemStack.EMPTY;
			} else {
				int k = removeAndCount(list, itemStack -> itemStack.is(Items.STICK), true);
				return !list.isEmpty() ? ItemStack.EMPTY : returnTag(Items.BYTE_TAG, ByteTag.valueOf((byte)k));
			}
		}
	}

	private static ItemStack craftFloats(List<ItemStack> list, int i) {
		NumericTag numericTag = removeExactlyOne(list, NbtCraftingRecipe::isFloatable);
		if (numericTag == null) {
			return ItemStack.EMPTY;
		} else {
			boolean bl = i == 1;
			boolean bl2 = i == 2;
			if (!bl && !bl2) {
				return syntaxError("Expected either single or double");
			} else {
				boolean bl3 = removeAndCount(list, itemStack -> itemStack.is(Items.BIT), false) > 0;
				if (!list.isEmpty()) {
					return syntaxError("Unexpected entries in when casting to float");
				} else if (bl) {
					float f = bl3 ? Float.intBitsToFloat(numericTag.getAsInt()) : numericTag.getAsFloat();
					return returnTag(Items.FLOAT_TAG, FloatTag.valueOf(f));
				} else {
					double d = bl3 ? Double.longBitsToDouble(numericTag.getAsLong()) : numericTag.getAsDouble();
					return returnTag(Items.DOUBLE_TAG, DoubleTag.valueOf(d));
				}
			}
		}
	}

	private static ItemStack parseList(List<ItemStack> list) {
		ListTag listTag = new ListTag();
		boolean bl = false;

		for (int i = 1; i < list.size(); i++) {
			if (bl) {
				return syntaxError("Unexpected value after closing bracket");
			}

			ItemStack itemStack = (ItemStack)list.get(i);
			if (itemStack.is(Items.RIGHT_SQUARE)) {
				bl = true;
			} else {
				if (!(itemStack.getItem() instanceof TagContainerItem<?> tagContainerItem)) {
					return syntaxError("Unexpected value in list: expected either tag or closing bracket");
				}

				Tag tag = tagContainerItem.getTag(itemStack);
				if (tag == null) {
					return syntaxError("OH NO INTERNAL ERROR");
				}

				if (!listTag.addTag(listTag.size(), tag)) {
					return syntaxError("Can't add element of type " + tag.getAsString() + " to list " + listTag.getAsString());
				}
			}
		}

		return !bl ? syntaxError("Expected closing bracket") : returnTag(Items.LIST_TAG, listTag);
	}

	private static ItemStack parseCompound(List<ItemStack> list) {
		CompoundTag compoundTag = new CompoundTag();
		boolean bl = false;
		String string = null;

		for (int i = 1; i < list.size(); i++) {
			if (bl) {
				return syntaxError("Unexpected value after closing bracket");
			}

			ItemStack itemStack = (ItemStack)list.get(i);
			if (itemStack.is(Items.RIGHT_CURLY)) {
				bl = true;
			} else if (itemStack.is(Items.NAME)) {
				if (string != null) {
					return syntaxError("Expected tag after name");
				}

				string = NameItem.getContainedName(itemStack);
			} else {
				if (!(itemStack.getItem() instanceof TagContainerItem<?> tagContainerItem)) {
					return syntaxError("Unexpected value in compound tag: expected either name, tag or closing bracket");
				}

				if (string == null) {
					return syntaxError("Expected name");
				}

				Tag tag = tagContainerItem.getTag(itemStack);
				if (tag == null) {
					return syntaxError("INTERNAL ERROR OH NO");
				}

				compoundTag.put(string, tag);
				string = null;
			}
		}

		if (string != null) {
			return syntaxError("Expected tag after name");
		} else {
			return !bl ? syntaxError("Expected closing bracket") : returnTag(Items.COMPOUND_TAG, compoundTag);
		}
	}

	private static ItemStack convertToNumberItem(byte[] bs) {
		ByteArrayDataInput byteArrayDataInput = ByteStreams.newDataInput(bs);

		return switch (bs.length) {
			case 0 -> returnTag(Items.BYTE_TAG, ByteTag.ZERO);
			case 1 -> returnTag(Items.BYTE_TAG, ByteTag.valueOf(byteArrayDataInput.readByte()));
			case 2 -> returnTag(Items.SHORT_TAG, ShortTag.valueOf(byteArrayDataInput.readShort()));
			case 3, 5, 6, 7 -> syntaxError("Number of bytes (" + bs.length + ") is not power of 2");
			case 4 -> returnTag(Items.INT_TAG, IntTag.valueOf(byteArrayDataInput.readInt()));
			case 8 -> returnTag(Items.LONG_TAG, LongTag.valueOf(byteArrayDataInput.readLong()));
			default -> syntaxError("Total number of bytes (" + bs.length + " exceeds 8");
		};
	}

	@Nullable
	private static NumericTag isFloatable(ItemStack itemStack) {
		if (itemStack.getItem() instanceof TagContainerItem<?> tagContainerItem) {
			Tag tag = tagContainerItem.getTag(itemStack);
			if (tag instanceof NumericTag) {
				return (NumericTag)tag;
			}
		}

		return null;
	}

	@Nullable
	private static byte[] concatAsBytes(List<ItemStack> list) {
		ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

		for (ItemStack itemStack : list) {
			if (!(itemStack.getItem() instanceof TagContainerItem<?> tagContainerItem)) {
				return null;
			}

			Tag tag = tagContainerItem.getTag(itemStack);
			if (tag instanceof ByteTag byteTag) {
				byteArrayDataOutput.writeByte(byteTag.getAsByte());
			} else if (tag instanceof ShortTag shortTag) {
				byteArrayDataOutput.writeShort(shortTag.getAsShort());
			} else if (tag instanceof IntTag intTag) {
				byteArrayDataOutput.writeInt(intTag.getAsInt());
			} else {
				if (!(tag instanceof LongTag longTag)) {
					return null;
				}

				byteArrayDataOutput.writeLong(longTag.getAsLong());
			}
		}

		return byteArrayDataOutput.toByteArray();
	}

	private static ItemStack concatSameTypes(List<ItemStack> list) {
		List<Tag> list2 = new ArrayList();

		for (ItemStack itemStack : list) {
			if (!(itemStack.getItem() instanceof TagContainerItem<?> tagContainerItem)) {
				return ItemStack.EMPTY;
			}

			list2.add(tagContainerItem.getTag(itemStack));
		}

		if (list2.size() < 2) {
			return ItemStack.EMPTY;
		} else {
			Tag tag = (Tag)list2.get(0);

			for (int i = 1; i < list2.size(); i++) {
				Tag tag2 = (Tag)list2.get(i);
				Tag tag3 = concatTags(tag, tag2);
				if (tag3 == null) {
					return syntaxError("Can't concatenate " + tag.getAsString() + " with " + tag2.getAsString());
				}

				tag = tag3;
			}

			if (tag instanceof CompoundTag compoundTag) {
				return returnTag(Items.COMPOUND_TAG, compoundTag);
			} else if (tag instanceof ListTag listTag) {
				return returnTag(Items.LIST_TAG, listTag);
			} else {
				return tag instanceof StringTag stringTag ? returnTag(Items.STRING_TAG, stringTag) : ItemStack.EMPTY;
			}
		}
	}

	@Nullable
	private static Tag concatTags(Tag tag, Tag tag2) {
		if (tag instanceof CompoundTag compoundTag && tag2 instanceof CompoundTag compoundTag2) {
			return compoundTag.copy().merge(compoundTag2);
		}

		if (tag instanceof ListTag listTag && tag2 instanceof ListTag listTag2) {
			ListTag listTag3 = listTag.copy();

			for (Tag tag3 : listTag2) {
				if (!listTag3.addTag(listTag3.size(), tag3)) {
					return null;
				}
			}

			return listTag3;
		}

		if (tag instanceof StringTag stringTag && tag2 instanceof StringTag stringTag2) {
			return StringTag.valueOf(stringTag.getAsString() + stringTag2.getAsString());
		}

		return null;
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.NBT_CRAFTING_RECIPE;
	}
}
