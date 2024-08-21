package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.PlaySoundConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public record Consumable(
	float consumeSeconds, ItemUseAnimation animation, Holder<SoundEvent> sound, boolean hasConsumeParticles, List<ConsumeEffect> onConsumeEffects
) {
	public static final float DEFAULT_CONSUME_SECONDS = 1.6F;
	private static final int CONSUME_EFFECTS_INTERVAL = 4;
	private static final float CONSUME_EFFECTS_START_FRACTION = 0.21875F;
	public static final Codec<Consumable> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("consume_seconds", 1.6F).forGetter(Consumable::consumeSeconds),
					ItemUseAnimation.CODEC.optionalFieldOf("animation", ItemUseAnimation.EAT).forGetter(Consumable::animation),
					SoundEvent.CODEC.optionalFieldOf("sound", SoundEvents.GENERIC_EAT).forGetter(Consumable::sound),
					Codec.BOOL.optionalFieldOf("has_consume_particles", Boolean.valueOf(true)).forGetter(Consumable::hasConsumeParticles),
					ConsumeEffect.CODEC.listOf().optionalFieldOf("on_consume_effects", List.of()).forGetter(Consumable::onConsumeEffects)
				)
				.apply(instance, Consumable::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, Consumable> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT,
		Consumable::consumeSeconds,
		ItemUseAnimation.STREAM_CODEC,
		Consumable::animation,
		SoundEvent.STREAM_CODEC,
		Consumable::sound,
		ByteBufCodecs.BOOL,
		Consumable::hasConsumeParticles,
		ConsumeEffect.STREAM_CODEC.apply(ByteBufCodecs.list()),
		Consumable::onConsumeEffects,
		Consumable::new
	);

	public InteractionResult startConsuming(LivingEntity livingEntity, ItemStack itemStack, InteractionHand interactionHand) {
		if (!this.canConsume(livingEntity, itemStack)) {
			return InteractionResult.FAIL;
		} else {
			boolean bl = this.consumeTicks() > 0;
			if (bl) {
				livingEntity.startUsingItem(interactionHand);
				return InteractionResult.CONSUME;
			} else {
				ItemStack itemStack2 = this.onConsume(livingEntity.level(), livingEntity, itemStack);
				return InteractionResult.CONSUME.heldItemTransformedTo(itemStack2);
			}
		}
	}

	public ItemStack onConsume(Level level, LivingEntity livingEntity, ItemStack itemStack) {
		RandomSource randomSource = livingEntity.getRandom();
		this.emitParticlesAndSounds(randomSource, livingEntity, itemStack, 16);
		if (livingEntity instanceof ServerPlayer serverPlayer) {
			serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
			CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemStack);
		}

		itemStack.getAllOfType(ConsumableListener.class).forEach(consumableListener -> consumableListener.onConsume(level, livingEntity, itemStack, this));
		if (!level.isClientSide) {
			this.onConsumeEffects.forEach(consumeEffect -> consumeEffect.apply(level, itemStack, livingEntity));
		}

		livingEntity.gameEvent(this.animation == ItemUseAnimation.DRINK ? GameEvent.DRINK : GameEvent.EAT);
		itemStack.consume(1, livingEntity);
		return itemStack;
	}

	public boolean canConsume(LivingEntity livingEntity, ItemStack itemStack) {
		FoodProperties foodProperties = itemStack.get(DataComponents.FOOD);
		return foodProperties != null && livingEntity instanceof Player player ? player.canEat(foodProperties.canAlwaysEat()) : true;
	}

	public int consumeTicks() {
		return (int)(this.consumeSeconds * 20.0F);
	}

	public void emitParticlesAndSounds(RandomSource randomSource, LivingEntity livingEntity, ItemStack itemStack, int i) {
		float f = randomSource.nextBoolean() ? 0.5F : 1.0F;
		float g = randomSource.triangle(1.0F, 0.2F);
		float h = 0.5F;
		float j = Mth.randomBetween(randomSource, 0.9F, 1.0F);
		float k = this.animation == ItemUseAnimation.DRINK ? 0.5F : f;
		float l = this.animation == ItemUseAnimation.DRINK ? j : g;
		if (this.hasConsumeParticles) {
			livingEntity.spawnItemParticles(itemStack, i);
		}

		SoundEvent soundEvent = livingEntity instanceof Consumable.OverrideConsumeSound overrideConsumeSound
			? overrideConsumeSound.getConsumeSound(itemStack)
			: this.sound.value();
		livingEntity.playSound(soundEvent, k, l);
	}

	public boolean shouldEmitParticlesAndSounds(int i) {
		int j = this.consumeTicks() - i;
		int k = (int)((float)this.consumeTicks() * 0.21875F);
		boolean bl = j > k;
		return bl && i % 4 == 0;
	}

	public static Consumable.Builder builder() {
		return new Consumable.Builder();
	}

	public static class Builder {
		private float consumeSeconds = 1.6F;
		private ItemUseAnimation animation = ItemUseAnimation.EAT;
		private Holder<SoundEvent> sound = SoundEvents.GENERIC_EAT;
		private boolean hasConsumeParticles = true;
		private final List<ConsumeEffect> onConsumeEffects = new ArrayList();

		Builder() {
		}

		public Consumable.Builder consumeSeconds(float f) {
			this.consumeSeconds = f;
			return this;
		}

		public Consumable.Builder animation(ItemUseAnimation itemUseAnimation) {
			this.animation = itemUseAnimation;
			return this;
		}

		public Consumable.Builder sound(Holder<SoundEvent> holder) {
			this.sound = holder;
			return this;
		}

		public Consumable.Builder soundAfterConsume(Holder<SoundEvent> holder) {
			return this.onConsume(new PlaySoundConsumeEffect(holder));
		}

		public Consumable.Builder hasConsumeParticles(boolean bl) {
			this.hasConsumeParticles = bl;
			return this;
		}

		public Consumable.Builder onConsume(ConsumeEffect consumeEffect) {
			this.onConsumeEffects.add(consumeEffect);
			return this;
		}

		public Consumable build() {
			return new Consumable(this.consumeSeconds, this.animation, this.sound, this.hasConsumeParticles, this.onConsumeEffects);
		}
	}

	public interface OverrideConsumeSound {
		SoundEvent getConsumeSound(ItemStack itemStack);
	}
}
