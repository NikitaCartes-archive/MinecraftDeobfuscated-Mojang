package net.minecraft.client.gui.screens.reporting;

import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ChatReport;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ChatReportScreen extends AbstractReportScreen<ChatReport.Builder> {
	private static final Component TITLE = Component.translatable("gui.chatReport.title");
	private static final Component SELECT_CHAT_MESSAGE = Component.translatable("gui.chatReport.select_chat");
	private MultiLineEditBox commentBox;
	private Button selectMessagesButton;
	private Button selectReasonButton;

	private ChatReportScreen(Screen screen, ReportingContext reportingContext, ChatReport.Builder builder) {
		super(TITLE, screen, reportingContext, builder);
	}

	public ChatReportScreen(Screen screen, ReportingContext reportingContext, UUID uUID) {
		this(screen, reportingContext, new ChatReport.Builder(uUID, reportingContext.sender().reportLimits()));
	}

	public ChatReportScreen(Screen screen, ReportingContext reportingContext, ChatReport chatReport) {
		this(screen, reportingContext, new ChatReport.Builder(chatReport, reportingContext.sender().reportLimits()));
	}

	@Override
	protected void addContent() {
		this.selectMessagesButton = this.layout
			.addChild(
				Button.builder(
						SELECT_CHAT_MESSAGE, button -> this.minecraft.setScreen(new ChatSelectionScreen(this, this.reportingContext, this.reportBuilder, builder -> {
								this.reportBuilder = builder;
								this.onReportChanged();
							}))
					)
					.width(280)
					.build()
			);
		this.selectReasonButton = Button.builder(
				SELECT_REASON, button -> this.minecraft.setScreen(new ReportReasonSelectionScreen(this, this.reportBuilder.reason(), reportReason -> {
						this.reportBuilder.setReason(reportReason);
						this.onReportChanged();
					}))
			)
			.width(280)
			.build();
		this.layout.addChild(CommonLayouts.labeledElement(this.font, this.selectReasonButton, OBSERVED_WHAT_LABEL));
		this.commentBox = this.createCommentBox(280, 9 * 8, string -> {
			this.reportBuilder.setComments(string);
			this.onReportChanged();
		});
		this.layout.addChild(CommonLayouts.labeledElement(this.font, this.commentBox, MORE_COMMENTS_LABEL, layoutSettings -> layoutSettings.paddingBottom(12)));
	}

	@Override
	protected void onReportChanged() {
		IntSet intSet = this.reportBuilder.reportedMessages();
		if (intSet.isEmpty()) {
			this.selectMessagesButton.setMessage(SELECT_CHAT_MESSAGE);
		} else {
			this.selectMessagesButton.setMessage(Component.translatable("gui.chatReport.selected_chat", intSet.size()));
		}

		ReportReason reportReason = this.reportBuilder.reason();
		if (reportReason != null) {
			this.selectReasonButton.setMessage(reportReason.title());
		} else {
			this.selectReasonButton.setMessage(SELECT_REASON);
		}

		super.onReportChanged();
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		return super.mouseReleased(d, e, i) ? true : this.commentBox.mouseReleased(d, e, i);
	}
}
