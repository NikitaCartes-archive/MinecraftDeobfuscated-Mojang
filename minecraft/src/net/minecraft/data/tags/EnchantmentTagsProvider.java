package net.minecraft.data.tags;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.enchantment.Enchantment;

public abstract class EnchantmentTagsProvider extends IntrinsicHolderTagsProvider<Enchantment> {
	private final FeatureFlagSet enabledFeatures;

	public EnchantmentTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, FeatureFlagSet featureFlagSet) {
		super(packOutput, Registries.ENCHANTMENT, completableFuture, enchantment -> enchantment.builtInRegistryHolder().key());
		this.enabledFeatures = featureFlagSet;
	}

	protected void tooltipOrder(HolderLookup.Provider provider, Enchantment... enchantments) {
		this.tag(EnchantmentTags.TOOLTIP_ORDER).add(enchantments);
		Set<Enchantment> set = Set.of(enchantments);
		List<String> list = (List<String>)provider.lookupOrThrow(Registries.ENCHANTMENT)
			.listElements()
			.filter(reference -> ((Enchantment)reference.value()).requiredFeatures().isSubsetOf(this.enabledFeatures))
			.filter(reference -> !set.contains(reference.value()))
			.map(Holder::getRegisteredName)
			.collect(Collectors.toList());
		if (!list.isEmpty()) {
			throw new IllegalStateException("Not all enchantments were registered for tooltip ordering. Missing: " + String.join(", ", list));
		}
	}
}
