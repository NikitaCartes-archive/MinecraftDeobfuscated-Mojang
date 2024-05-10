package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;

public class SuspiciousStewItem extends Item {
	public static final int DEFAULT_DURATION = 160;

	public SuspiciousStewItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
		if (tooltipFlag.isCreative()) {
			List<MobEffectInstance> list2 = new ArrayList();
			SuspiciousStewEffects suspiciousStewEffects = itemStack.getOrDefault(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY);

			for (SuspiciousStewEffects.Entry entry : suspiciousStewEffects.effects()) {
				list2.add(entry.createEffectInstance());
			}

			PotionContents.addPotionTooltip(list2, list::add, 1.0F, tooltipContext.tickRate());
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		SuspiciousStewEffects suspiciousStewEffects = itemStack.getOrDefault(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY);

		for (SuspiciousStewEffects.Entry entry : suspiciousStewEffects.effects()) {
			livingEntity.addEffect(entry.createEffectInstance());
		}

		return super.finishUsingItem(itemStack, level, livingEntity);
	}
}
