package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record Ignite(LevelBasedValue duration) implements EnchantmentEntityEffect {
	public static final MapCodec<Ignite> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(LevelBasedValue.CODEC.fieldOf("duration").forGetter(ignite -> ignite.duration)).apply(instance, Ignite::new)
	);

	@Override
	public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
		entity.igniteForSeconds(this.duration.calculate(i));
	}

	@Override
	public MapCodec<Ignite> codec() {
		return CODEC;
	}
}
