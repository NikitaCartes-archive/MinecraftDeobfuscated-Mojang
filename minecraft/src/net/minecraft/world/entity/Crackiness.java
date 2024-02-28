package net.minecraft.world.entity;

import net.minecraft.world.item.ItemStack;

public class Crackiness {
	public static final Crackiness GOLEM = new Crackiness(0.75F, 0.5F, 0.25F);
	public static final Crackiness WOLF_ARMOR = new Crackiness(0.95F, 0.69F, 0.32F);
	private final float fractionLow;
	private final float fractionMedium;
	private final float fractionHigh;

	private Crackiness(float f, float g, float h) {
		this.fractionLow = f;
		this.fractionMedium = g;
		this.fractionHigh = h;
	}

	public Crackiness.Level byFraction(float f) {
		if (f < this.fractionHigh) {
			return Crackiness.Level.HIGH;
		} else if (f < this.fractionMedium) {
			return Crackiness.Level.MEDIUM;
		} else {
			return f < this.fractionLow ? Crackiness.Level.LOW : Crackiness.Level.NONE;
		}
	}

	public Crackiness.Level byDamage(ItemStack itemStack) {
		return !itemStack.isDamageableItem() ? Crackiness.Level.NONE : this.byDamage(itemStack.getDamageValue(), itemStack.getMaxDamage());
	}

	public Crackiness.Level byDamage(int i, int j) {
		return this.byFraction((float)(j - i) / (float)j);
	}

	public static enum Level {
		NONE,
		LOW,
		MEDIUM,
		HIGH;
	}
}
