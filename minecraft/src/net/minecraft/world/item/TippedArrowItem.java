package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

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
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
		if (potionContents != null) {
			potionContents.addPotionTooltip(list::add, 0.125F, tooltipContext.tickRate());
		}
	}

	@Override
	public Component getName(ItemStack itemStack) {
		return (Component)itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
			.potion()
			.map(holder -> Component.translatable(this.descriptionId + ".effect." + ((Potion)holder.value()).name()))
			.orElseGet(() -> Component.translatable(this.descriptionId + ".effect.empty"));
	}
}
