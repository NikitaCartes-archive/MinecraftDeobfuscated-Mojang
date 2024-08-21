package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

public record SuspiciousStewEffects(List<SuspiciousStewEffects.Entry> effects) implements ConsumableListener, TooltipProvider {
	public static final SuspiciousStewEffects EMPTY = new SuspiciousStewEffects(List.of());
	public static final int DEFAULT_DURATION = 160;
	public static final Codec<SuspiciousStewEffects> CODEC = SuspiciousStewEffects.Entry.CODEC
		.listOf()
		.xmap(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);
	public static final StreamCodec<RegistryFriendlyByteBuf, SuspiciousStewEffects> STREAM_CODEC = SuspiciousStewEffects.Entry.STREAM_CODEC
		.apply(ByteBufCodecs.list())
		.map(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);

	public SuspiciousStewEffects withEffectAdded(SuspiciousStewEffects.Entry entry) {
		return new SuspiciousStewEffects(Util.copyAndAdd(this.effects, entry));
	}

	@Override
	public void onConsume(Level level, LivingEntity livingEntity, ItemStack itemStack, Consumable consumable) {
		for (SuspiciousStewEffects.Entry entry : this.effects) {
			livingEntity.addEffect(entry.createEffectInstance());
		}
	}

	@Override
	public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		if (tooltipFlag.isCreative()) {
			List<MobEffectInstance> list = new ArrayList();

			for (SuspiciousStewEffects.Entry entry : this.effects) {
				list.add(entry.createEffectInstance());
			}

			PotionContents.addPotionTooltip(list, consumer, 1.0F, tooltipContext.tickRate());
		}
	}

	public static record Entry(Holder<MobEffect> effect, int duration) {
		public static final Codec<SuspiciousStewEffects.Entry> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						MobEffect.CODEC.fieldOf("id").forGetter(SuspiciousStewEffects.Entry::effect),
						Codec.INT.lenientOptionalFieldOf("duration", Integer.valueOf(160)).forGetter(SuspiciousStewEffects.Entry::duration)
					)
					.apply(instance, SuspiciousStewEffects.Entry::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, SuspiciousStewEffects.Entry> STREAM_CODEC = StreamCodec.composite(
			MobEffect.STREAM_CODEC, SuspiciousStewEffects.Entry::effect, ByteBufCodecs.VAR_INT, SuspiciousStewEffects.Entry::duration, SuspiciousStewEffects.Entry::new
		);

		public MobEffectInstance createEffectInstance() {
			return new MobEffectInstance(this.effect, this.duration);
		}
	}
}
