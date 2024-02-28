package net.minecraft.world.item.component;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

public interface TooltipProvider {
	void addToTooltip(Consumer<Component> consumer, TooltipFlag tooltipFlag);
}
