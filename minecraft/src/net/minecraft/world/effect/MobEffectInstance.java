package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

public class MobEffectInstance implements Comparable<MobEffectInstance> {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int INFINITE_DURATION = -1;
	private static final String TAG_ID = "id";
	private static final String TAG_AMBIENT = "ambient";
	private static final String TAG_HIDDEN_EFFECT = "hidden_effect";
	private static final String TAG_AMPLIFIER = "amplifier";
	private static final String TAG_DURATION = "duration";
	private static final String TAG_SHOW_PARTICLES = "show_particles";
	private static final String TAG_SHOW_ICON = "show_icon";
	private final Holder<MobEffect> effect;
	private int duration;
	private int amplifier;
	private boolean ambient;
	private boolean visible;
	private boolean showIcon;
	@Nullable
	private MobEffectInstance hiddenEffect;
	private final MobEffectInstance.BlendState blendState = new MobEffectInstance.BlendState();

	public MobEffectInstance(Holder<MobEffect> holder) {
		this(holder, 0, 0);
	}

	public MobEffectInstance(Holder<MobEffect> holder, int i) {
		this(holder, i, 0);
	}

	public MobEffectInstance(Holder<MobEffect> holder, int i, int j) {
		this(holder, i, j, false, true);
	}

	public MobEffectInstance(Holder<MobEffect> holder, int i, int j, boolean bl, boolean bl2) {
		this(holder, i, j, bl, bl2, bl2);
	}

	public MobEffectInstance(Holder<MobEffect> holder, int i, int j, boolean bl, boolean bl2, boolean bl3) {
		this(holder, i, j, bl, bl2, bl3, null);
	}

	public MobEffectInstance(Holder<MobEffect> holder, int i, int j, boolean bl, boolean bl2, boolean bl3, @Nullable MobEffectInstance mobEffectInstance) {
		this.effect = holder;
		this.duration = i;
		this.amplifier = j;
		this.ambient = bl;
		this.visible = bl2;
		this.showIcon = bl3;
		this.hiddenEffect = mobEffectInstance;
	}

	public MobEffectInstance(MobEffectInstance mobEffectInstance) {
		this.effect = mobEffectInstance.effect;
		this.setDetailsFrom(mobEffectInstance);
	}

	public float getBlendFactor(LivingEntity livingEntity, float f) {
		return this.blendState.getFactor(livingEntity, f);
	}

	void setDetailsFrom(MobEffectInstance mobEffectInstance) {
		this.duration = mobEffectInstance.duration;
		this.amplifier = mobEffectInstance.amplifier;
		this.ambient = mobEffectInstance.ambient;
		this.visible = mobEffectInstance.visible;
		this.showIcon = mobEffectInstance.showIcon;
	}

	public boolean update(MobEffectInstance mobEffectInstance) {
		if (!this.effect.equals(mobEffectInstance.effect)) {
			LOGGER.warn("This method should only be called for matching effects!");
		}

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
		return !this.isInfiniteDuration() && this.duration != 0 ? int2IntFunction.applyAsInt(this.duration) : this.duration;
	}

	public Holder<MobEffect> getEffect() {
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
			int i = this.isInfiniteDuration() ? livingEntity.tickCount : this.duration;
			if (this.effect.value().shouldApplyEffectTickThisTick(i, this.amplifier) && !this.effect.value().applyEffectTick(livingEntity, this.amplifier)) {
				livingEntity.removeEffect(this.effect);
			}

			this.tickDownDuration();
			if (this.duration == 0 && this.hiddenEffect != null) {
				this.setDetailsFrom(this.hiddenEffect);
				this.hiddenEffect = this.hiddenEffect.hiddenEffect;
				runnable.run();
			}
		}

		this.blendState.tick(this);
		return this.hasRemainingDuration();
	}

	private boolean hasRemainingDuration() {
		return this.isInfiniteDuration() || this.duration > 0;
	}

	private int tickDownDuration() {
		if (this.hiddenEffect != null) {
			this.hiddenEffect.tickDownDuration();
		}

		return this.duration = this.mapDuration(i -> i - 1);
	}

	public void onEffectStarted(LivingEntity livingEntity) {
		this.effect.value().onEffectStarted(livingEntity, this.amplifier);
	}

	public String getDescriptionId() {
		return this.effect.value().getDescriptionId();
	}

	public String toString() {
		String string;
		if (this.amplifier > 0) {
			string = this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.describeDuration();
		} else {
			string = this.getDescriptionId() + ", Duration: " + this.describeDuration();
		}

		if (!this.visible) {
			string = string + ", Particles: false";
		}

		if (!this.showIcon) {
			string = string + ", Show Icon: false";
		}

		return string;
	}

	private String describeDuration() {
		return this.isInfiniteDuration() ? "infinite" : Integer.toString(this.duration);
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
		ResourceLocation resourceLocation = ((ResourceKey)this.effect.unwrapKey().orElseThrow()).location();
		compoundTag.putString("id", resourceLocation.toString());
		this.writeDetailsTo(compoundTag);
		return compoundTag;
	}

	private void writeDetailsTo(CompoundTag compoundTag) {
		compoundTag.putByte("amplifier", (byte)this.getAmplifier());
		compoundTag.putInt("duration", this.getDuration());
		compoundTag.putBoolean("ambient", this.isAmbient());
		compoundTag.putBoolean("show_particles", this.isVisible());
		compoundTag.putBoolean("show_icon", this.showIcon());
		if (this.hiddenEffect != null) {
			CompoundTag compoundTag2 = new CompoundTag();
			this.hiddenEffect.save(compoundTag2);
			compoundTag.put("hidden_effect", compoundTag2);
		}
	}

	@Nullable
	public static MobEffectInstance load(CompoundTag compoundTag) {
		ResourceLocation resourceLocation = ResourceLocation.tryParse(compoundTag.getString("id"));
		return resourceLocation == null
			? null
			: (MobEffectInstance)BuiltInRegistries.MOB_EFFECT.getHolder(resourceLocation).map(reference -> loadSpecifiedEffect(reference, compoundTag)).orElse(null);
	}

	private static MobEffectInstance loadSpecifiedEffect(Holder<MobEffect> holder, CompoundTag compoundTag) {
		int i = compoundTag.getByte("amplifier");
		int j = compoundTag.getInt("duration");
		boolean bl = compoundTag.getBoolean("ambient");
		boolean bl2 = true;
		if (compoundTag.contains("show_particles", 1)) {
			bl2 = compoundTag.getBoolean("show_particles");
		}

		boolean bl3 = bl2;
		if (compoundTag.contains("show_icon", 1)) {
			bl3 = compoundTag.getBoolean("show_icon");
		}

		MobEffectInstance mobEffectInstance = null;
		if (compoundTag.contains("hidden_effect", 10)) {
			mobEffectInstance = loadSpecifiedEffect(holder, compoundTag.getCompound("hidden_effect"));
		}

		return new MobEffectInstance(holder, j, Math.max(i, 0), bl, bl2, bl3, mobEffectInstance);
	}

	public int compareTo(MobEffectInstance mobEffectInstance) {
		int i = 32147;
		return (this.getDuration() <= 32147 || mobEffectInstance.getDuration() <= 32147) && (!this.isAmbient() || !mobEffectInstance.isAmbient())
			? ComparisonChain.start()
				.compareFalseFirst(this.isAmbient(), mobEffectInstance.isAmbient())
				.compareFalseFirst(this.isInfiniteDuration(), mobEffectInstance.isInfiniteDuration())
				.compare(this.getDuration(), mobEffectInstance.getDuration())
				.compare(this.getEffect().value().getColor(), mobEffectInstance.getEffect().value().getColor())
				.result()
			: ComparisonChain.start()
				.compare(this.isAmbient(), mobEffectInstance.isAmbient())
				.compare(this.getEffect().value().getColor(), mobEffectInstance.getEffect().value().getColor())
				.result();
	}

	public boolean is(Holder<MobEffect> holder) {
		return this.effect.equals(holder);
	}

	public void copyBlendState(MobEffectInstance mobEffectInstance) {
		this.blendState.copyFrom(mobEffectInstance.blendState);
	}

	public void skipBlending() {
		this.blendState.setImmediate(this);
	}

	static class BlendState {
		private float factor;
		private float factorPreviousFrame;

		public void setImmediate(MobEffectInstance mobEffectInstance) {
			this.factor = computeTarget(mobEffectInstance);
			this.factorPreviousFrame = this.factor;
		}

		public void copyFrom(MobEffectInstance.BlendState blendState) {
			this.factor = blendState.factor;
			this.factorPreviousFrame = blendState.factorPreviousFrame;
		}

		public void tick(MobEffectInstance mobEffectInstance) {
			this.factorPreviousFrame = this.factor;
			int i = getBlendDuration(mobEffectInstance);
			if (i == 0) {
				this.factor = 1.0F;
			} else {
				float f = computeTarget(mobEffectInstance);
				if (this.factor != f) {
					float g = 1.0F / (float)i;
					this.factor = this.factor + Mth.clamp(f - this.factor, -g, g);
				}
			}
		}

		private static float computeTarget(MobEffectInstance mobEffectInstance) {
			boolean bl = !mobEffectInstance.endsWithin(getBlendDuration(mobEffectInstance));
			return bl ? 1.0F : 0.0F;
		}

		private static int getBlendDuration(MobEffectInstance mobEffectInstance) {
			return mobEffectInstance.getEffect().value().getBlendDurationTicks();
		}

		public float getFactor(LivingEntity livingEntity, float f) {
			if (livingEntity.isRemoved()) {
				this.factorPreviousFrame = this.factor;
			}

			return Mth.lerp(f, this.factorPreviousFrame, this.factor);
		}
	}
}
