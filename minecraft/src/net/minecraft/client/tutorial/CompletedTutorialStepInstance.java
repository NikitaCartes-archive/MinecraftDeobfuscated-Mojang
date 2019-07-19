package net.minecraft.client.tutorial;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CompletedTutorialStepInstance implements TutorialStepInstance {
	private final Tutorial tutorial;

	public CompletedTutorialStepInstance(Tutorial tutorial) {
		this.tutorial = tutorial;
	}
}
