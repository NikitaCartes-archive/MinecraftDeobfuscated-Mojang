package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.GenericWaitingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.client.multiplayer.chat.report.Report;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ThrowingComponent;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public abstract class AbstractReportScreen<B extends Report.Builder<?>> extends Screen {
	private static final Component REPORT_SENT_MESSAGE = Component.translatable("gui.abuseReport.report_sent_msg");
	private static final Component REPORT_SENDING_TITLE = Component.translatable("gui.abuseReport.sending.title").withStyle(ChatFormatting.BOLD);
	private static final Component REPORT_SENT_TITLE = Component.translatable("gui.abuseReport.sent.title").withStyle(ChatFormatting.BOLD);
	private static final Component REPORT_ERROR_TITLE = Component.translatable("gui.abuseReport.error.title").withStyle(ChatFormatting.BOLD);
	private static final Component REPORT_SEND_GENERIC_ERROR = Component.translatable("gui.abuseReport.send.generic_error");
	protected static final Component SEND_REPORT = Component.translatable("gui.abuseReport.send");
	protected static final Component OBSERVED_WHAT_LABEL = Component.translatable("gui.abuseReport.observed_what");
	protected static final Component SELECT_REASON = Component.translatable("gui.abuseReport.select_reason");
	private static final Component DESCRIBE_PLACEHOLDER = Component.translatable("gui.abuseReport.describe");
	protected static final Component MORE_COMMENTS_LABEL = Component.translatable("gui.abuseReport.more_comments");
	private static final Component MORE_COMMENTS_NARRATION = Component.translatable("gui.abuseReport.comments");
	protected static final int MARGIN = 20;
	protected static final int SCREEN_WIDTH = 280;
	protected static final int SPACING = 8;
	private static final Logger LOGGER = LogUtils.getLogger();
	protected final Screen lastScreen;
	protected final ReportingContext reportingContext;
	protected B reportBuilder;

	protected AbstractReportScreen(Component component, Screen screen, ReportingContext reportingContext, B builder) {
		super(component);
		this.lastScreen = screen;
		this.reportingContext = reportingContext;
		this.reportBuilder = builder;
	}

	protected MultiLineEditBox createCommentBox(int i, int j, Consumer<String> consumer) {
		AbuseReportLimits abuseReportLimits = this.reportingContext.sender().reportLimits();
		MultiLineEditBox multiLineEditBox = new MultiLineEditBox(this.font, 0, 0, i, j, DESCRIBE_PLACEHOLDER, MORE_COMMENTS_NARRATION);
		multiLineEditBox.setValue(this.reportBuilder.comments());
		multiLineEditBox.setCharacterLimit(abuseReportLimits.maxOpinionCommentsLength());
		multiLineEditBox.setValueListener(consumer);
		return multiLineEditBox;
	}

	protected void sendReport() {
		this.reportBuilder.build(this.reportingContext).ifLeft(result -> {
			CompletableFuture<?> completableFuture = this.reportingContext.sender().send(result.id(), result.reportType(), result.report());
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
		this.clearDraft();
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

	void saveDraft() {
		if (this.reportBuilder.hasContent()) {
			this.reportingContext.setReportDraft(this.reportBuilder.report().copy());
		}
	}

	void clearDraft() {
		this.reportingContext.setReportDraft(null);
	}

	@Override
	public void onClose() {
		if (this.reportBuilder.hasContent()) {
			this.minecraft.setScreen(new AbstractReportScreen.DiscardReportWarningScreen());
		} else {
			this.minecraft.setScreen(this.lastScreen);
		}
	}

	@Override
	public void removed() {
		this.saveDraft();
		super.removed();
	}

	@Environment(EnvType.CLIENT)
	class DiscardReportWarningScreen extends WarningScreen {
		private static final Component TITLE = Component.translatable("gui.abuseReport.discard.title").withStyle(ChatFormatting.BOLD);
		private static final Component MESSAGE = Component.translatable("gui.abuseReport.discard.content");
		private static final Component RETURN = Component.translatable("gui.abuseReport.discard.return");
		private static final Component DRAFT = Component.translatable("gui.abuseReport.discard.draft");
		private static final Component DISCARD = Component.translatable("gui.abuseReport.discard.discard");

		protected DiscardReportWarningScreen() {
			super(TITLE, MESSAGE, MESSAGE);
		}

		@Override
		protected Layout addFooterButtons() {
			LinearLayout linearLayout = LinearLayout.vertical().spacing(8);
			linearLayout.defaultCellSetting().alignHorizontallyCenter();
			LinearLayout linearLayout2 = linearLayout.addChild(LinearLayout.horizontal().spacing(8));
			linearLayout2.addChild(Button.builder(RETURN, button -> this.onClose()).build());
			linearLayout2.addChild(Button.builder(DRAFT, button -> {
				AbstractReportScreen.this.saveDraft();
				this.minecraft.setScreen(AbstractReportScreen.this.lastScreen);
			}).build());
			linearLayout.addChild(Button.builder(DISCARD, button -> {
				AbstractReportScreen.this.clearDraft();
				this.minecraft.setScreen(AbstractReportScreen.this.lastScreen);
			}).build());
			return linearLayout;
		}

		@Override
		public void onClose() {
			this.minecraft.setScreen(AbstractReportScreen.this);
		}

		@Override
		public boolean shouldCloseOnEsc() {
			return false;
		}
	}
}
