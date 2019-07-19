package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface ItemPropertyFunction {
	@Environment(EnvType.CLIENT)
	float call(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity);
}
