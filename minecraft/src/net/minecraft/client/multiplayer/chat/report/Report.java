package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.datafixers.util.Either;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public abstract class Report {
	protected final UUID reportId;
	protected final Instant createdAt;
	protected final UUID reportedProfileId;
	protected String comments = "";
	@Nullable
	protected ReportReason reason;

	public Report(UUID uUID, Instant instant, UUID uUID2) {
		this.reportId = uUID;
		this.createdAt = instant;
		this.reportedProfileId = uUID2;
	}

	public boolean isReportedPlayer(UUID uUID) {
		return uUID.equals(this.reportedProfileId);
	}

	public abstract Report copy();

	public abstract Screen createScreen(Screen screen, ReportingContext reportingContext);

	@Environment(EnvType.CLIENT)
	public abstract static class Builder<R extends Report> {
		protected final R report;
		protected final AbuseReportLimits limits;

		protected Builder(R report, AbuseReportLimits abuseReportLimits) {
			this.report = report;
			this.limits = abuseReportLimits;
		}

		public R report() {
			return this.report;
		}

		public UUID reportedProfileId() {
			return this.report.reportedProfileId;
		}

		public String comments() {
			return this.report.comments;
		}

		public void setComments(String string) {
			this.report.comments = string;
		}

		@Nullable
		public ReportReason reason() {
			return this.report.reason;
		}

		public void setReason(ReportReason reportReason) {
			this.report.reason = reportReason;
		}

		public abstract boolean hasContent();

		@Nullable
		public abstract Report.CannotBuildReason checkBuildable();

		public abstract Either<Report.Result, Report.CannotBuildReason> build(ReportingContext reportingContext);
	}

	@Environment(EnvType.CLIENT)
	public static record CannotBuildReason(Component message) {
		public static final Report.CannotBuildReason NO_REASON = new Report.CannotBuildReason(Component.translatable("gui.abuseReport.send.no_reason"));
		public static final Report.CannotBuildReason NO_REPORTED_MESSAGES = new Report.CannotBuildReason(
			Component.translatable("gui.chatReport.send.no_reported_messages")
		);
		public static final Report.CannotBuildReason TOO_MANY_MESSAGES = new Report.CannotBuildReason(Component.translatable("gui.chatReport.send.too_many_messages"));
		public static final Report.CannotBuildReason COMMENT_TOO_LONG = new Report.CannotBuildReason(Component.translatable("gui.abuseReport.send.comment_too_long"));

		public Tooltip tooltip() {
			return Tooltip.create(this.message);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record Result(UUID id, ReportType reportType, AbuseReport report) {
	}
}
