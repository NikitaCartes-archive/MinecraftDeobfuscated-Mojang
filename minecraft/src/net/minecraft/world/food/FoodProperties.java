package net.minecraft.world.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.level.Level;

public record FoodProperties(int nutrition, float saturation, boolean canAlwaysEat) implements ConsumableListener {
	public static final Codec<FoodProperties> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(FoodProperties::nutrition),
					Codec.FLOAT.fieldOf("saturation").forGetter(FoodProperties::saturation),
					Codec.BOOL.optionalFieldOf("can_always_eat", Boolean.valueOf(false)).forGetter(FoodProperties::canAlwaysEat)
				)
				.apply(instance, FoodProperties::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, FoodProperties> DIRECT_STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		FoodProperties::nutrition,
		ByteBufCodecs.FLOAT,
		FoodProperties::saturation,
		ByteBufCodecs.BOOL,
		FoodProperties::canAlwaysEat,
		FoodProperties::new
	);

	@Override
	public void onConsume(Level level, LivingEntity livingEntity, ItemStack itemStack, Consumable consumable) {
		RandomSource randomSource = livingEntity.getRandom();
		level.playSound(
			null,
			livingEntity.getX(),
			livingEntity.getY(),
			livingEntity.getZ(),
			consumable.sound().value(),
			SoundSource.NEUTRAL,
			1.0F,
			randomSource.triangle(1.0F, 0.4F)
		);
		if (livingEntity instanceof Player player) {
			player.getFoodData().eat(this);
			level.playSound(
				null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, Mth.randomBetween(randomSource, 0.9F, 1.0F)
			);
		}
	}

	public static class Builder {
		private int nutrition;
		private float saturationModifier;
		private boolean canAlwaysEat;

		public FoodProperties.Builder nutrition(int i) {
			this.nutrition = i;
			return this;
		}

		public FoodProperties.Builder saturationModifier(float f) {
			this.saturationModifier = f;
			return this;
		}

		public FoodProperties.Builder alwaysEdible() {
			this.canAlwaysEat = true;
			return this;
		}

		public FoodProperties build() {
			float f = FoodConstants.saturationByModifier(this.nutrition, this.saturationModifier);
			return new FoodProperties(this.nutrition, f, this.canAlwaysEat);
		}
	}
}
