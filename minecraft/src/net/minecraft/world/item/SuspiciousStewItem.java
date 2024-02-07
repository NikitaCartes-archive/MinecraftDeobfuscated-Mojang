package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

public class SuspiciousStewItem extends Item {
	public static final String EFFECTS_TAG = "effects";
	public static final int DEFAULT_DURATION = 160;

	public SuspiciousStewItem(Item.Properties properties) {
		super(properties);
	}

	public static void saveMobEffects(ItemStack itemStack, List<SuspiciousEffectHolder.EffectEntry> list) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		SuspiciousEffectHolder.EffectEntry.LIST_CODEC.encodeStart(NbtOps.INSTANCE, list).result().ifPresent(tag -> compoundTag.put("effects", tag));
	}

	public static void appendMobEffects(ItemStack itemStack, List<SuspiciousEffectHolder.EffectEntry> list) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		List<SuspiciousEffectHolder.EffectEntry> list2 = new ArrayList();
		listPotionEffects(itemStack, list2::add);
		list2.addAll(list);
		SuspiciousEffectHolder.EffectEntry.LIST_CODEC.encodeStart(NbtOps.INSTANCE, list2).result().ifPresent(tag -> compoundTag.put("effects", tag));
	}

	private static void listPotionEffects(ItemStack itemStack, Consumer<SuspiciousEffectHolder.EffectEntry> consumer) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && compoundTag.contains("effects", 9)) {
			SuspiciousEffectHolder.EffectEntry.LIST_CODEC.parse(NbtOps.INSTANCE, compoundTag.getList("effects", 10)).result().ifPresent(list -> list.forEach(consumer));
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, level, list, tooltipFlag);
		if (tooltipFlag.isCreative()) {
			List<MobEffectInstance> list2 = new ArrayList();
			listPotionEffects(itemStack, effectEntry -> list2.add(effectEntry.createEffectInstance()));
			PotionUtils.addPotionTooltip(list2, list, 1.0F, level == null ? 20.0F : level.tickRateManager().tickrate());
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		ItemStack itemStack2 = super.finishUsingItem(itemStack, level, livingEntity);
		listPotionEffects(itemStack2, effectEntry -> livingEntity.addEffect(effectEntry.createEffectInstance()));
		return livingEntity.hasInfiniteMaterials() ? itemStack2 : new ItemStack(Items.BOWL);
	}
}
