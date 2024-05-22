package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.enchantment.effects.DamageImmunity;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public interface EnchantmentEffectComponents {
	Codec<DataComponentType<?>> COMPONENT_CODEC = Codec.lazyInitialized(() -> BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.byNameCodec());
	Codec<DataComponentMap> CODEC = DataComponentMap.makeCodec(COMPONENT_CODEC);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> DAMAGE_PROTECTION = register(
		"damage_protection", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	DataComponentType<List<ConditionalEffect<DamageImmunity>>> DAMAGE_IMMUNITY = register(
		"damage_immunity", builder -> builder.persistent(ConditionalEffect.codec(DamageImmunity.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> DAMAGE = register(
		"damage", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> SMASH_DAMAGE_PER_FALLEN_BLOCK = register(
		"smash_damage_per_fallen_block",
		builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> KNOCKBACK = register(
		"knockback", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> ARMOR_EFFECTIVENESS = register(
		"armor_effectiveness", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	DataComponentType<List<TargetedConditionalEffect<EnchantmentEntityEffect>>> POST_ATTACK = register(
		"post_attack", builder -> builder.persistent(TargetedConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> HIT_BLOCK = register(
		"hit_block", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParamSets.HIT_BLOCK).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> ITEM_DAMAGE = register(
		"item_damage", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
	);
	DataComponentType<List<EnchantmentAttributeEffect>> ATTRIBUTES = register(
		"attributes", builder -> builder.persistent(EnchantmentAttributeEffect.CODEC.codec().listOf())
	);
	DataComponentType<List<TargetedConditionalEffect<EnchantmentValueEffect>>> EQUIPMENT_DROPS = register(
		"equipment_drops",
		builder -> builder.persistent(TargetedConditionalEffect.equipmentDropsCodec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentLocationBasedEffect>>> LOCATION_CHANGED = register(
		"location_changed",
		builder -> builder.persistent(ConditionalEffect.codec(EnchantmentLocationBasedEffect.CODEC, LootContextParamSets.ENCHANTED_LOCATION).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> TICK = register(
		"tick", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> AMMO_USE = register(
		"ammo_use", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> PROJECTILE_PIERCING = register(
		"projectile_piercing", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> PROJECTILE_SPAWNED = register(
		"projectile_spawned", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> PROJECTILE_SPREAD = register(
		"projectile_spread", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> PROJECTILE_COUNT = register(
		"projectile_count", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> TRIDENT_RETURN_ACCELERATION = register(
		"trident_return_acceleration",
		builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> FISHING_TIME_REDUCTION = register(
		"fishing_time_reduction",
		builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> FISHING_LUCK_BONUS = register(
		"fishing_luck_bonus", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> BLOCK_EXPERIENCE = register(
		"block_experience", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> MOB_EXPERIENCE = register(
		"mob_experience", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf())
	);
	DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> REPAIR_WITH_XP = register(
		"repair_with_xp", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
	);
	DataComponentType<EnchantmentValueEffect> CROSSBOW_CHARGE_TIME = register("crossbow_charge_time", builder -> builder.persistent(EnchantmentValueEffect.CODEC));
	DataComponentType<List<CrossbowItem.ChargingSounds>> CROSSBOW_CHARGING_SOUNDS = register(
		"crossbow_charging_sounds", builder -> builder.persistent(CrossbowItem.ChargingSounds.CODEC.listOf())
	);
	DataComponentType<List<Holder<SoundEvent>>> TRIDENT_SOUND = register("trident_sound", builder -> builder.persistent(SoundEvent.CODEC.listOf()));
	DataComponentType<Unit> PREVENT_EQUIPMENT_DROP = register("prevent_equipment_drop", builder -> builder.persistent(Unit.CODEC));
	DataComponentType<Unit> PREVENT_ARMOR_CHANGE = register("prevent_armor_change", builder -> builder.persistent(Unit.CODEC));
	DataComponentType<EnchantmentValueEffect> TRIDENT_SPIN_ATTACK_STRENGTH = register(
		"trident_spin_attack_strength", builder -> builder.persistent(EnchantmentValueEffect.CODEC)
	);

	static DataComponentType<?> bootstrap(Registry<DataComponentType<?>> registry) {
		return DAMAGE_PROTECTION;
	}

	private static <T> DataComponentType<T> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		return Registry.register(
			BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, string, ((DataComponentType.Builder)unaryOperator.apply(DataComponentType.builder())).build()
		);
	}
}
