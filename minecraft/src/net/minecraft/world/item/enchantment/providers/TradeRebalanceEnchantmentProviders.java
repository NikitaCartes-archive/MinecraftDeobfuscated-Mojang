package net.minecraft.world.item.enchantment.providers;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public interface TradeRebalanceEnchantmentProviders {
	ResourceKey<EnchantmentProvider> TRADES_DESERT_ARMORER_BOOTS_4 = VanillaEnchantmentProviders.create("trades/desert_armorer_boots_4");
	ResourceKey<EnchantmentProvider> TRADES_DESERT_ARMORER_LEGGINGS_4 = VanillaEnchantmentProviders.create("trades/desert_armorer_leggings_4");
	ResourceKey<EnchantmentProvider> TRADES_DESERT_ARMORER_CHESTPLATE_4 = VanillaEnchantmentProviders.create("trades/desert_armorer_chestplate_4");
	ResourceKey<EnchantmentProvider> TRADES_DESERT_ARMORER_HELMET_4 = VanillaEnchantmentProviders.create("trades/desert_armorer_helmet_4");
	ResourceKey<EnchantmentProvider> TRADES_DESERT_ARMORER_LEGGINGS_5 = VanillaEnchantmentProviders.create("trades/desert_armorer_leggings_5");
	ResourceKey<EnchantmentProvider> TRADES_DESERT_ARMORER_CHESTPLATE_5 = VanillaEnchantmentProviders.create("trades/desert_armorer_chestplate_5");
	ResourceKey<EnchantmentProvider> TRADES_PLAINS_ARMORER_BOOTS_4 = VanillaEnchantmentProviders.create("trades/plains_armorer_boots_4");
	ResourceKey<EnchantmentProvider> TRADES_PLAINS_ARMORER_LEGGINGS_4 = VanillaEnchantmentProviders.create("trades/plains_armorer_leggings_4");
	ResourceKey<EnchantmentProvider> TRADES_PLAINS_ARMORER_CHESTPLATE_4 = VanillaEnchantmentProviders.create("trades/plains_armorer_chestplate_4");
	ResourceKey<EnchantmentProvider> TRADES_PLAINS_ARMORER_HELMET_4 = VanillaEnchantmentProviders.create("trades/plains_armorer_helmet_4");
	ResourceKey<EnchantmentProvider> TRADES_PLAINS_ARMORER_BOOTS_5 = VanillaEnchantmentProviders.create("trades/plains_armorer_boots_5");
	ResourceKey<EnchantmentProvider> TRADES_PLAINS_ARMORER_LEGGINGS_5 = VanillaEnchantmentProviders.create("trades/plains_armorer_leggings_5");
	ResourceKey<EnchantmentProvider> TRADES_SAVANNA_ARMORER_BOOTS_4 = VanillaEnchantmentProviders.create("trades/savanna_armorer_boots_4");
	ResourceKey<EnchantmentProvider> TRADES_SAVANNA_ARMORER_LEGGINGS_4 = VanillaEnchantmentProviders.create("trades/savanna_armorer_leggings_4");
	ResourceKey<EnchantmentProvider> TRADES_SAVANNA_ARMORER_CHESTPLATE_4 = VanillaEnchantmentProviders.create("trades/savanna_armorer_chestplate_4");
	ResourceKey<EnchantmentProvider> TRADES_SAVANNA_ARMORER_HELMET_4 = VanillaEnchantmentProviders.create("trades/savanna_armorer_helmet_4");
	ResourceKey<EnchantmentProvider> TRADES_SAVANNA_ARMORER_CHESTPLATE_5 = VanillaEnchantmentProviders.create("trades/savanna_armorer_chestplate_5");
	ResourceKey<EnchantmentProvider> TRADES_SAVANNA_ARMORER_HELMET_5 = VanillaEnchantmentProviders.create("trades/savanna_armorer_helmet_5");
	ResourceKey<EnchantmentProvider> TRADES_SNOW_ARMORER_BOOTS_4 = VanillaEnchantmentProviders.create("trades/snow_armorer_boots_4");
	ResourceKey<EnchantmentProvider> TRADES_SNOW_ARMORER_HELMET_4 = VanillaEnchantmentProviders.create("trades/snow_armorer_helmet_4");
	ResourceKey<EnchantmentProvider> TRADES_SNOW_ARMORER_BOOTS_5 = VanillaEnchantmentProviders.create("trades/snow_armorer_boots_5");
	ResourceKey<EnchantmentProvider> TRADES_SNOW_ARMORER_HELMET_5 = VanillaEnchantmentProviders.create("trades/snow_armorer_helmet_5");
	ResourceKey<EnchantmentProvider> TRADES_JUNGLE_ARMORER_BOOTS_4 = VanillaEnchantmentProviders.create("trades/jungle_armorer_boots_4");
	ResourceKey<EnchantmentProvider> TRADES_JUNGLE_ARMORER_LEGGINGS_4 = VanillaEnchantmentProviders.create("trades/jungle_armorer_leggings_4");
	ResourceKey<EnchantmentProvider> TRADES_JUNGLE_ARMORER_CHESTPLATE_4 = VanillaEnchantmentProviders.create("trades/jungle_armorer_chestplate_4");
	ResourceKey<EnchantmentProvider> TRADES_JUNGLE_ARMORER_HELMET_4 = VanillaEnchantmentProviders.create("trades/jungle_armorer_helmet_4");
	ResourceKey<EnchantmentProvider> TRADES_JUNGLE_ARMORER_BOOTS_5 = VanillaEnchantmentProviders.create("trades/jungle_armorer_boots_5");
	ResourceKey<EnchantmentProvider> TRADES_JUNGLE_ARMORER_HELMET_5 = VanillaEnchantmentProviders.create("trades/jungle_armorer_helmet_5");
	ResourceKey<EnchantmentProvider> TRADES_SWAMP_ARMORER_BOOTS_4 = VanillaEnchantmentProviders.create("trades/swamp_armorer_boots_4");
	ResourceKey<EnchantmentProvider> TRADES_SWAMP_ARMORER_LEGGINGS_4 = VanillaEnchantmentProviders.create("trades/swamp_armorer_leggings_4");
	ResourceKey<EnchantmentProvider> TRADES_SWAMP_ARMORER_CHESTPLATE_4 = VanillaEnchantmentProviders.create("trades/swamp_armorer_chestplate_4");
	ResourceKey<EnchantmentProvider> TRADES_SWAMP_ARMORER_HELMET_4 = VanillaEnchantmentProviders.create("trades/swamp_armorer_helmet_4");
	ResourceKey<EnchantmentProvider> TRADES_SWAMP_ARMORER_BOOTS_5 = VanillaEnchantmentProviders.create("trades/swamp_armorer_boots_5");
	ResourceKey<EnchantmentProvider> TRADES_SWAMP_ARMORER_HELMET_5 = VanillaEnchantmentProviders.create("trades/swamp_armorer_helmet_5");
	ResourceKey<EnchantmentProvider> TRADES_TAIGA_ARMORER_LEGGINGS_5 = VanillaEnchantmentProviders.create("trades/taiga_armorer_leggings_5");
	ResourceKey<EnchantmentProvider> TRADES_TAIGA_ARMORER_CHESTPLATE_5 = VanillaEnchantmentProviders.create("trades/taiga_armorer_chestplate_5");

	static void bootstrap(BootstrapContext<EnchantmentProvider> bootstrapContext) {
		HolderGetter<Enchantment> holderGetter = bootstrapContext.lookup(Registries.ENCHANTMENT);
		bootstrapContext.register(TRADES_DESERT_ARMORER_BOOTS_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.THORNS), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_DESERT_ARMORER_LEGGINGS_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.THORNS), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_DESERT_ARMORER_CHESTPLATE_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.THORNS), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_DESERT_ARMORER_HELMET_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.THORNS), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_DESERT_ARMORER_LEGGINGS_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.THORNS), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_DESERT_ARMORER_CHESTPLATE_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.THORNS), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_PLAINS_ARMORER_BOOTS_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.PROTECTION), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_PLAINS_ARMORER_LEGGINGS_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.PROTECTION), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_PLAINS_ARMORER_CHESTPLATE_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.PROTECTION), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_PLAINS_ARMORER_HELMET_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.PROTECTION), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_PLAINS_ARMORER_BOOTS_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.PROTECTION), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_PLAINS_ARMORER_LEGGINGS_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.PROTECTION), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SAVANNA_ARMORER_BOOTS_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.BINDING_CURSE), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SAVANNA_ARMORER_LEGGINGS_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.BINDING_CURSE), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SAVANNA_ARMORER_CHESTPLATE_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.BINDING_CURSE), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SAVANNA_ARMORER_HELMET_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.BINDING_CURSE), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SAVANNA_ARMORER_CHESTPLATE_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.BINDING_CURSE), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SAVANNA_ARMORER_HELMET_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.BINDING_CURSE), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SNOW_ARMORER_BOOTS_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.FROST_WALKER), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SNOW_ARMORER_HELMET_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.AQUA_AFFINITY), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SNOW_ARMORER_BOOTS_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.FROST_WALKER), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SNOW_ARMORER_HELMET_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.AQUA_AFFINITY), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_JUNGLE_ARMORER_BOOTS_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.UNBREAKING), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_JUNGLE_ARMORER_LEGGINGS_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.UNBREAKING), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_JUNGLE_ARMORER_CHESTPLATE_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.UNBREAKING), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_JUNGLE_ARMORER_HELMET_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.UNBREAKING), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_JUNGLE_ARMORER_BOOTS_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.FEATHER_FALLING), ConstantInt.of(1)));
		bootstrapContext.register(
			TRADES_JUNGLE_ARMORER_HELMET_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.PROJECTILE_PROTECTION), ConstantInt.of(1))
		);
		bootstrapContext.register(TRADES_SWAMP_ARMORER_BOOTS_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.MENDING), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SWAMP_ARMORER_LEGGINGS_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.MENDING), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SWAMP_ARMORER_CHESTPLATE_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.MENDING), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SWAMP_ARMORER_HELMET_4, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.MENDING), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SWAMP_ARMORER_BOOTS_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.DEPTH_STRIDER), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_SWAMP_ARMORER_HELMET_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.RESPIRATION), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_TAIGA_ARMORER_LEGGINGS_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.BLAST_PROTECTION), ConstantInt.of(1)));
		bootstrapContext.register(TRADES_TAIGA_ARMORER_CHESTPLATE_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.BLAST_PROTECTION), ConstantInt.of(1)));
	}
}
