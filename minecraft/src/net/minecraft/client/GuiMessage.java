package net.minecraft.client;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public record GuiMessage(int addedTime, Component content, @Nullable GuiMessageTag tag) {
	@Environment(EnvType.CLIENT)
	public static record Line(int addedTime, FormattedCharSequence content, @Nullable GuiMessageTag tag, boolean endOfEntry) {
	}
}
