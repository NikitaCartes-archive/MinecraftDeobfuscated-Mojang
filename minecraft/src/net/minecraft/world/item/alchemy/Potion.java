package net.minecraft.world.item.alchemy;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;

public class Potion {
	@Nullable
	private final String name;
	private final List<MobEffectInstance> effects;

	public Potion(MobEffectInstance... mobEffectInstances) {
		this(null, mobEffectInstances);
	}

	public Potion(@Nullable String string, MobEffectInstance... mobEffectInstances) {
		this.name = string;
		this.effects = List.of(mobEffectInstances);
	}

	public static String getName(Optional<Holder<Potion>> optional, String string) {
		if (optional.isPresent()) {
			String string2 = ((Potion)((Holder)optional.get()).value()).name;
			if (string2 != null) {
				return string + string2;
			}
		}

		String string2 = (String)optional.flatMap(Holder::unwrapKey).map(resourceKey -> resourceKey.location().getPath()).orElse("empty");
		return string + string2;
	}

	public List<MobEffectInstance> getEffects() {
		return this.effects;
	}

	public boolean hasInstantEffects() {
		if (!this.effects.isEmpty()) {
			for (MobEffectInstance mobEffectInstance : this.effects) {
				if (mobEffectInstance.getEffect().value().isInstantenous()) {
					return true;
				}
			}
		}

		return false;
	}
}
