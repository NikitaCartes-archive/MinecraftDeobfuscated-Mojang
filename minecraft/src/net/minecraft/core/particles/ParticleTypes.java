package net.minecraft.core.particles;

import java.util.Random;
import java.util.function.BiFunction;
import net.minecraft.core.Registry;

public class ParticleTypes {
	public static final SimpleParticleType AMBIENT_ENTITY_EFFECT = register("ambient_entity_effect", false);
	public static final SimpleParticleType ANGRY_VILLAGER = register("angry_villager", false);
	public static final SimpleParticleType BARRIER = register("barrier", false);
	public static final ParticleType<BlockParticleOption> BLOCK = register("block", BlockParticleOption.DESERIALIZER, BlockParticleOption.RANDOM_PROVIDER);
	public static final SimpleParticleType BUBBLE = register("bubble", false);
	public static final SimpleParticleType CLOUD = register("cloud", false);
	public static final SimpleParticleType CRIT = register("crit", false);
	public static final SimpleParticleType DAMAGE_INDICATOR = register("damage_indicator", true);
	public static final SimpleParticleType DRAGON_BREATH = register("dragon_breath", false);
	public static final SimpleParticleType DRIPPING_LAVA = register("dripping_lava", false);
	public static final SimpleParticleType FALLING_LAVA = register("falling_lava", false);
	public static final SimpleParticleType LANDING_LAVA = register("landing_lava", false);
	public static final SimpleParticleType DRIPPING_WATER = register("dripping_water", false);
	public static final SimpleParticleType FALLING_WATER = register("falling_water", false);
	public static final ParticleType<DustParticleOptions> DUST = register("dust", DustParticleOptions.DESERIALIZER, DustParticleOptions.RANDOM_PROVIDER);
	public static final SimpleParticleType EFFECT = register("effect", false);
	public static final SimpleParticleType ELDER_GUARDIAN = register("elder_guardian", true);
	public static final SimpleParticleType ENCHANTED_HIT = register("enchanted_hit", false);
	public static final SimpleParticleType ENCHANT = register("enchant", false);
	public static final SimpleParticleType END_ROD = register("end_rod", false);
	public static final SimpleParticleType ENTITY_EFFECT = register("entity_effect", false);
	public static final SimpleParticleType EXPLOSION_EMITTER = register("explosion_emitter", true);
	public static final SimpleParticleType EXPLOSION = register("explosion", true);
	public static final ParticleType<BlockParticleOption> FALLING_DUST = register(
		"falling_dust", BlockParticleOption.DESERIALIZER, BlockParticleOption.RANDOM_PROVIDER
	);
	public static final SimpleParticleType FIREWORK = register("firework", false);
	public static final SimpleParticleType FISHING = register("fishing", false);
	public static final SimpleParticleType FLAME = register("flame", false);
	public static final SimpleParticleType SOUL_FIRE_FLAME = register("soul_fire_flame", false);
	public static final SimpleParticleType SOUL = register("soul", false);
	public static final SimpleParticleType FLASH = register("flash", false);
	public static final SimpleParticleType HAPPY_VILLAGER = register("happy_villager", false);
	public static final SimpleParticleType COMPOSTER = register("composter", false);
	public static final SimpleParticleType HEART = register("heart", false);
	public static final SimpleParticleType INSTANT_EFFECT = register("instant_effect", false);
	public static final ParticleType<ItemParticleOption> ITEM = register("item", ItemParticleOption.DESERIALIZER, ItemParticleOption.RANDOM_PROVIDER);
	public static final SimpleParticleType ITEM_SLIME = register("item_slime", false);
	public static final SimpleParticleType ITEM_SNOWBALL = register("item_snowball", false);
	public static final SimpleParticleType LARGE_SMOKE = register("large_smoke", false);
	public static final SimpleParticleType LAVA = register("lava", false);
	public static final SimpleParticleType MYCELIUM = register("mycelium", false);
	public static final SimpleParticleType NOTE = register("note", false);
	public static final SimpleParticleType POOF = register("poof", true);
	public static final SimpleParticleType PORTAL = register("portal", false);
	public static final SimpleParticleType RAIN = register("rain", false);
	public static final SimpleParticleType SMOKE = register("smoke", false);
	public static final SimpleParticleType SNEEZE = register("sneeze", false);
	public static final SimpleParticleType SPIT = register("spit", true);
	public static final SimpleParticleType SQUID_INK = register("squid_ink", true);
	public static final SimpleParticleType SWEEP_ATTACK = register("sweep_attack", true);
	public static final SimpleParticleType TOTEM_OF_UNDYING = register("totem_of_undying", false);
	public static final SimpleParticleType UNDERWATER = register("underwater", false);
	public static final SimpleParticleType SPLASH = register("splash", false);
	public static final SimpleParticleType WITCH = register("witch", false);
	public static final SimpleParticleType BUBBLE_POP = register("bubble_pop", false);
	public static final SimpleParticleType CURRENT_DOWN = register("current_down", false);
	public static final SimpleParticleType BUBBLE_COLUMN_UP = register("bubble_column_up", false);
	public static final SimpleParticleType NAUTILUS = register("nautilus", false);
	public static final SimpleParticleType DOLPHIN = register("dolphin", false);
	public static final SimpleParticleType CAMPFIRE_COSY_SMOKE = register("campfire_cosy_smoke", true);
	public static final SimpleParticleType CAMPFIRE_SIGNAL_SMOKE = register("campfire_signal_smoke", true);
	public static final SimpleParticleType DRIPPING_HONEY = register("dripping_honey", false);
	public static final SimpleParticleType FALLING_HONEY = register("falling_honey", false);
	public static final SimpleParticleType LANDING_HONEY = register("landing_honey", false);
	public static final SimpleParticleType FALLING_NECTAR = register("falling_nectar", false);
	public static final SimpleParticleType ASH = register("ash", false);
	public static final SimpleParticleType CRIMSON_SPORE = register("crimson_spore", false);
	public static final SimpleParticleType WARPED_SPORE = register("warped_spore", false);
	public static final SimpleParticleType DRIPPING_OBSIDIAN_TEAR = register("dripping_obsidian_tear", false);
	public static final SimpleParticleType FALLING_OBSIDIAN_TEAR = register("falling_obsidian_tear", false);
	public static final SimpleParticleType LANDING_OBSIDIAN_TEAR = register("landing_obsidian_tear", false);
	public static final SimpleParticleType REVERSE_PORTAL = register("reverse_portal", false);

	private static SimpleParticleType register(String string, boolean bl) {
		return Registry.register(Registry.PARTICLE_TYPE, string, new SimpleParticleType(bl));
	}

	private static <T extends ParticleOptions> ParticleType<T> register(
		String string, ParticleOptions.Deserializer<T> deserializer, BiFunction<Random, ParticleType<T>, T> biFunction
	) {
		return Registry.register(Registry.PARTICLE_TYPE, string, new ParticleType<>(false, deserializer, biFunction));
	}
}
