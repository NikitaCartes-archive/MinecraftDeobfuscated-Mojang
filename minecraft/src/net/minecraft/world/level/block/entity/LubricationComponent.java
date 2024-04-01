package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public class LubricationComponent implements TooltipProvider {
	public static final Codec<LubricationComponent> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Codec.INT.fieldOf("level").forGetter(lubricationComponent -> lubricationComponent.level))
				.apply(instance, LubricationComponent::new)
	);
	public static final StreamCodec<ByteBuf, LubricationComponent> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
	private final int level;
	private final float lubricationFactor;

	public LubricationComponent(int i) {
		this.level = i;
		this.lubricationFactor = calculateLubricationFactor(i);
	}

	public boolean isLubricated() {
		return this.level >= 1;
	}

	public int getLevel() {
		return this.level;
	}

	@Override
	public void addToTooltip(Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		if (this.isLubricated()) {
			MutableComponent mutableComponent = this.level == 1
				? Component.translatable("lubrication.tooltip.lubricated")
				: Component.translatable("lubrication.tooltip.lubricated_times", this.level);
			consumer.accept(mutableComponent.withStyle(ChatFormatting.GOLD));
		}

		if (tooltipFlag.isAdvanced()) {
			consumer.accept(Component.literal("lubricationFactor: " + this.lubricationFactor).withStyle(ChatFormatting.GRAY));
		}
	}

	private static float calculateLubricationFactor(int i) {
		return i <= 0 ? 0.0F : 1.0F - (float)Math.pow(0.75, (double)i + 6.228262518959627);
	}

	public float applyToFriction(float f) {
		return this.isLubricated() ? 1.0F - (1.0F - f) * (1.0F - this.lubricationFactor) : f;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			LubricationComponent lubricationComponent = (LubricationComponent)object;
			return this.level == lubricationComponent.level;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.level});
	}

	public static void lubricate(ItemStack itemStack) {
		LubricationComponent lubricationComponent = itemStack.get(DataComponents.LUBRICATION);
		if (lubricationComponent != null) {
			itemStack.set(DataComponents.LUBRICATION, new LubricationComponent(lubricationComponent.level + 1));
		} else {
			itemStack.set(DataComponents.LUBRICATION, new LubricationComponent(1));
		}
	}
}
