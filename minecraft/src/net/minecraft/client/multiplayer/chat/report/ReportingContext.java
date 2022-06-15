package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.UserApiService;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.RollingMemoryChatLog;

@Environment(EnvType.CLIENT)
public record ReportingContext(AbuseReportSender sender, ReportEnvironment environment, ChatLog chatLog) {
	private static final int LOG_CAPACITY = 1024;

	public static ReportingContext create(ReportEnvironment reportEnvironment, UserApiService userApiService) {
		RollingMemoryChatLog rollingMemoryChatLog = new RollingMemoryChatLog(1024);
		AbuseReportSender abuseReportSender = AbuseReportSender.create(reportEnvironment, userApiService);
		return new ReportingContext(abuseReportSender, reportEnvironment, rollingMemoryChatLog);
	}

	public boolean matches(ReportEnvironment reportEnvironment) {
		return Objects.equals(this.environment, reportEnvironment);
	}
}
