package net.minecraft.world.item.alchemy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastColor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record PotionContents(Optional<Holder<Potion>> potion, Optional<Integer> customColor, List<MobEffectInstance> customEffects) {
	public static final PotionContents EMPTY = new PotionContents(Optional.empty(), Optional.empty(), List.of());
	private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);
	private static final int EMPTY_COLOR = 16253176;
	private static final int BASE_POTION_COLOR = 3694022;
	public static final Codec<PotionContents> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(BuiltInRegistries.POTION.holderByNameCodec(), "potion").forGetter(PotionContents::potion),
					ExtraCodecs.strictOptionalField(Codec.INT, "custom_color").forGetter(PotionContents::customColor),
					ExtraCodecs.strictOptionalField(MobEffectInstance.CODEC.listOf(), "custom_effects", List.of()).forGetter(PotionContents::customEffects)
				)
				.apply(instance, PotionContents::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, PotionContents> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.holderRegistry(Registries.POTION).apply(ByteBufCodecs::optional),
		PotionContents::potion,
		ByteBufCodecs.INT.apply(ByteBufCodecs::optional),
		PotionContents::customColor,
		MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list()),
		PotionContents::customEffects,
		PotionContents::new
	);

	public PotionContents(Holder<Potion> holder) {
		this(Optional.of(holder), Optional.empty(), List.of());
	}

	public static ItemStack createItemStack(Item item, Holder<Potion> holder) {
		ItemStack itemStack = new ItemStack(item);
		itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
		return itemStack;
	}

	public boolean is(Holder<Potion> holder) {
		return this.potion.isPresent() && ((Holder)this.potion.get()).is(holder) && this.customEffects.isEmpty();
	}

	public Iterable<MobEffectInstance> getAllEffects() {
		if (this.potion.isEmpty()) {
			return this.customEffects;
		} else {
			return (Iterable<MobEffectInstance>)(this.customEffects.isEmpty()
				? ((Potion)((Holder)this.potion.get()).value()).getEffects()
				: Iterables.concat(((Potion)((Holder)this.potion.get()).value()).getEffects(), this.customEffects));
		}
	}

	public void forEachEffect(Consumer<MobEffectInstance> consumer) {
		if (this.potion.isPresent()) {
			for (MobEffectInstance mobEffectInstance : ((Potion)((Holder)this.potion.get()).value()).getEffects()) {
				consumer.accept(new MobEffectInstance(mobEffectInstance));
			}
		}

		for (MobEffectInstance mobEffectInstance : this.customEffects) {
			consumer.accept(new MobEffectInstance(mobEffectInstance));
		}
	}

	public PotionContents withPotion(Holder<Potion> holder) {
		return new PotionContents(Optional.of(holder), this.customColor, this.customEffects);
	}

	public PotionContents withEffectAdded(MobEffectInstance mobEffectInstance) {
		return new PotionContents(this.potion, this.customColor, Util.copyAndAdd(this.customEffects, mobEffectInstance));
	}

	public int getColor() {
		if (this.customColor.isPresent()) {
			return (Integer)this.customColor.get();
		} else {
			return this.potion.isEmpty() ? 16253176 : getColor(this.getAllEffects());
		}
	}

	public int getColorForArrow() {
		return this.customColor.isPresent() ? (Integer)this.customColor.get() : getColor(this.getAllEffects());
	}

	public static int getColor(Holder<Potion> holder) {
		return getColor(holder.value().getEffects());
	}

	public static int getColor(Iterable<MobEffectInstance> iterable) {
		float f = 0.0F;
		float g = 0.0F;
		float h = 0.0F;
		int i = 0;

		for (MobEffectInstance mobEffectInstance : iterable) {
			if (mobEffectInstance.isVisible()) {
				int j = mobEffectInstance.getEffect().value().getColor();
				int k = mobEffectInstance.getAmplifier() + 1;
				f += (float)(k * FastColor.ARGB32.red(j)) / 255.0F;
				g += (float)(k * FastColor.ARGB32.green(j)) / 255.0F;
				h += (float)(k * FastColor.ARGB32.blue(j)) / 255.0F;
				i += k;
			}
		}

		return i == 0 ? 3694022 : FastColor.ARGB32.color(0, (int)(f / (float)i * 255.0F), (int)(g / (float)i * 255.0F), (int)(h / (float)i * 255.0F));
	}

	public boolean hasEffects() {
		return !this.customEffects.isEmpty() ? true : this.potion.isPresent() && !((Potion)((Holder)this.potion.get()).value()).getEffects().isEmpty();
	}

	public List<MobEffectInstance> customEffects() {
		return Lists.transform(this.customEffects, MobEffectInstance::new);
	}

	public void addPotionTooltip(Consumer<Component> consumer, float f, float g) {
		addPotionTooltip(this.getAllEffects(), consumer, f, g);
	}

	public static void addPotionTooltip(Iterable<MobEffectInstance> iterable, Consumer<Component> consumer, float f, float g) {
		List<Pair<Holder<Attribute>, AttributeModifier>> list = Lists.<Pair<Holder<Attribute>, AttributeModifier>>newArrayList();
		boolean bl = true;

		for (MobEffectInstance mobEffectInstance : iterable) {
			bl = false;
			MutableComponent mutableComponent = Component.translatable(mobEffectInstance.getDescriptionId());
			Holder<MobEffect> holder = mobEffectInstance.getEffect();
			holder.value().createModifiers(mobEffectInstance.getAmplifier(), (holderx, attributeModifierx) -> list.add(new Pair<>(holderx, attributeModifierx)));
			if (mobEffectInstance.getAmplifier() > 0) {
				mutableComponent = Component.translatable(
					"potion.withAmplifier", mutableComponent, Component.translatable("potion.potency." + mobEffectInstance.getAmplifier())
				);
			}

			if (!mobEffectInstance.endsWithin(20)) {
				mutableComponent = Component.translatable("potion.withDuration", mutableComponent, MobEffectUtil.formatDuration(mobEffectInstance, f, g));
			}

			consumer.accept(mutableComponent.withStyle(holder.value().getCategory().getTooltipFormatting()));
		}

		if (bl) {
			consumer.accept(NO_EFFECT);
		}

		if (!list.isEmpty()) {
			consumer.accept(CommonComponents.EMPTY);
			consumer.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

			for (Pair<Holder<Attribute>, AttributeModifier> pair : list) {
				AttributeModifier attributeModifier = pair.getSecond();
				double d = attributeModifier.getAmount();
				double e;
				if (attributeModifier.getOperation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE
					&& attributeModifier.getOperation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
					e = attributeModifier.getAmount();
				} else {
					e = attributeModifier.getAmount() * 100.0;
				}

				if (d > 0.0) {
					consumer.accept(
						Component.translatable(
								"attribute.modifier.plus." + attributeModifier.getOperation().id(),
								ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e),
								Component.translatable(pair.getFirst().value().getDescriptionId())
							)
							.withStyle(ChatFormatting.BLUE)
					);
				} else if (d < 0.0) {
					e *= -1.0;
					consumer.accept(
						Component.translatable(
								"attribute.modifier.take." + attributeModifier.getOperation().id(),
								ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e),
								Component.translatable(pair.getFirst().value().getDescriptionId())
							)
							.withStyle(ChatFormatting.RED)
					);
				}
			}
		}
	}
}
