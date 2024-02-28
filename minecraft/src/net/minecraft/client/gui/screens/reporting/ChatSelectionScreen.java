package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Optionull;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReport;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ChatSelectionScreen extends Screen {
	static final ResourceLocation CHECKMARK_SPRITE = new ResourceLocation("icon/checkmark");
	private static final Component TITLE = Component.translatable("gui.chatSelection.title");
	private static final Component CONTEXT_INFO = Component.translatable("gui.chatSelection.context");
	@Nullable
	private final Screen lastScreen;
	private final ReportingContext reportingContext;
	private Button confirmSelectedButton;
	private MultiLineLabel contextInfoLabel;
	@Nullable
	private ChatSelectionScreen.ChatSelectionList chatSelectionList;
	final ChatReport.Builder report;
	private final Consumer<ChatReport.Builder> onSelected;
	private ChatSelectionLogFiller chatLogFiller;

	public ChatSelectionScreen(@Nullable Screen screen, ReportingContext reportingContext, ChatReport.Builder builder, Consumer<ChatReport.Builder> consumer) {
		super(TITLE);
		this.lastScreen = screen;
		this.reportingContext = reportingContext;
		this.report = builder.copy();
		this.onSelected = consumer;
	}

	@Override
	protected void init() {
		this.chatLogFiller = new ChatSelectionLogFiller(this.reportingContext, this::canReport);
		this.contextInfoLabel = MultiLineLabel.create(this.font, CONTEXT_INFO, this.width - 16);
		this.chatSelectionList = this.addRenderableWidget(new ChatSelectionScreen.ChatSelectionList(this.minecraft, (this.contextInfoLabel.getLineCount() + 1) * 9));
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).bounds(this.width / 2 - 155, this.height - 32, 150, 20).build());
		this.confirmSelectedButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
			this.onSelected.accept(this.report);
			this.onClose();
		}).bounds(this.width / 2 - 155 + 160, this.height - 32, 150, 20).build());
		this.updateConfirmSelectedButton();
		this.extendLog();
		this.chatSelectionList.setScrollAmount((double)this.chatSelectionList.getMaxScroll());
	}

	private boolean canReport(LoggedChatMessage loggedChatMessage) {
		return loggedChatMessage.canReport(this.report.reportedProfileId());
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
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 16777215);
		AbuseReportLimits abuseReportLimits = this.reportingContext.sender().reportLimits();
		int k = this.report.reportedMessages().size();
		int l = abuseReportLimits.maxReportedMessageCount();
		Component component = Component.translatable("gui.chatSelection.selected", k, l);
		guiGraphics.drawCenteredString(this.font, component, this.width / 2, 16 + 9 * 3 / 2, -1);
		this.contextInfoLabel.renderCentered(guiGraphics, this.width / 2, this.chatSelectionList.getFooterTop());
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(super.getNarrationMessage(), CONTEXT_INFO);
	}

	@Environment(EnvType.CLIENT)
	public class ChatSelectionList extends ObjectSelectionList<ChatSelectionScreen.ChatSelectionList.Entry> implements ChatSelectionLogFiller.Output {
		@Nullable
		private ChatSelectionScreen.ChatSelectionList.Heading previousHeading;

		public ChatSelectionList(Minecraft minecraft, int i) {
			super(minecraft, ChatSelectionScreen.this.width, ChatSelectionScreen.this.height - i - 80, 40, 16);
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
		public void acceptMessage(int i, LoggedChatMessage.Player player) {
			boolean bl = player.canReport(ChatSelectionScreen.this.report.reportedProfileId());
			ChatTrustLevel chatTrustLevel = player.trustLevel();
			GuiMessageTag guiMessageTag = chatTrustLevel.createTag(player.message());
			ChatSelectionScreen.ChatSelectionList.Entry entry = new ChatSelectionScreen.ChatSelectionList.MessageEntry(
				i, player.toContentComponent(), player.toNarrationComponent(), guiMessageTag, bl, true
			);
			this.addEntryToTop(entry);
			this.updateHeading(player, bl);
		}

		private void updateHeading(LoggedChatMessage.Player player, boolean bl) {
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
		public int getRowWidth() {
			return Math.min(350, this.width - 50);
		}

		public int getMaxVisibleEntries() {
			return Mth.positiveCeilDiv(this.height, this.itemHeight);
		}

		@Override
		protected void renderItem(GuiGraphics guiGraphics, int i, int j, float f, int k, int l, int m, int n, int o) {
			ChatSelectionScreen.ChatSelectionList.Entry entry = this.getEntry(k);
			if (this.shouldHighlightEntry(entry)) {
				boolean bl = this.getSelected() == entry;
				int p = this.isFocused() && bl ? -1 : -8355712;
				this.renderSelection(guiGraphics, m, n, o, p, -16777216);
			}

			entry.render(guiGraphics, k, m, l, n, o, i, j, this.getHovered() == entry, f);
		}

		private boolean shouldHighlightEntry(ChatSelectionScreen.ChatSelectionList.Entry entry) {
			if (entry.canSelect()) {
				boolean bl = this.getSelected() == entry;
				boolean bl2 = this.getSelected() == null;
				boolean bl3 = this.getHovered() == entry;
				return bl || bl2 && bl3 && entry.canReport();
			} else {
				return false;
			}
		}

		@Nullable
		protected ChatSelectionScreen.ChatSelectionList.Entry nextEntry(ScreenDirection screenDirection) {
			return this.nextEntry(screenDirection, ChatSelectionScreen.ChatSelectionList.Entry::canSelect);
		}

		public void setSelected(@Nullable ChatSelectionScreen.ChatSelectionList.Entry entry) {
			super.setSelected(entry);
			ChatSelectionScreen.ChatSelectionList.Entry entry2 = this.nextEntry(ScreenDirection.UP);
			if (entry2 == null) {
				ChatSelectionScreen.this.onReachedScrollTop();
			}
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			ChatSelectionScreen.ChatSelectionList.Entry entry = this.getSelected();
			return entry != null && entry.keyPressed(i, j, k) ? true : super.keyPressed(i, j, k);
		}

		public int getFooterTop() {
			return this.getBottom() + 9;
		}

		@Environment(EnvType.CLIENT)
		public class DividerEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
			private static final int COLOR = -6250336;
			private final Component text;

			public DividerEntry(Component component) {
				this.text = component;
			}

			@Override
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				int p = j + m / 2;
				int q = k + l - 8;
				int r = ChatSelectionScreen.this.font.width(this.text);
				int s = (k + q - r) / 2;
				int t = p - 9 / 2;
				guiGraphics.drawString(ChatSelectionScreen.this.font, this.text, s, t, -6250336);
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

			public boolean canReport() {
				return this.canSelect();
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
			private static final int CHECKMARK_WIDTH = 9;
			private static final int CHECKMARK_HEIGHT = 8;
			private static final int INDENT_AMOUNT = 11;
			private static final int TAG_MARGIN_LEFT = 4;
			private final int chatId;
			private final FormattedText text;
			private final Component narration;
			@Nullable
			private final List<FormattedCharSequence> hoverText;
			@Nullable
			private final GuiMessageTag.Icon tagIcon;
			@Nullable
			private final List<FormattedCharSequence> tagHoverText;
			private final boolean canReport;
			private final boolean playerMessage;

			public MessageEntry(int i, Component component, Component component2, @Nullable GuiMessageTag guiMessageTag, boolean bl, boolean bl2) {
				this.chatId = i;
				this.tagIcon = Optionull.map(guiMessageTag, GuiMessageTag::icon);
				this.tagHoverText = guiMessageTag != null && guiMessageTag.text() != null
					? ChatSelectionScreen.this.font.split(guiMessageTag.text(), ChatSelectionList.this.getRowWidth())
					: null;
				this.canReport = bl;
				this.playerMessage = bl2;
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
			}

			@Override
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				if (this.isSelected() && this.canReport) {
					this.renderSelectedCheckmark(guiGraphics, j, k, m);
				}

				int p = k + this.getTextIndent();
				int q = j + 1 + (m - 9) / 2;
				guiGraphics.drawString(ChatSelectionScreen.this.font, Language.getInstance().getVisualOrder(this.text), p, q, this.canReport ? -1 : -1593835521);
				if (this.hoverText != null && bl) {
					ChatSelectionScreen.this.setTooltipForNextRenderPass(this.hoverText);
				}

				int r = ChatSelectionScreen.this.font.width(this.text);
				this.renderTag(guiGraphics, p + r + 4, j, m, n, o);
			}

			private void renderTag(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
				if (this.tagIcon != null) {
					int n = j + (k - this.tagIcon.height) / 2;
					this.tagIcon.draw(guiGraphics, i, n);
					if (this.tagHoverText != null && l >= i && l <= i + this.tagIcon.width && m >= n && m <= n + this.tagIcon.height) {
						ChatSelectionScreen.this.setTooltipForNextRenderPass(this.tagHoverText);
					}
				}
			}

			private void renderSelectedCheckmark(GuiGraphics guiGraphics, int i, int j, int k) {
				int m = i + (k - 8) / 2;
				RenderSystem.enableBlend();
				guiGraphics.blitSprite(ChatSelectionScreen.CHECKMARK_SPRITE, j, m, 9, 8);
				RenderSystem.disableBlend();
			}

			private int getMaximumTextWidth() {
				int i = this.tagIcon != null ? this.tagIcon.width + 4 : 0;
				return ChatSelectionList.this.getRowWidth() - this.getTextIndent() - 4 - i;
			}

			private int getTextIndent() {
				return this.playerMessage ? 11 : 0;
			}

			@Override
			public Component getNarration() {
				return (Component)(this.isSelected() ? Component.translatable("narrator.select", this.narration) : this.narration);
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				ChatSelectionList.this.setSelected(null);
				return this.toggleReport();
			}

			@Override
			public boolean keyPressed(int i, int j, int k) {
				return CommonInputs.selected(i) ? this.toggleReport() : false;
			}

			@Override
			public boolean isSelected() {
				return ChatSelectionScreen.this.report.isReported(this.chatId);
			}

			@Override
			public boolean canSelect() {
				return true;
			}

			@Override
			public boolean canReport() {
				return this.canReport;
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
			private static final int PADDING = 4;
			private final Component heading;
			private final Supplier<PlayerSkin> skin;
			private final boolean canReport;

			public MessageHeadingEntry(GameProfile gameProfile, Component component, boolean bl) {
				this.heading = component;
				this.canReport = bl;
				this.skin = ChatSelectionList.this.minecraft.getSkinManager().lookupInsecure(gameProfile);
			}

			@Override
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				int p = k - 12 + 4;
				int q = j + (m - 12) / 2;
				PlayerFaceRenderer.draw(guiGraphics, (PlayerSkin)this.skin.get(), p, q, 12);
				int r = j + 1 + (m - 9) / 2;
				guiGraphics.drawString(ChatSelectionScreen.this.font, this.heading, p + 12 + 4, r, this.canReport ? -1 : -1593835521);
			}
		}

		@Environment(EnvType.CLIENT)
		public class PaddingEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
			@Override
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			}
		}
	}
}
