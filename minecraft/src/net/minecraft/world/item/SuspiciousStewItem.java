package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

public class SuspiciousStewItem extends Item {
	public static final String EFFECTS_TAG = "Effects";
	public static final String EFFECT_ID_TAG = "EffectId";
	public static final String EFFECT_DURATION_TAG = "EffectDuration";
	public static final int DEFAULT_DURATION = 160;

	public SuspiciousStewItem(Item.Properties properties) {
		super(properties);
	}

	public static void saveMobEffect(ItemStack itemStack, MobEffect mobEffect, int i) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		ListTag listTag = compoundTag.getList("Effects", 9);
		CompoundTag compoundTag2 = new CompoundTag();
		compoundTag2.putInt("EffectId", MobEffect.getId(mobEffect));
		compoundTag2.putInt("EffectDuration", i);
		listTag.add(compoundTag2);
		compoundTag.put("Effects", listTag);
	}

	private static void listPotionEffects(ItemStack itemStack, Consumer<MobEffectInstance> consumer) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && compoundTag.contains("Effects", 9)) {
			ListTag listTag = compoundTag.getList("Effects", 10);

			for (int i = 0; i < listTag.size(); i++) {
				CompoundTag compoundTag2 = listTag.getCompound(i);
				int j;
				if (compoundTag2.contains("EffectDuration", 3)) {
					j = compoundTag2.getInt("EffectDuration");
				} else {
					j = 160;
				}

				MobEffect mobEffect = MobEffect.byId(compoundTag2.getInt("EffectId"));
				if (mobEffect != null) {
					consumer.accept(new MobEffectInstance(mobEffect, j));
				}
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, level, list, tooltipFlag);
		if (tooltipFlag.isCreative()) {
			List<MobEffectInstance> list2 = new ArrayList();
			listPotionEffects(itemStack, list2::add);
			PotionUtils.addPotionTooltip(list2, list, 1.0F);
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		ItemStack itemStack2 = super.finishUsingItem(itemStack, level, livingEntity);
		listPotionEffects(itemStack2, livingEntity::addEffect);
		return livingEntity instanceof Player && ((Player)livingEntity).getAbilities().instabuild ? itemStack2 : new ItemStack(Items.BOWL);
	}
}
