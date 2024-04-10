package net.minecraft.world.level;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Explosion {
	private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
	private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
	private final boolean fire;
	private final Explosion.BlockInteraction blockInteraction;
	private final RandomSource random = RandomSource.create();
	private final Level level;
	private final double x;
	private final double y;
	private final double z;
	@Nullable
	private final Entity source;
	private final float radius;
	private final DamageSource damageSource;
	private final ExplosionDamageCalculator damageCalculator;
	private final ParticleOptions smallExplosionParticles;
	private final ParticleOptions largeExplosionParticles;
	private final Holder<SoundEvent> explosionSound;
	private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();
	private final Map<Player, Vec3> hitPlayers = Maps.<Player, Vec3>newHashMap();

	public static DamageSource getDefaultDamageSource(Level level, @Nullable Entity entity) {
		return level.damageSources().explosion(entity, getIndirectSourceEntityInternal(entity));
	}

	public Explosion(
		Level level,
		@Nullable Entity entity,
		double d,
		double e,
		double f,
		float g,
		List<BlockPos> list,
		Explosion.BlockInteraction blockInteraction,
		ParticleOptions particleOptions,
		ParticleOptions particleOptions2,
		Holder<SoundEvent> holder
	) {
		this(level, entity, getDefaultDamageSource(level, entity), null, d, e, f, g, false, blockInteraction, particleOptions, particleOptions2, holder);
		this.toBlow.addAll(list);
	}

	public Explosion(
		Level level, @Nullable Entity entity, double d, double e, double f, float g, boolean bl, Explosion.BlockInteraction blockInteraction, List<BlockPos> list
	) {
		this(level, entity, d, e, f, g, bl, blockInteraction);
		this.toBlow.addAll(list);
	}

	public Explosion(Level level, @Nullable Entity entity, double d, double e, double f, float g, boolean bl, Explosion.BlockInteraction blockInteraction) {
		this(
			level,
			entity,
			getDefaultDamageSource(level, entity),
			null,
			d,
			e,
			f,
			g,
			bl,
			blockInteraction,
			ParticleTypes.EXPLOSION,
			ParticleTypes.EXPLOSION_EMITTER,
			SoundEvents.GENERIC_EXPLODE
		);
	}

	public Explosion(
		Level level,
		@Nullable Entity entity,
		@Nullable DamageSource damageSource,
		@Nullable ExplosionDamageCalculator explosionDamageCalculator,
		double d,
		double e,
		double f,
		float g,
		boolean bl,
		Explosion.BlockInteraction blockInteraction,
		ParticleOptions particleOptions,
		ParticleOptions particleOptions2,
		Holder<SoundEvent> holder
	) {
		this.level = level;
		this.source = entity;
		this.radius = g;
		this.x = d;
		this.y = e;
		this.z = f;
		this.fire = bl;
		this.blockInteraction = blockInteraction;
		this.damageSource = damageSource == null ? level.damageSources().explosion(this) : damageSource;
		this.damageCalculator = explosionDamageCalculator == null ? this.makeDamageCalculator(entity) : explosionDamageCalculator;
		this.smallExplosionParticles = particleOptions;
		this.largeExplosionParticles = particleOptions2;
		this.explosionSound = holder;
	}

	private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
		return (ExplosionDamageCalculator)(entity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(entity));
	}

	public static float getSeenPercent(Vec3 vec3, Entity entity) {
		AABB aABB = entity.getBoundingBox();
		double d = 1.0 / ((aABB.maxX - aABB.minX) * 2.0 + 1.0);
		double e = 1.0 / ((aABB.maxY - aABB.minY) * 2.0 + 1.0);
		double f = 1.0 / ((aABB.maxZ - aABB.minZ) * 2.0 + 1.0);
		double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
		double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
		if (!(d < 0.0) && !(e < 0.0) && !(f < 0.0)) {
			int i = 0;
			int j = 0;

			for (double k = 0.0; k <= 1.0; k += d) {
				for (double l = 0.0; l <= 1.0; l += e) {
					for (double m = 0.0; m <= 1.0; m += f) {
						double n = Mth.lerp(k, aABB.minX, aABB.maxX);
						double o = Mth.lerp(l, aABB.minY, aABB.maxY);
						double p = Mth.lerp(m, aABB.minZ, aABB.maxZ);
						Vec3 vec32 = new Vec3(n + g, o, p + h);
						if (entity.level().clip(new ClipContext(vec32, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS) {
							i++;
						}

						j++;
					}
				}
			}

			return (float)i / (float)j;
		} else {
			return 0.0F;
		}
	}

	public float radius() {
		return this.radius;
	}

	public Vec3 center() {
		return new Vec3(this.x, this.y, this.z);
	}

	public void explode() {
		this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
		Set<BlockPos> set = Sets.<BlockPos>newHashSet();
		int i = 16;

		for (int j = 0; j < 16; j++) {
			for (int k = 0; k < 16; k++) {
				for (int l = 0; l < 16; l++) {
					if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
						double d = (double)((float)j / 15.0F * 2.0F - 1.0F);
						double e = (double)((float)k / 15.0F * 2.0F - 1.0F);
						double f = (double)((float)l / 15.0F * 2.0F - 1.0F);
						double g = Math.sqrt(d * d + e * e + f * f);
						d /= g;
						e /= g;
						f /= g;
						float h = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
						double m = this.x;
						double n = this.y;
						double o = this.z;

						for (float p = 0.3F; h > 0.0F; h -= 0.22500001F) {
							BlockPos blockPos = BlockPos.containing(m, n, o);
							BlockState blockState = this.level.getBlockState(blockPos);
							FluidState fluidState = this.level.getFluidState(blockPos);
							if (!this.level.isInWorldBounds(blockPos)) {
								break;
							}

							Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockPos, blockState, fluidState);
							if (optional.isPresent()) {
								h -= (optional.get() + 0.3F) * 0.3F;
							}

							if (h > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockPos, blockState, h)) {
								set.add(blockPos);
							}

							m += d * 0.3F;
							n += e * 0.3F;
							o += f * 0.3F;
						}
					}
				}
			}
		}

		this.toBlow.addAll(set);
		float q = this.radius * 2.0F;
		int k = Mth.floor(this.x - (double)q - 1.0);
		int lx = Mth.floor(this.x + (double)q + 1.0);
		int r = Mth.floor(this.y - (double)q - 1.0);
		int s = Mth.floor(this.y + (double)q + 1.0);
		int t = Mth.floor(this.z - (double)q - 1.0);
		int u = Mth.floor(this.z + (double)q + 1.0);
		List<Entity> list = this.level.getEntities(this.source, new AABB((double)k, (double)r, (double)t, (double)lx, (double)s, (double)u));
		Vec3 vec3 = new Vec3(this.x, this.y, this.z);

		for (Entity entity : list) {
			if (!entity.ignoreExplosion(this)) {
				double v = Math.sqrt(entity.distanceToSqr(vec3)) / (double)q;
				if (v <= 1.0) {
					double w = entity.getX() - this.x;
					double x = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
					double y = entity.getZ() - this.z;
					double z = Math.sqrt(w * w + x * x + y * y);
					if (z != 0.0) {
						w /= z;
						x /= z;
						y /= z;
						if (this.damageCalculator.shouldDamageEntity(this, entity)) {
							entity.hurt(this.damageSource, this.damageCalculator.getEntityDamageAmount(this, entity));
						}

						double aa = (1.0 - v) * (double)getSeenPercent(vec3, entity) * (double)this.damageCalculator.getKnockbackMultiplier(entity);
						double ab;
						if (entity instanceof LivingEntity livingEntity) {
							ab = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingEntity, aa);
						} else {
							ab = aa;
						}

						w *= ab;
						x *= ab;
						y *= ab;
						Vec3 vec32 = new Vec3(w, x, y);
						entity.setDeltaMovement(entity.getDeltaMovement().add(vec32));
						if (entity instanceof Player) {
							Player player = (Player)entity;
							if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
								this.hitPlayers.put(player, vec32);
							}
						}

						entity.onExplosionHit(this.source);
					}
				}
			}
		}
	}

	public void finalizeExplosion(boolean bl) {
		if (this.level.isClientSide) {
			this.level
				.playLocalSound(
					this.x,
					this.y,
					this.z,
					this.explosionSound.value(),
					SoundSource.BLOCKS,
					4.0F,
					(1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F,
					false
				);
		}

		boolean bl2 = this.interactsWithBlocks();
		if (bl) {
			ParticleOptions particleOptions;
			if (!(this.radius < 2.0F) && bl2) {
				particleOptions = this.largeExplosionParticles;
			} else {
				particleOptions = this.smallExplosionParticles;
			}

			this.level.addParticle(particleOptions, this.x, this.y, this.z, 1.0, 0.0, 0.0);
		}

		if (bl2) {
			this.level.getProfiler().push("explosion_blocks");
			List<Pair<ItemStack, BlockPos>> list = new ArrayList();
			Util.shuffle(this.toBlow, this.level.random);

			for (BlockPos blockPos : this.toBlow) {
				this.level.getBlockState(blockPos).onExplosionHit(this.level, blockPos, this, (itemStack, blockPosx) -> addOrAppendStack(list, itemStack, blockPosx));
			}

			for (Pair<ItemStack, BlockPos> pair : list) {
				Block.popResource(this.level, pair.getSecond(), pair.getFirst());
			}

			this.level.getProfiler().pop();
		}

		if (this.fire) {
			for (BlockPos blockPos2 : this.toBlow) {
				if (this.random.nextInt(3) == 0
					&& this.level.getBlockState(blockPos2).isAir()
					&& this.level.getBlockState(blockPos2.below()).isSolidRender(this.level, blockPos2.below())) {
					this.level.setBlockAndUpdate(blockPos2, BaseFireBlock.getState(this.level, blockPos2));
				}
			}
		}
	}

	private static void addOrAppendStack(List<Pair<ItemStack, BlockPos>> list, ItemStack itemStack, BlockPos blockPos) {
		for (int i = 0; i < list.size(); i++) {
			Pair<ItemStack, BlockPos> pair = (Pair<ItemStack, BlockPos>)list.get(i);
			ItemStack itemStack2 = pair.getFirst();
			if (ItemEntity.areMergable(itemStack2, itemStack)) {
				list.set(i, Pair.of(ItemEntity.merge(itemStack2, itemStack, 16), pair.getSecond()));
				if (itemStack.isEmpty()) {
					return;
				}
			}
		}

		list.add(Pair.of(itemStack, blockPos));
	}

	public boolean interactsWithBlocks() {
		return this.blockInteraction != Explosion.BlockInteraction.KEEP;
	}

	public Map<Player, Vec3> getHitPlayers() {
		return this.hitPlayers;
	}

	@Nullable
	private static LivingEntity getIndirectSourceEntityInternal(@Nullable Entity entity) {
		if (entity == null) {
			return null;
		} else if (entity instanceof PrimedTnt primedTnt) {
			return primedTnt.getOwner();
		} else if (entity instanceof LivingEntity) {
			return (LivingEntity)entity;
		} else {
			if (entity instanceof Projectile projectile) {
				Entity entity2 = projectile.getOwner();
				if (entity2 instanceof LivingEntity) {
					return (LivingEntity)entity2;
				}
			}

			return null;
		}
	}

	@Nullable
	public LivingEntity getIndirectSourceEntity() {
		return getIndirectSourceEntityInternal(this.source);
	}

	@Nullable
	public Entity getDirectSourceEntity() {
		return this.source;
	}

	public void clearToBlow() {
		this.toBlow.clear();
	}

	public List<BlockPos> getToBlow() {
		return this.toBlow;
	}

	public Explosion.BlockInteraction getBlockInteraction() {
		return this.blockInteraction;
	}

	public ParticleOptions getSmallExplosionParticles() {
		return this.smallExplosionParticles;
	}

	public ParticleOptions getLargeExplosionParticles() {
		return this.largeExplosionParticles;
	}

	public Holder<SoundEvent> getExplosionSound() {
		return this.explosionSound;
	}

	public static enum BlockInteraction {
		KEEP,
		DESTROY,
		DESTROY_WITH_DECAY,
		TRIGGER_BLOCK;
	}
}
