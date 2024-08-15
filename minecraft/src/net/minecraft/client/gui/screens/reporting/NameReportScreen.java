package net.minecraft.client.gui.screens.reporting;

import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.NameReport;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class NameReportScreen extends AbstractReportScreen<NameReport.Builder> {
	private static final Component TITLE = Component.translatable("gui.abuseReport.name.title");
	private static final Component COMMENT_BOX_LABEL = Component.translatable("gui.abuseReport.name.comment_box_label");
	@Nullable
	private MultiLineEditBox commentBox;

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
	protected void addContent() {
		Component component = Component.literal(this.reportBuilder.report().getReportedName()).withStyle(ChatFormatting.YELLOW);
		this.layout
			.addChild(
				new StringWidget(Component.translatable("gui.abuseReport.name.reporting", component), this.font),
				layoutSettings -> layoutSettings.alignHorizontallyCenter().padding(0, 8)
			);
		this.commentBox = this.createCommentBox(280, 9 * 8, string -> {
			this.reportBuilder.setComments(string);
			this.onReportChanged();
		});
		this.layout.addChild(CommonLayouts.labeledElement(this.font, this.commentBox, COMMENT_BOX_LABEL, layoutSettings -> layoutSettings.paddingBottom(12)));
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		if (super.mouseReleased(d, e, i)) {
			return true;
		} else {
			return this.commentBox != null ? this.commentBox.mouseReleased(d, e, i) : false;
		}
	}
}
