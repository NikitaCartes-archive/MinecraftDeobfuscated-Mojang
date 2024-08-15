package com.mojang.blaze3d.framegraph;

import com.mojang.blaze3d.resource.ResourceDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface FramePass {
	<T> ResourceHandle<T> createsInternal(String string, ResourceDescriptor<T> resourceDescriptor);

	<T> void reads(ResourceHandle<T> resourceHandle);

	<T> ResourceHandle<T> readsAndWrites(ResourceHandle<T> resourceHandle);

	void requires(FramePass framePass);

	void disableCulling();

	void executes(Runnable runnable);
}
