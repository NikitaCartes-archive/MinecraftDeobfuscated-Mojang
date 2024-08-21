package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record PlaySoundConsumeEffect(Holder<SoundEvent> sound) implements ConsumeEffect {
	public static final MapCodec<PlaySoundConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(SoundEvent.CODEC.fieldOf("sound").forGetter(PlaySoundConsumeEffect::sound)).apply(instance, PlaySoundConsumeEffect::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, PlaySoundConsumeEffect> STREAM_CODEC = StreamCodec.composite(
		SoundEvent.STREAM_CODEC, PlaySoundConsumeEffect::sound, PlaySoundConsumeEffect::new
	);

	@Override
	public ConsumeEffect.Type<PlaySoundConsumeEffect> getType() {
		return ConsumeEffect.Type.PLAY_SOUND;
	}

	@Override
	public boolean apply(Level level, ItemStack itemStack, LivingEntity livingEntity) {
		level.playSound(null, livingEntity.blockPosition(), this.sound.value(), livingEntity.getSoundSource(), 1.0F, 1.0F);
		return true;
	}
}
