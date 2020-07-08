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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
    private static final MutableComponent NO_EFFECT = new TranslatableComponent("effect.none").withStyle(ChatFormatting.GRAY);

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
        if (compoundTag != null && compoundTag.contains("CustomPotionEffects", 9)) {
            ListTag listTag = compoundTag.getList("CustomPotionEffects", 10);
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
        if (compoundTag != null && compoundTag.contains("CustomPotionColor", 99)) {
            return compoundTag.getInt("CustomPotionColor");
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
        return Potion.byName(compoundTag.getString("Potion"));
    }

    public static ItemStack setPotion(ItemStack itemStack, Potion potion) {
        ResourceLocation resourceLocation = Registry.POTION.getKey(potion);
        if (potion == Potions.EMPTY) {
            itemStack.removeTagKey("Potion");
        } else {
            itemStack.getOrCreateTag().putString("Potion", resourceLocation.toString());
        }
        return itemStack;
    }

    public static ItemStack setCustomEffects(ItemStack itemStack, Collection<MobEffectInstance> collection) {
        if (collection.isEmpty()) {
            return itemStack;
        }
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        ListTag listTag = compoundTag.getList("CustomPotionEffects", 9);
        for (MobEffectInstance mobEffectInstance : collection) {
            listTag.add(mobEffectInstance.save(new CompoundTag()));
        }
        compoundTag.put("CustomPotionEffects", listTag);
        return itemStack;
    }

    @Environment(value=EnvType.CLIENT)
    public static void addPotionTooltip(ItemStack itemStack, List<Component> list, float f) {
        List<MobEffectInstance> list2 = PotionUtils.getMobEffects(itemStack);
        ArrayList<Pair<Attribute, AttributeModifier>> list3 = Lists.newArrayList();
        if (list2.isEmpty()) {
            list.add(NO_EFFECT);
        } else {
            for (MobEffectInstance mobEffectInstance : list2) {
                TranslatableComponent mutableComponent = new TranslatableComponent(mobEffectInstance.getDescriptionId());
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
                    mutableComponent = new TranslatableComponent("potion.withAmplifier", mutableComponent, new TranslatableComponent("potion.potency." + mobEffectInstance.getAmplifier()));
                }
                if (mobEffectInstance.getDuration() > 20) {
                    mutableComponent = new TranslatableComponent("potion.withDuration", mutableComponent, MobEffectUtil.formatDuration(mobEffectInstance, f));
                }
                list.add(mutableComponent.withStyle(mobEffect.getCategory().getTooltipFormatting()));
            }
        }
        if (!list3.isEmpty()) {
            list.add(TextComponent.EMPTY);
            list.add(new TranslatableComponent("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
            for (Pair pair : list3) {
                AttributeModifier attributeModifier3 = (AttributeModifier)pair.getSecond();
                double d = attributeModifier3.getAmount();
                double e = attributeModifier3.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE || attributeModifier3.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL ? attributeModifier3.getAmount() * 100.0 : attributeModifier3.getAmount();
                if (d > 0.0) {
                    list.add(new TranslatableComponent("attribute.modifier.plus." + attributeModifier3.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(e), new TranslatableComponent(((Attribute)pair.getFirst()).getDescriptionId())).withStyle(ChatFormatting.BLUE));
                    continue;
                }
                if (!(d < 0.0)) continue;
                list.add(new TranslatableComponent("attribute.modifier.take." + attributeModifier3.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(e *= -1.0), new TranslatableComponent(((Attribute)pair.getFirst()).getDescriptionId())).withStyle(ChatFormatting.RED));
            }
        }
    }
}

