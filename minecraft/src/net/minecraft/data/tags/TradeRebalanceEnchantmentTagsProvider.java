package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class TradeRebalanceEnchantmentTagsProvider extends TagsProvider<Enchantment> {
	public TradeRebalanceEnchantmentTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(packOutput, Registries.ENCHANTMENT, completableFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(EnchantmentTags.TRADES_DESERT_COMMON).add(Enchantments.FIRE_PROTECTION, Enchantments.THORNS, Enchantments.INFINITY);
		this.tag(EnchantmentTags.TRADES_JUNGLE_COMMON).add(Enchantments.FEATHER_FALLING, Enchantments.PROJECTILE_PROTECTION, Enchantments.POWER);
		this.tag(EnchantmentTags.TRADES_PLAINS_COMMON).add(Enchantments.PUNCH, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS);
		this.tag(EnchantmentTags.TRADES_SAVANNA_COMMON).add(Enchantments.KNOCKBACK, Enchantments.BINDING_CURSE, Enchantments.SWEEPING_EDGE);
		this.tag(EnchantmentTags.TRADES_SNOW_COMMON).add(Enchantments.AQUA_AFFINITY, Enchantments.LOOTING, Enchantments.FROST_WALKER);
		this.tag(EnchantmentTags.TRADES_SWAMP_COMMON).add(Enchantments.DEPTH_STRIDER, Enchantments.RESPIRATION, Enchantments.VANISHING_CURSE);
		this.tag(EnchantmentTags.TRADES_TAIGA_COMMON).add(Enchantments.BLAST_PROTECTION, Enchantments.FIRE_ASPECT, Enchantments.FLAME);
		this.tag(EnchantmentTags.TRADES_DESERT_SPECIAL).add(Enchantments.EFFICIENCY);
		this.tag(EnchantmentTags.TRADES_JUNGLE_SPECIAL).add(Enchantments.UNBREAKING);
		this.tag(EnchantmentTags.TRADES_PLAINS_SPECIAL).add(Enchantments.PROTECTION);
		this.tag(EnchantmentTags.TRADES_SAVANNA_SPECIAL).add(Enchantments.SHARPNESS);
		this.tag(EnchantmentTags.TRADES_SNOW_SPECIAL).add(Enchantments.SILK_TOUCH);
		this.tag(EnchantmentTags.TRADES_SWAMP_SPECIAL).add(Enchantments.MENDING);
		this.tag(EnchantmentTags.TRADES_TAIGA_SPECIAL).add(Enchantments.FORTUNE);
	}
}
