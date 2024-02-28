package net.minecraft.client.gui.screens.reporting;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ReportReasonSelectionScreen extends Screen {
	private static final Component REASON_TITLE = Component.translatable("gui.abuseReport.reason.title");
	private static final Component REASON_DESCRIPTION = Component.translatable("gui.abuseReport.reason.description");
	private static final Component READ_INFO_LABEL = Component.translatable("gui.abuseReport.read_info");
	private static final int DESCRIPTION_BOX_WIDTH = 320;
	private static final int DESCRIPTION_BOX_HEIGHT = 62;
	private static final int PADDING = 4;
	@Nullable
	private final Screen lastScreen;
	@Nullable
	private ReportReasonSelectionScreen.ReasonSelectionList reasonSelectionList;
	@Nullable
	ReportReason currentlySelectedReason;
	private final Consumer<ReportReason> onSelectedReason;
	final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

	public ReportReasonSelectionScreen(@Nullable Screen screen, @Nullable ReportReason reportReason, Consumer<ReportReason> consumer) {
		super(REASON_TITLE);
		this.lastScreen = screen;
		this.currentlySelectedReason = reportReason;
		this.onSelectedReason = consumer;
	}

	@Override
	protected void init() {
		this.layout.addTitleHeader(REASON_TITLE, this.font);
		LinearLayout linearLayout = this.layout.addToContents(LinearLayout.vertical().spacing(4));
		this.reasonSelectionList = linearLayout.addChild(new ReportReasonSelectionScreen.ReasonSelectionList(this.minecraft));
		ReportReasonSelectionScreen.ReasonSelectionList.Entry entry = Optionull.map(this.currentlySelectedReason, this.reasonSelectionList::findEntry);
		this.reasonSelectionList.setSelected(entry);
		linearLayout.addChild(SpacerElement.height(this.descriptionHeight()));
		LinearLayout linearLayout2 = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		linearLayout2.addChild(Button.builder(READ_INFO_LABEL, ConfirmLinkScreen.confirmLink(this, "https://aka.ms/aboutjavareporting")).build());
		linearLayout2.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
			ReportReasonSelectionScreen.ReasonSelectionList.Entry entryx = this.reasonSelectionList.getSelected();
			if (entryx != null) {
				this.onSelectedReason.accept(entryx.getReason());
			}

			this.minecraft.setScreen(this.lastScreen);
		}).build());
		this.layout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
		if (this.reasonSelectionList != null) {
			this.reasonSelectionList.updateSizeAndPosition(this.width, this.listHeight(), this.layout.getHeaderHeight());
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.fill(this.descriptionLeft(), this.descriptionTop(), this.descriptionRight(), this.descriptionBottom(), -16777216);
		guiGraphics.renderOutline(this.descriptionLeft(), this.descriptionTop(), this.descriptionWidth(), this.descriptionHeight(), -1);
		guiGraphics.drawString(this.font, REASON_DESCRIPTION, this.descriptionLeft() + 4, this.descriptionTop() + 4, -1);
		ReportReasonSelectionScreen.ReasonSelectionList.Entry entry = this.reasonSelectionList.getSelected();
		if (entry != null) {
			int k = this.descriptionLeft() + 4 + 16;
			int l = this.descriptionRight() - 4;
			int m = this.descriptionTop() + 4 + 9 + 2;
			int n = this.descriptionBottom() - 4;
			int o = l - k;
			int p = n - m;
			int q = this.font.wordWrapHeight(entry.reason.description(), o);
			guiGraphics.drawWordWrap(this.font, entry.reason.description(), k, m + (p - q) / 2, o, -1);
		}
	}

	private int descriptionLeft() {
		return (this.width - 320) / 2;
	}

	private int descriptionRight() {
		return (this.width + 320) / 2;
	}

	private int descriptionTop() {
		return this.descriptionBottom() - this.descriptionHeight();
	}

	private int descriptionBottom() {
		return this.height - this.layout.getFooterHeight() - 4;
	}

	private int descriptionWidth() {
		return 320;
	}

	private int descriptionHeight() {
		return 62;
	}

	int listHeight() {
		return this.layout.getContentHeight() - this.descriptionHeight() - 8;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Environment(EnvType.CLIENT)
	public class ReasonSelectionList extends ObjectSelectionList<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
		public ReasonSelectionList(Minecraft minecraft) {
			super(
				minecraft,
				ReportReasonSelectionScreen.this.width,
				ReportReasonSelectionScreen.this.listHeight(),
				ReportReasonSelectionScreen.this.layout.getHeaderHeight(),
				18
			);

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
				ReasonSelectionList.this.setSelected(this);
				return super.mouseClicked(d, e, i);
			}

			public ReportReason getReason() {
				return this.reason;
			}
		}
	}
}
