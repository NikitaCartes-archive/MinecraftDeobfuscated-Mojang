package net.minecraft.world.item.alchemy;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public class PotionUtils {
	public static final String TAG_CUSTOM_POTION_EFFECTS = "custom_potion_effects";
	public static final String TAG_CUSTOM_POTION_COLOR = "CustomPotionColor";
	public static final String TAG_POTION = "Potion";
	private static final int EMPTY_COLOR = 16253176;
	private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);

	public static List<MobEffectInstance> getMobEffects(ItemStack itemStack) {
		return getAllEffects(itemStack.getTag());
	}

	public static List<MobEffectInstance> getAllEffects(Holder<Potion> holder, Collection<MobEffectInstance> collection) {
		List<MobEffectInstance> list = new ArrayList();
		list.addAll(holder.value().getEffects());
		list.addAll(collection);
		return list;
	}

	public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag compoundTag) {
		List<MobEffectInstance> list = Lists.<MobEffectInstance>newArrayList();
		list.addAll(getPotion(compoundTag).value().getEffects());
		getCustomEffects(compoundTag, list);
		return list;
	}

	public static List<MobEffectInstance> getCustomEffects(ItemStack itemStack) {
		return getCustomEffects(itemStack.getTag());
	}

	public static List<MobEffectInstance> getCustomEffects(@Nullable CompoundTag compoundTag) {
		List<MobEffectInstance> list = Lists.<MobEffectInstance>newArrayList();
		getCustomEffects(compoundTag, list);
		return list;
	}

	public static void getCustomEffects(@Nullable CompoundTag compoundTag, List<MobEffectInstance> list) {
		if (compoundTag != null && compoundTag.contains("custom_potion_effects", 9)) {
			ListTag listTag = compoundTag.getList("custom_potion_effects", 10);

			for (int i = 0; i < listTag.size(); i++) {
				CompoundTag compoundTag2 = listTag.getCompound(i);
				MobEffectInstance mobEffectInstance = MobEffectInstance.load(compoundTag2);
				if (mobEffectInstance != null) {
					list.add(mobEffectInstance);
				}
			}
		}
	}

	public static int getColor(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && compoundTag.contains("CustomPotionColor", 99)) {
			return compoundTag.getInt("CustomPotionColor");
		} else {
			return getPotion(itemStack).is(Potions.EMPTY) ? 16253176 : getColor(getMobEffects(itemStack));
		}
	}

	public static int getColor(Holder<Potion> holder) {
		return holder.is(Potions.EMPTY) ? 16253176 : getColor(holder.value().getEffects());
	}

	public static int getColor(Collection<MobEffectInstance> collection) {
		int i = 3694022;
		if (collection.isEmpty()) {
			return 3694022;
		} else {
			float f = 0.0F;
			float g = 0.0F;
			float h = 0.0F;
			int j = 0;

			for (MobEffectInstance mobEffectInstance : collection) {
				if (mobEffectInstance.isVisible()) {
					int k = mobEffectInstance.getEffect().value().getColor();
					int l = mobEffectInstance.getAmplifier() + 1;
					f += (float)(l * (k >> 16 & 0xFF)) / 255.0F;
					g += (float)(l * (k >> 8 & 0xFF)) / 255.0F;
					h += (float)(l * (k >> 0 & 0xFF)) / 255.0F;
					j += l;
				}
			}

			if (j == 0) {
				return 0;
			} else {
				f = f / (float)j * 255.0F;
				g = g / (float)j * 255.0F;
				h = h / (float)j * 255.0F;
				return (int)f << 16 | (int)g << 8 | (int)h;
			}
		}
	}

	public static Holder<Potion> getPotion(ItemStack itemStack) {
		return getPotion(itemStack.getTag());
	}

	public static Holder<Potion> getPotion(@Nullable CompoundTag compoundTag) {
		return compoundTag == null ? Potions.EMPTY : Potion.byName(compoundTag.getString("Potion"));
	}

	public static ItemStack setPotion(ItemStack itemStack, Holder<Potion> holder) {
		Optional<ResourceKey<Potion>> optional = holder.unwrapKey();
		if (!optional.isEmpty() && !holder.is(Potions.EMPTY)) {
			itemStack.getOrCreateTag().putString("Potion", ((ResourceKey)optional.get()).location().toString());
		} else {
			itemStack.removeTagKey("Potion");
		}

		return itemStack;
	}

	public static ItemStack setCustomEffects(ItemStack itemStack, Collection<MobEffectInstance> collection) {
		if (collection.isEmpty()) {
			return itemStack;
		} else {
			CompoundTag compoundTag = itemStack.getOrCreateTag();
			ListTag listTag = compoundTag.getList("custom_potion_effects", 9);

			for (MobEffectInstance mobEffectInstance : collection) {
				listTag.add(mobEffectInstance.save());
			}

			compoundTag.put("custom_potion_effects", listTag);
			return itemStack;
		}
	}

	public static void addPotionTooltip(ItemStack itemStack, List<Component> list, float f, float g) {
		addPotionTooltip(getMobEffects(itemStack), list, f, g);
	}

	public static void addPotionTooltip(List<MobEffectInstance> list, List<Component> list2, float f, float g) {
		List<Pair<Holder<Attribute>, AttributeModifier>> list3 = Lists.<Pair<Holder<Attribute>, AttributeModifier>>newArrayList();
		if (list.isEmpty()) {
			list2.add(NO_EFFECT);
		} else {
			for (MobEffectInstance mobEffectInstance : list) {
				MutableComponent mutableComponent = Component.translatable(mobEffectInstance.getDescriptionId());
				Holder<MobEffect> holder = mobEffectInstance.getEffect();
				holder.value().createModifiers(mobEffectInstance.getAmplifier(), (holderx, attributeModifierx) -> list3.add(new Pair<>(holderx, attributeModifierx)));
				if (mobEffectInstance.getAmplifier() > 0) {
					mutableComponent = Component.translatable(
						"potion.withAmplifier", mutableComponent, Component.translatable("potion.potency." + mobEffectInstance.getAmplifier())
					);
				}

				if (!mobEffectInstance.endsWithin(20)) {
					mutableComponent = Component.translatable("potion.withDuration", mutableComponent, MobEffectUtil.formatDuration(mobEffectInstance, f, g));
				}

				list2.add(mutableComponent.withStyle(holder.value().getCategory().getTooltipFormatting()));
			}
		}

		if (!list3.isEmpty()) {
			list2.add(CommonComponents.EMPTY);
			list2.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

			for (Pair<Holder<Attribute>, AttributeModifier> pair : list3) {
				AttributeModifier attributeModifier = pair.getSecond();
				double d = attributeModifier.getAmount();
				double e;
				if (attributeModifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE
					&& attributeModifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
					e = attributeModifier.getAmount();
				} else {
					e = attributeModifier.getAmount() * 100.0;
				}

				if (d > 0.0) {
					list2.add(
						Component.translatable(
								"attribute.modifier.plus." + attributeModifier.getOperation().id(),
								ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(e),
								Component.translatable(pair.getFirst().value().getDescriptionId())
							)
							.withStyle(ChatFormatting.BLUE)
					);
				} else if (d < 0.0) {
					e *= -1.0;
					list2.add(
						Component.translatable(
								"attribute.modifier.take." + attributeModifier.getOperation().id(),
								ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(e),
								Component.translatable(pair.getFirst().value().getDescriptionId())
							)
							.withStyle(ChatFormatting.RED)
					);
				}
			}
		}
	}
}
