package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public class FireworkStarItem extends Item {
	public FireworkStarItem(Item.Properties properties) {
		super(properties);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		CompoundTag compoundTag = itemStack.getTagElement("Explosion");
		if (compoundTag != null) {
			appendHoverText(compoundTag, list);
		}
	}

	@Environment(EnvType.CLIENT)
	public static void appendHoverText(CompoundTag compoundTag, List<Component> list) {
		FireworkRocketItem.Shape shape = FireworkRocketItem.Shape.byId(compoundTag.getByte("Type"));
		list.add(new TranslatableComponent("item.minecraft.firework_star.shape." + shape.getName()).withStyle(ChatFormatting.GRAY));
		int[] is = compoundTag.getIntArray("Colors");
		if (is.length > 0) {
			list.add(appendColors(new TextComponent("").withStyle(ChatFormatting.GRAY), is));
		}

		int[] js = compoundTag.getIntArray("FadeColors");
		if (js.length > 0) {
			list.add(appendColors(new TranslatableComponent("item.minecraft.firework_star.fade_to").append(" ").withStyle(ChatFormatting.GRAY), js));
		}

		if (compoundTag.getBoolean("Trail")) {
			list.add(new TranslatableComponent("item.minecraft.firework_star.trail").withStyle(ChatFormatting.GRAY));
		}

		if (compoundTag.getBoolean("Flicker")) {
			list.add(new TranslatableComponent("item.minecraft.firework_star.flicker").withStyle(ChatFormatting.GRAY));
		}
	}

	@Environment(EnvType.CLIENT)
	private static Component appendColors(MutableComponent mutableComponent, int[] is) {
		for (int i = 0; i < is.length; i++) {
			if (i > 0) {
				mutableComponent.append(", ");
			}

			mutableComponent.append(getColorName(is[i]));
		}

		return mutableComponent;
	}

	@Environment(EnvType.CLIENT)
	private static Component getColorName(int i) {
		DyeColor dyeColor = DyeColor.byFireworkColor(i);
		return dyeColor == null
			? new TranslatableComponent("item.minecraft.firework_star.custom_color")
			: new TranslatableComponent("item.minecraft.firework_star." + dyeColor.getName());
	}
}
