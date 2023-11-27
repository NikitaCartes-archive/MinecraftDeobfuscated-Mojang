package net.minecraft.client.resources.server;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface PackLoadFeedback {
	void reportUpdate(UUID uUID, PackLoadFeedback.Update update);

	void reportFinalResult(UUID uUID, PackLoadFeedback.FinalResult finalResult);

	@Environment(EnvType.CLIENT)
	public static enum FinalResult {
		DECLINED,
		APPLIED,
		DISCARDED,
		DOWNLOAD_FAILED,
		ACTIVATION_FAILED;
	}

	@Environment(EnvType.CLIENT)
	public static enum Update {
		ACCEPTED,
		DOWNLOADED;
	}
}
