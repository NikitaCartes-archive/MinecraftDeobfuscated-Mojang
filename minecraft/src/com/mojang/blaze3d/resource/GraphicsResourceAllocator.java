package com.mojang.blaze3d.resource;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface GraphicsResourceAllocator {
	GraphicsResourceAllocator UNPOOLED = new GraphicsResourceAllocator() {
		@Override
		public <T> T acquire(ResourceDescriptor<T> resourceDescriptor) {
			return resourceDescriptor.allocate();
		}

		@Override
		public <T> void release(ResourceDescriptor<T> resourceDescriptor, T object) {
			resourceDescriptor.free(object);
		}
	};

	<T> T acquire(ResourceDescriptor<T> resourceDescriptor);

	<T> void release(ResourceDescriptor<T> resourceDescriptor, T object);
}
