/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.alchemy;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.Nullable;

public class PotionUtils {
    public static final String TAG_CUSTOM_POTION_EFFECTS = "CustomPotionEffects";
    public static final String TAG_CUSTOM_POTION_COLOR = "CustomPotionColor";
    public static final String TAG_POTION = "Potion";
    private static final int EMPTY_COLOR = 0xF800F8;
    private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);

    public static List<MobEffectInstance> getMobEffects(ItemStack itemStack) {
        return PotionUtils.getAllEffects(itemStack.getTag());
    }

    public static List<MobEffectInstance> getAllEffects(Potion potion, Collection<MobEffectInstance> collection) {
        ArrayList<MobEffectInstance> list = Lists.newArrayList();
        list.addAll(potion.getEffects());
        list.addAll(collection);
        return list;
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag compoundTag) {
        ArrayList<MobEffectInstance> list = Lists.newArrayList();
        list.addAll(PotionUtils.getPotion(compoundTag).getEffects());
        PotionUtils.getCustomEffects(compoundTag, list);
        return list;
    }

    public static List<MobEffectInstance> getCustomEffects(ItemStack itemStack) {
        return PotionUtils.getCustomEffects(itemStack.getTag());
    }

    public static List<MobEffectInstance> getCustomEffects(@Nullable CompoundTag compoundTag) {
        ArrayList<MobEffectInstance> list = Lists.newArrayList();
        PotionUtils.getCustomEffects(compoundTag, list);
        return list;
    }

    public static void getCustomEffects(@Nullable CompoundTag compoundTag, List<MobEffectInstance> list) {
        if (compoundTag != null && compoundTag.contains(TAG_CUSTOM_POTION_EFFECTS, 9)) {
            ListTag listTag = compoundTag.getList(TAG_CUSTOM_POTION_EFFECTS, 10);
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag2 = listTag.getCompound(i);
                MobEffectInstance mobEffectInstance = MobEffectInstance.load(compoundTag2);
                if (mobEffectInstance == null) continue;
                list.add(mobEffectInstance);
            }
        }
    }

    public static int getColor(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null && compoundTag.contains(TAG_CUSTOM_POTION_COLOR, 99)) {
            return compoundTag.getInt(TAG_CUSTOM_POTION_COLOR);
        }
        return PotionUtils.getPotion(itemStack) == Potions.EMPTY ? 0xF800F8 : PotionUtils.getColor(PotionUtils.getMobEffects(itemStack));
    }

    public static int getColor(Potion potion) {
        return potion == Potions.EMPTY ? 0xF800F8 : PotionUtils.getColor(potion.getEffects());
    }

    public static int getColor(Collection<MobEffectInstance> collection) {
        int i = 3694022;
        if (collection.isEmpty()) {
            return 3694022;
        }
        float f = 0.0f;
        float g = 0.0f;
        float h = 0.0f;
        int j = 0;
        for (MobEffectInstance mobEffectInstance : collection) {
            if (!mobEffectInstance.isVisible()) continue;
            int k = mobEffectInstance.getEffect().getColor();
            int l = mobEffectInstance.getAmplifier() + 1;
            f += (float)(l * (k >> 16 & 0xFF)) / 255.0f;
            g += (float)(l * (k >> 8 & 0xFF)) / 255.0f;
            h += (float)(l * (k >> 0 & 0xFF)) / 255.0f;
            j += l;
        }
        if (j == 0) {
            return 0;
        }
        f = f / (float)j * 255.0f;
        g = g / (float)j * 255.0f;
        h = h / (float)j * 255.0f;
        return (int)f << 16 | (int)g << 8 | (int)h;
    }

    public static Potion getPotion(ItemStack itemStack) {
        return PotionUtils.getPotion(itemStack.getTag());
    }

    public static Potion getPotion(@Nullable CompoundTag compoundTag) {
        if (compoundTag == null) {
            return Potions.EMPTY;
        }
        return Potion.byName(compoundTag.getString(TAG_POTION));
    }

    public static ItemStack setPotion(ItemStack itemStack, Potion potion) {
        ResourceLocation resourceLocation = BuiltInRegistries.POTION.getKey(potion);
        if (potion == Potions.EMPTY) {
            itemStack.removeTagKey(TAG_POTION);
        } else {
            itemStack.getOrCreateTag().putString(TAG_POTION, resourceLocation.toString());
        }
        return itemStack;
    }

    public static ItemStack setCustomEffects(ItemStack itemStack, Collection<MobEffectInstance> collection) {
        if (collection.isEmpty()) {
            return itemStack;
        }
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        ListTag listTag = compoundTag.getList(TAG_CUSTOM_POTION_EFFECTS, 9);
        for (MobEffectInstance mobEffectInstance : collection) {
            listTag.add(mobEffectInstance.save(new CompoundTag()));
        }
        compoundTag.put(TAG_CUSTOM_POTION_EFFECTS, listTag);
        return itemStack;
    }

    public static void addPotionTooltip(ItemStack itemStack, List<Component> list, float f) {
        PotionUtils.addPotionTooltip(PotionUtils.getMobEffects(itemStack), list, f);
    }

    public static void addPotionTooltip(List<MobEffectInstance> list, List<Component> list2, float f) {
        ArrayList<Pair<Attribute, AttributeModifier>> list3 = Lists.newArrayList();
        if (list.isEmpty()) {
            list2.add(NO_EFFECT);
        } else {
            for (MobEffectInstance mobEffectInstance : list) {
                MutableComponent mutableComponent = Component.translatable(mobEffectInstance.getDescriptionId());
                MobEffect mobEffect = mobEffectInstance.getEffect();
                Map<Attribute, AttributeModifier> map = mobEffect.getAttributeModifiers();
                if (!map.isEmpty()) {
                    for (Map.Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                        AttributeModifier attributeModifier = entry.getValue();
                        AttributeModifier attributeModifier2 = new AttributeModifier(attributeModifier.getName(), mobEffect.getAttributeModifierValue(mobEffectInstance.getAmplifier(), attributeModifier), attributeModifier.getOperation());
                        list3.add(new Pair<Attribute, AttributeModifier>(entry.getKey(), attributeModifier2));
                    }
                }
                if (mobEffectInstance.getAmplifier() > 0) {
                    mutableComponent = Component.translatable("potion.withAmplifier", mutableComponent, Component.translatable("potion.potency." + mobEffectInstance.getAmplifier()));
                }
                if (!mobEffectInstance.endsWithin(20)) {
                    mutableComponent = Component.translatable("potion.withDuration", mutableComponent, MobEffectUtil.formatDuration(mobEffectInstance, f));
                }
                list2.add(mutableComponent.withStyle(mobEffect.getCategory().getTooltipFormatting()));
            }
        }
        if (!list3.isEmpty()) {
            list2.add(CommonComponents.EMPTY);
            list2.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
            for (Pair pair : list3) {
                AttributeModifier attributeModifier3 = (AttributeModifier)pair.getSecond();
                double d = attributeModifier3.getAmount();
                double e = attributeModifier3.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE || attributeModifier3.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL ? attributeModifier3.getAmount() * 100.0 : attributeModifier3.getAmount();
                if (d > 0.0) {
                    list2.add(Component.translatable("attribute.modifier.plus." + attributeModifier3.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(e), Component.translatable(((Attribute)pair.getFirst()).getDescriptionId())).withStyle(ChatFormatting.BLUE));
                    continue;
                }
                if (!(d < 0.0)) continue;
                list2.add(Component.translatable("attribute.modifier.take." + attributeModifier3.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(e *= -1.0), Component.translatable(((Attribute)pair.getFirst()).getDescriptionId())).withStyle(ChatFormatting.RED));
            }
        }
    }
}

