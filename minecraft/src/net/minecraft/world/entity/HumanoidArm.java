package net.minecraft.world.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum HumanoidArm {
	LEFT(new TranslatableComponent("options.mainHand.left")),
	RIGHT(new TranslatableComponent("options.mainHand.right"));

	private final Component name;

	private HumanoidArm(Component component) {
		this.name = component;
	}

	@Environment(EnvType.CLIENT)
	public HumanoidArm getOpposite() {
		return this == LEFT ? RIGHT : LEFT;
	}

	public String toString() {
		return this.name.getString();
	}
}
