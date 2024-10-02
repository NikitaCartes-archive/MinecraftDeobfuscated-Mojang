package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record ChangeItemDamage(LevelBasedValue amount) implements EnchantmentEntityEffect {
	public static final MapCodec<ChangeItemDamage> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(LevelBasedValue.CODEC.fieldOf("amount").forGetter(changeItemDamage -> changeItemDamage.amount))
				.apply(instance, ChangeItemDamage::new)
	);

	@Override
	public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
		ItemStack itemStack = enchantedItemInUse.itemStack();
		if (itemStack.has(DataComponents.MAX_DAMAGE) && itemStack.has(DataComponents.DAMAGE)) {
			ServerPlayer serverPlayer2 = enchantedItemInUse.owner() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
			int j = (int)this.amount.calculate(i);
			itemStack.hurtAndBreak(j, serverLevel, serverPlayer2, enchantedItemInUse.onBreak());
		}
	}

	@Override
	public MapCodec<ChangeItemDamage> codec() {
		return CODEC;
	}
}
