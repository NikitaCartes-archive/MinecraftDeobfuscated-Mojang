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
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public interface RegistryResourceAccess {
	<E> Map<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>> listResources(ResourceKey<? extends Registry<E>> resourceKey);

	<E> Optional<RegistryResourceAccess.EntryThunk<E>> getResource(ResourceKey<E> resourceKey);

	static RegistryResourceAccess forResourceManager(ResourceManager resourceManager) {
		return new RegistryResourceAccess() {
			private static final String JSON = ".json";

			@Override
			public <E> Map<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>> listResources(ResourceKey<? extends Registry<E>> resourceKey) {
				String string = registryDirPath(resourceKey.location());
				Map<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>> map = Maps.<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>>newHashMap();
				resourceManager.listResources(string, resourceLocation -> resourceLocation.getPath().endsWith(".json")).forEach((resourceLocation, resourceThunk) -> {
					String string2 = resourceLocation.getPath();
					String string3 = string2.substring(string.length() + 1, string2.length() - ".json".length());
					ResourceKey<E> resourceKey2 = ResourceKey.create(resourceKey, new ResourceLocation(resourceLocation.getNamespace(), string3));
					map.put(resourceKey2, (RegistryResourceAccess.EntryThunk<>)(dynamicOps, decoder) -> {
						try {
							Resource resource = resourceThunk.open();

							DataResult var6x;
							try {
								var6x = this.decodeElement(dynamicOps, decoder, resource);
							} catch (Throwable var9) {
								if (resource != null) {
									try {
										resource.close();
									} catch (Throwable var8x) {
										var9.addSuppressed(var8x);
									}
								}

								throw var9;
							}

							if (resource != null) {
								resource.close();
							}

							return var6x;
						} catch (JsonIOException | JsonSyntaxException | IOException var10) {
							return DataResult.error("Failed to parse " + resourceLocation + " file: " + var10.getMessage());
						}
					});
				});
				return map;
			}

			@Override
			public <E> Optional<RegistryResourceAccess.EntryThunk<E>> getResource(ResourceKey<E> resourceKey) {
				ResourceLocation resourceLocation = elementPath(resourceKey);
				return !resourceManager.hasResource(resourceLocation) ? Optional.empty() : Optional.of((RegistryResourceAccess.EntryThunk<>)(dynamicOps, decoder) -> {
					try {
						Resource resource = resourceManager.getResource(resourceLocation);

						DataResult var6;
						try {
							var6 = this.decodeElement(dynamicOps, decoder, resource);
						} catch (Throwable var9) {
							if (resource != null) {
								try {
									resource.close();
								} catch (Throwable var8) {
									var9.addSuppressed(var8);
								}
							}

							throw var9;
						}

						if (resource != null) {
							resource.close();
						}

						return var6;
					} catch (JsonIOException | JsonSyntaxException | IOException var10) {
						return DataResult.error("Failed to parse " + resourceLocation + " file: " + var10.getMessage());
					}
				});
			}

			private <E> DataResult<RegistryResourceAccess.ParsedEntry<E>> decodeElement(DynamicOps<JsonElement> dynamicOps, Decoder<E> decoder, Resource resource) throws IOException {
				Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);

				DataResult var6;
				try {
					JsonElement jsonElement = JsonParser.parseReader(reader);
					var6 = decoder.parse(dynamicOps, jsonElement).map(RegistryResourceAccess.ParsedEntry::createWithoutId);
				} catch (Throwable var8) {
					try {
						reader.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}

					throw var8;
				}

				reader.close();
				return var6;
			}

			private static String registryDirPath(ResourceLocation resourceLocation) {
				return resourceLocation.getPath();
			}

			private static <E> ResourceLocation elementPath(ResourceKey<E> resourceKey) {
				return new ResourceLocation(
					resourceKey.location().getNamespace(), registryDirPath(resourceKey.registry()) + "/" + resourceKey.location().getPath() + ".json"
				);
			}

			public String toString() {
				return "ResourceAccess[" + resourceManager + "]";
			}
		};
	}

	@FunctionalInterface
	public interface EntryThunk<E> {
		DataResult<RegistryResourceAccess.ParsedEntry<E>> parseElement(DynamicOps<JsonElement> dynamicOps, Decoder<E> decoder);
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
		public <E> Map<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>> listResources(ResourceKey<? extends Registry<E>> resourceKey) {
			return (Map<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>>)this.entries
				.entrySet()
				.stream()
				.filter(entry -> ((ResourceKey)entry.getKey()).isFor(resourceKey))
				.collect(Collectors.toMap(entry -> (ResourceKey)entry.getKey(), entry -> ((RegistryResourceAccess.InMemoryStorage.Entry)entry.getValue())::parse));
		}

		@Override
		public <E> Optional<RegistryResourceAccess.EntryThunk<E>> getResource(ResourceKey<E> resourceKey) {
			RegistryResourceAccess.InMemoryStorage.Entry entry = (RegistryResourceAccess.InMemoryStorage.Entry)this.entries.get(resourceKey);
			if (entry == null) {
				DataResult<RegistryResourceAccess.ParsedEntry<E>> dataResult = DataResult.error("Unknown element: " + resourceKey);
				return Optional.of((RegistryResourceAccess.EntryThunk<>)(dynamicOps, decoder) -> dataResult);
			} else {
				return Optional.of(entry::parse);
			}
		}

		static record Entry(JsonElement data, int id, Lifecycle lifecycle) {
			public <E> DataResult<RegistryResourceAccess.ParsedEntry<E>> parse(DynamicOps<JsonElement> dynamicOps, Decoder<E> decoder) {
				return decoder.parse(dynamicOps, this.data).setLifecycle(this.lifecycle).map(object -> RegistryResourceAccess.ParsedEntry.createWithId(object, this.id));
			}
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
