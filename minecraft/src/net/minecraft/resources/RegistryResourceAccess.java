package net.minecraft.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public interface RegistryResourceAccess {
	<E> Collection<ResourceKey<E>> listResources(ResourceKey<? extends Registry<E>> resourceKey);

	<E> Optional<DataResult<RegistryResourceAccess.ParsedEntry<E>>> parseElement(
		DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder
	);

	static RegistryResourceAccess forResourceManager(ResourceManager resourceManager) {
		return new RegistryResourceAccess() {
			private static final String JSON = ".json";

			@Override
			public <E> Collection<ResourceKey<E>> listResources(ResourceKey<? extends Registry<E>> resourceKey) {
				String string = registryDirPath(resourceKey);
				Set<ResourceKey<E>> set = new HashSet();
				resourceManager.listResources(string, stringx -> stringx.endsWith(".json")).forEach(resourceLocation -> {
					String string2 = resourceLocation.getPath();
					String string3 = string2.substring(string.length() + 1, string2.length() - ".json".length());
					set.add(ResourceKey.create(resourceKey, new ResourceLocation(resourceLocation.getNamespace(), string3)));
				});
				return set;
			}

			@Override
			public <E> Optional<DataResult<RegistryResourceAccess.ParsedEntry<E>>> parseElement(
				DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder
			) {
				ResourceLocation resourceLocation = elementPath(resourceKey, resourceKey2);
				if (!resourceManager.hasResource(resourceLocation)) {
					return Optional.empty();
				} else {
					try {
						Resource resource = resourceManager.getResource(resourceLocation);

						Optional var9;
						try {
							Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);

							try {
								JsonElement jsonElement = JsonParser.parseReader(reader);
								var9 = Optional.of(decoder.parse(dynamicOps, jsonElement).map(RegistryResourceAccess.ParsedEntry::createWithoutId));
							} catch (Throwable var12) {
								try {
									reader.close();
								} catch (Throwable var11) {
									var12.addSuppressed(var11);
								}

								throw var12;
							}

							reader.close();
						} catch (Throwable var13) {
							if (resource != null) {
								try {
									resource.close();
								} catch (Throwable var10) {
									var13.addSuppressed(var10);
								}
							}

							throw var13;
						}

						if (resource != null) {
							resource.close();
						}

						return var9;
					} catch (JsonIOException | JsonSyntaxException | IOException var14) {
						return Optional.of(DataResult.error("Failed to parse " + resourceLocation + " file: " + var14.getMessage()));
					}
				}
			}

			private static String registryDirPath(ResourceKey<? extends Registry<?>> resourceKey) {
				return resourceKey.location().getPath();
			}

			private static <E> ResourceLocation elementPath(ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2) {
				return new ResourceLocation(resourceKey2.location().getNamespace(), registryDirPath(resourceKey) + "/" + resourceKey2.location().getPath() + ".json");
			}

			public String toString() {
				return "ResourceAccess[" + resourceManager + "]";
			}
		};
	}

	public static final class InMemoryStorage implements RegistryResourceAccess {
		private static final Logger LOGGER = LogUtils.getLogger();
		private final Map<ResourceKey<?>, RegistryResourceAccess.InMemoryStorage.Entry> entries = Maps.<ResourceKey<?>, RegistryResourceAccess.InMemoryStorage.Entry>newIdentityHashMap();

		public <E> void add(RegistryAccess registryAccess, ResourceKey<E> resourceKey, Encoder<E> encoder, int i, E object, Lifecycle lifecycle) {
			DataResult<JsonElement> dataResult = encoder.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registryAccess), object);
			Optional<PartialResult<JsonElement>> optional = dataResult.error();
			if (optional.isPresent()) {
				LOGGER.error("Error adding element: {}", ((PartialResult)optional.get()).message());
			} else {
				this.entries.put(resourceKey, new RegistryResourceAccess.InMemoryStorage.Entry((JsonElement)dataResult.result().get(), i, lifecycle));
			}
		}

		@Override
		public <E> Collection<ResourceKey<E>> listResources(ResourceKey<? extends Registry<E>> resourceKey) {
			return (Collection<ResourceKey<E>>)this.entries
				.keySet()
				.stream()
				.flatMap(resourceKey2 -> resourceKey2.cast(resourceKey).stream())
				.collect(Collectors.toList());
		}

		@Override
		public <E> Optional<DataResult<RegistryResourceAccess.ParsedEntry<E>>> parseElement(
			DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder
		) {
			RegistryResourceAccess.InMemoryStorage.Entry entry = (RegistryResourceAccess.InMemoryStorage.Entry)this.entries.get(resourceKey2);
			return entry == null
				? Optional.of(DataResult.error("Unknown element: " + resourceKey2))
				: Optional.of(
					decoder.parse(dynamicOps, entry.data).setLifecycle(entry.lifecycle).map(object -> RegistryResourceAccess.ParsedEntry.createWithId(object, entry.id))
				);
		}

		static record Entry(JsonElement data, int id, Lifecycle lifecycle) {
		}
	}

	public static record ParsedEntry<E>(E value, OptionalInt fixedId) {
		public static <E> RegistryResourceAccess.ParsedEntry<E> createWithoutId(E object) {
			return new RegistryResourceAccess.ParsedEntry<>(object, OptionalInt.empty());
		}

		public static <E> RegistryResourceAccess.ParsedEntry<E> createWithId(E object, int i) {
			return new RegistryResourceAccess.ParsedEntry<>(object, OptionalInt.of(i));
		}
	}
}
