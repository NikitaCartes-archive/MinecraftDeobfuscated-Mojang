package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FallbackResourceManager implements ResourceManager {
	static final Logger LOGGER = LogManager.getLogger();
	protected final List<PackResources> fallbacks = Lists.<PackResources>newArrayList();
	private final PackType type;
	private final String namespace;

	public FallbackResourceManager(PackType packType, String string) {
		this.type = packType;
		this.namespace = string;
	}

	public void add(PackResources packResources) {
		this.fallbacks.add(packResources);
	}

	@Override
	public Set<String> getNamespaces() {
		return ImmutableSet.of(this.namespace);
	}

	@Override
	public Resource getResource(ResourceLocation resourceLocation) throws IOException {
		this.validateLocation(resourceLocation);
		PackResources packResources = null;
		ResourceLocation resourceLocation2 = getMetadataLocation(resourceLocation);

		for (int i = this.fallbacks.size() - 1; i >= 0; i--) {
			PackResources packResources2 = (PackResources)this.fallbacks.get(i);
			if (packResources == null && packResources2.hasResource(this.type, resourceLocation2)) {
				packResources = packResources2;
			}

			if (packResources2.hasResource(this.type, resourceLocation)) {
				InputStream inputStream = null;
				if (packResources != null) {
					inputStream = this.getWrappedResource(resourceLocation2, packResources);
				}

				return new SimpleResource(packResources2.getName(), resourceLocation, this.getWrappedResource(resourceLocation, packResources2), inputStream);
			}
		}

		throw new FileNotFoundException(resourceLocation.toString());
	}

	@Override
	public boolean hasResource(ResourceLocation resourceLocation) {
		if (!this.isValidLocation(resourceLocation)) {
			return false;
		} else {
			for (int i = this.fallbacks.size() - 1; i >= 0; i--) {
				PackResources packResources = (PackResources)this.fallbacks.get(i);
				if (packResources.hasResource(this.type, resourceLocation)) {
					return true;
				}
			}

			return false;
		}
	}

	protected InputStream getWrappedResource(ResourceLocation resourceLocation, PackResources packResources) throws IOException {
		InputStream inputStream = packResources.getResource(this.type, resourceLocation);
		return (InputStream)(LOGGER.isDebugEnabled()
			? new FallbackResourceManager.LeakedResourceWarningInputStream(inputStream, resourceLocation, packResources.getName())
			: inputStream);
	}

	private void validateLocation(ResourceLocation resourceLocation) throws IOException {
		if (!this.isValidLocation(resourceLocation)) {
			throw new IOException("Invalid relative path to resource: " + resourceLocation);
		}
	}

	private boolean isValidLocation(ResourceLocation resourceLocation) {
		return !resourceLocation.getPath().contains("..");
	}

	@Override
	public List<Resource> getResources(ResourceLocation resourceLocation) throws IOException {
		this.validateLocation(resourceLocation);
		List<Resource> list = Lists.<Resource>newArrayList();
		ResourceLocation resourceLocation2 = getMetadataLocation(resourceLocation);

		for (PackResources packResources : this.fallbacks) {
			if (packResources.hasResource(this.type, resourceLocation)) {
				InputStream inputStream = packResources.hasResource(this.type, resourceLocation2) ? this.getWrappedResource(resourceLocation2, packResources) : null;
				list.add(new SimpleResource(packResources.getName(), resourceLocation, this.getWrappedResource(resourceLocation, packResources), inputStream));
			}
		}

		if (list.isEmpty()) {
			throw new FileNotFoundException(resourceLocation.toString());
		} else {
			return list;
		}
	}

	@Override
	public Collection<ResourceLocation> listResources(String string, Predicate<String> predicate) {
		List<ResourceLocation> list = Lists.<ResourceLocation>newArrayList();

		for (PackResources packResources : this.fallbacks) {
			list.addAll(packResources.getResources(this.type, this.namespace, string, Integer.MAX_VALUE, predicate));
		}

		Collections.sort(list);
		return list;
	}

	@Override
	public Stream<PackResources> listPacks() {
		return this.fallbacks.stream();
	}

	static ResourceLocation getMetadataLocation(ResourceLocation resourceLocation) {
		return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + ".mcmeta");
	}

	static class LeakedResourceWarningInputStream extends FilterInputStream {
		private final String message;
		private boolean closed;

		public LeakedResourceWarningInputStream(InputStream inputStream, ResourceLocation resourceLocation, String string) {
			super(inputStream);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			new Exception().printStackTrace(new PrintStream(byteArrayOutputStream));
			this.message = "Leaked resource: '" + resourceLocation + "' loaded from pack: '" + string + "'\n" + byteArrayOutputStream;
		}

		public void close() throws IOException {
			super.close();
			this.closed = true;
		}

		protected void finalize() throws Throwable {
			if (!this.closed) {
				FallbackResourceManager.LOGGER.warn(this.message);
			}

			super.finalize();
		}
	}
}
