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
	static final Logger LOGGER = LogManager.getLogger();
	private static final String JSON = ".json";
	private final RegistryReadOps.ResourceAccess resources;
	private final RegistryAccess registryAccess;
	private final Map<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> readCache;
	private final RegistryReadOps<JsonElement> jsonOps;

	public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess registryAccess) {
		return createAndLoad(dynamicOps, RegistryReadOps.ResourceAccess.forResourceManager(resourceManager), registryAccess);
	}

	public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> dynamicOps, RegistryReadOps.ResourceAccess resourceAccess, RegistryAccess registryAccess) {
		RegistryReadOps<T> registryReadOps = new RegistryReadOps<>(dynamicOps, resourceAccess, registryAccess, Maps.newIdentityHashMap());
		RegistryAccess.load(registryAccess, registryReadOps);
		return registryReadOps;
	}

	public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess registryAccess) {
		return create(dynamicOps, RegistryReadOps.ResourceAccess.forResourceManager(resourceManager), registryAccess);
	}

	public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, RegistryReadOps.ResourceAccess resourceAccess, RegistryAccess registryAccess) {
		return new RegistryReadOps<>(dynamicOps, resourceAccess, registryAccess, Maps.newIdentityHashMap());
	}

	private RegistryReadOps(
		DynamicOps<T> dynamicOps,
		RegistryReadOps.ResourceAccess resourceAccess,
		RegistryAccess registryAccess,
		IdentityHashMap<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> identityHashMap
	) {
		super(dynamicOps);
		this.resources = resourceAccess;
		this.registryAccess = registryAccess;
		this.readCache = identityHashMap;
		this.jsonOps = dynamicOps == JsonOps.INSTANCE ? this : new RegistryReadOps<>(JsonOps.INSTANCE, resourceAccess, registryAccess, identityHashMap);
	}

	protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T object, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
		Optional<WritableRegistry<E>> optional = this.registryAccess.ownedRegistry(resourceKey);
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
		final ResourceKey<E> resourceKey2 = ResourceKey.create(resourceKey, resourceLocation);
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
			Optional<DataResult<Pair<E, OptionalInt>>> optional = this.resources.parseElement(this.jsonOps, resourceKey, resourceKey2, codec);
			DataResult<Supplier<E>> dataResult2;
			if (!optional.isPresent()) {
				dataResult2 = DataResult.success(new Supplier<E>() {
					public E get() {
						return writableRegistry.get(resourceKey2);
					}

					public String toString() {
						return resourceKey2.toString();
					}
				}, Lifecycle.stable());
			} else {
				DataResult<Pair<E, OptionalInt>> dataResult3 = (DataResult<Pair<E, OptionalInt>>)optional.get();
				Optional<Pair<E, OptionalInt>> optional2 = dataResult3.result();
				if (optional2.isPresent()) {
					Pair<E, OptionalInt> pair = (Pair<E, OptionalInt>)optional2.get();
					writableRegistry.registerOrOverride(pair.getSecond(), resourceKey2, pair.getFirst(), dataResult3.lifecycle());
				}

				dataResult2 = dataResult3.map(pairx -> () -> writableRegistry.get(resourceKey2));
			}

			readCache.values.put(resourceKey2, dataResult2);
			return dataResult2;
		}
	}

	private <E> RegistryReadOps.ReadCache<E> readCache(ResourceKey<? extends Registry<E>> resourceKey) {
		return (RegistryReadOps.ReadCache<E>)this.readCache.computeIfAbsent(resourceKey, resourceKeyx -> new RegistryReadOps.ReadCache());
	}

	protected <E> DataResult<Registry<E>> registry(ResourceKey<? extends Registry<E>> resourceKey) {
		return (DataResult<Registry<E>>)this.registryAccess
			.ownedRegistry(resourceKey)
			.map(writableRegistry -> DataResult.success(writableRegistry, writableRegistry.elementsLifecycle()))
			.orElseGet(() -> DataResult.error("Unknown registry: " + resourceKey));
	}

	static final class ReadCache<E> {
		final Map<ResourceKey<E>, DataResult<Supplier<E>>> values = Maps.<ResourceKey<E>, DataResult<Supplier<E>>>newIdentityHashMap();
	}

	public interface ResourceAccess {
		Collection<ResourceLocation> listResources(ResourceKey<? extends Registry<?>> resourceKey);

		<E> Optional<DataResult<Pair<E, OptionalInt>>> parseElement(
			DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder
		);

		static RegistryReadOps.ResourceAccess forResourceManager(ResourceManager resourceManager) {
			return new RegistryReadOps.ResourceAccess() {
				@Override
				public Collection<ResourceLocation> listResources(ResourceKey<? extends Registry<?>> resourceKey) {
					return resourceManager.listResources(resourceKey.location().getPath(), string -> string.endsWith(".json"));
				}

				@Override
				public <E> Optional<DataResult<Pair<E, OptionalInt>>> parseElement(
					DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder
				) {
					ResourceLocation resourceLocation = resourceKey2.location();
					ResourceLocation resourceLocation2 = new ResourceLocation(
						resourceLocation.getNamespace(), resourceKey.location().getPath() + "/" + resourceLocation.getPath() + ".json"
					);
					if (!resourceManager.hasResource(resourceLocation2)) {
						return Optional.empty();
					} else {
						try {
							Resource resource = resourceManager.getResource(resourceLocation2);

							Optional var11;
							try {
								Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);

								try {
									JsonParser jsonParser = new JsonParser();
									JsonElement jsonElement = jsonParser.parse(reader);
									var11 = Optional.of(decoder.parse(dynamicOps, jsonElement).map(object -> Pair.of(object, OptionalInt.empty())));
								} catch (Throwable var14) {
									try {
										reader.close();
									} catch (Throwable var13) {
										var14.addSuppressed(var13);
									}

									throw var14;
								}

								reader.close();
							} catch (Throwable var15) {
								if (resource != null) {
									try {
										resource.close();
									} catch (Throwable var12) {
										var15.addSuppressed(var12);
									}
								}

								throw var15;
							}

							if (resource != null) {
								resource.close();
							}

							return var11;
						} catch (JsonIOException | JsonSyntaxException | IOException var16) {
							return Optional.of(DataResult.error("Failed to parse " + resourceLocation2 + " file: " + var16.getMessage()));
						}
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
					this.data.put(resourceKey, (JsonElement)dataResult.result().get());
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
			public <E> Optional<DataResult<Pair<E, OptionalInt>>> parseElement(
				DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder
			) {
				JsonElement jsonElement = (JsonElement)this.data.get(resourceKey2);
				return jsonElement == null
					? Optional.of(DataResult.error("Unknown element: " + resourceKey2))
					: Optional.of(
						decoder.parse(dynamicOps, jsonElement)
							.setLifecycle((Lifecycle)this.lifecycles.get(resourceKey2))
							.map(object -> Pair.of(object, OptionalInt.of(this.ids.getInt(resourceKey2))))
					);
			}
		}
	}
}
