package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class PlayerHeadItem extends StandingAndWallBlockItem {
	public PlayerHeadItem(Block block, Block block2, Item.Properties properties) {
		super(block, block2, Direction.DOWN, properties);
	}

	@Override
	public Component getName(ItemStack itemStack) {
		ResolvableProfile resolvableProfile = itemStack.get(DataComponents.PROFILE);
		return (Component)(resolvableProfile != null && resolvableProfile.name().isPresent()
			? Component.translatable(this.descriptionId + ".named", resolvableProfile.name().get())
			: super.getName(itemStack));
	}

	@Override
	public void verifyComponentsAfterLoad(ItemStack itemStack) {
		ResolvableProfile resolvableProfile = itemStack.get(DataComponents.PROFILE);
		if (resolvableProfile != null && !resolvableProfile.isResolved()) {
			resolvableProfile.resolve()
				.thenAcceptAsync(resolvableProfilex -> itemStack.set(DataComponents.PROFILE, resolvableProfilex), SkullBlockEntity.CHECKED_MAIN_THREAD_EXECUTOR);
		}
	}
}
