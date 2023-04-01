package net.minecraft.voting.rules.actual;

import net.minecraft.voting.rules.EnumRule;
import net.minecraft.world.item.DyeColor;

public abstract class ColorRule extends EnumRule<DyeColor> {
	protected ColorRule(DyeColor dyeColor) {
		super(DyeColor.values(), dyeColor, DyeColor.CODEC);
	}
}
