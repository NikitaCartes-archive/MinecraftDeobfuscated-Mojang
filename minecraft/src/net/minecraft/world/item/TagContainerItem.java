package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class TagContainerItem<T extends Tag> extends Item {
	private static final String TAG_VALUE = "value";
	private final TagType<T> tagType;

	public TagContainerItem(Item.Properties properties, TagType<T> tagType) {
		super(properties);
		this.tagType = tagType;
	}

	public TagType<T> getTagType() {
		return this.tagType;
	}

	@Nullable
	public T getTag(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag == null) {
			return null;
		} else if (this.tagType != CompoundTag.TYPE) {
			Tag tag = compoundTag.get("value");
			return (T)(tag != null && tag.getType() == this.tagType ? tag : null);
		} else {
			return (T)compoundTag;
		}
	}

	public void setTag(ItemStack itemStack, T tag) {
		if (this.tagType != CompoundTag.TYPE) {
			CompoundTag compoundTag = itemStack.getOrCreateTag();
			compoundTag.put("value", tag);
		} else if (tag instanceof CompoundTag compoundTag2) {
			itemStack.setTag(compoundTag2);
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		T tag = this.getTag(itemStack);
		if (tag != null) {
			list.add(Component.literal(tag.getAsString()).withStyle(ChatFormatting.GREEN));
		}

		super.appendHoverText(itemStack, level, list, tooltipFlag);
	}
}
