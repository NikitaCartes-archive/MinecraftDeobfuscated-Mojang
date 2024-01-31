package net.minecraft.world.item;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;

public interface DyeableLeatherItem {
	String TAG_COLOR = "color";
	String TAG_DISPLAY = "display";
	int DEFAULT_LEATHER_COLOR = 10511680;

	static boolean hasCustomColor(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTagElement("display");
		return compoundTag != null && compoundTag.contains("color", 99);
	}

	static int getColor(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTagElement("display");
		return compoundTag != null && compoundTag.contains("color", 99) ? compoundTag.getInt("color") : 10511680;
	}

	static void clearColor(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTagElement("display");
		if (compoundTag != null && compoundTag.contains("color")) {
			compoundTag.remove("color");
		}
	}

	static void setColor(ItemStack itemStack, int i) {
		itemStack.getOrCreateTagElement("display").putInt("color", i);
	}

	static ItemStack dyeArmor(ItemStack itemStack, List<DyeItem> list) {
		if (!itemStack.is(ItemTags.DYEABLE)) {
			return ItemStack.EMPTY;
		} else {
			int[] is = new int[3];
			int i = 0;
			int j = 0;
			ItemStack itemStack2 = itemStack.copyWithCount(1);
			if (hasCustomColor(itemStack)) {
				int k = getColor(itemStack2);
				float f = (float)(k >> 16 & 0xFF) / 255.0F;
				float g = (float)(k >> 8 & 0xFF) / 255.0F;
				float h = (float)(k & 0xFF) / 255.0F;
				i += (int)(Math.max(f, Math.max(g, h)) * 255.0F);
				is[0] += (int)(f * 255.0F);
				is[1] += (int)(g * 255.0F);
				is[2] += (int)(h * 255.0F);
				j++;
			}

			for (DyeItem dyeItem : list) {
				float[] fs = dyeItem.getDyeColor().getTextureDiffuseColors();
				int l = (int)(fs[0] * 255.0F);
				int m = (int)(fs[1] * 255.0F);
				int n = (int)(fs[2] * 255.0F);
				i += Math.max(l, Math.max(m, n));
				is[0] += l;
				is[1] += m;
				is[2] += n;
				j++;
			}

			int k = is[0] / j;
			int o = is[1] / j;
			int p = is[2] / j;
			float h = (float)i / (float)j;
			float q = (float)Math.max(k, Math.max(o, p));
			k = (int)((float)k * h / q);
			o = (int)((float)o * h / q);
			p = (int)((float)p * h / q);
			int var24 = (k << 8) + o;
			var24 = (var24 << 8) + p;
			setColor(itemStack2, var24);
			return itemStack2;
		}
	}
}
