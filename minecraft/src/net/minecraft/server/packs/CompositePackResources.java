package net.minecraft.server.packs;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;

public class CompositePackResources implements PackResources {
	private final PackResources primaryPackResources;
	private final List<PackResources> packResourcesStack;

	public CompositePackResources(PackResources packResources, List<PackResources> list) {
		this.primaryPackResources = packResources;
		List<PackResources> list2 = new ArrayList(list.size() + 1);
		list2.addAll(Lists.reverse(list));
		list2.add(packResources);
		this.packResourcesStack = List.copyOf(list2);
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getRootResource(String... strings) {
		return this.primaryPackResources.getRootResource(strings);
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
		for (PackResources packResources : this.packResourcesStack) {
			IoSupplier<InputStream> ioSupplier = packResources.getResource(packType, resourceLocation);
			if (ioSupplier != null) {
				return ioSupplier;
			}
		}

		return null;
	}

	@Override
	public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
		Map<ResourceLocation, IoSupplier<InputStream>> map = new HashMap();

		for (PackResources packResources : this.packResourcesStack) {
			packResources.listResources(packType, string, string2, map::putIfAbsent);
		}

		map.forEach(resourceOutput);
	}

	@Override
	public Set<String> getNamespaces(PackType packType) {
		Set<String> set = new HashSet();

		for (PackResources packResources : this.packResourcesStack) {
			set.addAll(packResources.getNamespaces(packType));
		}

		return set;
	}

	@Nullable
	@Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
		return this.primaryPackResources.getMetadataSection(metadataSectionSerializer);
	}

	@Override
	public String packId() {
		return this.primaryPackResources.packId();
	}

	@Override
	public boolean isBuiltin() {
		return this.primaryPackResources.isBuiltin();
	}

	@Override
	public void close() {
		this.packResourcesStack.forEach(PackResources::close);
	}
}
