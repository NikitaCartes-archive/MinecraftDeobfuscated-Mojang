package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.tree.CommandNode;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.ClientChatPreview;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.PreviewedArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class ChatScreen extends Screen {
	public static final double MOUSE_SCROLL_SPEED = 7.0;
	private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
	private static final int PREVIEW_MARGIN_SIDES = 2;
	private static final int PREVIEW_PADDING = 2;
	private static final int PREVIEW_MARGIN_BOTTOM = 15;
	private static final Component PREVIEW_WARNING_TITLE = Component.translatable("chatPreview.warning.toast.title");
	private static final Component PREVIEW_WARNING_TOAST = Component.translatable("chatPreview.warning.toast");
	private static final Component PREVIEW_HINT = Component.translatable("chat.preview").withStyle(ChatFormatting.DARK_GRAY);
	private String historyBuffer = "";
	private int historyPos = -1;
	protected EditBox input;
	private String initial;
	CommandSuggestions commandSuggestions;
	private ClientChatPreview chatPreview;

	public ChatScreen(String string) {
		super(Component.translatable("chat_screen.title"));
		this.initial = string;
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
		this.input = new EditBox(this.font, 4, this.height - 12, this.width - 4, 12, Component.translatable("chat.editBox")) {
			@Override
			protected MutableComponent createNarrationMessage() {
				return super.createNarrationMessage().append(ChatScreen.this.commandSuggestions.getNarrationMessage());
			}
		};
		this.input.setMaxLength(256);
		this.input.setBordered(false);
		this.input.setValue(this.initial);
		this.input.setResponder(this::onEdited);
		this.addWidget(this.input);
		this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
		this.commandSuggestions.updateCommandInfo();
		this.setInitialFocus(this.input);
		this.chatPreview = new ClientChatPreview(this.minecraft);
		this.updateChatPreview(this.input.getValue());
		ServerData serverData = this.minecraft.getCurrentServer();
		if (serverData != null && this.minecraft.options.chatPreview().get()) {
			ServerData.ChatPreview chatPreview = serverData.getChatPreview();
			if (chatPreview != null && serverData.previewsChat() && chatPreview.showToast()) {
				ServerList.saveSingleServer(serverData);
				SystemToast systemToast = SystemToast.multiline(
					this.minecraft, SystemToast.SystemToastIds.CHAT_PREVIEW_WARNING, PREVIEW_WARNING_TITLE, PREVIEW_WARNING_TOAST
				);
				this.minecraft.getToasts().addToast(systemToast);
			}
		}
	}

	@Override
	public void resize(Minecraft minecraft, int i, int j) {
		String string = this.input.getValue();
		this.init(minecraft, i, j);
		this.setChatLine(string);
		this.commandSuggestions.updateCommandInfo();
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
		this.minecraft.gui.getChat().resetChatScroll();
	}

	@Override
	public void tick() {
		this.input.tick();
		this.chatPreview.tick();
	}

	private void onEdited(String string) {
		String string2 = this.input.getValue();
		this.commandSuggestions.setAllowSuggestions(!string2.equals(this.initial));
		this.commandSuggestions.updateCommandInfo();
		this.updateChatPreview(string2);
	}

	private void updateChatPreview(String string) {
		String string2 = this.normalizeChatMessage(string);
		if (this.sendsChatPreviewRequests()) {
			this.requestPreview(string2);
		} else {
			this.chatPreview.disable();
		}
	}

	private void requestPreview(String string) {
		if (string.startsWith("/")) {
			this.requestCommandArgumentPreview(string);
		} else {
			this.requestChatMessagePreview(string);
		}
	}

	private void requestChatMessagePreview(String string) {
		this.chatPreview.update(string);
	}

	private void requestCommandArgumentPreview(String string) {
		CommandNode<SharedSuggestionProvider> commandNode = this.commandSuggestions.getNodeAt(this.input.getCursorPosition());
		if (commandNode != null && PreviewedArgument.isPreviewed(commandNode)) {
			this.chatPreview.update(string);
		} else {
			this.chatPreview.disable();
		}
	}

	private boolean sendsChatPreviewRequests() {
		if (this.minecraft.player == null) {
			return false;
		} else if (!this.minecraft.options.chatPreview().get()) {
			return false;
		} else {
			ServerData serverData = this.minecraft.getCurrentServer();
			return serverData != null && serverData.previewsChat();
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (this.commandSuggestions.keyPressed(i, j, k)) {
			return true;
		} else if (super.keyPressed(i, j, k)) {
			return true;
		} else if (i == 256) {
			this.minecraft.setScreen(null);
			return true;
		} else if (i == 257 || i == 335) {
			this.handleChatInput(this.input.getValue(), true);
			this.minecraft.setScreen(null);
			return true;
		} else if (i == 265) {
			this.moveInHistory(-1);
			return true;
		} else if (i == 264) {
			this.moveInHistory(1);
			return true;
		} else if (i == 266) {
			this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
			return true;
		} else if (i == 267) {
			this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		f = Mth.clamp(f, -1.0, 1.0);
		if (this.commandSuggestions.mouseScrolled(f)) {
			return true;
		} else {
			if (!hasShiftDown()) {
				f *= 7.0;
			}

			this.minecraft.gui.getChat().scrollChat((int)f);
			return true;
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.commandSuggestions.mouseClicked((double)((int)d), (double)((int)e), i)) {
			return true;
		} else {
			if (i == 0) {
				ChatComponent chatComponent = this.minecraft.gui.getChat();
				if (chatComponent.handleChatQueueClicked(d, e)) {
					return true;
				}

				Style style = this.getComponentStyleAt(d, e);
				if (style != null && this.handleComponentClicked(style)) {
					this.initial = this.input.getValue();
					return true;
				}
			}

			return this.input.mouseClicked(d, e, i) ? true : super.mouseClicked(d, e, i);
		}
	}

	@Override
	protected void insertText(String string, boolean bl) {
		if (bl) {
			this.input.setValue(string);
		} else {
			this.input.insertText(string);
		}
	}

	public void moveInHistory(int i) {
		int j = this.historyPos + i;
		int k = this.minecraft.gui.getChat().getRecentChat().size();
		j = Mth.clamp(j, 0, k);
		if (j != this.historyPos) {
			if (j == k) {
				this.historyPos = k;
				this.input.setValue(this.historyBuffer);
			} else {
				if (this.historyPos == k) {
					this.historyBuffer = this.input.getValue();
				}

				this.input.setValue((String)this.minecraft.gui.getChat().getRecentChat().get(j));
				this.commandSuggestions.setAllowSuggestions(false);
				this.historyPos = j;
			}
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.setFocused(this.input);
		this.input.setFocus(true);
		fill(poseStack, 2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
		this.input.render(poseStack, i, j, f);
		if (this.chatPreview.isEnabled()) {
			this.renderChatPreview(poseStack);
		} else {
			this.commandSuggestions.render(poseStack, i, j);
		}

		Style style = this.getComponentStyleAt((double)i, (double)j);
		if (style != null && style.getHoverEvent() != null) {
			this.renderComponentHoverEffect(poseStack, style, i, j);
		}

		super.render(poseStack, i, j, f);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void setChatLine(String string) {
		this.input.setValue(string);
	}

	@Override
	protected void updateNarrationState(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.getTitle());
		narrationElementOutput.add(NarratedElementType.USAGE, USAGE_TEXT);
		String string = this.input.getValue();
		if (!string.isEmpty()) {
			narrationElementOutput.nest().add(NarratedElementType.TITLE, Component.translatable("chat_screen.message", string));
		}
	}

	public void renderChatPreview(PoseStack poseStack) {
		int i = (int)(255.0 * (this.minecraft.options.chatOpacity().get() * 0.9F + 0.1F));
		int j = (int)(255.0 * this.minecraft.options.textBackgroundOpacity().get());
		int k = this.chatPreviewWidth();
		List<FormattedCharSequence> list = this.peekChatPreview();
		int l = this.chatPreviewHeight(list);
		RenderSystem.enableBlend();
		poseStack.pushPose();
		poseStack.translate((double)this.chatPreviewLeft(), (double)this.chatPreviewTop(l), 0.0);
		fill(poseStack, 0, 0, k, l, j << 24);
		poseStack.translate(2.0, 2.0, 0.0);

		for (int m = 0; m < list.size(); m++) {
			FormattedCharSequence formattedCharSequence = (FormattedCharSequence)list.get(m);
			this.minecraft.font.drawShadow(poseStack, formattedCharSequence, 0.0F, (float)(m * 9), i << 24 | 16777215);
		}

		poseStack.popPose();
		RenderSystem.disableBlend();
	}

	@Nullable
	private Style getComponentStyleAt(double d, double e) {
		Style style = this.minecraft.gui.getChat().getClickedComponentStyleAt(d, e);
		if (style == null) {
			style = this.getChatPreviewStyleAt(d, e);
		}

		return style;
	}

	@Nullable
	private Style getChatPreviewStyleAt(double d, double e) {
		if (this.minecraft.options.hideGui) {
			return null;
		} else {
			List<FormattedCharSequence> list = this.peekChatPreview();
			int i = this.chatPreviewHeight(list);
			if (!(d < (double)this.chatPreviewLeft())
				&& !(d > (double)this.chatPreviewRight())
				&& !(e < (double)this.chatPreviewTop(i))
				&& !(e > (double)this.chatPreviewBottom())) {
				int j = this.chatPreviewLeft() + 2;
				int k = this.chatPreviewTop(i) + 2;
				int l = (Mth.floor(e) - k) / 9;
				if (l >= 0 && l < list.size()) {
					FormattedCharSequence formattedCharSequence = (FormattedCharSequence)list.get(l);
					return this.minecraft.font.getSplitter().componentStyleAtWidth(formattedCharSequence, (int)(d - (double)j));
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	private List<FormattedCharSequence> peekChatPreview() {
		Component component = this.chatPreview.peek();
		return component != null ? this.font.split(component, this.chatPreviewWidth()) : List.of(PREVIEW_HINT.getVisualOrderText());
	}

	private int chatPreviewWidth() {
		return this.minecraft.screen.width - 4;
	}

	private int chatPreviewHeight(List<FormattedCharSequence> list) {
		return Math.max(list.size(), 1) * 9 + 4;
	}

	private int chatPreviewBottom() {
		return this.minecraft.screen.height - 15;
	}

	private int chatPreviewTop(int i) {
		return this.chatPreviewBottom() - i;
	}

	private int chatPreviewLeft() {
		return 2;
	}

	private int chatPreviewRight() {
		return this.minecraft.screen.width - 2;
	}

	public void handleChatInput(String string, boolean bl) {
		string = this.normalizeChatMessage(string);
		if (!string.isEmpty()) {
			if (bl) {
				this.minecraft.gui.getChat().addRecentChat(string);
			}

			Component component = this.chatPreview.pull(string);
			if (string.startsWith("/")) {
				this.minecraft.player.commandSigned(string.substring(1), component);
			} else {
				this.minecraft.player.chatSigned(string, component);
			}
		}
	}

	public String normalizeChatMessage(String string) {
		return StringUtils.normalizeSpace(string.trim());
	}

	public ClientChatPreview getChatPreview() {
		return this.chatPreview;
	}
}
