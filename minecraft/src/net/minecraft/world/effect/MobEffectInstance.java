package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

public class MobEffectInstance implements Comparable<MobEffectInstance> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final MobEffect effect;
	int duration;
	private int amplifier;
	private boolean ambient;
	private boolean noCounter;
	private boolean visible;
	private boolean showIcon;
	@Nullable
	private MobEffectInstance hiddenEffect;
	private Optional<MobEffectInstance.FactorData> factorData;

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

	public MobEffectInstance(
		MobEffect mobEffect,
		int i,
		int j,
		boolean bl,
		boolean bl2,
		boolean bl3,
		@Nullable MobEffectInstance mobEffectInstance,
		Optional<MobEffectInstance.FactorData> optional
	) {
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

	public Optional<MobEffectInstance.FactorData> getFactorData() {
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
			if (mobEffectInstance.duration < this.duration) {
				MobEffectInstance mobEffectInstance2 = this.hiddenEffect;
				this.hiddenEffect = new MobEffectInstance(this);
				this.hiddenEffect.hiddenEffect = mobEffectInstance2;
			}

			this.amplifier = mobEffectInstance.amplifier;
			this.duration = mobEffectInstance.duration;
			bl = true;
		} else if (mobEffectInstance.duration > this.duration) {
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

		if (i != this.duration) {
			this.factorData.ifPresent(factorData -> factorData.effectChangedTimestamp = factorData.effectChangedTimestamp + (this.duration - i));
			bl = true;
		}

		return bl;
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
		if (this.duration > 0) {
			if (this.effect.isDurationEffectTick(this.duration, this.amplifier)) {
				this.applyEffect(livingEntity);
			}

			this.tickDownDuration();
			if (this.duration == 0 && this.hiddenEffect != null) {
				this.setDetailsFrom(this.hiddenEffect);
				this.hiddenEffect = this.hiddenEffect.hiddenEffect;
				runnable.run();
			}
		}

		this.factorData.ifPresent(factorData -> factorData.update(this));
		return this.duration > 0;
	}

	private int tickDownDuration() {
		if (this.hiddenEffect != null) {
			this.hiddenEffect.tickDownDuration();
		}

		return --this.duration;
	}

	public void applyEffect(LivingEntity livingEntity) {
		if (this.duration > 0) {
			this.effect.applyEffectTick(livingEntity, this.amplifier);
		}
	}

	public String getDescriptionId() {
		return this.effect.getDescriptionId();
	}

	public String toString() {
		String string;
		if (this.amplifier > 0) {
			string = this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.duration;
		} else {
			string = this.getDescriptionId() + ", Duration: " + this.duration;
		}

		if (!this.visible) {
			string = string + ", Particles: false";
		}

		if (!this.showIcon) {
			string = string + ", Show Icon: false";
		}

		return string;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof MobEffectInstance mobEffectInstance)
				? false
				: this.duration == mobEffectInstance.duration
					&& this.amplifier == mobEffectInstance.amplifier
					&& this.ambient == mobEffectInstance.ambient
					&& this.effect.equals(mobEffectInstance.effect);
		}
	}

	public int hashCode() {
		int i = this.effect.hashCode();
		i = 31 * i + this.duration;
		i = 31 * i + this.amplifier;
		return 31 * i + (this.ambient ? 1 : 0);
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

		this.factorData
			.ifPresent(
				factorData -> MobEffectInstance.FactorData.CODEC
						.encodeStart(NbtOps.INSTANCE, factorData)
						.resultOrPartial(LOGGER::error)
						.ifPresent(tag -> compoundTag.put("FactorCalculationData", tag))
			);
	}

	@Nullable
	public static MobEffectInstance load(CompoundTag compoundTag) {
		int i = compoundTag.getInt("Id");
		MobEffect mobEffect = MobEffect.byId(i);
		return mobEffect == null ? null : loadSpecifiedEffect(mobEffect, compoundTag);
	}

	private static MobEffectInstance loadSpecifiedEffect(MobEffect mobEffect, CompoundTag compoundTag) {
		int i = compoundTag.getByte("Amplifier");
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
			mobEffectInstance = loadSpecifiedEffect(mobEffect, compoundTag.getCompound("HiddenEffect"));
		}

		Optional<MobEffectInstance.FactorData> optional;
		if (compoundTag.contains("FactorCalculationData", 10)) {
			optional = MobEffectInstance.FactorData.CODEC
				.parse(new Dynamic<>(NbtOps.INSTANCE, compoundTag.getCompound("FactorCalculationData")))
				.resultOrPartial(LOGGER::error);
		} else {
			optional = Optional.empty();
		}

		return new MobEffectInstance(mobEffect, j, Math.max(i, 0), bl, bl2, bl3, mobEffectInstance, optional);
	}

	public void setNoCounter(boolean bl) {
		this.noCounter = bl;
	}

	public boolean isNoCounter() {
		return this.noCounter;
	}

	public int compareTo(MobEffectInstance mobEffectInstance) {
		int i = 32147;
		return (this.getDuration() <= 32147 || mobEffectInstance.getDuration() <= 32147) && (!this.isAmbient() || !mobEffectInstance.isAmbient())
			? ComparisonChain.start()
				.compare(this.isAmbient(), mobEffectInstance.isAmbient())
				.compare(this.getDuration(), mobEffectInstance.getDuration())
				.compare(this.getEffect().getColor(), mobEffectInstance.getEffect().getColor())
				.result()
			: ComparisonChain.start()
				.compare(this.isAmbient(), mobEffectInstance.isAmbient())
				.compare(this.getEffect().getColor(), mobEffectInstance.getEffect().getColor())
				.result();
	}

	public static class FactorData {
		public static final Codec<MobEffectInstance.FactorData> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("padding_duration").forGetter(factorData -> factorData.paddingDuration),
						Codec.FLOAT.fieldOf("factor_start").orElse(0.0F).forGetter(factorData -> factorData.factorStart),
						Codec.FLOAT.fieldOf("factor_target").orElse(1.0F).forGetter(factorData -> factorData.factorTarget),
						Codec.FLOAT.fieldOf("factor_current").orElse(0.0F).forGetter(factorData -> factorData.factorCurrent),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("effect_changed_timestamp").orElse(0).forGetter(factorData -> factorData.effectChangedTimestamp),
						Codec.FLOAT.fieldOf("factor_previous_frame").orElse(0.0F).forGetter(factorData -> factorData.factorPreviousFrame),
						Codec.BOOL.fieldOf("had_effect_last_tick").orElse(false).forGetter(factorData -> factorData.hadEffectLastTick)
					)
					.apply(instance, MobEffectInstance.FactorData::new)
		);
		private final int paddingDuration;
		private float factorStart;
		private float factorTarget;
		private float factorCurrent;
		int effectChangedTimestamp;
		private float factorPreviousFrame;
		private boolean hadEffectLastTick;

		public FactorData(int i, float f, float g, float h, int j, float k, boolean bl) {
			this.paddingDuration = i;
			this.factorStart = f;
			this.factorTarget = g;
			this.factorCurrent = h;
			this.effectChangedTimestamp = j;
			this.factorPreviousFrame = k;
			this.hadEffectLastTick = bl;
		}

		public FactorData(int i) {
			this(i, 0.0F, 1.0F, 0.0F, 0, 0.0F, false);
		}

		public void update(MobEffectInstance mobEffectInstance) {
			this.factorPreviousFrame = this.factorCurrent;
			boolean bl = mobEffectInstance.duration > this.paddingDuration;
			if (this.hadEffectLastTick != bl) {
				this.hadEffectLastTick = bl;
				this.effectChangedTimestamp = mobEffectInstance.duration;
				this.factorStart = this.factorCurrent;
				this.factorTarget = bl ? 1.0F : 0.0F;
			}

			float f = Mth.clamp(((float)this.effectChangedTimestamp - (float)mobEffectInstance.duration) / (float)this.paddingDuration, 0.0F, 1.0F);
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
