package net.minecraft.resources;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistryReadOps<T> extends DelegatingOps<T> {
	private static final Logger LOGGER = LogManager.getLogger();
	private final RegistryReadOps.ResourceAccess resources;
	private final RegistryAccess.RegistryHolder registryHolder;
	private final Map<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> readCache;
	private final RegistryReadOps<JsonElement> jsonOps;

	public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess.RegistryHolder registryHolder) {
		return create(dynamicOps, RegistryReadOps.ResourceAccess.forResourceManager(resourceManager), registryHolder);
	}

	public static <T> RegistryReadOps<T> create(
		DynamicOps<T> dynamicOps, RegistryReadOps.ResourceAccess resourceAccess, RegistryAccess.RegistryHolder registryHolder
	) {
		RegistryReadOps<T> registryReadOps = new RegistryReadOps<>(dynamicOps, resourceAccess, registryHolder, Maps.newIdentityHashMap());
		RegistryAccess.load(registryHolder, registryReadOps);
		return registryReadOps;
	}

	private RegistryReadOps(
		DynamicOps<T> dynamicOps,
		RegistryReadOps.ResourceAccess resourceAccess,
		RegistryAccess.RegistryHolder registryHolder,
		IdentityHashMap<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> identityHashMap
	) {
		super(dynamicOps);
		this.resources = resourceAccess;
		this.registryHolder = registryHolder;
		this.readCache = identityHashMap;
		this.jsonOps = dynamicOps == JsonOps.INSTANCE ? this : new RegistryReadOps<>(JsonOps.INSTANCE, resourceAccess, registryHolder, identityHashMap);
	}

	protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T object, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
		Optional<WritableRegistry<E>> optional = this.registryHolder.registry(resourceKey);
		if (!optional.isPresent()) {
			return DataResult.error("Unknown registry: " + resourceKey);
		} else {
			WritableRegistry<E> writableRegistry = (WritableRegistry<E>)optional.get();
			DataResult<Pair<ResourceLocation, T>> dataResult = ResourceLocation.CODEC.decode(this.delegate, object);
			if (!dataResult.result().isPresent()) {
				return !bl ? DataResult.error("Inline definitions not allowed here") : codec.decode(this, object).map(pairx -> pairx.mapFirst(objectx -> () -> objectx));
			} else {
				Pair<ResourceLocation, T> pair = (Pair<ResourceLocation, T>)dataResult.result().get();
				ResourceLocation resourceLocation = pair.getFirst();
				return this.readAndRegisterElement(resourceKey, writableRegistry, codec, resourceLocation).map(supplier -> Pair.of(supplier, pair.getSecond()));
			}
		}
	}

	public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> mappedRegistry, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
		Collection<ResourceLocation> collection = this.resources.listResources(resourceKey);
		DataResult<MappedRegistry<E>> dataResult = DataResult.success(mappedRegistry, Lifecycle.stable());
		String string = resourceKey.location().getPath() + "/";

		for (ResourceLocation resourceLocation : collection) {
			String string2 = resourceLocation.getPath();
			if (!string2.endsWith(".json")) {
				LOGGER.warn("Skipping resource {} since it is not a json file", resourceLocation);
			} else if (!string2.startsWith(string)) {
				LOGGER.warn("Skipping resource {} since it does not have a registry name prefix", resourceLocation);
			} else {
				String string3 = string2.substring(string.length(), string2.length() - ".json".length());
				ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), string3);
				dataResult = dataResult.flatMap(
					mappedRegistryx -> this.readAndRegisterElement(resourceKey, mappedRegistryx, codec, resourceLocation2).map(supplier -> mappedRegistryx)
				);
			}
		}

		return dataResult.setPartial(mappedRegistry);
	}

	private <E> DataResult<Supplier<E>> readAndRegisterElement(
		ResourceKey<? extends Registry<E>> resourceKey, WritableRegistry<E> writableRegistry, Codec<E> codec, ResourceLocation resourceLocation
	) {
		ResourceKey<E> resourceKey2 = ResourceKey.create(resourceKey, resourceLocation);
		RegistryReadOps.ReadCache<E> readCache = this.readCache(resourceKey);
		DataResult<Supplier<E>> dataResult = (DataResult<Supplier<E>>)readCache.values.get(resourceKey2);
		if (dataResult != null) {
			return dataResult;
		} else {
			Supplier<E> supplier = Suppliers.memoize(() -> {
				E object = writableRegistry.get(resourceKey2);
				if (object == null) {
					throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + resourceKey2);
				} else {
					return (T)object;
				}
			});
			readCache.values.put(resourceKey2, DataResult.success(supplier));
			DataResult<Pair<E, OptionalInt>> dataResult2 = this.resources.parseElement(this.jsonOps, resourceKey, resourceKey2, codec);
			Optional<Pair<E, OptionalInt>> optional = dataResult2.result();
			if (optional.isPresent()) {
				Pair<E, OptionalInt> pair = (Pair<E, OptionalInt>)optional.get();
				writableRegistry.registerOrOverride(pair.getSecond(), resourceKey2, pair.getFirst(), dataResult2.lifecycle());
			}

			DataResult<Supplier<E>> dataResult3;
			if (!optional.isPresent() && writableRegistry.get(resourceKey2) != null) {
				dataResult3 = DataResult.success(() -> writableRegistry.get(resourceKey2), Lifecycle.stable());
			} else {
				dataResult3 = dataResult2.map(pair -> () -> writableRegistry.get(resourceKey2));
			}

			readCache.values.put(resourceKey2, dataResult3);
			return dataResult3;
		}
	}

	private <E> RegistryReadOps.ReadCache<E> readCache(ResourceKey<? extends Registry<E>> resourceKey) {
		return (RegistryReadOps.ReadCache<E>)this.readCache.computeIfAbsent(resourceKey, resourceKeyx -> new RegistryReadOps.ReadCache());
	}

	protected <E> DataResult<Registry<E>> registry(ResourceKey<? extends Registry<E>> resourceKey) {
		return (DataResult<Registry<E>>)this.registryHolder
			.registry(resourceKey)
			.map(writableRegistry -> DataResult.success(writableRegistry, writableRegistry.elementsLifecycle()))
			.orElseGet(() -> DataResult.error("Unknown registry: " + resourceKey));
	}

	static final class ReadCache<E> {
		private final Map<ResourceKey<E>, DataResult<Supplier<E>>> values = Maps.<ResourceKey<E>, DataResult<Supplier<E>>>newIdentityHashMap();

		private ReadCache() {
		}
	}

	public interface ResourceAccess {
		Collection<ResourceLocation> listResources(ResourceKey<? extends Registry<?>> resourceKey);

		<E> DataResult<Pair<E, OptionalInt>> parseElement(
			DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder
		);

		static RegistryReadOps.ResourceAccess forResourceManager(ResourceManager resourceManager) {
			return new RegistryReadOps.ResourceAccess() {
				@Override
				public Collection<ResourceLocation> listResources(ResourceKey<? extends Registry<?>> resourceKey) {
					return resourceManager.listResources(resourceKey.location().getPath(), string -> string.endsWith(".json"));
				}

				@Override
				public <E> DataResult<Pair<E, OptionalInt>> parseElement(
					DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder
				) {
					ResourceLocation resourceLocation = resourceKey2.location();
					ResourceLocation resourceLocation2 = new ResourceLocation(
						resourceLocation.getNamespace(), resourceKey.location().getPath() + "/" + resourceLocation.getPath() + ".json"
					);

					try {
						Resource resource = resourceManager.getResource(resourceLocation2);
						Throwable var8 = null;

						DataResult var13;
						try {
							Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
							Throwable var10 = null;

							try {
								JsonParser jsonParser = new JsonParser();
								JsonElement jsonElement = jsonParser.parse(reader);
								var13 = decoder.parse(dynamicOps, jsonElement).map(object -> Pair.of(object, OptionalInt.empty()));
							} catch (Throwable var38) {
								var10 = var38;
								throw var38;
							} finally {
								if (reader != null) {
									if (var10 != null) {
										try {
											reader.close();
										} catch (Throwable var37) {
											var10.addSuppressed(var37);
										}
									} else {
										reader.close();
									}
								}
							}
						} catch (Throwable var40) {
							var8 = var40;
							throw var40;
						} finally {
							if (resource != null) {
								if (var8 != null) {
									try {
										resource.close();
									} catch (Throwable var36) {
										var8.addSuppressed(var36);
									}
								} else {
									resource.close();
								}
							}
						}

						return var13;
					} catch (JsonIOException | JsonSyntaxException | IOException var42) {
						return DataResult.error("Failed to parse " + resourceLocation2 + " file: " + var42.getMessage());
					}
				}

				public String toString() {
					return "ResourceAccess[" + resourceManager + "]";
				}
			};
		}

		public static final class MemoryMap implements RegistryReadOps.ResourceAccess {
			private final Map<ResourceKey<?>, JsonElement> data = Maps.<ResourceKey<?>, JsonElement>newIdentityHashMap();
			private final Object2IntMap<ResourceKey<?>> ids = new Object2IntOpenCustomHashMap<>(Util.identityStrategy());
			private final Map<ResourceKey<?>, Lifecycle> lifecycles = Maps.<ResourceKey<?>, Lifecycle>newIdentityHashMap();

			public <E> void add(RegistryAccess.RegistryHolder registryHolder, ResourceKey<E> resourceKey, Encoder<E> encoder, int i, E object, Lifecycle lifecycle) {
				DataResult<JsonElement> dataResult = encoder.encodeStart(RegistryWriteOps.create(JsonOps.INSTANCE, registryHolder), object);
				Optional<PartialResult<JsonElement>> optional = dataResult.error();
				if (optional.isPresent()) {
					RegistryReadOps.LOGGER.error("Error adding element: {}", ((PartialResult)optional.get()).message());
				} else {
					this.data.put(resourceKey, dataResult.result().get());
					this.ids.put(resourceKey, i);
					this.lifecycles.put(resourceKey, lifecycle);
				}
			}

			@Override
			public Collection<ResourceLocation> listResources(ResourceKey<? extends Registry<?>> resourceKey) {
				return (Collection<ResourceLocation>)this.data
					.keySet()
					.stream()
					.filter(resourceKey2 -> resourceKey2.isFor(resourceKey))
					.map(
						resourceKey2 -> new ResourceLocation(
								resourceKey2.location().getNamespace(), resourceKey.location().getPath() + "/" + resourceKey2.location().getPath() + ".json"
							)
					)
					.collect(Collectors.toList());
			}

			@Override
			public <E> DataResult<Pair<E, OptionalInt>> parseElement(
				DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder
			) {
				JsonElement jsonElement = (JsonElement)this.data.get(resourceKey2);
				return jsonElement == null
					? DataResult.error("Unknown element: " + resourceKey2)
					: decoder.parse(dynamicOps, jsonElement)
						.setLifecycle((Lifecycle)this.lifecycles.get(resourceKey2))
						.map(object -> Pair.of(object, OptionalInt.of(this.ids.getInt(resourceKey2))));
			}
		}
	}
}
