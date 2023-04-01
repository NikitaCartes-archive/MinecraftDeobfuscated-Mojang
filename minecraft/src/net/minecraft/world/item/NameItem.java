package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class NameItem extends Item {
	private static final String TAG_NAME = "name";

	public NameItem(Item.Properties properties) {
		super(properties);
	}

	@Nullable
	public static String getContainedName(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null && compoundTag.contains("name", 8) ? compoundTag.getString("name") : null;
	}

	public static void setContainedName(ItemStack itemStack, String string) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		compoundTag.putString("name", string);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		String string = getContainedName(itemStack);
		if (string != null) {
			list.add(Component.literal(string).withStyle(ChatFormatting.GREEN));
		}

		super.appendHoverText(itemStack, level, list, tooltipFlag);
	}
}
