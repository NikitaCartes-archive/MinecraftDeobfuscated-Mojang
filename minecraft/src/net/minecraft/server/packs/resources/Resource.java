package net.minecraft.server.packs.resources;

import com.google.common.annotations.VisibleForTesting;
import java.io.Closeable;
import java.io.InputStream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

public interface Resource extends Closeable {
	ResourceLocation getLocation();

	InputStream getInputStream();

	@VisibleForTesting
	boolean hasMetadata();

	@Nullable
	<T> T getMetadata(MetadataSectionSerializer<T> metadataSectionSerializer);

	String getSourceName();
}
