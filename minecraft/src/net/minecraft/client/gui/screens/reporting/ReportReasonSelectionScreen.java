package net.minecraft.client.gui.screens.reporting;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ReportReasonSelectionScreen extends Screen {
	private static final Component REASON_TITLE = Component.translatable("gui.abuseReport.reason.title");
	private static final Component REASON_DESCRIPTION = Component.translatable("gui.abuseReport.reason.description");
	private static final int FOOTER_HEIGHT = 80;
	@Nullable
	private final Screen lastScreen;
	@Nullable
	private ReportReasonSelectionScreen.ReasonSelectionList reasonSelectionList;
	@Nullable
	private final ReportReason selectedReasonOnInit;
	private final Consumer<ReportReason> onSelectedReason;

	public ReportReasonSelectionScreen(@Nullable Screen screen, @Nullable ReportReason reportReason, Consumer<ReportReason> consumer) {
		super(REASON_TITLE);
		this.lastScreen = screen;
		this.selectedReasonOnInit = reportReason;
		this.onSelectedReason = consumer;
	}

	@Override
	protected void init() {
		this.reasonSelectionList = new ReportReasonSelectionScreen.ReasonSelectionList(this.minecraft);
		this.reasonSelectionList.setRenderBackground(false);
		this.addWidget(this.reasonSelectionList);
		ReportReasonSelectionScreen.ReasonSelectionList.Entry entry = Util.mapNullable(this.selectedReasonOnInit, this.reasonSelectionList::findEntry);
		this.reasonSelectionList.setSelected(entry);
		this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height - 32, 150, 20, CommonComponents.GUI_DONE, button -> {
			ReportReasonSelectionScreen.ReasonSelectionList.Entry entryx = this.reasonSelectionList.getSelected();
			if (entryx != null) {
				this.onSelectedReason.accept(entryx.getReason());
			}

			this.minecraft.setScreen(this.lastScreen);
		}));
		super.init();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.reasonSelectionList.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 16777215);
		super.render(poseStack, i, j, f);
		int k = this.height - 80;
		int l = this.height - 35;
		int m = this.width / 2 - 160;
		int n = this.width / 2 + 160;
		fill(poseStack, m, k, n, l, 2130706432);
		drawString(poseStack, this.font, REASON_DESCRIPTION, m + 2, k + 2, -8421505);
		ReportReasonSelectionScreen.ReasonSelectionList.Entry entry = this.reasonSelectionList.getSelected();
		if (entry != null) {
			int o = this.font.wordWrapHeight(entry.reason.description(), 280);
			int p = l - k + 10;
			this.font.drawWordWrap(entry.reason.description(), m + 20, k + (p - o) / 2, n - m - 40, -1);
		}
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Environment(EnvType.CLIENT)
	public class ReasonSelectionList extends ObjectSelectionList<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
		public ReasonSelectionList(Minecraft minecraft) {
			super(minecraft, ReportReasonSelectionScreen.this.width, ReportReasonSelectionScreen.this.height, 40, ReportReasonSelectionScreen.this.height - 80, 18);

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
			return 280;
		}

		@Environment(EnvType.CLIENT)
		public class Entry extends ObjectSelectionList.Entry<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
			final ReportReason reason;

			public Entry(ReportReason reportReason) {
				this.reason = reportReason;
			}

			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				GuiComponent.drawString(poseStack, ReportReasonSelectionScreen.this.font, this.reason.title(), k, j + 1, -1);
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
