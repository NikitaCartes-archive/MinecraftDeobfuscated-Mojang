package net.minecraft.world.item.alchemy;

import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

public class Potion {
	@Nullable
	private final String name;
	private final List<MobEffectInstance> effects;

	public static Holder<Potion> byName(String string) {
		ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
		return resourceLocation == null ? Potions.EMPTY : (Holder)BuiltInRegistries.POTION.getHolder(resourceLocation).map(Function.identity()).orElse(Potions.EMPTY);
	}

	public Potion(MobEffectInstance... mobEffectInstances) {
		this(null, mobEffectInstances);
	}

	public Potion(@Nullable String string, MobEffectInstance... mobEffectInstances) {
		this.name = string;
		this.effects = List.of(mobEffectInstances);
	}

	public static String getName(Holder<Potion> holder, String string) {
		String string2 = holder.value().name;
		if (string2 != null) {
			return string + string2;
		} else {
			ResourceKey<Potion> resourceKey = (ResourceKey<Potion>)holder.unwrapKey().orElse(Potions.EMPTY_ID);
			return string + resourceKey.location().getPath();
		}
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
