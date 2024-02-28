package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;

public class FireworkStarItem extends Item {
	public FireworkStarItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		FireworkExplosion fireworkExplosion = itemStack.get(DataComponents.FIREWORK_EXPLOSION);
		if (fireworkExplosion != null) {
			fireworkExplosion.addToTooltip(list::add, tooltipFlag);
		}
	}
}
