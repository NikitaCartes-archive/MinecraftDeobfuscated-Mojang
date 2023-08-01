package net.minecraft.advancements;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum FrameType {
	TASK("task", ChatFormatting.GREEN),
	CHALLENGE("challenge", ChatFormatting.DARK_PURPLE),
	GOAL("goal", ChatFormatting.GREEN);

	private final String name;
	private final ChatFormatting chatColor;
	private final Component displayName;

	private FrameType(String string2, ChatFormatting chatFormatting) {
		this.name = string2;
		this.chatColor = chatFormatting;
		this.displayName = Component.translatable("advancements.toast." + string2);
	}

	public String getName() {
		return this.name;
	}

	public static FrameType byName(String string) {
		for (FrameType frameType : values()) {
			if (frameType.name.equals(string)) {
				return frameType;
			}
		}

		throw new IllegalArgumentException("Unknown frame type '" + string + "'");
	}

	public ChatFormatting getChatColor() {
		return this.chatColor;
	}

	public Component getDisplayName() {
		return this.displayName;
	}
}
