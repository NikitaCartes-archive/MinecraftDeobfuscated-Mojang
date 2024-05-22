package net.minecraft.world.entity.projectile.windcharge;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.phys.Vec3;

public class WindCharge extends AbstractWindCharge {
	private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
		true, false, Optional.of(1.1F), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
	);
	private static final float RADIUS = 1.2F;
	private int noDeflectTicks = 5;

	public WindCharge(EntityType<? extends AbstractWindCharge> entityType, Level level) {
		super(entityType, level);
	}

	public WindCharge(Player player, Level level, double d, double e, double f) {
		super(EntityType.WIND_CHARGE, level, player, d, e, f);
	}

	public WindCharge(Level level, double d, double e, double f, Vec3 vec3) {
		super(EntityType.WIND_CHARGE, d, e, f, vec3, level);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.noDeflectTicks > 0) {
			this.noDeflectTicks--;
		}
	}

	@Override
	public boolean deflect(ProjectileDeflection projectileDeflection, @Nullable Entity entity, @Nullable Entity entity2, boolean bl) {
		return this.noDeflectTicks > 0 ? false : super.deflect(projectileDeflection, entity, entity2, bl);
	}

	@Override
	protected void explode() {
		this.level()
			.explode(
				this,
				null,
				EXPLOSION_DAMAGE_CALCULATOR,
				this.getX(),
				this.getY(),
				this.getZ(),
				1.2F,
				false,
				Level.ExplosionInteraction.TRIGGER,
				ParticleTypes.GUST_EMITTER_SMALL,
				ParticleTypes.GUST_EMITTER_LARGE,
				SoundEvents.WIND_CHARGE_BURST
			);
	}
}
