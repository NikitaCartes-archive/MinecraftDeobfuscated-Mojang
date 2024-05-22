package net.minecraft.client.gui.screens.advancements;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public enum AdvancementWidgetType {
	OBTAINED(
		ResourceLocation.withDefaultNamespace("advancements/box_obtained"),
		ResourceLocation.withDefaultNamespace("advancements/task_frame_obtained"),
		ResourceLocation.withDefaultNamespace("advancements/challenge_frame_obtained"),
		ResourceLocation.withDefaultNamespace("advancements/goal_frame_obtained")
	),
	UNOBTAINED(
		ResourceLocation.withDefaultNamespace("advancements/box_unobtained"),
		ResourceLocation.withDefaultNamespace("advancements/task_frame_unobtained"),
		ResourceLocation.withDefaultNamespace("advancements/challenge_frame_unobtained"),
		ResourceLocation.withDefaultNamespace("advancements/goal_frame_unobtained")
	);

	private final ResourceLocation boxSprite;
	private final ResourceLocation taskFrameSprite;
	private final ResourceLocation challengeFrameSprite;
	private final ResourceLocation goalFrameSprite;

	private AdvancementWidgetType(
		final ResourceLocation resourceLocation,
		final ResourceLocation resourceLocation2,
		final ResourceLocation resourceLocation3,
		final ResourceLocation resourceLocation4
	) {
		this.boxSprite = resourceLocation;
		this.taskFrameSprite = resourceLocation2;
		this.challengeFrameSprite = resourceLocation3;
		this.goalFrameSprite = resourceLocation4;
	}

	public ResourceLocation boxSprite() {
		return this.boxSprite;
	}

	public ResourceLocation frameSprite(AdvancementType advancementType) {
		return switch (advancementType) {
			case TASK -> this.taskFrameSprite;
			case CHALLENGE -> this.challengeFrameSprite;
			case GOAL -> this.goalFrameSprite;
		};
	}
}
