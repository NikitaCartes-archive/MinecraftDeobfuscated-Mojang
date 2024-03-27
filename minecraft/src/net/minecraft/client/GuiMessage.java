package net.minecraft.client;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public record GuiMessage(int addedTime, Component content, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag) {
	@Nullable
	public GuiMessageTag.Icon icon() {
		return this.tag != null ? this.tag.icon() : null;
	}

	@Environment(EnvType.CLIENT)
	public static record Line(int addedTime, FormattedCharSequence content, @Nullable GuiMessageTag tag, boolean endOfEntry) {
	}
}
