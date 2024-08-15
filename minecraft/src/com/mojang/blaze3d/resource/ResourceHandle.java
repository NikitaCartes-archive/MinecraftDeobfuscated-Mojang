package com.mojang.blaze3d.resource;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ResourceHandle<T> {
	ResourceHandle<?> INVALID_HANDLE = () -> {
		throw new IllegalStateException("Cannot dereference handle with no underlying resource");
	};

	static <T> ResourceHandle<T> invalid() {
		return (ResourceHandle<T>)INVALID_HANDLE;
	}

	T get();
}
