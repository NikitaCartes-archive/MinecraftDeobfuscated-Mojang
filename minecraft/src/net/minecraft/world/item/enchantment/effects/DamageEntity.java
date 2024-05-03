package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record DamageEntity(LevelBasedValue minDamage, LevelBasedValue maxDamage, Holder<DamageType> damageType) implements EnchantmentEntityEffect {
	public static final MapCodec<DamageEntity> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					LevelBasedValue.CODEC.fieldOf("min_damage").forGetter(DamageEntity::minDamage),
					LevelBasedValue.CODEC.fieldOf("max_damage").forGetter(DamageEntity::maxDamage),
					DamageType.CODEC.fieldOf("damage_type").forGetter(DamageEntity::damageType)
				)
				.apply(instance, DamageEntity::new)
	);

	@Override
	public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
		float f = Mth.randomBetween(entity.getRandom(), this.minDamage.calculate(i), this.maxDamage.calculate(i));
		entity.hurt(new DamageSource(this.damageType, enchantedItemInUse.owner()), f);
	}

	@Override
	public MapCodec<DamageEntity> codec() {
		return CODEC;
	}
}
