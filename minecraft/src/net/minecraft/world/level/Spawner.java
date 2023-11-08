package net.minecraft.world.level;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public interface Spawner {
	void setEntityId(EntityType<?> entityType, RandomSource randomSource);

	static void appendHoverText(ItemStack itemStack, List<Component> list, String string) {
		Component component = getSpawnEntityDisplayName(itemStack, string);
		if (component != null) {
			list.add(component);
		} else {
			list.add(CommonComponents.EMPTY);
			list.add(Component.translatable("block.minecraft.spawner.desc1").withStyle(ChatFormatting.GRAY));
			list.add(CommonComponents.space().append(Component.translatable("block.minecraft.spawner.desc2").withStyle(ChatFormatting.BLUE)));
		}
	}

	@Nullable
	static Component getSpawnEntityDisplayName(ItemStack itemStack, String string) {
		CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
		if (compoundTag != null) {
			ResourceLocation resourceLocation = getEntityKey(compoundTag, string);
			if (resourceLocation != null) {
				return (Component)BuiltInRegistries.ENTITY_TYPE
					.getOptional(resourceLocation)
					.map(entityType -> Component.translatable(entityType.getDescriptionId()).withStyle(ChatFormatting.GRAY))
					.orElse(null);
			}
		}

		return null;
	}

	@Nullable
	private static ResourceLocation getEntityKey(CompoundTag compoundTag, String string) {
		if (compoundTag.contains(string, 10)) {
			String string2 = compoundTag.getCompound(string).getCompound("entity").getString("id");
			return ResourceLocation.tryParse(string2);
		} else {
			return null;
		}
	}
}
