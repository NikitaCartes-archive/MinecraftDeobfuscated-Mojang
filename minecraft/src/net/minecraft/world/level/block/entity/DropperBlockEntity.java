package net.minecraft.world.level.block.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class DropperBlockEntity extends DispenserBlockEntity {
	public DropperBlockEntity() {
		super(BlockEntityType.DROPPER);
	}

	@Override
	protected Component getDefaultName() {
		return new TranslatableComponent("container.dropper");
	}
}
