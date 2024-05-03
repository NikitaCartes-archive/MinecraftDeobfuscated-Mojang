package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantments;

public class VanillaEnchantmentTagsProvider extends EnchantmentTagsProvider {
	public VanillaEnchantmentTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(packOutput, completableFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tooltipOrder(
			provider,
			new ResourceKey[]{
				Enchantments.BINDING_CURSE,
				Enchantments.VANISHING_CURSE,
				Enchantments.RIPTIDE,
				Enchantments.CHANNELING,
				Enchantments.WIND_BURST,
				Enchantments.FROST_WALKER,
				Enchantments.SHARPNESS,
				Enchantments.SMITE,
				Enchantments.BANE_OF_ARTHROPODS,
				Enchantments.IMPALING,
				Enchantments.POWER,
				Enchantments.DENSITY,
				Enchantments.BREACH,
				Enchantments.PIERCING,
				Enchantments.SWEEPING_EDGE,
				Enchantments.MULTISHOT,
				Enchantments.FIRE_ASPECT,
				Enchantments.FLAME,
				Enchantments.KNOCKBACK,
				Enchantments.PUNCH,
				Enchantments.PROTECTION,
				Enchantments.BLAST_PROTECTION,
				Enchantments.FIRE_PROTECTION,
				Enchantments.PROJECTILE_PROTECTION,
				Enchantments.FEATHER_FALLING,
				Enchantments.FORTUNE,
				Enchantments.LOOTING,
				Enchantments.SILK_TOUCH,
				Enchantments.LUCK_OF_THE_SEA,
				Enchantments.EFFICIENCY,
				Enchantments.QUICK_CHARGE,
				Enchantments.LURE,
				Enchantments.RESPIRATION,
				Enchantments.AQUA_AFFINITY,
				Enchantments.SOUL_SPEED,
				Enchantments.SWIFT_SNEAK,
				Enchantments.DEPTH_STRIDER,
				Enchantments.THORNS,
				Enchantments.LOYALTY,
				Enchantments.UNBREAKING,
				Enchantments.INFINITY,
				Enchantments.MENDING
			}
		);
		this.tag(EnchantmentTags.ARMOR_EXCLUSIVE)
			.add(Enchantments.PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.PROJECTILE_PROTECTION);
		this.tag(EnchantmentTags.BOOTS_EXCLUSIVE).add(Enchantments.FROST_WALKER, Enchantments.DEPTH_STRIDER);
		this.tag(EnchantmentTags.BOW_EXCLUSIVE).add(Enchantments.INFINITY, Enchantments.MENDING);
		this.tag(EnchantmentTags.CROSSBOW_EXCLUSIVE).add(Enchantments.MULTISHOT, Enchantments.PIERCING);
		this.tag(EnchantmentTags.DAMAGE_EXCLUSIVE)
			.add(Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.IMPALING, Enchantments.DENSITY, Enchantments.BREACH);
		this.tag(EnchantmentTags.MINING_EXCLUSIVE).add(Enchantments.FORTUNE, Enchantments.SILK_TOUCH);
		this.tag(EnchantmentTags.RIPTIDE_EXCLUSIVE).add(Enchantments.LOYALTY, Enchantments.CHANNELING);
		this.tag(EnchantmentTags.TREASURE)
			.add(
				Enchantments.BINDING_CURSE,
				Enchantments.VANISHING_CURSE,
				Enchantments.SWIFT_SNEAK,
				Enchantments.SOUL_SPEED,
				Enchantments.FROST_WALKER,
				Enchantments.MENDING,
				Enchantments.WIND_BURST
			);
		this.tag(EnchantmentTags.NON_TREASURE)
			.add(
				Enchantments.PROTECTION,
				Enchantments.FIRE_PROTECTION,
				Enchantments.FEATHER_FALLING,
				Enchantments.BLAST_PROTECTION,
				Enchantments.PROJECTILE_PROTECTION,
				Enchantments.RESPIRATION,
				Enchantments.AQUA_AFFINITY,
				Enchantments.THORNS,
				Enchantments.DEPTH_STRIDER,
				Enchantments.SHARPNESS,
				Enchantments.SMITE,
				Enchantments.BANE_OF_ARTHROPODS,
				Enchantments.KNOCKBACK,
				Enchantments.FIRE_ASPECT,
				Enchantments.LOOTING,
				Enchantments.SWEEPING_EDGE,
				Enchantments.EFFICIENCY,
				Enchantments.SILK_TOUCH,
				Enchantments.UNBREAKING,
				Enchantments.FORTUNE,
				Enchantments.POWER,
				Enchantments.PUNCH,
				Enchantments.FLAME,
				Enchantments.INFINITY,
				Enchantments.LUCK_OF_THE_SEA,
				Enchantments.LURE,
				Enchantments.LOYALTY,
				Enchantments.IMPALING,
				Enchantments.RIPTIDE,
				Enchantments.CHANNELING,
				Enchantments.MULTISHOT,
				Enchantments.QUICK_CHARGE,
				Enchantments.PIERCING,
				Enchantments.DENSITY,
				Enchantments.BREACH
			);
		this.tag(EnchantmentTags.DOUBLE_TRADE_PRICE).addTag(EnchantmentTags.TREASURE);
		this.tag(EnchantmentTags.IN_ENCHANTING_TABLE).addTag(EnchantmentTags.NON_TREASURE);
		this.tag(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT).addTag(EnchantmentTags.NON_TREASURE);
		this.tag(EnchantmentTags.ON_TRADED_EQUIPMENT).addTag(EnchantmentTags.NON_TREASURE);
		this.tag(EnchantmentTags.ON_RANDOM_LOOT)
			.addTag(EnchantmentTags.NON_TREASURE)
			.add(Enchantments.BINDING_CURSE, Enchantments.VANISHING_CURSE, Enchantments.FROST_WALKER, Enchantments.MENDING);
		this.tag(EnchantmentTags.TRADEABLE)
			.addTag(EnchantmentTags.NON_TREASURE)
			.add(Enchantments.BINDING_CURSE, Enchantments.VANISHING_CURSE, Enchantments.FROST_WALKER, Enchantments.MENDING);
		this.tag(EnchantmentTags.CURSE).add(Enchantments.BINDING_CURSE, Enchantments.VANISHING_CURSE);
		this.tag(EnchantmentTags.SMELTS_LOOT).add(Enchantments.FIRE_ASPECT);
		this.tag(EnchantmentTags.PREVENTS_BEE_SPAWNS_WHEN_MINING).add(Enchantments.SILK_TOUCH);
		this.tag(EnchantmentTags.PREVENTS_DECORATED_POT_SHATTERING).add(Enchantments.SILK_TOUCH);
		this.tag(EnchantmentTags.PREVENTS_ICE_MELTING).add(Enchantments.SILK_TOUCH);
		this.tag(EnchantmentTags.PREVENTS_INFESTED_SPAWNS).add(Enchantments.SILK_TOUCH);
	}
}
