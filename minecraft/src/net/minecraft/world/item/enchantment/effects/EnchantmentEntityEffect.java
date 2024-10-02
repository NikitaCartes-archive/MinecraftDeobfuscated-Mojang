package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

public interface EnchantmentEntityEffect extends EnchantmentLocationBasedEffect {
	Codec<EnchantmentEntityEffect> CODEC = BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE
		.byNameCodec()
		.dispatch(EnchantmentEntityEffect::codec, Function.identity());

	static MapCodec<? extends EnchantmentEntityEffect> bootstrap(Registry<MapCodec<? extends EnchantmentEntityEffect>> registry) {
		Registry.register(registry, "all_of", AllOf.EntityEffects.CODEC);
		Registry.register(registry, "apply_mob_effect", ApplyMobEffect.CODEC);
		Registry.register(registry, "change_item_damage", ChangeItemDamage.CODEC);
		Registry.register(registry, "damage_entity", DamageEntity.CODEC);
		Registry.register(registry, "explode", ExplodeEffect.CODEC);
		Registry.register(registry, "ignite", Ignite.CODEC);
		Registry.register(registry, "play_sound", PlaySoundEffect.CODEC);
		Registry.register(registry, "replace_block", ReplaceBlock.CODEC);
		Registry.register(registry, "replace_disk", ReplaceDisk.CODEC);
		Registry.register(registry, "run_function", RunFunction.CODEC);
		Registry.register(registry, "set_block_properties", SetBlockProperties.CODEC);
		Registry.register(registry, "spawn_particles", SpawnParticlesEffect.CODEC);
		return Registry.register(registry, "summon_entity", SummonEntityEffect.CODEC);
	}

	void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3);

	@Override
	default void onChangedBlock(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, boolean bl) {
		this.apply(serverLevel, i, enchantedItemInUse, entity, vec3);
	}

	@Override
	MapCodec<? extends EnchantmentEntityEffect> codec();
}
