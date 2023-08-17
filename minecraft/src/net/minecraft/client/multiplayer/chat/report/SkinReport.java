package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.SkinReportScreen;
import net.minecraft.client.resources.PlayerSkin;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class SkinReport extends Report {
	final Supplier<PlayerSkin> skinGetter;

	SkinReport(UUID uUID, Instant instant, UUID uUID2, Supplier<PlayerSkin> supplier) {
		super(uUID, instant, uUID2);
		this.skinGetter = supplier;
	}

	public Supplier<PlayerSkin> getSkinGetter() {
		return this.skinGetter;
	}

	public SkinReport copy() {
		SkinReport skinReport = new SkinReport(this.reportId, this.createdAt, this.reportedProfileId, this.skinGetter);
		skinReport.comments = this.comments;
		skinReport.reason = this.reason;
		return skinReport;
	}

	@Override
	public Screen createScreen(Screen screen, ReportingContext reportingContext) {
		return new SkinReportScreen(screen, reportingContext, this);
	}

	@Environment(EnvType.CLIENT)
	public static class Builder extends Report.Builder<SkinReport> {
		public Builder(SkinReport skinReport, AbuseReportLimits abuseReportLimits) {
			super(skinReport, abuseReportLimits);
		}

		public Builder(UUID uUID, Supplier<PlayerSkin> supplier, AbuseReportLimits abuseReportLimits) {
			super(new SkinReport(UUID.randomUUID(), Instant.now(), uUID, supplier), abuseReportLimits);
		}

		@Override
		public boolean hasContent() {
			return StringUtils.isNotEmpty(this.comments()) || this.reason() != null;
		}

		@Nullable
		@Override
		public Report.CannotBuildReason checkBuildable() {
			if (this.report.reason == null) {
				return Report.CannotBuildReason.NO_REASON;
			} else {
				return this.report.comments.length() > this.limits.maxOpinionCommentsLength() ? Report.CannotBuildReason.COMMENT_TOO_LONG : null;
			}
		}

		@Override
		public Either<Report.Result, Report.CannotBuildReason> build(ReportingContext reportingContext) {
			Report.CannotBuildReason cannotBuildReason = this.checkBuildable();
			if (cannotBuildReason != null) {
				return Either.right(cannotBuildReason);
			} else {
				String string = ((ReportReason)Objects.requireNonNull(this.report.reason)).backendName();
				ReportedEntity reportedEntity = new ReportedEntity(this.report.reportedProfileId);
				PlayerSkin playerSkin = (PlayerSkin)this.report.skinGetter.get();
				String string2 = playerSkin.textureUrl();
				AbuseReport abuseReport = AbuseReport.skin(this.report.comments, string, string2, reportedEntity, this.report.createdAt);
				return Either.left(new Report.Result(this.report.reportId, ReportType.SKIN, abuseReport));
			}
		}
	}
}
