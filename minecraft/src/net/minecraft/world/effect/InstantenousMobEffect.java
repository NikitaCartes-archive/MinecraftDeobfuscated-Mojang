package net.minecraft.world.effect;

public class InstantenousMobEffect extends MobEffect {
	public InstantenousMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public boolean isInstantenous() {
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int i, int j) {
		return i >= 1;
	}
}
