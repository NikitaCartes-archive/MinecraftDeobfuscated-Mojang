package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.server.packs.PackResources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ResourceLoadStateTracker {
	private static final Logger LOGGER = LogManager.getLogger();
	@Nullable
	private ResourceLoadStateTracker.ReloadState reloadState;
	private int reloadCount;

	public void startReload(ResourceLoadStateTracker.ReloadReason reloadReason, List<PackResources> list) {
		this.reloadCount++;
		if (this.reloadState != null && !this.reloadState.finished) {
			LOGGER.warn("Reload already ongoing, replacing");
		}

		this.reloadState = new ResourceLoadStateTracker.ReloadState(
			reloadReason, (List<String>)list.stream().map(PackResources::getName).collect(ImmutableList.toImmutableList())
		);
	}

	public void startRecovery(Throwable throwable) {
		if (this.reloadState == null) {
			LOGGER.warn("Trying to signal reload recovery, but nothing was started");
			this.reloadState = new ResourceLoadStateTracker.ReloadState(ResourceLoadStateTracker.ReloadReason.UNKNOWN, ImmutableList.of());
		}

		this.reloadState.recoveryReloadInfo = new ResourceLoadStateTracker.RecoveryInfo(throwable);
	}

	public void finishReload() {
		if (this.reloadState == null) {
			LOGGER.warn("Trying to finish reload, but nothing was started");
		} else {
			this.reloadState.finished = true;
		}
	}

	public void fillCrashReport(CrashReport crashReport) {
		CrashReportCategory crashReportCategory = crashReport.addCategory("Last reload");
		crashReportCategory.setDetail("Reload number", this.reloadCount);
		if (this.reloadState != null) {
			this.reloadState.fillCrashInfo(crashReportCategory);
		}
	}

	@Environment(EnvType.CLIENT)
	static class RecoveryInfo {
		private final Throwable error;

		RecoveryInfo(Throwable throwable) {
			this.error = throwable;
		}

		public void fillCrashInfo(CrashReportCategory crashReportCategory) {
			crashReportCategory.setDetail("Recovery", "Yes");
			crashReportCategory.setDetail("Recovery reason", (CrashReportDetail<String>)(() -> {
				StringWriter stringWriter = new StringWriter();
				this.error.printStackTrace(new PrintWriter(stringWriter));
				return stringWriter.toString();
			}));
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum ReloadReason {
		INITIAL("initial"),
		MANUAL("manual"),
		UNKNOWN("unknown");

		final String name;

		private ReloadReason(String string2) {
			this.name = string2;
		}
	}

	@Environment(EnvType.CLIENT)
	static class ReloadState {
		private final ResourceLoadStateTracker.ReloadReason reloadReason;
		private final List<String> packs;
		@Nullable
		ResourceLoadStateTracker.RecoveryInfo recoveryReloadInfo;
		boolean finished;

		ReloadState(ResourceLoadStateTracker.ReloadReason reloadReason, List<String> list) {
			this.reloadReason = reloadReason;
			this.packs = list;
		}

		public void fillCrashInfo(CrashReportCategory crashReportCategory) {
			crashReportCategory.setDetail("Reload reason", this.reloadReason.name);
			crashReportCategory.setDetail("Finished", this.finished ? "Yes" : "No");
			crashReportCategory.setDetail("Packs", (CrashReportDetail<String>)(() -> String.join(", ", this.packs)));
			if (this.recoveryReloadInfo != null) {
				this.recoveryReloadInfo.fillCrashInfo(crashReportCategory);
			}
		}
	}
}
