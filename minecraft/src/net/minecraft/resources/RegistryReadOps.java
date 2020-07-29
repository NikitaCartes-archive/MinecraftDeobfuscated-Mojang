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
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
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
	private final RegistryAccess.RegistryHolder registryHolder;
	private final Map<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> readCache = Maps.<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>>newIdentityHashMap();

	public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess.RegistryHolder registryHolder) {
		RegistryReadOps<T> registryReadOps = new RegistryReadOps<>(dynamicOps, resourceManager, registryHolder);
		RegistryAccess.load(registryHolder, registryReadOps);
		return registryReadOps;
	}

	private RegistryReadOps(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess.RegistryHolder registryHolder) {
		super(dynamicOps);
		this.resourceManager = resourceManager;
		this.registryHolder = registryHolder;
	}

	protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T object, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
		Optional<WritableRegistry<E>> optional = this.registryHolder.registry(resourceKey);
		if (!optional.isPresent()) {
			return DataResult.error("Unknown registry: " + resourceKey);
		} else {
			WritableRegistry<E> writableRegistry = (WritableRegistry<E>)optional.get();
			DataResult<Pair<ResourceLocation, T>> dataResult = ResourceLocation.CODEC.decode(this.delegate, object);
			if (!dataResult.result().isPresent()) {
				return codec.decode(this.delegate, object).map(pairx -> pairx.mapFirst(objectx -> () -> objectx));
			} else {
				Pair<ResourceLocation, T> pair = (Pair<ResourceLocation, T>)dataResult.result().get();
				ResourceLocation resourceLocation = pair.getFirst();
				return this.readAndRegisterElement(resourceKey, writableRegistry, codec, resourceLocation).map(supplier -> Pair.of(supplier, pair.getSecond()));
			}
		}
	}

	public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> mappedRegistry, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
		ResourceLocation resourceLocation = resourceKey.location();
		Collection<ResourceLocation> collection = this.resourceManager.listResources(resourceLocation.getPath(), stringx -> stringx.endsWith(".json"));
		DataResult<MappedRegistry<E>> dataResult = DataResult.success(mappedRegistry, Lifecycle.stable());
		String string = resourceLocation.getPath() + "/";

		for (ResourceLocation resourceLocation2 : collection) {
			String string2 = resourceLocation2.getPath();
			if (!string2.endsWith(".json")) {
				LOGGER.warn("Skipping resource {} since it is not a json file", resourceLocation2);
			} else if (!string2.startsWith(string)) {
				LOGGER.warn("Skipping resource {} since it does not have a registry name prefix", resourceLocation2);
			} else {
				String string3 = string2.substring(string.length(), string2.length() - ".json".length());
				ResourceLocation resourceLocation3 = new ResourceLocation(resourceLocation2.getNamespace(), string3);
				dataResult = dataResult.flatMap(
					mappedRegistryx -> this.readAndRegisterElement(resourceKey, mappedRegistryx, codec, resourceLocation3).map(supplier -> mappedRegistryx)
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
			DataResult<E> dataResult2 = this.readElementFromFile(resourceKey, resourceKey2, codec);
			DataResult<E> dataResult3;
			if (dataResult2.result().isPresent()) {
				writableRegistry.registerOrOverride(resourceKey2, dataResult2.result().get());
				dataResult3 = dataResult2;
			} else {
				E object = writableRegistry.get(resourceKey2);
				if (object != null) {
					dataResult3 = DataResult.success(object, Lifecycle.stable());
				} else {
					dataResult3 = dataResult2;
				}
			}

			DataResult<Supplier<E>> dataResult4 = dataResult3.map(objectx -> () -> object);
			readCache.values.put(resourceKey2, dataResult4);
			return dataResult4;
		}
	}

	private <E> DataResult<E> readElementFromFile(ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Codec<E> codec) {
		ResourceLocation resourceLocation = resourceKey2.location();
		ResourceLocation resourceLocation2 = new ResourceLocation(
			resourceLocation.getNamespace(), resourceKey.location().getPath() + "/" + resourceLocation.getPath() + ".json"
		);

		try {
			Resource resource = this.resourceManager.getResource(resourceLocation2);
			Throwable var7 = null;

			DataResult var12;
			try {
				Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
				Throwable var9 = null;

				try {
					JsonParser jsonParser = new JsonParser();
					JsonElement jsonElement = jsonParser.parse(reader);
					var12 = codec.parse(new RegistryReadOps<>(JsonOps.INSTANCE, this.resourceManager, this.registryHolder), jsonElement);
				} catch (Throwable var37) {
					var9 = var37;
					throw var37;
				} finally {
					if (reader != null) {
						if (var9 != null) {
							try {
								reader.close();
							} catch (Throwable var36) {
								var9.addSuppressed(var36);
							}
						} else {
							reader.close();
						}
					}
				}
			} catch (Throwable var39) {
				var7 = var39;
				throw var39;
			} finally {
				if (resource != null) {
					if (var7 != null) {
						try {
							resource.close();
						} catch (Throwable var35) {
							var7.addSuppressed(var35);
						}
					} else {
						resource.close();
					}
				}
			}

			return var12;
		} catch (JsonIOException | JsonSyntaxException | IOException var41) {
			return DataResult.error("Failed to parse " + resourceLocation2 + " file: " + var41.getMessage());
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
