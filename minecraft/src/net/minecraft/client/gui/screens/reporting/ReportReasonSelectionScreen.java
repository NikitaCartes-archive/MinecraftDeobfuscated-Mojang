package net.minecraft.client.gui.screens.reporting;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ReportReasonSelectionScreen extends Screen {
	private static final Component REASON_TITLE = Component.translatable("gui.abuseReport.reason.title");
	private static final Component REASON_DESCRIPTION = Component.translatable("gui.abuseReport.reason.description");
	private static final Component READ_INFO_LABEL = Component.translatable("gui.chatReport.read_info");
	private static final int FOOTER_HEIGHT = 95;
	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 20;
	private static final int CONTENT_WIDTH = 320;
	private static final int PADDING = 4;
	@Nullable
	private final Screen lastScreen;
	@Nullable
	private ReportReasonSelectionScreen.ReasonSelectionList reasonSelectionList;
	@Nullable
	ReportReason currentlySelectedReason;
	private final Consumer<ReportReason> onSelectedReason;

	public ReportReasonSelectionScreen(@Nullable Screen screen, @Nullable ReportReason reportReason, Consumer<ReportReason> consumer) {
		super(REASON_TITLE);
		this.lastScreen = screen;
		this.currentlySelectedReason = reportReason;
		this.onSelectedReason = consumer;
	}

	@Override
	protected void init() {
		this.reasonSelectionList = new ReportReasonSelectionScreen.ReasonSelectionList(this.minecraft);
		this.addWidget(this.reasonSelectionList);
		ReportReasonSelectionScreen.ReasonSelectionList.Entry entry = Optionull.map(this.currentlySelectedReason, this.reasonSelectionList::findEntry);
		this.reasonSelectionList.setSelected(entry);
		int i = this.width / 2 - 150 - 5;
		this.addRenderableWidget(Button.builder(READ_INFO_LABEL, button -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
				if (bl) {
					Util.getPlatform().openUri("https://aka.ms/aboutjavareporting");
				}

				this.minecraft.setScreen(this);
			}, "https://aka.ms/aboutjavareporting", true))).bounds(i, this.buttonTop(), 150, 20).build());
		int j = this.width / 2 + 5;
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
			ReportReasonSelectionScreen.ReasonSelectionList.Entry entryx = this.reasonSelectionList.getSelected();
			if (entryx != null) {
				this.onSelectedReason.accept(entryx.getReason());
			}

			this.minecraft.setScreen(this.lastScreen);
		}).bounds(j, this.buttonTop(), 150, 20).build());
		super.init();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.reasonSelectionList.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
		guiGraphics.fill(this.contentLeft(), this.descriptionTop(), this.contentRight(), this.descriptionBottom(), 2130706432);
		guiGraphics.drawString(this.font, REASON_DESCRIPTION, this.contentLeft() + 4, this.descriptionTop() + 4, -8421505);
		ReportReasonSelectionScreen.ReasonSelectionList.Entry entry = this.reasonSelectionList.getSelected();
		if (entry != null) {
			int k = this.contentLeft() + 4 + 16;
			int l = this.contentRight() - 4;
			int m = this.descriptionTop() + 4 + 9 + 2;
			int n = this.descriptionBottom() - 4;
			int o = l - k;
			int p = n - m;
			int q = this.font.wordWrapHeight(entry.reason.description(), o);
			guiGraphics.drawWordWrap(this.font, entry.reason.description(), k, m + (p - q) / 2, o, -1);
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderDirtBackground(guiGraphics);
	}

	private int buttonTop() {
		return this.height - 20 - 4;
	}

	private int contentLeft() {
		return (this.width - 320) / 2;
	}

	private int contentRight() {
		return (this.width + 320) / 2;
	}

	private int descriptionTop() {
		return this.height - 95 + 4;
	}

	private int descriptionBottom() {
		return this.buttonTop() - 4;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Environment(EnvType.CLIENT)
	public class ReasonSelectionList extends ObjectSelectionList<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
		public ReasonSelectionList(Minecraft minecraft) {
			super(minecraft, ReportReasonSelectionScreen.this.width, ReportReasonSelectionScreen.this.height, 40, ReportReasonSelectionScreen.this.height - 95, 18);

			for (ReportReason reportReason : ReportReason.values()) {
				this.addEntry(new ReportReasonSelectionScreen.ReasonSelectionList.Entry(reportReason));
			}
		}

		@Nullable
		public ReportReasonSelectionScreen.ReasonSelectionList.Entry findEntry(ReportReason reportReason) {
			return (ReportReasonSelectionScreen.ReasonSelectionList.Entry)this.children()
				.stream()
				.filter(entry -> entry.reason == reportReason)
				.findFirst()
				.orElse(null);
		}

		@Override
		public int getRowWidth() {
			return 320;
		}

		@Override
		protected int getScrollbarPosition() {
			return this.getRowRight() - 2;
		}

		public void setSelected(@Nullable ReportReasonSelectionScreen.ReasonSelectionList.Entry entry) {
			super.setSelected(entry);
			ReportReasonSelectionScreen.this.currentlySelectedReason = entry != null ? entry.getReason() : null;
		}

		@Environment(EnvType.CLIENT)
		public class Entry extends ObjectSelectionList.Entry<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
			final ReportReason reason;

			public Entry(ReportReason reportReason) {
				this.reason = reportReason;
			}

			@Override
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				int p = k + 1;
				int q = j + (m - 9) / 2 + 1;
				guiGraphics.drawString(ReportReasonSelectionScreen.this.font, this.reason.title(), p, q, -1);
			}

			@Override
			public Component getNarration() {
				return Component.translatable("gui.abuseReport.reason.narration", this.reason.title(), this.reason.description());
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				if (i == 0) {
					ReasonSelectionList.this.setSelected(this);
					return true;
				} else {
					return false;
				}
			}

			public ReportReason getReason() {
				return this.reason;
			}
		}
	}
}
