package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ClearAllStatusEffectsConsumeEffect() implements ConsumeEffect {
	public static final ClearAllStatusEffectsConsumeEffect INSTANCE = new ClearAllStatusEffectsConsumeEffect();
	public static final MapCodec<ClearAllStatusEffectsConsumeEffect> CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, ClearAllStatusEffectsConsumeEffect> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public ConsumeEffect.Type<ClearAllStatusEffectsConsumeEffect> getType() {
		return ConsumeEffect.Type.CLEAR_ALL_EFFECTS;
	}

	@Override
	public boolean apply(Level level, ItemStack itemStack, LivingEntity livingEntity) {
		return livingEntity.removeAllEffects();
	}
}
