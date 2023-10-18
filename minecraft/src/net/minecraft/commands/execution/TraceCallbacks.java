package net.minecraft.commands.execution;

import net.minecraft.resources.ResourceLocation;

public interface TraceCallbacks extends AutoCloseable {
	void onCommand(int i, String string);

	void onReturn(int i, String string, int j);

	void onError(String string);

	void onCall(int i, ResourceLocation resourceLocation, int j);

	void close();
}
