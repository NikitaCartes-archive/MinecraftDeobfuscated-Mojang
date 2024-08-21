package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record UseCooldown(float seconds, Optional<ResourceLocation> cooldownGroup) {
	public static final Codec<UseCooldown> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.POSITIVE_FLOAT.fieldOf("seconds").forGetter(UseCooldown::seconds),
					ResourceLocation.CODEC.optionalFieldOf("cooldownGroup").forGetter(UseCooldown::cooldownGroup)
				)
				.apply(instance, UseCooldown::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, UseCooldown> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, UseCooldown::seconds, ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), UseCooldown::cooldownGroup, UseCooldown::new
	);

	public UseCooldown(float f) {
		this(f, Optional.empty());
	}

	public int ticks() {
		return (int)(this.seconds * 20.0F);
	}

	public void apply(ItemStack itemStack, LivingEntity livingEntity) {
		if (livingEntity instanceof Player player) {
			player.getCooldowns().addCooldown(itemStack, this.ticks());
		}
	}
}
