package net.minecraft.client.gui.screens.reporting;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.NameReport;
import net.minecraft.client.multiplayer.chat.report.Report;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class NameReportScreen extends AbstractReportScreen<NameReport.Builder> {
	private static final int BUTTON_WIDTH = 120;
	private static final Component TITLE = Component.translatable("gui.abuseReport.name.title");
	private final LinearLayout layout = LinearLayout.vertical().spacing(8);
	private MultiLineEditBox commentBox;
	private Button sendButton;

	private NameReportScreen(Screen screen, ReportingContext reportingContext, NameReport.Builder builder) {
		super(TITLE, screen, reportingContext, builder);
	}

	public NameReportScreen(Screen screen, ReportingContext reportingContext, UUID uUID, String string) {
		this(screen, reportingContext, new NameReport.Builder(uUID, string, reportingContext.sender().reportLimits()));
	}

	public NameReportScreen(Screen screen, ReportingContext reportingContext, NameReport nameReport) {
		this(screen, reportingContext, new NameReport.Builder(nameReport, reportingContext.sender().reportLimits()));
	}

	@Override
	protected void init() {
		this.layout.defaultCellSetting().alignHorizontallyCenter();
		this.layout.addChild(new StringWidget(this.title, this.font));
		Component component = Component.literal(this.reportBuilder.report().getReportedName()).withStyle(ChatFormatting.YELLOW);
		this.layout
			.addChild(
				new StringWidget(Component.translatable("gui.abuseReport.name.reporting", component), this.font),
				layoutSettings -> layoutSettings.alignHorizontallyLeft().padding(0, 8)
			);
		this.commentBox = this.createCommentBox(280, 9 * 8, string -> {
			this.reportBuilder.setComments(string);
			this.onReportChanged();
		});
		this.layout.addChild(CommonLayouts.labeledElement(this.font, this.commentBox, MORE_COMMENTS_LABEL, layoutSettings -> layoutSettings.paddingBottom(12)));
		LinearLayout linearLayout = this.layout.addChild(LinearLayout.horizontal().spacing(8));
		linearLayout.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).width(120).build());
		this.sendButton = linearLayout.addChild(Button.builder(SEND_REPORT, button -> this.sendReport()).width(120).build());
		this.onReportChanged();
		this.layout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
		FrameLayout.centerInRectangle(this.layout, this.getRectangle());
	}

	private void onReportChanged() {
		Report.CannotBuildReason cannotBuildReason = this.reportBuilder.checkBuildable();
		this.sendButton.active = cannotBuildReason == null;
		this.sendButton.setTooltip(Optionull.map(cannotBuildReason, Report.CannotBuildReason::tooltip));
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		return super.mouseReleased(d, e, i) ? true : this.commentBox.mouseReleased(d, e, i);
	}
}
