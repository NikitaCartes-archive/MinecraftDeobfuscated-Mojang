package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

public class MobEffectInstance implements Comparable<MobEffectInstance> {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int INFINITE_DURATION = -1;
	public static final int MIN_AMPLIFIER = 0;
	public static final int MAX_AMPLIFIER = 127;
	public static final Codec<MobEffectInstance> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("id").forGetter(MobEffectInstance::getEffect),
					MobEffectInstance.Details.MAP_CODEC.forGetter(MobEffectInstance::asDetails)
				)
				.apply(instance, MobEffectInstance::new)
	);
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
		this.amplifier = Mth.clamp(j, 0, 127);
		this.ambient = bl;
		this.visible = bl2;
		this.showIcon = bl3;
		this.hiddenEffect = mobEffectInstance;
	}

	public MobEffectInstance(MobEffectInstance mobEffectInstance) {
		this.effect = mobEffectInstance.effect;
		this.setDetailsFrom(mobEffectInstance);
	}

	private MobEffectInstance(Holder<MobEffect> holder, MobEffectInstance.Details details) {
		this(
			holder,
			details.duration(),
			details.amplifier(),
			details.ambient(),
			details.showParticles(),
			details.showIcon(),
			(MobEffectInstance)details.hiddenEffect().map(detailsx -> new MobEffectInstance(holder, detailsx)).orElse(null)
		);
	}

	private MobEffectInstance.Details asDetails() {
		return new MobEffectInstance.Details(
			this.getAmplifier(),
			this.getDuration(),
			this.isAmbient(),
			this.isVisible(),
			this.showIcon(),
			Optional.ofNullable(this.hiddenEffect).map(MobEffectInstance::asDetails)
		);
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

	public Tag save() {
		return Util.getOrThrow(CODEC.encodeStart(NbtOps.INSTANCE, this), IllegalStateException::new);
	}

	@Nullable
	public static MobEffectInstance load(CompoundTag compoundTag) {
		return (MobEffectInstance)CODEC.parse(NbtOps.INSTANCE, compoundTag).resultOrPartial(LOGGER::error).orElse(null);
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

	static record Details(int amplifier, int duration, boolean ambient, boolean showParticles, boolean showIcon, Optional<MobEffectInstance.Details> hiddenEffect) {
		public static final MapCodec<MobEffectInstance.Details> MAP_CODEC = ExtraCodecs.recursiveMap(
			"MobEffectInstance.Details",
			codec -> RecordCodecBuilder.mapCodec(
					instance -> instance.group(
								ExtraCodecs.strictOptionalField(ExtraCodecs.UNSIGNED_BYTE, "amplifier", 0).forGetter(MobEffectInstance.Details::amplifier),
								ExtraCodecs.strictOptionalField(Codec.INT, "duration", 0).forGetter(MobEffectInstance.Details::duration),
								ExtraCodecs.strictOptionalField(Codec.BOOL, "ambient", false).forGetter(MobEffectInstance.Details::ambient),
								ExtraCodecs.strictOptionalField(Codec.BOOL, "show_particles", true).forGetter(MobEffectInstance.Details::showParticles),
								ExtraCodecs.strictOptionalField(Codec.BOOL, "show_icon").forGetter(details -> Optional.of(details.showIcon())),
								ExtraCodecs.strictOptionalField(codec, "hidden_effect").forGetter(MobEffectInstance.Details::hiddenEffect)
							)
							.apply(instance, MobEffectInstance.Details::create)
				)
		);

		private static MobEffectInstance.Details create(
			int i, int j, boolean bl, boolean bl2, Optional<Boolean> optional, Optional<MobEffectInstance.Details> optional2
		) {
			return new MobEffectInstance.Details(i, j, bl, bl2, (Boolean)optional.orElse(bl2), optional2);
		}
	}
}
