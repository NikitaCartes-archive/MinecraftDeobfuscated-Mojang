package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public record ExplodeEffect(
	boolean attributeToUser,
	Optional<Holder<DamageType>> damageType,
	Optional<LevelBasedValue> knockbackMultiplier,
	Optional<HolderSet<Block>> immuneBlocks,
	Vec3 offset,
	LevelBasedValue radius,
	boolean createFire,
	Level.ExplosionInteraction blockInteraction,
	ParticleOptions smallParticle,
	ParticleOptions largeParticle,
	Holder<SoundEvent> sound
) implements EnchantmentEntityEffect {
	public static final MapCodec<ExplodeEffect> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.BOOL.optionalFieldOf("attribute_to_user", Boolean.valueOf(false)).forGetter(ExplodeEffect::attributeToUser),
					DamageType.CODEC.optionalFieldOf("damage_type").forGetter(ExplodeEffect::damageType),
					LevelBasedValue.CODEC.optionalFieldOf("knockback_multiplier").forGetter(ExplodeEffect::knockbackMultiplier),
					RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("immune_blocks").forGetter(ExplodeEffect::immuneBlocks),
					Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(ExplodeEffect::offset),
					LevelBasedValue.CODEC.fieldOf("radius").forGetter(ExplodeEffect::radius),
					Codec.BOOL.optionalFieldOf("create_fire", Boolean.valueOf(false)).forGetter(ExplodeEffect::createFire),
					Level.ExplosionInteraction.CODEC.fieldOf("block_interaction").forGetter(ExplodeEffect::blockInteraction),
					ParticleTypes.CODEC.fieldOf("small_particle").forGetter(ExplodeEffect::smallParticle),
					ParticleTypes.CODEC.fieldOf("large_particle").forGetter(ExplodeEffect::largeParticle),
					SoundEvent.CODEC.fieldOf("sound").forGetter(ExplodeEffect::sound)
				)
				.apply(instance, ExplodeEffect::new)
	);

	@Override
	public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
		Vec3 vec32 = vec3.add(this.offset);
		serverLevel.explode(
			this.attributeToUser ? entity : null,
			this.getDamageSource(entity, vec32),
			new SimpleExplosionDamageCalculator(
				this.blockInteraction != Level.ExplosionInteraction.NONE,
				this.damageType.isPresent(),
				this.knockbackMultiplier.map(levelBasedValue -> levelBasedValue.calculate(i)),
				this.immuneBlocks
			),
			vec32.x(),
			vec32.y(),
			vec32.z(),
			Math.max(this.radius.calculate(i), 0.0F),
			this.createFire,
			this.blockInteraction,
			this.smallParticle,
			this.largeParticle,
			this.sound
		);
	}

	@Nullable
	private DamageSource getDamageSource(Entity entity, Vec3 vec3) {
		if (this.damageType.isEmpty()) {
			return null;
		} else {
			return this.attributeToUser
				? new DamageSource((Holder<DamageType>)this.damageType.get(), entity)
				: new DamageSource((Holder<DamageType>)this.damageType.get(), vec3);
		}
	}

	@Override
	public MapCodec<ExplodeEffect> codec() {
		return CODEC;
	}
}
