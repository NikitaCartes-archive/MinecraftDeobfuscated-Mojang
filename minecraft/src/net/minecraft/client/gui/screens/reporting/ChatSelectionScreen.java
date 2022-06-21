package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.LoggedChat;
import net.minecraft.client.multiplayer.chat.report.ChatReportBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ChatSelectionScreen extends Screen {
	private static final Component TITLE = Component.translatable("gui.chatSelection.title");
	private static final Component CONTEXT_INFO = Component.translatable("gui.chatSelection.context").withStyle(ChatFormatting.GRAY);
	@Nullable
	private final Screen lastScreen;
	private final ReportingContext reportingContext;
	private Button confirmSelectedButton;
	private MultiLineLabel contextInfoLabel;
	@Nullable
	private ChatSelectionScreen.ChatSelectionList chatSelectionList;
	final ChatReportBuilder report;
	private final Consumer<ChatReportBuilder> onSelected;
	private ChatSelectionLogFiller chatLogFiller;
	@Nullable
	private List<FormattedCharSequence> tooltip;

	public ChatSelectionScreen(
		@Nullable Screen screen, ReportingContext reportingContext, ChatReportBuilder chatReportBuilder, Consumer<ChatReportBuilder> consumer
	) {
		super(TITLE);
		this.lastScreen = screen;
		this.reportingContext = reportingContext;
		this.report = chatReportBuilder.copy();
		this.onSelected = consumer;
	}

	@Override
	protected void init() {
		this.chatLogFiller = new ChatSelectionLogFiller(this.reportingContext.chatLog(), this::canReport);
		this.contextInfoLabel = MultiLineLabel.create(this.font, CONTEXT_INFO, this.width - 16);
		this.chatSelectionList = new ChatSelectionScreen.ChatSelectionList(this.minecraft, (this.contextInfoLabel.getLineCount() + 1) * 9);
		this.chatSelectionList.setRenderBackground(false);
		this.addWidget(this.chatSelectionList);
		this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 32, 150, 20, CommonComponents.GUI_BACK, button -> this.onClose()));
		this.confirmSelectedButton = this.addRenderableWidget(
			new Button(this.width / 2 - 155 + 160, this.height - 32, 150, 20, CommonComponents.GUI_DONE, button -> {
				this.onSelected.accept(this.report);
				this.onClose();
			})
		);
		this.updateConfirmSelectedButton();
		this.extendLog();
		this.chatSelectionList.setScrollAmount((double)this.chatSelectionList.getMaxScroll());
	}

	private boolean canReport(LoggedChat loggedChat) {
		return loggedChat.canReport(this.report.reportedProfileId());
	}

	private void extendLog() {
		int i = this.chatSelectionList.getMaxVisibleEntries();
		this.chatLogFiller.fillNextPage(i, this.chatSelectionList);
	}

	void onReachedScrollTop() {
		this.extendLog();
	}

	void updateConfirmSelectedButton() {
		this.confirmSelectedButton.active = !this.report.reportedMessages().isEmpty();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.chatSelectionList.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 16777215);
		AbuseReportLimits abuseReportLimits = this.reportingContext.sender().reportLimits();
		int k = this.report.reportedMessages().size();
		int l = abuseReportLimits.maxReportedMessageCount();
		Component component = Component.translatable("gui.chatSelection.selected", k, l);
		drawCenteredString(poseStack, this.font, component, this.width / 2, 16 + 9 * 3 / 2, 10526880);
		this.contextInfoLabel.renderCentered(poseStack, this.width / 2, this.chatSelectionList.getFooterTop());
		super.render(poseStack, i, j, f);
		if (this.tooltip != null) {
			this.renderTooltip(poseStack, this.tooltip, i, j);
			this.tooltip = null;
		}
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(super.getNarrationMessage(), CONTEXT_INFO);
	}

	void setTooltip(@Nullable List<FormattedCharSequence> list) {
		this.tooltip = list;
	}

	@Environment(EnvType.CLIENT)
	public class ChatSelectionList extends ObjectSelectionList<ChatSelectionScreen.ChatSelectionList.Entry> implements ChatSelectionLogFiller.Output {
		@Nullable
		private ChatSelectionScreen.ChatSelectionList.Heading previousHeading;

		public ChatSelectionList(Minecraft minecraft, int i) {
			super(minecraft, ChatSelectionScreen.this.width, ChatSelectionScreen.this.height, 40, ChatSelectionScreen.this.height - 40 - i, 16);
		}

		@Override
		public void setScrollAmount(double d) {
			double e = this.getScrollAmount();
			super.setScrollAmount(d);
			if ((float)this.getMaxScroll() > 1.0E-5F && d <= 1.0E-5F && !Mth.equal(d, e)) {
				ChatSelectionScreen.this.onReachedScrollTop();
			}
		}

		@Override
		public void acceptMessage(int i, LoggedChat loggedChat) {
			Component component = loggedChat.toContentComponent();
			Component component2 = loggedChat.toNarrationComponent();
			boolean bl = loggedChat.canReport(ChatSelectionScreen.this.report.reportedProfileId());
			if (loggedChat instanceof LoggedChat.Player player) {
				ChatSelectionScreen.ChatSelectionList.Entry entry = new ChatSelectionScreen.ChatSelectionList.MessageEntry(i, component, component2, bl, true);
				this.addEntryToTop(entry);
				if (ChatSelectionScreen.this.report.isReported(i)) {
					this.setSelected(entry);
				}

				this.updateHeading(player, bl);
			} else {
				this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.MessageEntry(i, component, component2, bl, false));
				this.previousHeading = null;
			}
		}

		private void updateHeading(LoggedChat.Player player, boolean bl) {
			ChatSelectionScreen.ChatSelectionList.Entry entry = new ChatSelectionScreen.ChatSelectionList.MessageHeadingEntry(
				player.profile(), player.toHeadingComponent(), bl
			);
			this.addEntryToTop(entry);
			ChatSelectionScreen.ChatSelectionList.Heading heading = new ChatSelectionScreen.ChatSelectionList.Heading(player.profileId(), entry);
			if (this.previousHeading != null && this.previousHeading.canCombine(heading)) {
				this.removeEntryFromTop(this.previousHeading.entry());
			}

			this.previousHeading = heading;
		}

		@Override
		public void acceptDivider(Component component) {
			this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.PaddingEntry());
			this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.DividerEntry(component));
			this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.PaddingEntry());
			this.previousHeading = null;
		}

		@Override
		protected int getScrollbarPosition() {
			return (this.width + this.getRowWidth()) / 2;
		}

		@Override
		public int getRowWidth() {
			return Math.min(350, this.width - 50);
		}

		public int getMaxVisibleEntries() {
			return Mth.positiveCeilDiv(this.y1 - this.y0, this.itemHeight);
		}

		@Override
		protected void renderItem(PoseStack poseStack, int i, int j, float f, int k, int l, int m, int n, int o) {
			ChatSelectionScreen.ChatSelectionList.Entry entry = this.getEntry(k);
			if (entry.isSelected()) {
				int p = this.isFocused() ? -1 : -8355712;
				this.renderSelection(poseStack, m, n, o, p, -16777216);
			} else if (entry == this.getSelected()) {
				this.renderSelection(poseStack, m, n, o, -16777216, -16777216);
			}

			boolean bl = entry == this.getHovered();
			entry.render(poseStack, k, m, l, n, o, i, j, bl, f);
		}

		@Override
		protected void moveSelection(AbstractSelectionList.SelectionDirection selectionDirection) {
			if (!this.moveSelectableSelection(selectionDirection) && selectionDirection == AbstractSelectionList.SelectionDirection.UP) {
				ChatSelectionScreen.this.onReachedScrollTop();
				this.moveSelectableSelection(selectionDirection);
			}
		}

		private boolean moveSelectableSelection(AbstractSelectionList.SelectionDirection selectionDirection) {
			return this.moveSelection(selectionDirection, ChatSelectionScreen.ChatSelectionList.Entry::canSelect);
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			ChatSelectionScreen.ChatSelectionList.Entry entry = this.getSelected();
			return entry != null && entry.keyPressed(i, j, k) ? true : super.keyPressed(i, j, k);
		}

		public int getFooterTop() {
			return this.y1 + 9;
		}

		@Environment(EnvType.CLIENT)
		public class DividerEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
			private static final int COLOR = -6250336;
			private final Component text;

			public DividerEntry(Component component) {
				this.text = component;
			}

			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				int p = j + m / 2;
				int q = k + l - 8;
				int r = ChatSelectionScreen.this.font.width(this.text);
				int s = (k + q - r) / 2;
				int t = p - 9 / 2;
				GuiComponent.drawString(poseStack, ChatSelectionScreen.this.font, this.text, s, t, -6250336);
			}

			@Override
			public Component getNarration() {
				return this.text;
			}
		}

		@Environment(EnvType.CLIENT)
		public abstract class Entry extends ObjectSelectionList.Entry<ChatSelectionScreen.ChatSelectionList.Entry> {
			@Override
			public Component getNarration() {
				return CommonComponents.EMPTY;
			}

			public boolean isSelected() {
				return false;
			}

			public boolean canSelect() {
				return false;
			}
		}

		@Environment(EnvType.CLIENT)
		static record Heading(UUID sender, ChatSelectionScreen.ChatSelectionList.Entry entry) {
			public boolean canCombine(ChatSelectionScreen.ChatSelectionList.Heading heading) {
				return heading.sender.equals(this.sender);
			}
		}

		@Environment(EnvType.CLIENT)
		public class MessageEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
			private static final int INDENT_AMOUNT = 8;
			private final int chatId;
			private final FormattedText text;
			private final Component narration;
			@Nullable
			private final List<FormattedCharSequence> hoverText;
			private final boolean canReport;
			private final boolean playerMessage;

			public MessageEntry(int i, Component component, Component component2, boolean bl, boolean bl2) {
				this.chatId = i;
				FormattedText formattedText = ChatSelectionScreen.this.font
					.substrByWidth(component, this.getMaximumTextWidth() - ChatSelectionScreen.this.font.width(CommonComponents.ELLIPSIS));
				if (component != formattedText) {
					this.text = FormattedText.composite(formattedText, CommonComponents.ELLIPSIS);
					this.hoverText = ChatSelectionScreen.this.font.split(component, ChatSelectionList.this.getRowWidth());
				} else {
					this.text = component;
					this.hoverText = null;
				}

				this.narration = component2;
				this.canReport = bl;
				this.playerMessage = bl2;
			}

			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				if (bl && this.canReport) {
					GuiComponent.fill(poseStack, k - 1, j - 1, k + l - 3, j + m + 1, -16777216);
				}

				int p = k + this.getTextIndent();
				int q = j + 1 + (m - 9) / 2;
				GuiComponent.drawString(poseStack, ChatSelectionScreen.this.font, Language.getInstance().getVisualOrder(this.text), p, q, this.canReport ? -1 : -1593835521);
				if (this.hoverText != null && bl) {
					ChatSelectionScreen.this.setTooltip(this.hoverText);
				}
			}

			private int getMaximumTextWidth() {
				return ChatSelectionList.this.getRowWidth() - this.getTextIndent();
			}

			private int getTextIndent() {
				return this.playerMessage ? 8 : 0;
			}

			@Override
			public Component getNarration() {
				return (Component)(this.isSelected() ? Component.translatable("narrator.select", this.narration) : this.narration);
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				return i == 0 ? this.toggleReport() : false;
			}

			@Override
			public boolean keyPressed(int i, int j, int k) {
				return i != 257 && i != 32 && i != 335 ? false : this.toggleReport();
			}

			@Override
			public boolean isSelected() {
				return ChatSelectionScreen.this.report.isReported(this.chatId);
			}

			@Override
			public boolean canSelect() {
				return true;
			}

			private boolean toggleReport() {
				if (this.canReport) {
					ChatSelectionScreen.this.report.toggleReported(this.chatId);
					ChatSelectionScreen.this.updateConfirmSelectedButton();
					return true;
				} else {
					return false;
				}
			}
		}

		@Environment(EnvType.CLIENT)
		public class MessageHeadingEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
			private static final int FACE_SIZE = 12;
			private final Component heading;
			private final ResourceLocation skin;
			private final boolean canReport;

			public MessageHeadingEntry(GameProfile gameProfile, Component component, boolean bl) {
				this.heading = component;
				this.canReport = bl;
				this.skin = this.getSkinLocation(gameProfile);
			}

			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				int p = k - 12 - 4;
				int q = j + (m - 12) / 2;
				this.renderFace(poseStack, p, q, this.skin);
				int r = j + 1 + (m - 9) / 2;
				GuiComponent.drawString(poseStack, ChatSelectionScreen.this.font, this.heading, k, r, this.canReport ? -1 : -1593835521);
			}

			private void renderFace(PoseStack poseStack, int i, int j, ResourceLocation resourceLocation) {
				RenderSystem.setShaderTexture(0, resourceLocation);
				PlayerFaceRenderer.draw(poseStack, i, j, 12);
			}

			private ResourceLocation getSkinLocation(GameProfile gameProfile) {
				SkinManager skinManager = ChatSelectionList.this.minecraft.getSkinManager();
				MinecraftProfileTexture minecraftProfileTexture = (MinecraftProfileTexture)skinManager.getInsecureSkinInformation(gameProfile).get(Type.SKIN);
				return minecraftProfileTexture != null
					? skinManager.registerTexture(minecraftProfileTexture, Type.SKIN)
					: DefaultPlayerSkin.getDefaultSkin(UUIDUtil.getOrCreatePlayerUUID(gameProfile));
			}
		}

		@Environment(EnvType.CLIENT)
		public class PaddingEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			}
		}
	}
}
