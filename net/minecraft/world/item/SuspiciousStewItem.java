/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SuspiciousStewItem
extends Item {
    public static final String EFFECTS_TAG = "Effects";
    public static final String EFFECT_ID_TAG = "EffectId";
    public static final String EFFECT_DURATION_TAG = "EffectDuration";
    public static final int DEFAULT_DURATION = 160;

    public SuspiciousStewItem(Item.Properties properties) {
        super(properties);
    }

    public static void saveMobEffect(ItemStack itemStack, MobEffect mobEffect, int i) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        ListTag listTag = compoundTag.getList(EFFECTS_TAG, 9);
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.putInt(EFFECT_ID_TAG, MobEffect.getId(mobEffect));
        compoundTag2.putInt(EFFECT_DURATION_TAG, i);
        listTag.add(compoundTag2);
        compoundTag.put(EFFECTS_TAG, listTag);
    }

    private static void listPotionEffects(ItemStack itemStack, Consumer<MobEffectInstance> consumer) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null && compoundTag.contains(EFFECTS_TAG, 9)) {
            ListTag listTag = compoundTag.getList(EFFECTS_TAG, 10);
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag2 = listTag.getCompound(i);
                int j = compoundTag2.contains(EFFECT_DURATION_TAG, 3) ? compoundTag2.getInt(EFFECT_DURATION_TAG) : 160;
                MobEffect mobEffect = MobEffect.byId(compoundTag2.getInt(EFFECT_ID_TAG));
                if (mobEffect == null) continue;
                consumer.accept(new MobEffectInstance(mobEffect, j));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        if (tooltipFlag.isCreative()) {
            ArrayList<MobEffectInstance> list2 = new ArrayList<MobEffectInstance>();
            SuspiciousStewItem.listPotionEffects(itemStack, list2::add);
            PotionUtils.addPotionTooltip(list2, list, 1.0f);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        ItemStack itemStack2 = super.finishUsingItem(itemStack, level, livingEntity);
        SuspiciousStewItem.listPotionEffects(itemStack2, livingEntity::addEffect);
        if (livingEntity instanceof Player && ((Player)livingEntity).getAbilities().instabuild) {
            return itemStack2;
        }
        return new ItemStack(Items.BOWL);
    }
}

