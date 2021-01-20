/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.VibrationParticleOption;

public class ParticleTypes {
    public static final SimpleParticleType AMBIENT_ENTITY_EFFECT = ParticleTypes.register("ambient_entity_effect", false);
    public static final SimpleParticleType ANGRY_VILLAGER = ParticleTypes.register("angry_villager", false);
    public static final SimpleParticleType BARRIER = ParticleTypes.register("barrier", false);
    public static final ParticleType<BlockParticleOption> BLOCK = ParticleTypes.register("block", BlockParticleOption.DESERIALIZER, BlockParticleOption::codec);
    public static final SimpleParticleType BUBBLE = ParticleTypes.register("bubble", false);
    public static final SimpleParticleType CLOUD = ParticleTypes.register("cloud", false);
    public static final SimpleParticleType CRIT = ParticleTypes.register("crit", false);
    public static final SimpleParticleType DAMAGE_INDICATOR = ParticleTypes.register("damage_indicator", true);
    public static final SimpleParticleType DRAGON_BREATH = ParticleTypes.register("dragon_breath", false);
    public static final SimpleParticleType DRIPPING_LAVA = ParticleTypes.register("dripping_lava", false);
    public static final SimpleParticleType FALLING_LAVA = ParticleTypes.register("falling_lava", false);
    public static final SimpleParticleType LANDING_LAVA = ParticleTypes.register("landing_lava", false);
    public static final SimpleParticleType DRIPPING_WATER = ParticleTypes.register("dripping_water", false);
    public static final SimpleParticleType FALLING_WATER = ParticleTypes.register("falling_water", false);
    public static final ParticleType<DustParticleOptions> DUST = ParticleTypes.register("dust", DustParticleOptions.DESERIALIZER, particleType -> DustParticleOptions.CODEC);
    public static final ParticleType<DustColorTransitionOptions> DUST_COLOR_TRANSITION = ParticleTypes.register("dust_color_transition", DustColorTransitionOptions.DESERIALIZER, particleType -> DustColorTransitionOptions.CODEC);
    public static final SimpleParticleType EFFECT = ParticleTypes.register("effect", false);
    public static final SimpleParticleType ELDER_GUARDIAN = ParticleTypes.register("elder_guardian", true);
    public static final SimpleParticleType ENCHANTED_HIT = ParticleTypes.register("enchanted_hit", false);
    public static final SimpleParticleType ENCHANT = ParticleTypes.register("enchant", false);
    public static final SimpleParticleType END_ROD = ParticleTypes.register("end_rod", false);
    public static final SimpleParticleType ENTITY_EFFECT = ParticleTypes.register("entity_effect", false);
    public static final SimpleParticleType EXPLOSION_EMITTER = ParticleTypes.register("explosion_emitter", true);
    public static final SimpleParticleType EXPLOSION = ParticleTypes.register("explosion", true);
    public static final ParticleType<BlockParticleOption> FALLING_DUST = ParticleTypes.register("falling_dust", BlockParticleOption.DESERIALIZER, BlockParticleOption::codec);
    public static final SimpleParticleType FIREWORK = ParticleTypes.register("firework", false);
    public static final SimpleParticleType FISHING = ParticleTypes.register("fishing", false);
    public static final SimpleParticleType FLAME = ParticleTypes.register("flame", false);
    public static final SimpleParticleType SOUL_FIRE_FLAME = ParticleTypes.register("soul_fire_flame", false);
    public static final SimpleParticleType SOUL = ParticleTypes.register("soul", false);
    public static final SimpleParticleType FLASH = ParticleTypes.register("flash", false);
    public static final SimpleParticleType HAPPY_VILLAGER = ParticleTypes.register("happy_villager", false);
    public static final SimpleParticleType COMPOSTER = ParticleTypes.register("composter", false);
    public static final SimpleParticleType HEART = ParticleTypes.register("heart", false);
    public static final SimpleParticleType INSTANT_EFFECT = ParticleTypes.register("instant_effect", false);
    public static final ParticleType<ItemParticleOption> ITEM = ParticleTypes.register("item", ItemParticleOption.DESERIALIZER, ItemParticleOption::codec);
    public static final ParticleType<VibrationParticleOption> VIBRATION = ParticleTypes.register("vibration", VibrationParticleOption.DESERIALIZER, particleType -> VibrationParticleOption.CODEC);
    public static final SimpleParticleType ITEM_SLIME = ParticleTypes.register("item_slime", false);
    public static final SimpleParticleType ITEM_SNOWBALL = ParticleTypes.register("item_snowball", false);
    public static final SimpleParticleType LARGE_SMOKE = ParticleTypes.register("large_smoke", false);
    public static final SimpleParticleType LAVA = ParticleTypes.register("lava", false);
    public static final SimpleParticleType MYCELIUM = ParticleTypes.register("mycelium", false);
    public static final SimpleParticleType NOTE = ParticleTypes.register("note", false);
    public static final SimpleParticleType POOF = ParticleTypes.register("poof", true);
    public static final SimpleParticleType PORTAL = ParticleTypes.register("portal", false);
    public static final SimpleParticleType RAIN = ParticleTypes.register("rain", false);
    public static final SimpleParticleType SMOKE = ParticleTypes.register("smoke", false);
    public static final SimpleParticleType SNEEZE = ParticleTypes.register("sneeze", false);
    public static final SimpleParticleType SPIT = ParticleTypes.register("spit", true);
    public static final SimpleParticleType SQUID_INK = ParticleTypes.register("squid_ink", true);
    public static final SimpleParticleType SWEEP_ATTACK = ParticleTypes.register("sweep_attack", true);
    public static final SimpleParticleType TOTEM_OF_UNDYING = ParticleTypes.register("totem_of_undying", false);
    public static final SimpleParticleType UNDERWATER = ParticleTypes.register("underwater", false);
    public static final SimpleParticleType SPLASH = ParticleTypes.register("splash", false);
    public static final SimpleParticleType WITCH = ParticleTypes.register("witch", false);
    public static final SimpleParticleType BUBBLE_POP = ParticleTypes.register("bubble_pop", false);
    public static final SimpleParticleType CURRENT_DOWN = ParticleTypes.register("current_down", false);
    public static final SimpleParticleType BUBBLE_COLUMN_UP = ParticleTypes.register("bubble_column_up", false);
    public static final SimpleParticleType NAUTILUS = ParticleTypes.register("nautilus", false);
    public static final SimpleParticleType DOLPHIN = ParticleTypes.register("dolphin", false);
    public static final SimpleParticleType CAMPFIRE_COSY_SMOKE = ParticleTypes.register("campfire_cosy_smoke", true);
    public static final SimpleParticleType CAMPFIRE_SIGNAL_SMOKE = ParticleTypes.register("campfire_signal_smoke", true);
    public static final SimpleParticleType DRIPPING_HONEY = ParticleTypes.register("dripping_honey", false);
    public static final SimpleParticleType FALLING_HONEY = ParticleTypes.register("falling_honey", false);
    public static final SimpleParticleType LANDING_HONEY = ParticleTypes.register("landing_honey", false);
    public static final SimpleParticleType FALLING_NECTAR = ParticleTypes.register("falling_nectar", false);
    public static final SimpleParticleType ASH = ParticleTypes.register("ash", false);
    public static final SimpleParticleType CRIMSON_SPORE = ParticleTypes.register("crimson_spore", false);
    public static final SimpleParticleType WARPED_SPORE = ParticleTypes.register("warped_spore", false);
    public static final SimpleParticleType DRIPPING_OBSIDIAN_TEAR = ParticleTypes.register("dripping_obsidian_tear", false);
    public static final SimpleParticleType FALLING_OBSIDIAN_TEAR = ParticleTypes.register("falling_obsidian_tear", false);
    public static final SimpleParticleType LANDING_OBSIDIAN_TEAR = ParticleTypes.register("landing_obsidian_tear", false);
    public static final SimpleParticleType REVERSE_PORTAL = ParticleTypes.register("reverse_portal", false);
    public static final SimpleParticleType WHITE_ASH = ParticleTypes.register("white_ash", false);
    public static final SimpleParticleType SMALL_FLAME = ParticleTypes.register("small_flame", false);
    public static final SimpleParticleType SNOWFLAKE = ParticleTypes.register("snowflake", false);
    public static final SimpleParticleType DRIPPING_DRIPSTONE_LAVA = ParticleTypes.register("dripping_dripstone_lava", false);
    public static final SimpleParticleType FALLING_DRIPSTONE_LAVA = ParticleTypes.register("falling_dripstone_lava", false);
    public static final SimpleParticleType DRIPPING_DRIPSTONE_WATER = ParticleTypes.register("dripping_dripstone_water", false);
    public static final SimpleParticleType FALLING_DRIPSTONE_WATER = ParticleTypes.register("falling_dripstone_water", false);
    public static final SimpleParticleType GLOW_SQUID_INK = ParticleTypes.register("glow_squid_ink", true);
    public static final SimpleParticleType GLOW = ParticleTypes.register("glow", true);
    public static final Codec<ParticleOptions> CODEC = Registry.PARTICLE_TYPE.dispatch("type", ParticleOptions::getType, ParticleType::codec);

    private static SimpleParticleType register(String string, boolean bl) {
        return Registry.register(Registry.PARTICLE_TYPE, string, new SimpleParticleType(bl));
    }

    private static <T extends ParticleOptions> ParticleType<T> register(String string, ParticleOptions.Deserializer<T> deserializer, final Function<ParticleType<T>, Codec<T>> function) {
        return Registry.register(Registry.PARTICLE_TYPE, string, new ParticleType<T>(false, deserializer){

            @Override
            public Codec<T> codec() {
                return (Codec)function.apply(this);
            }
        });
    }
}

