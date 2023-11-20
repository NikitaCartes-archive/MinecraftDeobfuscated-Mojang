package net.minecraft.client.resources.server;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface PackLoadFeedback {
	void sendResponse(UUID uUID, PackLoadFeedback.Result result);

	@Environment(EnvType.CLIENT)
	public static enum Result {
		ACCEPTED,
		DECLINED,
		APPLIED,
		DISCARDED,
		DOWNLOAD_FAILED,
		ACTIVATION_FAILED;
	}
}
