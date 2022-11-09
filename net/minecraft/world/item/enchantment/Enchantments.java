/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.enchantment;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.ArrowDamageEnchantment;
import net.minecraft.world.item.enchantment.ArrowFireEnchantment;
import net.minecraft.world.item.enchantment.ArrowInfiniteEnchantment;
import net.minecraft.world.item.enchantment.ArrowKnockbackEnchantment;
import net.minecraft.world.item.enchantment.ArrowPiercingEnchantment;
import net.minecraft.world.item.enchantment.BindingCurseEnchantment;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.DiggingEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.FireAspectEnchantment;
import net.minecraft.world.item.enchantment.FishingSpeedEnchantment;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.item.enchantment.KnockbackEnchantment;
import net.minecraft.world.item.enchantment.LootBonusEnchantment;
import net.minecraft.world.item.enchantment.MendingEnchantment;
import net.minecraft.world.item.enchantment.MultiShotEnchantment;
import net.minecraft.world.item.enchantment.OxygenEnchantment;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.item.enchantment.QuickChargeEnchantment;
import net.minecraft.world.item.enchantment.SoulSpeedEnchantment;
import net.minecraft.world.item.enchantment.SweepingEdgeEnchantment;
import net.minecraft.world.item.enchantment.SwiftSneakEnchantment;
import net.minecraft.world.item.enchantment.ThornsEnchantment;
import net.minecraft.world.item.enchantment.TridentChannelingEnchantment;
import net.minecraft.world.item.enchantment.TridentImpalerEnchantment;
import net.minecraft.world.item.enchantment.TridentLoyaltyEnchantment;
import net.minecraft.world.item.enchantment.TridentRiptideEnchantment;
import net.minecraft.world.item.enchantment.UntouchingEnchantment;
import net.minecraft.world.item.enchantment.VanishingCurseEnchantment;
import net.minecraft.world.item.enchantment.WaterWalkerEnchantment;
import net.minecraft.world.item.enchantment.WaterWorkerEnchantment;

public class Enchantments {
    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static final Enchantment ALL_DAMAGE_PROTECTION = Enchantments.register("protection", new ProtectionEnchantment(Enchantment.Rarity.COMMON, ProtectionEnchantment.Type.ALL, ARMOR_SLOTS));
    public static final Enchantment FIRE_PROTECTION = Enchantments.register("fire_protection", new ProtectionEnchantment(Enchantment.Rarity.UNCOMMON, ProtectionEnchantment.Type.FIRE, ARMOR_SLOTS));
    public static final Enchantment FALL_PROTECTION = Enchantments.register("feather_falling", new ProtectionEnchantment(Enchantment.Rarity.UNCOMMON, ProtectionEnchantment.Type.FALL, ARMOR_SLOTS));
    public static final Enchantment BLAST_PROTECTION = Enchantments.register("blast_protection", new ProtectionEnchantment(Enchantment.Rarity.RARE, ProtectionEnchantment.Type.EXPLOSION, ARMOR_SLOTS));
    public static final Enchantment PROJECTILE_PROTECTION = Enchantments.register("projectile_protection", new ProtectionEnchantment(Enchantment.Rarity.UNCOMMON, ProtectionEnchantment.Type.PROJECTILE, ARMOR_SLOTS));
    public static final Enchantment RESPIRATION = Enchantments.register("respiration", new OxygenEnchantment(Enchantment.Rarity.RARE, ARMOR_SLOTS));
    public static final Enchantment AQUA_AFFINITY = Enchantments.register("aqua_affinity", new WaterWorkerEnchantment(Enchantment.Rarity.RARE, ARMOR_SLOTS));
    public static final Enchantment THORNS = Enchantments.register("thorns", new ThornsEnchantment(Enchantment.Rarity.VERY_RARE, ARMOR_SLOTS));
    public static final Enchantment DEPTH_STRIDER = Enchantments.register("depth_strider", new WaterWalkerEnchantment(Enchantment.Rarity.RARE, ARMOR_SLOTS));
    public static final Enchantment FROST_WALKER = Enchantments.register("frost_walker", new FrostWalkerEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.FEET));
    public static final Enchantment BINDING_CURSE = Enchantments.register("binding_curse", new BindingCurseEnchantment(Enchantment.Rarity.VERY_RARE, ARMOR_SLOTS));
    public static final Enchantment SOUL_SPEED = Enchantments.register("soul_speed", new SoulSpeedEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.FEET));
    public static final Enchantment SWIFT_SNEAK = Enchantments.register("swift_sneak", new SwiftSneakEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.LEGS));
    public static final Enchantment SHARPNESS = Enchantments.register("sharpness", new DamageEnchantment(Enchantment.Rarity.COMMON, 0, EquipmentSlot.MAINHAND));
    public static final Enchantment SMITE = Enchantments.register("smite", new DamageEnchantment(Enchantment.Rarity.UNCOMMON, 1, EquipmentSlot.MAINHAND));
    public static final Enchantment BANE_OF_ARTHROPODS = Enchantments.register("bane_of_arthropods", new DamageEnchantment(Enchantment.Rarity.UNCOMMON, 2, EquipmentSlot.MAINHAND));
    public static final Enchantment KNOCKBACK = Enchantments.register("knockback", new KnockbackEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.MAINHAND));
    public static final Enchantment FIRE_ASPECT = Enchantments.register("fire_aspect", new FireAspectEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));
    public static final Enchantment MOB_LOOTING = Enchantments.register("looting", new LootBonusEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, EquipmentSlot.MAINHAND));
    public static final Enchantment SWEEPING_EDGE = Enchantments.register("sweeping", new SweepingEdgeEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));
    public static final Enchantment BLOCK_EFFICIENCY = Enchantments.register("efficiency", new DiggingEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));
    public static final Enchantment SILK_TOUCH = Enchantments.register("silk_touch", new UntouchingEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.MAINHAND));
    public static final Enchantment UNBREAKING = Enchantments.register("unbreaking", new DigDurabilityEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.MAINHAND));
    public static final Enchantment BLOCK_FORTUNE = Enchantments.register("fortune", new LootBonusEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.DIGGER, EquipmentSlot.MAINHAND));
    public static final Enchantment POWER_ARROWS = Enchantments.register("power", new ArrowDamageEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));
    public static final Enchantment PUNCH_ARROWS = Enchantments.register("punch", new ArrowKnockbackEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));
    public static final Enchantment FLAMING_ARROWS = Enchantments.register("flame", new ArrowFireEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));
    public static final Enchantment INFINITY_ARROWS = Enchantments.register("infinity", new ArrowInfiniteEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.MAINHAND));
    public static final Enchantment FISHING_LUCK = Enchantments.register("luck_of_the_sea", new LootBonusEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.FISHING_ROD, EquipmentSlot.MAINHAND));
    public static final Enchantment FISHING_SPEED = Enchantments.register("lure", new FishingSpeedEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.FISHING_ROD, EquipmentSlot.MAINHAND));
    public static final Enchantment LOYALTY = Enchantments.register("loyalty", new TridentLoyaltyEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.MAINHAND));
    public static final Enchantment IMPALING = Enchantments.register("impaling", new TridentImpalerEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));
    public static final Enchantment RIPTIDE = Enchantments.register("riptide", new TridentRiptideEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));
    public static final Enchantment CHANNELING = Enchantments.register("channeling", new TridentChannelingEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.MAINHAND));
    public static final Enchantment MULTISHOT = Enchantments.register("multishot", new MultiShotEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));
    public static final Enchantment QUICK_CHARGE = Enchantments.register("quick_charge", new QuickChargeEnchantment(Enchantment.Rarity.UNCOMMON, EquipmentSlot.MAINHAND));
    public static final Enchantment PIERCING = Enchantments.register("piercing", new ArrowPiercingEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));
    public static final Enchantment MENDING = Enchantments.register("mending", new MendingEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.values()));
    public static final Enchantment VANISHING_CURSE = Enchantments.register("vanishing_curse", new VanishingCurseEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.values()));

    private static Enchantment register(String string, Enchantment enchantment) {
        return Registry.register(BuiltInRegistries.ENCHANTMENT, string, enchantment);
    }
}

