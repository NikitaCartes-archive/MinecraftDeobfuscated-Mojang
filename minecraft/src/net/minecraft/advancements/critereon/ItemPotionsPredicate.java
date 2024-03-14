package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

public record ItemPotionsPredicate(HolderSet<Potion> potions) implements SingleComponentItemPredicate<PotionContents> {
	public static final Codec<ItemPotionsPredicate> CODEC = RegistryCodecs.homogeneousList(Registries.POTION)
		.xmap(ItemPotionsPredicate::new, ItemPotionsPredicate::potions);

	@Override
	public DataComponentType<PotionContents> componentType() {
		return DataComponents.POTION_CONTENTS;
	}

	public boolean matches(ItemStack itemStack, PotionContents potionContents) {
		Optional<Holder<Potion>> optional = potionContents.potion();
		return !optional.isEmpty() && this.potions.contains((Holder<Potion>)optional.get());
	}

	public static ItemSubPredicate potions(HolderSet<Potion> holderSet) {
		return new ItemPotionsPredicate(holderSet);
	}
}
