/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class AreaEffectCloud
extends Entity {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.PARTICLE);
    private Potion potion = Potions.EMPTY;
    private final List<MobEffectInstance> effects = Lists.newArrayList();
    private final Map<Entity, Integer> victims = Maps.newHashMap();
    private int duration = 600;
    private int waitTime = 20;
    private int reapplicationDelay = 20;
    private boolean fixedColor;
    private int durationOnUse;
    private float radiusOnUse;
    private float radiusPerTick;
    private LivingEntity owner;
    private UUID ownerUUID;

    public AreaEffectCloud(EntityType<? extends AreaEffectCloud> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setRadius(3.0f);
    }

    public AreaEffectCloud(Level level, double d, double e, double f) {
        this((EntityType<? extends AreaEffectCloud>)EntityType.AREA_EFFECT_CLOUD, level);
        this.setPos(d, e, f);
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_COLOR, 0);
        this.getEntityData().define(DATA_RADIUS, Float.valueOf(0.5f));
        this.getEntityData().define(DATA_WAITING, false);
        this.getEntityData().define(DATA_PARTICLE, ParticleTypes.ENTITY_EFFECT);
    }

    public void setRadius(float f) {
        if (!this.level.isClientSide) {
            this.getEntityData().set(DATA_RADIUS, Float.valueOf(f));
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
        return this.getEntityData().get(DATA_RADIUS).floatValue();
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
        block23: {
            boolean bl2;
            float f;
            boolean bl;
            block21: {
                ParticleOptions particleOptions;
                block22: {
                    super.tick();
                    bl = this.isWaiting();
                    f = this.getRadius();
                    if (!this.level.isClientSide) break block21;
                    particleOptions = this.getParticle();
                    if (!bl) break block22;
                    if (!this.random.nextBoolean()) break block23;
                    for (int i = 0; i < 2; ++i) {
                        float g = this.random.nextFloat() * ((float)Math.PI * 2);
                        float h = Mth.sqrt(this.random.nextFloat()) * 0.2f;
                        float j = Mth.cos(g) * h;
                        float k = Mth.sin(g) * h;
                        if (particleOptions.getType() == ParticleTypes.ENTITY_EFFECT) {
                            int l = this.random.nextBoolean() ? 0xFFFFFF : this.getColor();
                            int m = l >> 16 & 0xFF;
                            int n = l >> 8 & 0xFF;
                            int o = l & 0xFF;
                            this.level.addAlwaysVisibleParticle(particleOptions, this.getX() + (double)j, this.getY(), this.getZ() + (double)k, (float)m / 255.0f, (float)n / 255.0f, (float)o / 255.0f);
                            continue;
                        }
                        this.level.addAlwaysVisibleParticle(particleOptions, this.getX() + (double)j, this.getY(), this.getZ() + (double)k, 0.0, 0.0, 0.0);
                    }
                    break block23;
                }
                float p = (float)Math.PI * f * f;
                int q = 0;
                while ((float)q < p) {
                    float h = this.random.nextFloat() * ((float)Math.PI * 2);
                    float j = Mth.sqrt(this.random.nextFloat()) * f;
                    float k = Mth.cos(h) * j;
                    float r = Mth.sin(h) * j;
                    if (particleOptions.getType() == ParticleTypes.ENTITY_EFFECT) {
                        int m = this.getColor();
                        int n = m >> 16 & 0xFF;
                        int o = m >> 8 & 0xFF;
                        int s = m & 0xFF;
                        this.level.addAlwaysVisibleParticle(particleOptions, this.getX() + (double)k, this.getY(), this.getZ() + (double)r, (float)n / 255.0f, (float)o / 255.0f, (float)s / 255.0f);
                    } else {
                        this.level.addAlwaysVisibleParticle(particleOptions, this.getX() + (double)k, this.getY(), this.getZ() + (double)r, (0.5 - this.random.nextDouble()) * 0.15, 0.01f, (0.5 - this.random.nextDouble()) * 0.15);
                    }
                    ++q;
                }
                break block23;
            }
            if (this.tickCount >= this.waitTime + this.duration) {
                this.discard();
                return;
            }
            boolean bl3 = bl2 = this.tickCount < this.waitTime;
            if (bl != bl2) {
                this.setWaiting(bl2);
            }
            if (bl2) {
                return;
            }
            if (this.radiusPerTick != 0.0f) {
                if ((f += this.radiusPerTick) < 0.5f) {
                    this.discard();
                    return;
                }
                this.setRadius(f);
            }
            if (this.tickCount % 5 == 0) {
                Iterator<Map.Entry<Entity, Integer>> iterator = this.victims.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Entity, Integer> entry = iterator.next();
                    if (this.tickCount < entry.getValue()) continue;
                    iterator.remove();
                }
                ArrayList<MobEffectInstance> list = Lists.newArrayList();
                for (MobEffectInstance mobEffectInstance : this.potion.getEffects()) {
                    list.add(new MobEffectInstance(mobEffectInstance.getEffect(), mobEffectInstance.getDuration() / 4, mobEffectInstance.getAmplifier(), mobEffectInstance.isAmbient(), mobEffectInstance.isVisible()));
                }
                list.addAll(this.effects);
                if (list.isEmpty()) {
                    this.victims.clear();
                } else {
                    List<LivingEntity> list2 = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
                    if (!list2.isEmpty()) {
                        for (LivingEntity livingEntity : list2) {
                            double e;
                            double d;
                            double t;
                            if (this.victims.containsKey(livingEntity) || !livingEntity.isAffectedByPotions() || !((t = (d = livingEntity.getX() - this.getX()) * d + (e = livingEntity.getZ() - this.getZ()) * e) <= (double)(f * f))) continue;
                            this.victims.put(livingEntity, this.tickCount + this.reapplicationDelay);
                            for (MobEffectInstance mobEffectInstance2 : list) {
                                if (mobEffectInstance2.getEffect().isInstantenous()) {
                                    mobEffectInstance2.getEffect().applyInstantenousEffect(this, this.getOwner(), livingEntity, mobEffectInstance2.getAmplifier(), 0.5);
                                    continue;
                                }
                                livingEntity.addEffect(new MobEffectInstance(mobEffectInstance2));
                            }
                            if (this.radiusOnUse != 0.0f) {
                                if ((f += this.radiusOnUse) < 0.5f) {
                                    this.discard();
                                    return;
                                }
                                this.setRadius(f);
                            }
                            if (this.durationOnUse == 0) continue;
                            this.duration += this.durationOnUse;
                            if (this.duration > 0) continue;
                            this.discard();
                            return;
                        }
                    }
                }
            }
        }
    }

    public void setRadiusOnUse(float f) {
        this.radiusOnUse = f;
    }

    public void setRadiusPerTick(float f) {
        this.radiusPerTick = f;
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
        Entity entity;
        if (this.owner == null && this.ownerUUID != null && this.level instanceof ServerLevel && (entity = ((ServerLevel)this.level).getEntity(this.ownerUUID)) instanceof LivingEntity) {
            this.owner = (LivingEntity)entity;
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
                this.setParticle(ParticleArgument.readParticle(new StringReader(compoundTag.getString("Particle"))));
            } catch (CommandSyntaxException commandSyntaxException) {
                LOGGER.warn("Couldn't load custom particle {}", (Object)compoundTag.getString("Particle"), (Object)commandSyntaxException);
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
            for (int i = 0; i < listTag.size(); ++i) {
                MobEffectInstance mobEffectInstance = MobEffectInstance.load(listTag.getCompound(i));
                if (mobEffectInstance == null) continue;
                this.addEffect(mobEffectInstance);
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
        if (this.potion != Potions.EMPTY && this.potion != null) {
            compoundTag.putString("Potion", Registry.POTION.getKey(this.potion).toString());
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

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0f, 0.5f);
    }
}

