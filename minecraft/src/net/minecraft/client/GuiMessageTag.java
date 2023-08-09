package net.minecraft.client;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public record GuiMessageTag(int indicatorColor, @Nullable GuiMessageTag.Icon icon, @Nullable Component text, @Nullable String logTag) {
	private static final Component SYSTEM_TEXT = Component.translatable("chat.tag.system");
	private static final Component SYSTEM_TEXT_SINGLE_PLAYER = Component.translatable("chat.tag.system_single_player");
	private static final Component CHAT_NOT_SECURE_TEXT = Component.translatable("chat.tag.not_secure");
	private static final Component CHAT_MODIFIED_TEXT = Component.translatable("chat.tag.modified");
	private static final Component CHAT_ERROR_TEXT = Component.translatable("chat.tag.error");
	private static final int CHAT_NOT_SECURE_INDICATOR_COLOR = 13684944;
	private static final int CHAT_MODIFIED_INDICATOR_COLOR = 6316128;
	private static final GuiMessageTag SYSTEM = new GuiMessageTag(13684944, null, SYSTEM_TEXT, "System");
	private static final GuiMessageTag SYSTEM_SINGLE_PLAYER = new GuiMessageTag(13684944, null, SYSTEM_TEXT_SINGLE_PLAYER, "System");
	private static final GuiMessageTag CHAT_NOT_SECURE = new GuiMessageTag(13684944, null, CHAT_NOT_SECURE_TEXT, "Not Secure");
	private static final GuiMessageTag CHAT_ERROR = new GuiMessageTag(16733525, null, CHAT_ERROR_TEXT, "Chat Error");

	public static GuiMessageTag system() {
		return SYSTEM;
	}

	public static GuiMessageTag systemSinglePlayer() {
		return SYSTEM_SINGLE_PLAYER;
	}

	public static GuiMessageTag chatNotSecure() {
		return CHAT_NOT_SECURE;
	}

	public static GuiMessageTag chatModified(String string) {
		Component component = Component.literal(string).withStyle(ChatFormatting.GRAY);
		Component component2 = Component.empty().append(CHAT_MODIFIED_TEXT).append(CommonComponents.NEW_LINE).append(component);
		return new GuiMessageTag(6316128, GuiMessageTag.Icon.CHAT_MODIFIED, component2, "Modified");
	}

	public static GuiMessageTag chatError() {
		return CHAT_ERROR;
	}

	@Environment(EnvType.CLIENT)
	public static enum Icon {
		CHAT_MODIFIED(new ResourceLocation("icon/chat_modified"), 9, 9);

		public final ResourceLocation sprite;
		public final int width;
		public final int height;

		private Icon(ResourceLocation resourceLocation, int j, int k) {
			this.sprite = resourceLocation;
			this.width = j;
			this.height = k;
		}

		public void draw(GuiGraphics guiGraphics, int i, int j) {
			guiGraphics.blitSprite(this.sprite, i, j, this.width, this.height);
		}
	}
}
