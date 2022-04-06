/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public interface RegistryResourceAccess {
    public <E> Map<ResourceKey<E>, EntryThunk<E>> listResources(ResourceKey<? extends Registry<E>> var1);

    public <E> Optional<EntryThunk<E>> getResource(ResourceKey<E> var1);

    public static RegistryResourceAccess forResourceManager(final ResourceManager resourceManager) {
        return new RegistryResourceAccess(){
            private static final String JSON = ".json";

            @Override
            public <E> Map<ResourceKey<E>, EntryThunk<E>> listResources(ResourceKey<? extends Registry<E>> resourceKey) {
                String string = _1.registryDirPath(resourceKey.location());
                HashMap map = Maps.newHashMap();
                resourceManager.listResources(string, resourceLocation -> resourceLocation.getPath().endsWith(JSON)).forEach((resourceLocation, resource) -> {
                    String string2 = resourceLocation.getPath();
                    String string3 = string2.substring(string.length() + 1, string2.length() - JSON.length());
                    ResourceKey resourceKey2 = ResourceKey.create(resourceKey, new ResourceLocation(resourceLocation.getNamespace(), string3));
                    map.put(resourceKey2, (dynamicOps, decoder) -> {
                        DataResult dataResult;
                        block8: {
                            BufferedReader reader = resource.openAsReader();
                            try {
                                dataResult = this.decodeElement(dynamicOps, decoder, reader);
                                if (reader == null) break block8;
                            } catch (Throwable throwable) {
                                try {
                                    if (reader != null) {
                                        try {
                                            ((Reader)reader).close();
                                        } catch (Throwable throwable2) {
                                            throwable.addSuppressed(throwable2);
                                        }
                                    }
                                    throw throwable;
                                } catch (JsonIOException | JsonSyntaxException | IOException exception) {
                                    return DataResult.error("Failed to parse " + resourceLocation + " file: " + exception.getMessage());
                                }
                            }
                            ((Reader)reader).close();
                        }
                        return dataResult;
                    });
                });
                return map;
            }

            @Override
            public <E> Optional<EntryThunk<E>> getResource(ResourceKey<E> resourceKey) {
                ResourceLocation resourceLocation = _1.elementPath(resourceKey);
                return resourceManager.getResource(resourceLocation).map(resource -> (dynamicOps, decoder) -> {
                    DataResult dataResult;
                    block8: {
                        BufferedReader reader = resource.openAsReader();
                        try {
                            dataResult = this.decodeElement(dynamicOps, decoder, reader);
                            if (reader == null) break block8;
                        } catch (Throwable throwable) {
                            try {
                                if (reader != null) {
                                    try {
                                        ((Reader)reader).close();
                                    } catch (Throwable throwable2) {
                                        throwable.addSuppressed(throwable2);
                                    }
                                }
                                throw throwable;
                            } catch (JsonIOException | JsonSyntaxException | IOException exception) {
                                return DataResult.error("Failed to parse " + resourceLocation + " file: " + exception.getMessage());
                            }
                        }
                        ((Reader)reader).close();
                    }
                    return dataResult;
                });
            }

            private <E> DataResult<ParsedEntry<E>> decodeElement(DynamicOps<JsonElement> dynamicOps, Decoder<E> decoder, Reader reader) throws IOException {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                return decoder.parse(dynamicOps, jsonElement).map(ParsedEntry::createWithoutId);
            }

            private static String registryDirPath(ResourceLocation resourceLocation) {
                return resourceLocation.getPath();
            }

            private static <E> ResourceLocation elementPath(ResourceKey<E> resourceKey) {
                return new ResourceLocation(resourceKey.location().getNamespace(), _1.registryDirPath(resourceKey.registry()) + "/" + resourceKey.location().getPath() + JSON);
            }

            public String toString() {
                return "ResourceAccess[" + resourceManager + "]";
            }
        };
    }

    public static final class InMemoryStorage
    implements RegistryResourceAccess {
        private static final Logger LOGGER = LogUtils.getLogger();
        private final Map<ResourceKey<?>, Entry> entries = Maps.newIdentityHashMap();

        public <E> void add(RegistryAccess registryAccess, ResourceKey<E> resourceKey, Encoder<E> encoder, int i, E object, Lifecycle lifecycle) {
            DataResult<JsonElement> dataResult = encoder.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registryAccess), object);
            Optional<DataResult.PartialResult<JsonElement>> optional = dataResult.error();
            if (optional.isPresent()) {
                LOGGER.error("Error adding element: {}", (Object)optional.get().message());
            } else {
                this.entries.put(resourceKey, new Entry(dataResult.result().get(), i, lifecycle));
            }
        }

        @Override
        public <E> Map<ResourceKey<E>, EntryThunk<E>> listResources(ResourceKey<? extends Registry<E>> resourceKey) {
            return this.entries.entrySet().stream().filter(entry -> ((ResourceKey)entry.getKey()).isFor(resourceKey)).collect(Collectors.toMap(entry -> (ResourceKey)entry.getKey(), entry -> ((Entry)entry.getValue())::parse));
        }

        @Override
        public <E> Optional<EntryThunk<E>> getResource(ResourceKey<E> resourceKey) {
            Entry entry = this.entries.get(resourceKey);
            if (entry == null) {
                DataResult dataResult = DataResult.error("Unknown element: " + resourceKey);
                return Optional.of((dynamicOps, decoder) -> dataResult);
            }
            return Optional.of(entry::parse);
        }

        record Entry(JsonElement data, int id, Lifecycle lifecycle) {
            public <E> DataResult<ParsedEntry<E>> parse(DynamicOps<JsonElement> dynamicOps, Decoder<E> decoder) {
                return decoder.parse(dynamicOps, this.data).setLifecycle(this.lifecycle).map(object -> ParsedEntry.createWithId(object, this.id));
            }
        }
    }

    @FunctionalInterface
    public static interface EntryThunk<E> {
        public DataResult<ParsedEntry<E>> parseElement(DynamicOps<JsonElement> var1, Decoder<E> var2);
    }

    public record ParsedEntry<E>(E value, OptionalInt fixedId) {
        public static <E> ParsedEntry<E> createWithoutId(E object) {
            return new ParsedEntry<E>(object, OptionalInt.empty());
        }

        public static <E> ParsedEntry<E> createWithId(E object, int i) {
            return new ParsedEntry<E>(object, OptionalInt.of(i));
        }
    }
}

