/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public interface DamageTypeTags {
    public static final TagKey<DamageType> DAMAGES_HELMET = DamageTypeTags.create("damages_helmet");
    public static final TagKey<DamageType> BYPASSES_ARMOR = DamageTypeTags.create("bypasses_armor");
    public static final TagKey<DamageType> BYPASSES_INVULNERABILITY = DamageTypeTags.create("bypasses_invulnerability");
    public static final TagKey<DamageType> BYPASSES_EFFECTS = DamageTypeTags.create("bypasses_effects");
    public static final TagKey<DamageType> BYPASSES_RESISTANCE = DamageTypeTags.create("bypasses_resistance");
    public static final TagKey<DamageType> BYPASSES_ENCHANTMENTS = DamageTypeTags.create("bypasses_enchantments");
    public static final TagKey<DamageType> IS_FIRE = DamageTypeTags.create("is_fire");
    public static final TagKey<DamageType> IS_PROJECTILE = DamageTypeTags.create("is_projectile");
    public static final TagKey<DamageType> WITCH_RESISTANT_TO = DamageTypeTags.create("witch_resistant_to");
    public static final TagKey<DamageType> IS_EXPLOSION = DamageTypeTags.create("is_explosion");
    public static final TagKey<DamageType> IS_FALL = DamageTypeTags.create("is_fall");
    public static final TagKey<DamageType> IS_DROWNING = DamageTypeTags.create("is_drowning");
    public static final TagKey<DamageType> IS_FREEZING = DamageTypeTags.create("is_freezing");
    public static final TagKey<DamageType> IS_LIGHTNING = DamageTypeTags.create("is_lightning");
    public static final TagKey<DamageType> NO_ANGER = DamageTypeTags.create("no_anger");
    public static final TagKey<DamageType> NO_IMPACT = DamageTypeTags.create("no_impact");
    public static final TagKey<DamageType> ALWAYS_MOST_SIGNIFICANT_FALL = DamageTypeTags.create("always_most_significant_fall");
    public static final TagKey<DamageType> WITHER_IMMUNE_TO = DamageTypeTags.create("wither_immune_to");
    public static final TagKey<DamageType> IGNITES_ARMOR_STANDS = DamageTypeTags.create("ignites_armor_stands");
    public static final TagKey<DamageType> BURNS_ARMOR_STANDS = DamageTypeTags.create("burns_armor_stands");
    public static final TagKey<DamageType> AVOIDS_GUARDIAN_THORNS = DamageTypeTags.create("avoids_guardian_thorns");
    public static final TagKey<DamageType> ALWAYS_TRIGGERS_SILVERFISH = DamageTypeTags.create("always_triggers_silverfish");

    private static TagKey<DamageType> create(String string) {
        return TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(string));
    }
}

