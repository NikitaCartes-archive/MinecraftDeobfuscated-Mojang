package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.TooltipFlag;

public record Fireworks(int flightDuration, List<FireworkExplosion> explosions) implements TooltipProvider {
	public static final int MAX_EXPLOSIONS = 256;
	public static final Codec<Fireworks> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("flight_duration", 0).forGetter(Fireworks::flightDuration),
					FireworkExplosion.CODEC.sizeLimitedListOf(256).optionalFieldOf("explosions", List.of()).forGetter(Fireworks::explosions)
				)
				.apply(instance, Fireworks::new)
	);
	public static final StreamCodec<ByteBuf, Fireworks> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, Fireworks::flightDuration, FireworkExplosion.STREAM_CODEC.apply(ByteBufCodecs.list(256)), Fireworks::explosions, Fireworks::new
	);

	@Override
	public void addToTooltip(Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		if (this.flightDuration > 0) {
			consumer.accept(
				Component.translatable("item.minecraft.firework_rocket.flight")
					.append(CommonComponents.SPACE)
					.append(String.valueOf(this.flightDuration))
					.withStyle(ChatFormatting.GRAY)
			);
		}

		for(FireworkExplosion fireworkExplosion : this.explosions) {
			fireworkExplosion.addShapeNameTooltip(consumer);
			fireworkExplosion.addAdditionalTooltip(component -> consumer.accept(Component.literal("  ").append(component)));
		}
	}
}
