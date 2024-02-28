package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public class TippedArrowItem extends ArrowItem {
	public TippedArrowItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack getDefaultInstance() {
		ItemStack itemStack = super.getDefaultInstance();
		itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.POISON));
		return itemStack;
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
		if (potionContents != null) {
			potionContents.addPotionTooltip(list::add, 0.125F, level == null ? 20.0F : level.tickRateManager().tickrate());
		}
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return Potion.getName(itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion(), this.getDescriptionId() + ".effect.");
	}
}
