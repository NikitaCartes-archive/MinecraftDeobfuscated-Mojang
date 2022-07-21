package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.GenericWaitingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.client.multiplayer.chat.report.ChatReportBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ThrowingComponent;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ChatReportScreen extends Screen {
	private static final int BUTTON_WIDTH = 120;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_MARGIN = 20;
	private static final int BUTTON_MARGIN_HALF = 10;
	private static final int LABEL_HEIGHT = 25;
	private static final int SCREEN_WIDTH = 280;
	private static final int SCREEN_HEIGHT = 300;
	private static final Component OBSERVED_WHAT_LABEL = Component.translatable("gui.chatReport.observed_what");
	private static final Component SELECT_REASON = Component.translatable("gui.chatReport.select_reason");
	private static final Component MORE_COMMENTS_LABEL = Component.translatable("gui.chatReport.more_comments");
	private static final Component DESCRIBE_PLACEHOLDER = Component.translatable("gui.chatReport.describe");
	private static final Component REPORT_SENT_MESSAGE = Component.translatable("gui.chatReport.report_sent_msg");
	private static final Component SELECT_CHAT_MESSAGE = Component.translatable("gui.chatReport.select_chat");
	private static final Component REPORT_SENDING_TITLE = Component.translatable("gui.abuseReport.sending.title").withStyle(ChatFormatting.BOLD);
	private static final Component REPORT_SENT_TITLE = Component.translatable("gui.abuseReport.sent.title").withStyle(ChatFormatting.BOLD);
	private static final Component REPORT_ERROR_TITLE = Component.translatable("gui.abuseReport.error.title").withStyle(ChatFormatting.BOLD);
	private static final Component REPORT_SEND_GENERIC_ERROR = Component.translatable("gui.abuseReport.send.generic_error");
	private static final Logger LOGGER = LogUtils.getLogger();
	@Nullable
	final Screen lastScreen;
	private final ReportingContext reportingContext;
	@Nullable
	private MultiLineLabel reasonDescriptionLabel;
	@Nullable
	private MultiLineEditBox commentBox;
	private Button sendButton;
	private ChatReportBuilder report;
	@Nullable
	ChatReportBuilder.CannotBuildReason cannotBuildReason;

	public ChatReportScreen(Screen screen, ReportingContext reportingContext, UUID uUID) {
		super(Component.translatable("gui.chatReport.title"));
		this.lastScreen = screen;
		this.reportingContext = reportingContext;
		this.report = new ChatReportBuilder(uUID, reportingContext.sender().reportLimits());
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		AbuseReportLimits abuseReportLimits = this.reportingContext.sender().reportLimits();
		int i = this.width / 2;
		ReportReason reportReason = this.report.reason();
		if (reportReason != null) {
			this.reasonDescriptionLabel = MultiLineLabel.create(this.font, reportReason.description(), 280);
		} else {
			this.reasonDescriptionLabel = null;
		}

		IntSet intSet = this.report.reportedMessages();
		Component component;
		if (intSet.isEmpty()) {
			component = SELECT_CHAT_MESSAGE;
		} else {
			component = Component.translatable("gui.chatReport.selected_chat", intSet.size());
		}

		this.addRenderableWidget(
			new Button(
				this.contentLeft(),
				this.selectChatTop(),
				280,
				20,
				component,
				button -> this.minecraft.setScreen(new ChatSelectionScreen(this, this.reportingContext, this.report, chatReportBuilder -> {
						this.report = chatReportBuilder;
						this.onReportChanged();
					}))
			)
		);
		Component component2 = Util.mapNullable(reportReason, ReportReason::title, SELECT_REASON);
		this.addRenderableWidget(
			new Button(
				this.contentLeft(),
				this.selectInfoTop(),
				280,
				20,
				component2,
				button -> this.minecraft.setScreen(new ReportReasonSelectionScreen(this, this.report.reason(), reportReasonx -> {
						this.report.setReason(reportReasonx);
						this.onReportChanged();
					}))
			)
		);
		this.commentBox = this.addRenderableWidget(
			new MultiLineEditBox(
				this.minecraft.font,
				this.contentLeft(),
				this.commentBoxTop(),
				280,
				this.commentBoxBottom() - this.commentBoxTop(),
				DESCRIBE_PLACEHOLDER,
				Component.translatable("gui.chatReport.comments")
			)
		);
		this.commentBox.setValue(this.report.comments());
		this.commentBox.setCharacterLimit(abuseReportLimits.maxOpinionCommentsLength());
		this.commentBox.setValueListener(string -> {
			this.report.setComments(string);
			this.onReportChanged();
		});
		this.addRenderableWidget(new Button(i - 120, this.completeButtonTop(), 120, 20, CommonComponents.GUI_BACK, button -> this.onClose()));
		this.sendButton = this.addRenderableWidget(
			new Button(
				i + 10,
				this.completeButtonTop(),
				120,
				20,
				Component.translatable("gui.chatReport.send"),
				button -> this.sendReport(),
				new ChatReportScreen.SubmitButtonTooltip()
			)
		);
		this.onReportChanged();
	}

	private void onReportChanged() {
		this.cannotBuildReason = this.report.checkBuildable();
		this.sendButton.active = this.cannotBuildReason == null;
	}

	private void sendReport() {
		this.report.build(this.reportingContext).ifLeft(result -> {
			CompletableFuture<?> completableFuture = this.reportingContext.sender().send(result.id(), result.report());
			this.minecraft.setScreen(GenericWaitingScreen.createWaiting(REPORT_SENDING_TITLE, CommonComponents.GUI_CANCEL, () -> {
				this.minecraft.setScreen(this);
				completableFuture.cancel(true);
			}));
			completableFuture.handleAsync((object, throwable) -> {
				if (throwable == null) {
					this.onReportSendSuccess();
				} else {
					if (throwable instanceof CancellationException) {
						return null;
					}

					this.onReportSendError(throwable);
				}

				return null;
			}, this.minecraft);
		}).ifRight(cannotBuildReason -> this.displayReportSendError(cannotBuildReason.message()));
	}

	private void onReportSendSuccess() {
		this.minecraft
			.setScreen(GenericWaitingScreen.createCompleted(REPORT_SENT_TITLE, REPORT_SENT_MESSAGE, CommonComponents.GUI_DONE, () -> this.minecraft.setScreen(null)));
	}

	private void onReportSendError(Throwable throwable) {
		LOGGER.error("Encountered error while sending abuse report", throwable);
		Component component;
		if (throwable.getCause() instanceof ThrowingComponent throwingComponent) {
			component = throwingComponent.getComponent();
		} else {
			component = REPORT_SEND_GENERIC_ERROR;
		}

		this.displayReportSendError(component);
	}

	private void displayReportSendError(Component component) {
		Component component2 = component.copy().withStyle(ChatFormatting.RED);
		this.minecraft
			.setScreen(GenericWaitingScreen.createCompleted(REPORT_ERROR_TITLE, component2, CommonComponents.GUI_BACK, () -> this.minecraft.setScreen(this)));
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		int k = this.width / 2;
		RenderSystem.disableDepthTest();
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, k, 10, 16777215);
		drawCenteredString(poseStack, this.font, OBSERVED_WHAT_LABEL, k, this.selectChatTop() - 9 - 6, 16777215);
		if (this.reasonDescriptionLabel != null) {
			this.reasonDescriptionLabel.renderLeftAligned(poseStack, this.contentLeft(), this.selectInfoTop() + 20 + 5, 9, 16777215);
		}

		drawString(poseStack, this.font, MORE_COMMENTS_LABEL, this.contentLeft(), this.commentBoxTop() - 9 - 6, 16777215);
		super.render(poseStack, i, j, f);
		RenderSystem.enableDepthTest();
	}

	@Override
	public void tick() {
		this.commentBox.tick();
		super.tick();
	}

	@Override
	public void onClose() {
		if (!this.commentBox.getValue().isEmpty()) {
			this.minecraft.setScreen(new ChatReportScreen.DiscardReportWarningScreen());
		} else {
			this.minecraft.setScreen(this.lastScreen);
		}
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		return super.mouseReleased(d, e, i) ? true : this.commentBox.mouseReleased(d, e, i);
	}

	private int contentLeft() {
		return this.width / 2 - 140;
	}

	private int contentRight() {
		return this.width / 2 + 140;
	}

	private int contentTop() {
		return Math.max((this.height - 300) / 2, 0);
	}

	private int contentBottom() {
		return Math.min((this.height + 300) / 2, this.height);
	}

	private int selectChatTop() {
		return this.contentTop() + 40;
	}

	private int selectInfoTop() {
		return this.selectChatTop() + 10 + 20;
	}

	private int commentBoxTop() {
		int i = this.selectInfoTop() + 20 + 25;
		if (this.reasonDescriptionLabel != null) {
			i += (this.reasonDescriptionLabel.getLineCount() + 1) * 9;
		}

		return i;
	}

	private int commentBoxBottom() {
		return this.completeButtonTop() - 20;
	}

	private int completeButtonTop() {
		return this.contentBottom() - 20 - 10;
	}

	@Environment(EnvType.CLIENT)
	class DiscardReportWarningScreen extends WarningScreen {
		private static final Component TITLE = Component.translatable("gui.chatReport.discard.title").withStyle(ChatFormatting.BOLD);
		private static final Component MESSAGE = Component.translatable("gui.chatReport.discard.content");
		private static final Component RETURN = Component.translatable("gui.chatReport.discard.return");
		private static final Component DISCARD = Component.translatable("gui.chatReport.discard.discard");

		protected DiscardReportWarningScreen() {
			super(TITLE, MESSAGE, MESSAGE);
		}

		@Override
		protected void initButtons(int i) {
			this.addRenderableWidget(new Button(this.width / 2 - 155, 100 + i, 150, 20, RETURN, button -> this.onClose()));
			this.addRenderableWidget(new Button(this.width / 2 + 5, 100 + i, 150, 20, DISCARD, button -> this.minecraft.setScreen(ChatReportScreen.this.lastScreen)));
		}

		@Override
		public void onClose() {
			this.minecraft.setScreen(ChatReportScreen.this);
		}

		@Override
		public boolean shouldCloseOnEsc() {
			return false;
		}

		@Override
		protected void renderTitle(PoseStack poseStack) {
			drawString(poseStack, this.font, this.title, this.width / 2 - 155, 30, 16777215);
		}
	}

	@Environment(EnvType.CLIENT)
	class SubmitButtonTooltip implements Button.OnTooltip {
		@Override
		public void onTooltip(Button button, PoseStack poseStack, int i, int j) {
			if (ChatReportScreen.this.cannotBuildReason != null) {
				Component component = ChatReportScreen.this.cannotBuildReason.message();
				ChatReportScreen.this.renderTooltip(poseStack, ChatReportScreen.this.font.split(component, Math.max(ChatReportScreen.this.width / 2 - 43, 170)), i, j);
			}
		}
	}
}
