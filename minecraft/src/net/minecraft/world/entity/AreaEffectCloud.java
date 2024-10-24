package net.minecraft.world.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;

public class AreaEffectCloud extends Entity implements TraceableEntity {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int TIME_BETWEEN_APPLICATIONS = 5;
	private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.PARTICLE);
	private static final float MAX_RADIUS = 32.0F;
	private static final float MINIMAL_RADIUS = 0.5F;
	private static final float DEFAULT_RADIUS = 3.0F;
	public static final float DEFAULT_WIDTH = 6.0F;
	public static final float HEIGHT = 0.5F;
	private PotionContents potionContents = PotionContents.EMPTY;
	private final Map<Entity, Integer> victims = Maps.<Entity, Integer>newHashMap();
	private int duration = 600;
	private int waitTime = 20;
	private int reapplicationDelay = 20;
	private int durationOnUse;
	private float radiusOnUse;
	private float radiusPerTick;
	@Nullable
	private LivingEntity owner;
	@Nullable
	private UUID ownerUUID;

	public AreaEffectCloud(EntityType<? extends AreaEffectCloud> entityType, Level level) {
		super(entityType, level);
		this.noPhysics = true;
	}

	public AreaEffectCloud(Level level, double d, double e, double f) {
		this(EntityType.AREA_EFFECT_CLOUD, level);
		this.setPos(d, e, f);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_RADIUS, 3.0F);
		builder.define(DATA_WAITING, false);
		builder.define(DATA_PARTICLE, ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, -1));
	}

	public void setRadius(float f) {
		if (!this.level().isClientSide) {
			this.getEntityData().set(DATA_RADIUS, Mth.clamp(f, 0.0F, 32.0F));
		}
	}

	@Override
	public void refreshDimensions() {
		double d = this.getX();
		double e = this.getY();
		double f = this.getZ();
		super.refreshDimensions();
		this.setPos(d, e, f);
	}

	public float getRadius() {
		return this.getEntityData().get(DATA_RADIUS);
	}

	public void setPotionContents(PotionContents potionContents) {
		this.potionContents = potionContents;
		this.updateColor();
	}

	private void updateColor() {
		ParticleOptions particleOptions = this.entityData.get(DATA_PARTICLE);
		if (particleOptions instanceof ColorParticleOption colorParticleOption) {
			int i = this.potionContents.equals(PotionContents.EMPTY) ? 0 : this.potionContents.getColor();
			this.entityData.set(DATA_PARTICLE, ColorParticleOption.create(colorParticleOption.getType(), ARGB.opaque(i)));
		}
	}

	public void addEffect(MobEffectInstance mobEffectInstance) {
		this.setPotionContents(this.potionContents.withEffectAdded(mobEffectInstance));
	}

	public ParticleOptions getParticle() {
		return this.getEntityData().get(DATA_PARTICLE);
	}

	public void setParticle(ParticleOptions particleOptions) {
		this.getEntityData().set(DATA_PARTICLE, particleOptions);
	}

	protected void setWaiting(boolean bl) {
		this.getEntityData().set(DATA_WAITING, bl);
	}

	public boolean isWaiting() {
		return this.getEntityData().get(DATA_WAITING);
	}

	public int getDuration() {
		return this.duration;
	}

	public void setDuration(int i) {
		this.duration = i;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level() instanceof ServerLevel serverLevel) {
			this.serverTick(serverLevel);
		} else {
			this.clientTick();
		}
	}

	private void clientTick() {
		boolean bl = this.isWaiting();
		float f = this.getRadius();
		if (!bl || !this.random.nextBoolean()) {
			ParticleOptions particleOptions = this.getParticle();
			int i;
			float g;
			if (bl) {
				i = 2;
				g = 0.2F;
			} else {
				i = Mth.ceil((float) Math.PI * f * f);
				g = f;
			}

			for (int j = 0; j < i; j++) {
				float h = this.random.nextFloat() * (float) (Math.PI * 2);
				float k = Mth.sqrt(this.random.nextFloat()) * g;
				double d = this.getX() + (double)(Mth.cos(h) * k);
				double e = this.getY();
				double l = this.getZ() + (double)(Mth.sin(h) * k);
				if (particleOptions.getType() == ParticleTypes.ENTITY_EFFECT) {
					if (bl && this.random.nextBoolean()) {
						this.level().addAlwaysVisibleParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, -1), d, e, l, 0.0, 0.0, 0.0);
					} else {
						this.level().addAlwaysVisibleParticle(particleOptions, d, e, l, 0.0, 0.0, 0.0);
					}
				} else if (bl) {
					this.level().addAlwaysVisibleParticle(particleOptions, d, e, l, 0.0, 0.0, 0.0);
				} else {
					this.level().addAlwaysVisibleParticle(particleOptions, d, e, l, (0.5 - this.random.nextDouble()) * 0.15, 0.01F, (0.5 - this.random.nextDouble()) * 0.15);
				}
			}
		}
	}

	private void serverTick(ServerLevel serverLevel) {
		if (this.tickCount >= this.waitTime + this.duration) {
			this.discard();
		} else {
			boolean bl = this.isWaiting();
			boolean bl2 = this.tickCount < this.waitTime;
			if (bl != bl2) {
				this.setWaiting(bl2);
			}

			if (!bl2) {
				float f = this.getRadius();
				if (this.radiusPerTick != 0.0F) {
					f += this.radiusPerTick;
					if (f < 0.5F) {
						this.discard();
						return;
					}

					this.setRadius(f);
				}

				if (this.tickCount % 5 == 0) {
					this.victims.entrySet().removeIf(entry -> this.tickCount >= (Integer)entry.getValue());
					if (!this.potionContents.hasEffects()) {
						this.victims.clear();
					} else {
						List<MobEffectInstance> list = Lists.<MobEffectInstance>newArrayList();
						if (this.potionContents.potion().isPresent()) {
							for (MobEffectInstance mobEffectInstance : ((Potion)((Holder)this.potionContents.potion().get()).value()).getEffects()) {
								list.add(
									new MobEffectInstance(
										mobEffectInstance.getEffect(),
										mobEffectInstance.mapDuration(i -> i / 4),
										mobEffectInstance.getAmplifier(),
										mobEffectInstance.isAmbient(),
										mobEffectInstance.isVisible()
									)
								);
							}
						}

						list.addAll(this.potionContents.customEffects());
						List<LivingEntity> list2 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
						if (!list2.isEmpty()) {
							for (LivingEntity livingEntity : list2) {
								if (!this.victims.containsKey(livingEntity) && livingEntity.isAffectedByPotions() && !list.stream().noneMatch(livingEntity::canBeAffected)) {
									double d = livingEntity.getX() - this.getX();
									double e = livingEntity.getZ() - this.getZ();
									double g = d * d + e * e;
									if (g <= (double)(f * f)) {
										this.victims.put(livingEntity, this.tickCount + this.reapplicationDelay);

										for (MobEffectInstance mobEffectInstance2 : list) {
											if (mobEffectInstance2.getEffect().value().isInstantenous()) {
												mobEffectInstance2.getEffect()
													.value()
													.applyInstantenousEffect(serverLevel, this, this.getOwner(), livingEntity, mobEffectInstance2.getAmplifier(), 0.5);
											} else {
												livingEntity.addEffect(new MobEffectInstance(mobEffectInstance2), this);
											}
										}

										if (this.radiusOnUse != 0.0F) {
											f += this.radiusOnUse;
											if (f < 0.5F) {
												this.discard();
												return;
											}

											this.setRadius(f);
										}

										if (this.durationOnUse != 0) {
											this.duration = this.duration + this.durationOnUse;
											if (this.duration <= 0) {
												this.discard();
												return;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public float getRadiusOnUse() {
		return this.radiusOnUse;
	}

	public void setRadiusOnUse(float f) {
		this.radiusOnUse = f;
	}

	public float getRadiusPerTick() {
		return this.radiusPerTick;
	}

	public void setRadiusPerTick(float f) {
		this.radiusPerTick = f;
	}

	public int getDurationOnUse() {
		return this.durationOnUse;
	}

	public void setDurationOnUse(int i) {
		this.durationOnUse = i;
	}

	public int getWaitTime() {
		return this.waitTime;
	}

	public void setWaitTime(int i) {
		this.waitTime = i;
	}

	public void setOwner(@Nullable LivingEntity livingEntity) {
		this.owner = livingEntity;
		this.ownerUUID = livingEntity == null ? null : livingEntity.getUUID();
	}

	@Nullable
	public LivingEntity getOwner() {
		if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel) {
			Entity entity = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
			if (entity instanceof LivingEntity) {
				this.owner = (LivingEntity)entity;
			}
		}

		return this.owner;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		this.tickCount = compoundTag.getInt("Age");
		this.duration = compoundTag.getInt("Duration");
		this.waitTime = compoundTag.getInt("WaitTime");
		this.reapplicationDelay = compoundTag.getInt("ReapplicationDelay");
		this.durationOnUse = compoundTag.getInt("DurationOnUse");
		this.radiusOnUse = compoundTag.getFloat("RadiusOnUse");
		this.radiusPerTick = compoundTag.getFloat("RadiusPerTick");
		this.setRadius(compoundTag.getFloat("Radius"));
		if (compoundTag.hasUUID("Owner")) {
			this.ownerUUID = compoundTag.getUUID("Owner");
		}

		RegistryOps<Tag> registryOps = this.registryAccess().createSerializationContext(NbtOps.INSTANCE);
		if (compoundTag.contains("Particle", 10)) {
			ParticleTypes.CODEC
				.parse(registryOps, compoundTag.get("Particle"))
				.resultOrPartial(string -> LOGGER.warn("Failed to parse area effect cloud particle options: '{}'", string))
				.ifPresent(this::setParticle);
		}

		if (compoundTag.contains("potion_contents")) {
			PotionContents.CODEC
				.parse(registryOps, compoundTag.get("potion_contents"))
				.resultOrPartial(string -> LOGGER.warn("Failed to parse area effect cloud potions: '{}'", string))
				.ifPresent(this::setPotionContents);
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putInt("Age", this.tickCount);
		compoundTag.putInt("Duration", this.duration);
		compoundTag.putInt("WaitTime", this.waitTime);
		compoundTag.putInt("ReapplicationDelay", this.reapplicationDelay);
		compoundTag.putInt("DurationOnUse", this.durationOnUse);
		compoundTag.putFloat("RadiusOnUse", this.radiusOnUse);
		compoundTag.putFloat("RadiusPerTick", this.radiusPerTick);
		compoundTag.putFloat("Radius", this.getRadius());
		RegistryOps<Tag> registryOps = this.registryAccess().createSerializationContext(NbtOps.INSTANCE);
		compoundTag.put("Particle", ParticleTypes.CODEC.encodeStart(registryOps, this.getParticle()).getOrThrow());
		if (this.ownerUUID != null) {
			compoundTag.putUUID("Owner", this.ownerUUID);
		}

		if (!this.potionContents.equals(PotionContents.EMPTY)) {
			Tag tag = PotionContents.CODEC.encodeStart(registryOps, this.potionContents).getOrThrow();
			compoundTag.put("potion_contents", tag);
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_RADIUS.equals(entityDataAccessor)) {
			this.refreshDimensions();
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return EntityDimensions.scalable(this.getRadius() * 2.0F, 0.5F);
	}

	@Override
	public final boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
		return false;
	}
}
