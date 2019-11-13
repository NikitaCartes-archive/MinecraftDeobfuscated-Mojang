package net.minecraft.world.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SuspiciousStewItem extends Item {
	public SuspiciousStewItem(Item.Properties properties) {
		super(properties);
	}

	public static void saveMobEffect(ItemStack itemStack, MobEffect mobEffect, int i) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		ListTag listTag = compoundTag.getList("Effects", 9);
		CompoundTag compoundTag2 = new CompoundTag();
		compoundTag2.putByte("EffectId", (byte)MobEffect.getId(mobEffect));
		compoundTag2.putInt("EffectDuration", i);
		listTag.add(compoundTag2);
		compoundTag.put("Effects", listTag);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		ItemStack itemStack2 = super.finishUsingItem(itemStack, level, livingEntity);
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && compoundTag.contains("Effects", 9)) {
			ListTag listTag = compoundTag.getList("Effects", 10);

			for (int i = 0; i < listTag.size(); i++) {
				int j = 160;
				CompoundTag compoundTag2 = listTag.getCompound(i);
				if (compoundTag2.contains("EffectDuration", 3)) {
					j = compoundTag2.getInt("EffectDuration");
				}

				MobEffect mobEffect = MobEffect.byId(compoundTag2.getByte("EffectId"));
				if (mobEffect != null) {
					livingEntity.addEffect(new MobEffectInstance(mobEffect, j));
				}
			}
		}

		return livingEntity instanceof Player && ((Player)livingEntity).abilities.instabuild ? itemStack2 : new ItemStack(Items.BOWL);
	}
}
