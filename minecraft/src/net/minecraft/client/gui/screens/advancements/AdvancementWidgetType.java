package net.minecraft.client.gui.screens.advancements;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.FrameType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public enum AdvancementWidgetType {
	OBTAINED(
		new ResourceLocation("advancements/box_obtained"),
		new ResourceLocation("advancements/task_frame_obtained"),
		new ResourceLocation("advancements/challenge_frame_obtained"),
		new ResourceLocation("advancements/goal_frame_obtained")
	),
	UNOBTAINED(
		new ResourceLocation("advancements/box_unobtained"),
		new ResourceLocation("advancements/task_frame_unobtained"),
		new ResourceLocation("advancements/challenge_frame_unobtained"),
		new ResourceLocation("advancements/goal_frame_unobtained")
	);

	private final ResourceLocation boxSprite;
	private final ResourceLocation taskFrameSprite;
	private final ResourceLocation challengeFrameSprite;
	private final ResourceLocation goalFrameSprite;

	private AdvancementWidgetType(
		ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3, ResourceLocation resourceLocation4
	) {
		this.boxSprite = resourceLocation;
		this.taskFrameSprite = resourceLocation2;
		this.challengeFrameSprite = resourceLocation3;
		this.goalFrameSprite = resourceLocation4;
	}

	public ResourceLocation boxSprite() {
		return this.boxSprite;
	}

	public ResourceLocation frameSprite(FrameType frameType) {
		return switch (frameType) {
			case TASK -> this.taskFrameSprite;
			case CHALLENGE -> this.challengeFrameSprite;
			case GOAL -> this.goalFrameSprite;
		};
	}
}
