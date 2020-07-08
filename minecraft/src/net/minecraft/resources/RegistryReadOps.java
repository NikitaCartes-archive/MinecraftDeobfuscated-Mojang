package net.minecraft.resources;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
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
	private final ResourceManager resourceManager;
	private final RegistryAccess registryHolder;
	private final Map<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> readCache = Maps.<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>>newIdentityHashMap();

	public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess registryAccess) {
		return new RegistryReadOps<>(dynamicOps, resourceManager, registryAccess);
	}

	private RegistryReadOps(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess registryAccess) {
		super(dynamicOps);
		this.resourceManager = resourceManager;
		this.registryHolder = registryAccess;
	}

	protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T object, ResourceKey<? extends Registry<E>> resourceKey, MapCodec<E> mapCodec) {
		Optional<WritableRegistry<E>> optional = this.registryHolder.registry(resourceKey);
		if (!optional.isPresent()) {
			return DataResult.error("Unknown registry: " + resourceKey);
		} else {
			WritableRegistry<E> writableRegistry = (WritableRegistry<E>)optional.get();
			DataResult<Pair<ResourceLocation, T>> dataResult = ResourceLocation.CODEC.decode(this.delegate, object);
			if (!dataResult.result().isPresent()) {
				return MappedRegistry.withName(resourceKey, mapCodec).codec().decode(this.delegate, object).map(pairx -> pairx.mapFirst(pairxx -> {
						writableRegistry.register((ResourceKey<E>)pairxx.getFirst(), pairxx.getSecond());
						writableRegistry.setPersistent((ResourceKey<E>)pairxx.getFirst());
						return pairxx::getSecond;
					}));
			} else {
				Pair<ResourceLocation, T> pair = (Pair<ResourceLocation, T>)dataResult.result().get();
				ResourceLocation resourceLocation = pair.getFirst();
				return this.readAndRegisterElement(resourceKey, writableRegistry, mapCodec, resourceLocation).map(supplier -> Pair.of(supplier, pair.getSecond()));
			}
		}
	}

	public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> mappedRegistry, ResourceKey<? extends Registry<E>> resourceKey, MapCodec<E> mapCodec) {
		ResourceLocation resourceLocation = resourceKey.location();
		Collection<ResourceLocation> collection = this.resourceManager.listResources(resourceLocation, stringx -> stringx.endsWith(".json"));
		DataResult<MappedRegistry<E>> dataResult = DataResult.success(mappedRegistry, Lifecycle.stable());

		for (ResourceLocation resourceLocation2 : collection) {
			String string = resourceLocation2.getPath();
			if (!string.endsWith(".json")) {
				LOGGER.warn("Skipping resource {} since it is not a json file", resourceLocation2);
			} else if (!string.startsWith(resourceLocation.getPath() + "/")) {
				LOGGER.warn("Skipping resource {} since it does not have a registry name prefix", resourceLocation2);
			} else {
				String string2 = string.substring(0, string.length() - ".json".length()).substring(resourceLocation.getPath().length() + 1);
				int i = string2.indexOf(47);
				if (i < 0) {
					LOGGER.warn("Skipping resource {} since it does not have a namespace", resourceLocation2);
				} else {
					String string3 = string2.substring(0, i);
					String string4 = string2.substring(i + 1);
					ResourceLocation resourceLocation3 = new ResourceLocation(string3, string4);
					dataResult = dataResult.flatMap(
						mappedRegistryx -> this.readAndRegisterElement(resourceKey, mappedRegistryx, mapCodec, resourceLocation3).map(supplier -> mappedRegistryx)
					);
				}
			}
		}

		return dataResult.setPartial(mappedRegistry);
	}

	private <E> DataResult<Supplier<E>> readAndRegisterElement(
		ResourceKey<? extends Registry<E>> resourceKey, WritableRegistry<E> writableRegistry, MapCodec<E> mapCodec, ResourceLocation resourceLocation
	) {
		ResourceKey<E> resourceKey2 = ResourceKey.create(resourceKey, resourceLocation);
		E object = writableRegistry.get(resourceKey2);
		if (object != null) {
			return DataResult.success(() -> object, Lifecycle.stable());
		} else {
			RegistryReadOps.ReadCache<E> readCache = this.readCache(resourceKey);
			DataResult<Supplier<E>> dataResult = (DataResult<Supplier<E>>)readCache.values.get(resourceKey2);
			if (dataResult != null) {
				return dataResult;
			} else {
				Supplier<E> supplier = Suppliers.memoize(() -> {
					E objectx = writableRegistry.get(resourceKey2);
					if (objectx == null) {
						throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + resourceKey2);
					} else {
						return (T)objectx;
					}
				});
				readCache.values.put(resourceKey2, DataResult.success(supplier));
				DataResult<E> dataResult2 = this.readElementFromFile(resourceKey, resourceKey2, mapCodec);
				dataResult2.result().ifPresent(objectx -> writableRegistry.register(resourceKey2, objectx));
				DataResult<Supplier<E>> dataResult3 = dataResult2.map(objectx -> () -> objectx);
				readCache.values.put(resourceKey2, dataResult3);
				return dataResult3;
			}
		}
	}

	private <E> DataResult<E> readElementFromFile(ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, MapCodec<E> mapCodec) {
		ResourceLocation resourceLocation = new ResourceLocation(
			resourceKey.location().getNamespace(),
			resourceKey.location().getPath() + "/" + resourceKey2.location().getNamespace() + "/" + resourceKey2.location().getPath() + ".json"
		);

		try {
			Resource resource = this.resourceManager.getResource(resourceLocation);
			Throwable var6 = null;

			DataResult var11;
			try {
				Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
				Throwable var8 = null;

				try {
					JsonParser jsonParser = new JsonParser();
					JsonElement jsonElement = jsonParser.parse(reader);
					var11 = mapCodec.codec().parse(new RegistryReadOps<>(JsonOps.INSTANCE, this.resourceManager, this.registryHolder), jsonElement);
				} catch (Throwable var36) {
					var8 = var36;
					throw var36;
				} finally {
					if (reader != null) {
						if (var8 != null) {
							try {
								reader.close();
							} catch (Throwable var35) {
								var8.addSuppressed(var35);
							}
						} else {
							reader.close();
						}
					}
				}
			} catch (Throwable var38) {
				var6 = var38;
				throw var38;
			} finally {
				if (resource != null) {
					if (var6 != null) {
						try {
							resource.close();
						} catch (Throwable var34) {
							var6.addSuppressed(var34);
						}
					} else {
						resource.close();
					}
				}
			}

			return var11;
		} catch (JsonIOException | JsonSyntaxException | IOException var40) {
			return DataResult.error("Failed to parse file: " + var40.getMessage());
		}
	}

	private <E> RegistryReadOps.ReadCache<E> readCache(ResourceKey<? extends Registry<E>> resourceKey) {
		return (RegistryReadOps.ReadCache<E>)this.readCache.computeIfAbsent(resourceKey, resourceKeyx -> new RegistryReadOps.ReadCache());
	}

	static final class ReadCache<E> {
		private final Map<ResourceKey<E>, DataResult<Supplier<E>>> values = Maps.<ResourceKey<E>, DataResult<Supplier<E>>>newIdentityHashMap();

		private ReadCache() {
		}
	}
}
