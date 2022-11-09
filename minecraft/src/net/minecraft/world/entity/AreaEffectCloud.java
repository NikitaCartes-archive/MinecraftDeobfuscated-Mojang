package net.minecraft.world.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;

public class AreaEffectCloud extends Entity {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int TIME_BETWEEN_APPLICATIONS = 5;
	private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.PARTICLE);
	private static final float MAX_RADIUS = 32.0F;
	private static final float MINIMAL_RADIUS = 0.5F;
	private static final float DEFAULT_RADIUS = 3.0F;
	public static final float DEFAULT_WIDTH = 6.0F;
	public static final float HEIGHT = 0.5F;
	private Potion potion = Potions.EMPTY;
	private final List<MobEffectInstance> effects = Lists.<MobEffectInstance>newArrayList();
	private final Map<Entity, Integer> victims = Maps.<Entity, Integer>newHashMap();
	private int duration = 600;
	private int waitTime = 20;
	private int reapplicationDelay = 20;
	private boolean fixedColor;
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
	protected void defineSynchedData() {
		this.getEntityData().define(DATA_COLOR, 0);
		this.getEntityData().define(DATA_RADIUS, 3.0F);
		this.getEntityData().define(DATA_WAITING, false);
		this.getEntityData().define(DATA_PARTICLE, ParticleTypes.ENTITY_EFFECT);
	}

	public void setRadius(float f) {
		if (!this.level.isClientSide) {
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

	public void setPotion(Potion potion) {
		this.potion = potion;
		if (!this.fixedColor) {
			this.updateColor();
		}
	}

	private void updateColor() {
		if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
			this.getEntityData().set(DATA_COLOR, 0);
		} else {
			this.getEntityData().set(DATA_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
		}
	}

	public void addEffect(MobEffectInstance mobEffectInstance) {
		this.effects.add(mobEffectInstance);
		if (!this.fixedColor) {
			this.updateColor();
		}
	}

	public int getColor() {
		return this.getEntityData().get(DATA_COLOR);
	}

	public void setFixedColor(int i) {
		this.fixedColor = true;
		this.getEntityData().set(DATA_COLOR, i);
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
		boolean bl = this.isWaiting();
		float f = this.getRadius();
		if (this.level.isClientSide) {
			if (bl && this.random.nextBoolean()) {
				return;
			}

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
				double n;
				double o;
				double p;
				if (particleOptions.getType() == ParticleTypes.ENTITY_EFFECT) {
					int m = bl && this.random.nextBoolean() ? 16777215 : this.getColor();
					n = (double)((float)(m >> 16 & 0xFF) / 255.0F);
					o = (double)((float)(m >> 8 & 0xFF) / 255.0F);
					p = (double)((float)(m & 0xFF) / 255.0F);
				} else if (bl) {
					n = 0.0;
					o = 0.0;
					p = 0.0;
				} else {
					n = (0.5 - this.random.nextDouble()) * 0.15;
					o = 0.01F;
					p = (0.5 - this.random.nextDouble()) * 0.15;
				}

				this.level.addAlwaysVisibleParticle(particleOptions, d, e, l, n, o, p);
			}
		} else {
			if (this.tickCount >= this.waitTime + this.duration) {
				this.discard();
				return;
			}

			boolean bl2 = this.tickCount < this.waitTime;
			if (bl != bl2) {
				this.setWaiting(bl2);
			}

			if (bl2) {
				return;
			}

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
				List<MobEffectInstance> list = Lists.<MobEffectInstance>newArrayList();

				for (MobEffectInstance mobEffectInstance : this.potion.getEffects()) {
					list.add(
						new MobEffectInstance(
							mobEffectInstance.getEffect(),
							mobEffectInstance.getDuration() / 4,
							mobEffectInstance.getAmplifier(),
							mobEffectInstance.isAmbient(),
							mobEffectInstance.isVisible()
						)
					);
				}

				list.addAll(this.effects);
				if (list.isEmpty()) {
					this.victims.clear();
				} else {
					List<LivingEntity> list2 = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
					if (!list2.isEmpty()) {
						for (LivingEntity livingEntity : list2) {
							if (!this.victims.containsKey(livingEntity) && livingEntity.isAffectedByPotions()) {
								double q = livingEntity.getX() - this.getX();
								double r = livingEntity.getZ() - this.getZ();
								double s = q * q + r * r;
								if (s <= (double)(f * f)) {
									this.victims.put(livingEntity, this.tickCount + this.reapplicationDelay);

									for (MobEffectInstance mobEffectInstance2 : list) {
										if (mobEffectInstance2.getEffect().isInstantenous()) {
											mobEffectInstance2.getEffect().applyInstantenousEffect(this, this.getOwner(), livingEntity, mobEffectInstance2.getAmplifier(), 0.5);
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
		if (this.owner == null && this.ownerUUID != null && this.level instanceof ServerLevel) {
			Entity entity = ((ServerLevel)this.level).getEntity(this.ownerUUID);
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

		if (compoundTag.contains("Particle", 8)) {
			try {
				this.setParticle(ParticleArgument.readParticle(new StringReader(compoundTag.getString("Particle")), BuiltInRegistries.PARTICLE_TYPE.asLookup()));
			} catch (CommandSyntaxException var5) {
				LOGGER.warn("Couldn't load custom particle {}", compoundTag.getString("Particle"), var5);
			}
		}

		if (compoundTag.contains("Color", 99)) {
			this.setFixedColor(compoundTag.getInt("Color"));
		}

		if (compoundTag.contains("Potion", 8)) {
			this.setPotion(PotionUtils.getPotion(compoundTag));
		}

		if (compoundTag.contains("Effects", 9)) {
			ListTag listTag = compoundTag.getList("Effects", 10);
			this.effects.clear();

			for (int i = 0; i < listTag.size(); i++) {
				MobEffectInstance mobEffectInstance = MobEffectInstance.load(listTag.getCompound(i));
				if (mobEffectInstance != null) {
					this.addEffect(mobEffectInstance);
				}
			}
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
		compoundTag.putString("Particle", this.getParticle().writeToString());
		if (this.ownerUUID != null) {
			compoundTag.putUUID("Owner", this.ownerUUID);
		}

		if (this.fixedColor) {
			compoundTag.putInt("Color", this.getColor());
		}

		if (this.potion != Potions.EMPTY) {
			compoundTag.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
		}

		if (!this.effects.isEmpty()) {
			ListTag listTag = new ListTag();

			for (MobEffectInstance mobEffectInstance : this.effects) {
				listTag.add(mobEffectInstance.save(new CompoundTag()));
			}

			compoundTag.put("Effects", listTag);
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_RADIUS.equals(entityDataAccessor)) {
			this.refreshDimensions();
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	public Potion getPotion() {
		return this.potion;
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return EntityDimensions.scalable(this.getRadius() * 2.0F, 0.5F);
	}
}
