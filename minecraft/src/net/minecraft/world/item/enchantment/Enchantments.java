package net.minecraft.world.item.enchantment;

import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;

public class Enchantments {
	private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	public static final Enchantment PROTECTION = register(
		"protection",
		new ProtectionEnchantment(
			Enchantment.definition(ItemTags.ARMOR_ENCHANTABLE, 10, 4, Enchantment.dynamicCost(1, 11), Enchantment.dynamicCost(12, 11), 1, ARMOR_SLOTS),
			ProtectionEnchantment.Type.ALL
		)
	);
	public static final Enchantment FIRE_PROTECTION = register(
		"fire_protection",
		new ProtectionEnchantment(
			Enchantment.definition(ItemTags.ARMOR_ENCHANTABLE, 5, 4, Enchantment.dynamicCost(10, 8), Enchantment.dynamicCost(18, 8), 2, ARMOR_SLOTS),
			ProtectionEnchantment.Type.FIRE
		)
	);
	public static final Enchantment FEATHER_FALLING = register(
		"feather_falling",
		new ProtectionEnchantment(
			Enchantment.definition(ItemTags.FOOT_ARMOR_ENCHANTABLE, 5, 4, Enchantment.dynamicCost(5, 6), Enchantment.dynamicCost(11, 6), 2, ARMOR_SLOTS),
			ProtectionEnchantment.Type.FALL
		)
	);
	public static final Enchantment BLAST_PROTECTION = register(
		"blast_protection",
		new ProtectionEnchantment(
			Enchantment.definition(ItemTags.ARMOR_ENCHANTABLE, 2, 4, Enchantment.dynamicCost(5, 8), Enchantment.dynamicCost(13, 8), 4, ARMOR_SLOTS),
			ProtectionEnchantment.Type.EXPLOSION
		)
	);
	public static final Enchantment PROJECTILE_PROTECTION = register(
		"projectile_protection",
		new ProtectionEnchantment(
			Enchantment.definition(ItemTags.ARMOR_ENCHANTABLE, 5, 4, Enchantment.dynamicCost(3, 6), Enchantment.dynamicCost(9, 6), 2, ARMOR_SLOTS),
			ProtectionEnchantment.Type.PROJECTILE
		)
	);
	public static final Enchantment RESPIRATION = register(
		"respiration",
		new Enchantment(
			Enchantment.definition(ItemTags.HEAD_ARMOR_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(10, 10), Enchantment.dynamicCost(40, 10), 4, ARMOR_SLOTS)
		)
	);
	public static final Enchantment AQUA_AFFINITY = register(
		"aqua_affinity",
		new Enchantment(Enchantment.definition(ItemTags.HEAD_ARMOR_ENCHANTABLE, 2, 1, Enchantment.constantCost(1), Enchantment.constantCost(41), 4, ARMOR_SLOTS))
	);
	public static final Enchantment THORNS = register(
		"thorns",
		new ThornsEnchantment(
			Enchantment.definition(
				ItemTags.ARMOR_ENCHANTABLE, ItemTags.CHEST_ARMOR_ENCHANTABLE, 1, 3, Enchantment.dynamicCost(10, 20), Enchantment.dynamicCost(60, 20), 8, ARMOR_SLOTS
			)
		)
	);
	public static final Enchantment DEPTH_STRIDER = register(
		"depth_strider",
		new WaterWalkerEnchantment(
			Enchantment.definition(ItemTags.FOOT_ARMOR_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(10, 10), Enchantment.dynamicCost(25, 10), 4, ARMOR_SLOTS)
		)
	);
	public static final Enchantment FROST_WALKER = register(
		"frost_walker",
		new FrostWalkerEnchantment(
			Enchantment.definition(ItemTags.FOOT_ARMOR_ENCHANTABLE, 2, 2, Enchantment.dynamicCost(10, 10), Enchantment.dynamicCost(25, 10), 4, EquipmentSlot.FEET)
		)
	);
	public static final Enchantment BINDING_CURSE = register(
		"binding_curse",
		new BindingCurseEnchantment(
			Enchantment.definition(ItemTags.EQUIPPABLE_ENCHANTABLE, 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, ARMOR_SLOTS)
		)
	);
	public static final Enchantment SOUL_SPEED = register(
		"soul_speed",
		new SoulSpeedEnchantment(
			Enchantment.definition(ItemTags.FOOT_ARMOR_ENCHANTABLE, 1, 3, Enchantment.dynamicCost(10, 10), Enchantment.dynamicCost(25, 10), 8, EquipmentSlot.FEET)
		)
	);
	public static final Enchantment SWIFT_SNEAK = register(
		"swift_sneak",
		new SwiftSneakEnchantment(
			Enchantment.definition(ItemTags.LEG_ARMOR_ENCHANTABLE, 1, 3, Enchantment.dynamicCost(25, 25), Enchantment.dynamicCost(75, 25), 8, EquipmentSlot.LEGS)
		)
	);
	public static final Enchantment SHARPNESS = register(
		"sharpness",
		new DamageEnchantment(
			Enchantment.definition(
				ItemTags.WEAPON_ENCHANTABLE, ItemTags.SWORD_ENCHANTABLE, 10, 5, Enchantment.dynamicCost(1, 11), Enchantment.dynamicCost(21, 11), 1, EquipmentSlot.MAINHAND
			),
			Optional.empty()
		)
	);
	public static final Enchantment SMITE = register(
		"smite",
		new DamageEnchantment(
			Enchantment.definition(
				ItemTags.WEAPON_ENCHANTABLE, ItemTags.SWORD_ENCHANTABLE, 5, 5, Enchantment.dynamicCost(5, 8), Enchantment.dynamicCost(25, 8), 2, EquipmentSlot.MAINHAND
			),
			Optional.of(EntityTypeTags.SENSITIVE_TO_SMITE)
		)
	);
	public static final Enchantment BANE_OF_ARTHROPODS = register(
		"bane_of_arthropods",
		new DamageEnchantment(
			Enchantment.definition(
				ItemTags.WEAPON_ENCHANTABLE, ItemTags.SWORD_ENCHANTABLE, 5, 5, Enchantment.dynamicCost(5, 8), Enchantment.dynamicCost(25, 8), 2, EquipmentSlot.MAINHAND
			),
			Optional.of(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)
		)
	);
	public static final Enchantment KNOCKBACK = register(
		"knockback",
		new Enchantment(
			Enchantment.definition(ItemTags.SWORD_ENCHANTABLE, 5, 2, Enchantment.dynamicCost(5, 20), Enchantment.dynamicCost(55, 20), 2, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment FIRE_ASPECT = register(
		"fire_aspect",
		new Enchantment(
			Enchantment.definition(ItemTags.SWORD_ENCHANTABLE, 2, 2, Enchantment.dynamicCost(10, 20), Enchantment.dynamicCost(60, 20), 4, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment LOOTING = register(
		"looting",
		new LootBonusEnchantment(
			Enchantment.definition(ItemTags.SWORD_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment SWEEPING_EDGE = register(
		"sweeping_edge",
		new Enchantment(
			Enchantment.definition(ItemTags.SWORD_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(5, 9), Enchantment.dynamicCost(20, 9), 4, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment EFFICIENCY = register(
		"efficiency",
		new Enchantment(
			Enchantment.definition(ItemTags.MINING_ENCHANTABLE, 10, 5, Enchantment.dynamicCost(1, 10), Enchantment.dynamicCost(51, 10), 1, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment SILK_TOUCH = register(
		"silk_touch",
		new UntouchingEnchantment(
			Enchantment.definition(ItemTags.MINING_LOOT_ENCHANTABLE, 1, 1, Enchantment.constantCost(15), Enchantment.constantCost(65), 8, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment UNBREAKING = register(
		"unbreaking",
		new DigDurabilityEnchantment(
			Enchantment.definition(ItemTags.DURABILITY_ENCHANTABLE, 5, 3, Enchantment.dynamicCost(5, 8), Enchantment.dynamicCost(55, 8), 2, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment FORTUNE = register(
		"fortune",
		new LootBonusEnchantment(
			Enchantment.definition(ItemTags.MINING_LOOT_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment POWER = register(
		"power",
		new Enchantment(
			Enchantment.definition(ItemTags.BOW_ENCHANTABLE, 10, 5, Enchantment.dynamicCost(1, 10), Enchantment.dynamicCost(16, 10), 1, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment PUNCH = register(
		"punch",
		new Enchantment(
			Enchantment.definition(ItemTags.BOW_ENCHANTABLE, 2, 2, Enchantment.dynamicCost(12, 20), Enchantment.dynamicCost(37, 20), 4, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment FLAME = register(
		"flame",
		new Enchantment(Enchantment.definition(ItemTags.BOW_ENCHANTABLE, 2, 1, Enchantment.constantCost(20), Enchantment.constantCost(50), 4, EquipmentSlot.MAINHAND))
	);
	public static final Enchantment INFINITY = register(
		"infinity",
		new ArrowInfiniteEnchantment(
			Enchantment.definition(ItemTags.BOW_ENCHANTABLE, 1, 1, Enchantment.constantCost(20), Enchantment.constantCost(50), 8, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment LUCK_OF_THE_SEA = register(
		"luck_of_the_sea",
		new LootBonusEnchantment(
			Enchantment.definition(ItemTags.FISHING_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment LURE = register(
		"lure",
		new Enchantment(
			Enchantment.definition(ItemTags.FISHING_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment LOYALTY = register(
		"loyalty",
		new Enchantment(
			Enchantment.definition(ItemTags.TRIDENT_ENCHANTABLE, 5, 3, Enchantment.dynamicCost(12, 7), Enchantment.constantCost(50), 2, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment IMPALING = register(
		"impaling",
		new DamageEnchantment(
			Enchantment.definition(ItemTags.TRIDENT_ENCHANTABLE, 2, 5, Enchantment.dynamicCost(1, 8), Enchantment.dynamicCost(21, 8), 4, EquipmentSlot.MAINHAND),
			Optional.of(EntityTypeTags.SENSITIVE_TO_IMPALING)
		)
	);
	public static final Enchantment RIPTIDE = register(
		"riptide",
		new TridentRiptideEnchantment(
			Enchantment.definition(ItemTags.TRIDENT_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(17, 7), Enchantment.constantCost(50), 4, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment CHANNELING = register(
		"channeling",
		new Enchantment(
			Enchantment.definition(ItemTags.TRIDENT_ENCHANTABLE, 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment MULTISHOT = register(
		"multishot",
		new MultiShotEnchantment(
			Enchantment.definition(ItemTags.CROSSBOW_ENCHANTABLE, 2, 1, Enchantment.constantCost(20), Enchantment.constantCost(50), 4, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment QUICK_CHARGE = register(
		"quick_charge",
		new Enchantment(
			Enchantment.definition(ItemTags.CROSSBOW_ENCHANTABLE, 5, 3, Enchantment.dynamicCost(12, 20), Enchantment.constantCost(50), 2, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment PIERCING = register(
		"piercing",
		new ArrowPiercingEnchantment(
			Enchantment.definition(ItemTags.CROSSBOW_ENCHANTABLE, 10, 4, Enchantment.dynamicCost(1, 10), Enchantment.constantCost(50), 1, EquipmentSlot.MAINHAND)
		)
	);
	public static final Enchantment MENDING = register(
		"mending",
		new MendingEnchantment(
			Enchantment.definition(ItemTags.DURABILITY_ENCHANTABLE, 2, 1, Enchantment.dynamicCost(25, 25), Enchantment.dynamicCost(75, 25), 4, EquipmentSlot.values())
		)
	);
	public static final Enchantment VANISHING_CURSE = register(
		"vanishing_curse",
		new VanishingCurseEnchantment(
			Enchantment.definition(ItemTags.VANISHING_ENCHANTABLE, 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, EquipmentSlot.values())
		)
	);

	private static Enchantment register(String string, Enchantment enchantment) {
		return Registry.register(BuiltInRegistries.ENCHANTMENT, string, enchantment);
	}
}
