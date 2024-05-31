package net.minecraft.client.gui.screens.reporting;

import java.util.UUID;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.multiplayer.chat.report.SkinReport;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class SkinReportScreen extends AbstractReportScreen<SkinReport.Builder> {
	private static final int SKIN_WIDTH = 85;
	private static final int FORM_WIDTH = 178;
	private static final Component TITLE = Component.translatable("gui.abuseReport.skin.title");
	private MultiLineEditBox commentBox;
	private Button selectReasonButton;

	private SkinReportScreen(Screen screen, ReportingContext reportingContext, SkinReport.Builder builder) {
		super(TITLE, screen, reportingContext, builder);
	}

	public SkinReportScreen(Screen screen, ReportingContext reportingContext, UUID uUID, Supplier<PlayerSkin> supplier) {
		this(screen, reportingContext, new SkinReport.Builder(uUID, supplier, reportingContext.sender().reportLimits()));
	}

	public SkinReportScreen(Screen screen, ReportingContext reportingContext, SkinReport skinReport) {
		this(screen, reportingContext, new SkinReport.Builder(skinReport, reportingContext.sender().reportLimits()));
	}

	@Override
	protected void addContent() {
		LinearLayout linearLayout = this.layout.addChild(LinearLayout.horizontal().spacing(8));
		linearLayout.defaultCellSetting().alignVerticallyMiddle();
		linearLayout.addChild(new PlayerSkinWidget(85, 120, this.minecraft.getEntityModels(), this.reportBuilder.report().getSkinGetter()));
		LinearLayout linearLayout2 = linearLayout.addChild(LinearLayout.vertical().spacing(8));
		this.selectReasonButton = Button.builder(
				SELECT_REASON, button -> this.minecraft.setScreen(new ReportReasonSelectionScreen(this, this.reportBuilder.reason(), reportReason -> {
						this.reportBuilder.setReason(reportReason);
						this.onReportChanged();
					}))
			)
			.width(178)
			.build();
		linearLayout2.addChild(CommonLayouts.labeledElement(this.font, this.selectReasonButton, OBSERVED_WHAT_LABEL));
		this.commentBox = this.createCommentBox(178, 9 * 8, string -> {
			this.reportBuilder.setComments(string);
			this.onReportChanged();
		});
		linearLayout2.addChild(CommonLayouts.labeledElement(this.font, this.commentBox, MORE_COMMENTS_LABEL, layoutSettings -> layoutSettings.paddingBottom(12)));
	}

	@Override
	protected void onReportChanged() {
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
