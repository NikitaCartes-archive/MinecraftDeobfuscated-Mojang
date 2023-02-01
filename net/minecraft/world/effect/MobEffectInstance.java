/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MobEffectInstance
implements Comparable<MobEffectInstance> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int INFINITE_DURATION = -1;
    private final MobEffect effect;
    private int duration;
    private int amplifier;
    private boolean ambient;
    private boolean visible;
    private boolean showIcon;
    @Nullable
    private MobEffectInstance hiddenEffect;
    private final Optional<FactorData> factorData;

    public MobEffectInstance(MobEffect mobEffect) {
        this(mobEffect, 0, 0);
    }

    public MobEffectInstance(MobEffect mobEffect, int i) {
        this(mobEffect, i, 0);
    }

    public MobEffectInstance(MobEffect mobEffect, int i, int j) {
        this(mobEffect, i, j, false, true);
    }

    public MobEffectInstance(MobEffect mobEffect, int i, int j, boolean bl, boolean bl2) {
        this(mobEffect, i, j, bl, bl2, bl2);
    }

    public MobEffectInstance(MobEffect mobEffect, int i, int j, boolean bl, boolean bl2, boolean bl3) {
        this(mobEffect, i, j, bl, bl2, bl3, null, mobEffect.createFactorData());
    }

    public MobEffectInstance(MobEffect mobEffect, int i, int j, boolean bl, boolean bl2, boolean bl3, @Nullable MobEffectInstance mobEffectInstance, Optional<FactorData> optional) {
        this.effect = mobEffect;
        this.duration = i;
        this.amplifier = j;
        this.ambient = bl;
        this.visible = bl2;
        this.showIcon = bl3;
        this.hiddenEffect = mobEffectInstance;
        this.factorData = optional;
    }

    public MobEffectInstance(MobEffectInstance mobEffectInstance) {
        this.effect = mobEffectInstance.effect;
        this.factorData = this.effect.createFactorData();
        this.setDetailsFrom(mobEffectInstance);
    }

    public Optional<FactorData> getFactorData() {
        return this.factorData;
    }

    void setDetailsFrom(MobEffectInstance mobEffectInstance) {
        this.duration = mobEffectInstance.duration;
        this.amplifier = mobEffectInstance.amplifier;
        this.ambient = mobEffectInstance.ambient;
        this.visible = mobEffectInstance.visible;
        this.showIcon = mobEffectInstance.showIcon;
    }

    public boolean update(MobEffectInstance mobEffectInstance) {
        if (this.effect != mobEffectInstance.effect) {
            LOGGER.warn("This method should only be called for matching effects!");
        }
        int i = this.duration;
        boolean bl = false;
        if (mobEffectInstance.amplifier > this.amplifier) {
            if (mobEffectInstance.isShorterDurationThan(this)) {
                MobEffectInstance mobEffectInstance2 = this.hiddenEffect;
                this.hiddenEffect = new MobEffectInstance(this);
                this.hiddenEffect.hiddenEffect = mobEffectInstance2;
            }
            this.amplifier = mobEffectInstance.amplifier;
            this.duration = mobEffectInstance.duration;
            bl = true;
        } else if (this.isShorterDurationThan(mobEffectInstance)) {
            if (mobEffectInstance.amplifier == this.amplifier) {
                this.duration = mobEffectInstance.duration;
                bl = true;
            } else if (this.hiddenEffect == null) {
                this.hiddenEffect = new MobEffectInstance(mobEffectInstance);
            } else {
                this.hiddenEffect.update(mobEffectInstance);
            }
        }
        if (!mobEffectInstance.ambient && this.ambient || bl) {
            this.ambient = mobEffectInstance.ambient;
            bl = true;
        }
        if (mobEffectInstance.visible != this.visible) {
            this.visible = mobEffectInstance.visible;
            bl = true;
        }
        if (mobEffectInstance.showIcon != this.showIcon) {
            this.showIcon = mobEffectInstance.showIcon;
            bl = true;
        }
        return bl;
    }

    private boolean isShorterDurationThan(MobEffectInstance mobEffectInstance) {
        return !this.isInfiniteDuration() && (this.duration < mobEffectInstance.duration || mobEffectInstance.isInfiniteDuration());
    }

    public boolean isInfiniteDuration() {
        return this.duration == -1;
    }

    public boolean endsWithin(int i) {
        return !this.isInfiniteDuration() && this.duration <= i;
    }

    public int mapDuration(Int2IntFunction int2IntFunction) {
        if (this.isInfiniteDuration()) {
            return -1;
        }
        return int2IntFunction.applyAsInt(this.duration);
    }

    public MobEffect getEffect() {
        return this.effect;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public boolean isAmbient() {
        return this.ambient;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean showIcon() {
        return this.showIcon;
    }

    public boolean tick(LivingEntity livingEntity, Runnable runnable) {
        if (this.hasRemainingDuration()) {
            int i;
            int n = i = this.isInfiniteDuration() ? livingEntity.tickCount : this.duration;
            if (this.effect.isDurationEffectTick(i, this.amplifier)) {
                this.applyEffect(livingEntity);
            }
            this.tickDownDuration();
            if (this.duration == 0 && this.hiddenEffect != null) {
                this.setDetailsFrom(this.hiddenEffect);
                this.hiddenEffect = this.hiddenEffect.hiddenEffect;
                runnable.run();
            }
        }
        this.factorData.ifPresent(factorData -> factorData.tick(this));
        return this.hasRemainingDuration();
    }

    private boolean hasRemainingDuration() {
        return this.isInfiniteDuration() || this.duration > 0;
    }

    private int tickDownDuration() {
        if (this.hiddenEffect != null) {
            this.hiddenEffect.tickDownDuration();
        }
        this.duration = this.mapDuration(i -> i - 1);
        return this.duration;
    }

    public void applyEffect(LivingEntity livingEntity) {
        if (this.hasRemainingDuration()) {
            this.effect.applyEffectTick(livingEntity, this.amplifier);
        }
    }

    public String getDescriptionId() {
        return this.effect.getDescriptionId();
    }

    public String toString() {
        String string = this.amplifier > 0 ? this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.describeDuration() : this.getDescriptionId() + ", Duration: " + this.describeDuration();
        if (!this.visible) {
            string = string + ", Particles: false";
        }
        if (!this.showIcon) {
            string = string + ", Show Icon: false";
        }
        return string;
    }

    private String describeDuration() {
        if (this.isInfiniteDuration()) {
            return "infinite";
        }
        return Integer.toString(this.duration);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof MobEffectInstance) {
            MobEffectInstance mobEffectInstance = (MobEffectInstance)object;
            return this.duration == mobEffectInstance.duration && this.amplifier == mobEffectInstance.amplifier && this.ambient == mobEffectInstance.ambient && this.effect.equals(mobEffectInstance.effect);
        }
        return false;
    }

    public int hashCode() {
        int i = this.effect.hashCode();
        i = 31 * i + this.duration;
        i = 31 * i + this.amplifier;
        i = 31 * i + (this.ambient ? 1 : 0);
        return i;
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putInt("Id", MobEffect.getId(this.getEffect()));
        this.writeDetailsTo(compoundTag);
        return compoundTag;
    }

    private void writeDetailsTo(CompoundTag compoundTag) {
        compoundTag.putByte("Amplifier", (byte)this.getAmplifier());
        compoundTag.putInt("Duration", this.getDuration());
        compoundTag.putBoolean("Ambient", this.isAmbient());
        compoundTag.putBoolean("ShowParticles", this.isVisible());
        compoundTag.putBoolean("ShowIcon", this.showIcon());
        if (this.hiddenEffect != null) {
            CompoundTag compoundTag2 = new CompoundTag();
            this.hiddenEffect.save(compoundTag2);
            compoundTag.put("HiddenEffect", compoundTag2);
        }
        this.factorData.ifPresent(factorData -> FactorData.CODEC.encodeStart(NbtOps.INSTANCE, (FactorData)factorData).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("FactorCalculationData", (Tag)tag)));
    }

    @Nullable
    public static MobEffectInstance load(CompoundTag compoundTag) {
        int i = compoundTag.getInt("Id");
        MobEffect mobEffect = MobEffect.byId(i);
        if (mobEffect == null) {
            return null;
        }
        return MobEffectInstance.loadSpecifiedEffect(mobEffect, compoundTag);
    }

    private static MobEffectInstance loadSpecifiedEffect(MobEffect mobEffect, CompoundTag compoundTag) {
        byte i = compoundTag.getByte("Amplifier");
        int j = compoundTag.getInt("Duration");
        boolean bl = compoundTag.getBoolean("Ambient");
        boolean bl2 = true;
        if (compoundTag.contains("ShowParticles", 1)) {
            bl2 = compoundTag.getBoolean("ShowParticles");
        }
        boolean bl3 = bl2;
        if (compoundTag.contains("ShowIcon", 1)) {
            bl3 = compoundTag.getBoolean("ShowIcon");
        }
        MobEffectInstance mobEffectInstance = null;
        if (compoundTag.contains("HiddenEffect", 10)) {
            mobEffectInstance = MobEffectInstance.loadSpecifiedEffect(mobEffect, compoundTag.getCompound("HiddenEffect"));
        }
        Optional<FactorData> optional = compoundTag.contains("FactorCalculationData", 10) ? FactorData.CODEC.parse(new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag.getCompound("FactorCalculationData"))).resultOrPartial(LOGGER::error) : Optional.empty();
        return new MobEffectInstance(mobEffect, j, Math.max(i, 0), bl, bl2, bl3, mobEffectInstance, optional);
    }

    @Override
    public int compareTo(MobEffectInstance mobEffectInstance) {
        int i = 32147;
        if (this.getDuration() > 32147 && mobEffectInstance.getDuration() > 32147 || this.isAmbient() && mobEffectInstance.isAmbient()) {
            return ComparisonChain.start().compare(this.isAmbient(), mobEffectInstance.isAmbient()).compare(this.getEffect().getColor(), mobEffectInstance.getEffect().getColor()).result();
        }
        return ComparisonChain.start().compareFalseFirst(this.isAmbient(), mobEffectInstance.isAmbient()).compareFalseFirst(this.isInfiniteDuration(), mobEffectInstance.isInfiniteDuration()).compare(this.getDuration(), mobEffectInstance.getDuration()).compare(this.getEffect().getColor(), mobEffectInstance.getEffect().getColor()).result();
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((MobEffectInstance)object);
    }

    public static class FactorData {
        public static final Codec<FactorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("padding_duration")).forGetter(factorData -> factorData.paddingDuration), ((MapCodec)Codec.FLOAT.fieldOf("factor_start")).orElse(Float.valueOf(0.0f)).forGetter(factorData -> Float.valueOf(factorData.factorStart)), ((MapCodec)Codec.FLOAT.fieldOf("factor_target")).orElse(Float.valueOf(1.0f)).forGetter(factorData -> Float.valueOf(factorData.factorTarget)), ((MapCodec)Codec.FLOAT.fieldOf("factor_current")).orElse(Float.valueOf(0.0f)).forGetter(factorData -> Float.valueOf(factorData.factorCurrent)), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ticks_active")).orElse(0).forGetter(factorData -> factorData.ticksActive), ((MapCodec)Codec.FLOAT.fieldOf("factor_previous_frame")).orElse(Float.valueOf(0.0f)).forGetter(factorData -> Float.valueOf(factorData.factorPreviousFrame)), ((MapCodec)Codec.BOOL.fieldOf("had_effect_last_tick")).orElse(false).forGetter(factorData -> factorData.hadEffectLastTick)).apply((Applicative<FactorData, ?>)instance, FactorData::new));
        private final int paddingDuration;
        private float factorStart;
        private float factorTarget;
        private float factorCurrent;
        private int ticksActive;
        private float factorPreviousFrame;
        private boolean hadEffectLastTick;

        public FactorData(int i, float f, float g, float h, int j, float k, boolean bl) {
            this.paddingDuration = i;
            this.factorStart = f;
            this.factorTarget = g;
            this.factorCurrent = h;
            this.ticksActive = j;
            this.factorPreviousFrame = k;
            this.hadEffectLastTick = bl;
        }

        public FactorData(int i) {
            this(i, 0.0f, 1.0f, 0.0f, 0, 0.0f, false);
        }

        public void tick(MobEffectInstance mobEffectInstance) {
            this.factorPreviousFrame = this.factorCurrent;
            boolean bl = !mobEffectInstance.endsWithin(this.paddingDuration);
            ++this.ticksActive;
            if (this.hadEffectLastTick != bl) {
                this.hadEffectLastTick = bl;
                this.ticksActive = 0;
                this.factorStart = this.factorCurrent;
                this.factorTarget = bl ? 1.0f : 0.0f;
            }
            float f = Mth.clamp((float)this.ticksActive / (float)this.paddingDuration, 0.0f, 1.0f);
            this.factorCurrent = Mth.lerp(f, this.factorStart, this.factorTarget);
        }

        public float getFactor(LivingEntity livingEntity, float f) {
            if (livingEntity.isRemoved()) {
                this.factorPreviousFrame = this.factorCurrent;
            }
            return Mth.lerp(f, this.factorPreviousFrame, this.factorCurrent);
        }
    }
}

