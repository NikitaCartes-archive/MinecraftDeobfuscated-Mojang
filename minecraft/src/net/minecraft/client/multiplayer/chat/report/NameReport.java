package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.NameReportScreen;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class NameReport extends Report {
	private final String reportedName;

	NameReport(UUID uUID, Instant instant, UUID uUID2, String string) {
		super(uUID, instant, uUID2);
		this.reportedName = string;
	}

	public String getReportedName() {
		return this.reportedName;
	}

	public NameReport copy() {
		NameReport nameReport = new NameReport(this.reportId, this.createdAt, this.reportedProfileId, this.reportedName);
		nameReport.comments = this.comments;
		return nameReport;
	}

	@Override
	public Screen createScreen(Screen screen, ReportingContext reportingContext) {
		return new NameReportScreen(screen, reportingContext, this);
	}

	@Environment(EnvType.CLIENT)
	public static class Builder extends Report.Builder<NameReport> {
		public Builder(NameReport nameReport, AbuseReportLimits abuseReportLimits) {
			super(nameReport, abuseReportLimits);
		}

		public Builder(UUID uUID, String string, AbuseReportLimits abuseReportLimits) {
			super(new NameReport(UUID.randomUUID(), Instant.now(), uUID, string), abuseReportLimits);
		}

		@Override
		public boolean hasContent() {
			return StringUtils.isNotEmpty(this.comments());
		}

		@Nullable
		@Override
		public Report.CannotBuildReason checkBuildable() {
			return this.report.comments.length() > this.limits.maxOpinionCommentsLength() ? Report.CannotBuildReason.COMMENT_TOO_LONG : null;
		}

		@Override
		public Either<Report.Result, Report.CannotBuildReason> build(ReportingContext reportingContext) {
			Report.CannotBuildReason cannotBuildReason = this.checkBuildable();
			if (cannotBuildReason != null) {
				return Either.right(cannotBuildReason);
			} else {
				ReportedEntity reportedEntity = new ReportedEntity(this.report.reportedProfileId);
				AbuseReport abuseReport = AbuseReport.name(this.report.comments, reportedEntity, this.report.createdAt);
				return Either.left(new Report.Result(this.report.reportId, ReportType.USERNAME, abuseReport));
			}
		}
	}
}
