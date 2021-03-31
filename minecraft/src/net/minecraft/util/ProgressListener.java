package net.minecraft.util;

import net.minecraft.network.chat.Component;

public interface ProgressListener {
	void progressStartNoAbort(Component component);

	void progressStart(Component component);

	void progressStage(Component component);

	void progressStagePercentage(int i);

	void stop();
}
