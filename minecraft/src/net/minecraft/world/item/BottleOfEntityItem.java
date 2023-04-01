package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class BottleOfEntityItem extends Item {
	public static final String ENTITY_TAG = "entityTag";

	public BottleOfEntityItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		livingEntity.gameEvent(GameEvent.DRINK);
		if (!level.isClientSide) {
			transformToEntity(itemStack, level, livingEntity);
			return Items.GLASS_BOTTLE.getDefaultInstance();
		} else {
			return itemStack;
		}
	}

	public static void transformToEntity(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && compoundTag.contains("entityTag", 10)) {
			CompoundTag compoundTag2 = compoundTag.getCompound("entityTag");
			EntityType<?> entityType = level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE).get(new ResourceLocation(compoundTag2.getString("id")));
			if (entityType != null) {
				livingEntity.updateTransform(entityTransformType -> entityTransformType.withEntity(entityType, Optional.ofNullable(compoundTag2)));
			}
		}
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		return 32;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.DRINK;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		return ItemUtils.startUsingInstantly(level, player, interactionHand);
	}

	@Override
	public Component getName(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && compoundTag.contains("entityTag", 10)) {
			CompoundTag compoundTag2 = compoundTag.getCompound("entityTag");
			EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(compoundTag2.getString("id")));
			return Component.translatable("item.minecraft.bottle_of_entity.specific", Component.translatable(entityType.getDescriptionId()));
		} else {
			return super.getName(itemStack);
		}
	}
}
