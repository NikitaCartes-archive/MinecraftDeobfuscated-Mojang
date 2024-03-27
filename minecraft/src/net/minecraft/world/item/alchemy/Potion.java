package net.minecraft.world.item.alchemy;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class Potion implements FeatureElement {
	@Nullable
	private final String name;
	private final List<MobEffectInstance> effects;
	private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

	public Potion(MobEffectInstance... mobEffectInstances) {
		this(null, mobEffectInstances);
	}

	public Potion(@Nullable String string, MobEffectInstance... mobEffectInstances) {
		this.name = string;
		this.effects = List.of(mobEffectInstances);
	}

	public Potion requiredFeatures(FeatureFlag... featureFlags) {
		this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlags);
		return this;
	}

	@Override
	public FeatureFlagSet requiredFeatures() {
		return this.requiredFeatures;
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
