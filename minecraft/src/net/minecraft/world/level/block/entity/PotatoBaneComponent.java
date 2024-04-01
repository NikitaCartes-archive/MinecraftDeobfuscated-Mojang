package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record PotatoBaneComponent(float damageBoost) implements TooltipProvider {
	public static final Codec<PotatoBaneComponent> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Codec.FLOAT.fieldOf("damage_boost").forGetter(PotatoBaneComponent::damageBoost)).apply(instance, PotatoBaneComponent::new)
	);
	public static final StreamCodec<ByteBuf, PotatoBaneComponent> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

	@Override
	public void addToTooltip(Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		consumer.accept(Component.translatable("potato_bane.tooltip.damage_boost", this.damageBoost).withStyle(ChatFormatting.GREEN));
	}

	public static float getPotatoDamageBoost(ItemStack itemStack, Entity entity) {
		if (entity.isPotato()) {
			PotatoBaneComponent potatoBaneComponent = itemStack.get(DataComponents.POTATO_BANE);
			if (potatoBaneComponent != null) {
				return potatoBaneComponent.damageBoost;
			}
		}

		return 0.0F;
	}
}
