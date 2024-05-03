package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record ApplyMobEffect(
	HolderSet<MobEffect> toApply, LevelBasedValue minDuration, LevelBasedValue maxDuration, LevelBasedValue minAmplifier, LevelBasedValue maxAmplifier
) implements EnchantmentEntityEffect {
	public static final MapCodec<ApplyMobEffect> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("to_apply").forGetter(ApplyMobEffect::toApply),
					LevelBasedValue.CODEC.fieldOf("min_duration").forGetter(ApplyMobEffect::minDuration),
					LevelBasedValue.CODEC.fieldOf("max_duration").forGetter(ApplyMobEffect::maxDuration),
					LevelBasedValue.CODEC.fieldOf("min_amplifier").forGetter(ApplyMobEffect::minAmplifier),
					LevelBasedValue.CODEC.fieldOf("max_amplifier").forGetter(ApplyMobEffect::maxAmplifier)
				)
				.apply(instance, ApplyMobEffect::new)
	);

	@Override
	public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
		if (entity instanceof LivingEntity livingEntity) {
			RandomSource randomSource = livingEntity.getRandom();
			Optional<Holder<MobEffect>> optional = this.toApply.getRandomElement(randomSource);
			if (optional.isPresent()) {
				int j = Math.round(Mth.randomBetween(randomSource, this.minDuration.calculate(i), this.maxDuration.calculate(i)) * 20.0F);
				int k = Math.min(0, Math.round(Mth.randomBetween(randomSource, this.minAmplifier.calculate(i), this.maxAmplifier.calculate(i))));
				livingEntity.addEffect(new MobEffectInstance((Holder<MobEffect>)optional.get(), j, k));
			}
		}
	}

	@Override
	public MapCodec<ApplyMobEffect> codec() {
		return CODEC;
	}
}
