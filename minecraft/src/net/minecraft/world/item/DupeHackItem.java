package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.voting.rules.Rules;

public class DupeHackItem extends Item {
	public DupeHackItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean hasCraftingRemainingItem() {
		return (double)((Integer)Rules.DUPE_HACK_BREAK_CHANCE.get()).intValue() / 100.0 < Math.random();
	}

	@Nullable
	@Override
	public Item getCraftingRemainingItem() {
		return this;
	}
}
