/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface RegistryResourceAccess {
    public <E> Collection<ResourceKey<E>> listResources(ResourceKey<? extends Registry<E>> var1);

    public <E> Optional<DataResult<ParsedEntry<E>>> parseElement(DynamicOps<JsonElement> var1, ResourceKey<? extends Registry<E>> var2, ResourceKey<E> var3, Decoder<E> var4);

    public static RegistryResourceAccess forResourceManager(final ResourceManager resourceManager) {
        return new RegistryResourceAccess(){
            private static final String JSON = ".json";

            @Override
            public <E> Collection<ResourceKey<E>> listResources(ResourceKey<? extends Registry<E>> resourceKey) {
                String string2 = _1.registryDirPath(resourceKey);
                HashSet set = new HashSet();
                resourceManager.listResources(string2, string -> string.endsWith(JSON)).forEach(resourceLocation -> {
                    String string2 = resourceLocation.getPath();
                    String string3 = string2.substring(string2.length() + 1, string2.length() - JSON.length());
                    set.add(ResourceKey.create(resourceKey, new ResourceLocation(resourceLocation.getNamespace(), string3)));
                });
                return set;
            }

            /*
             * Enabled aggressive exception aggregation
             */
            @Override
            public <E> Optional<DataResult<ParsedEntry<E>>> parseElement(DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder) {
                ResourceLocation resourceLocation = _1.elementPath(resourceKey, resourceKey2);
                if (!resourceManager.hasResource(resourceLocation)) {
                    return Optional.empty();
                }
                try (Resource resource = resourceManager.getResource(resourceLocation);){
                    Optional<DataResult<ParsedEntry<E>>> optional;
                    try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);){
                        JsonElement jsonElement = JsonParser.parseReader(reader);
                        optional = Optional.of(decoder.parse(dynamicOps, jsonElement).map(ParsedEntry::createWithoutId));
                    }
                    return optional;
                } catch (JsonIOException | JsonSyntaxException | IOException exception) {
                    return Optional.of(DataResult.error("Failed to parse " + resourceLocation + " file: " + exception.getMessage()));
                }
            }

            private static String registryDirPath(ResourceKey<? extends Registry<?>> resourceKey) {
                return resourceKey.location().getPath();
            }

            private static <E> ResourceLocation elementPath(ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2) {
                return new ResourceLocation(resourceKey2.location().getNamespace(), _1.registryDirPath(resourceKey) + "/" + resourceKey2.location().getPath() + JSON);
            }

            public String toString() {
                return "ResourceAccess[" + resourceManager + "]";
            }
        };
    }

    public static final class InMemoryStorage
    implements RegistryResourceAccess {
        private static final Logger LOGGER = LogManager.getLogger();
        private final Map<ResourceKey<?>, Entry> entries = Maps.newIdentityHashMap();

        public <E> void add(RegistryAccess.RegistryHolder registryHolder, ResourceKey<E> resourceKey, Encoder<E> encoder, int i, E object, Lifecycle lifecycle) {
            DataResult<JsonElement> dataResult = encoder.encodeStart(RegistryWriteOps.create(JsonOps.INSTANCE, registryHolder), object);
            Optional<DataResult.PartialResult<JsonElement>> optional = dataResult.error();
            if (optional.isPresent()) {
                LOGGER.error("Error adding element: {}", (Object)optional.get().message());
            } else {
                this.entries.put(resourceKey, new Entry(dataResult.result().get(), i, lifecycle));
            }
        }

        @Override
        public <E> Collection<ResourceKey<E>> listResources(ResourceKey<? extends Registry<E>> resourceKey) {
            return this.entries.keySet().stream().flatMap(resourceKey2 -> resourceKey2.cast(resourceKey).stream()).collect(Collectors.toList());
        }

        @Override
        public <E> Optional<DataResult<ParsedEntry<E>>> parseElement(DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder) {
            Entry entry = this.entries.get(resourceKey2);
            if (entry == null) {
                return Optional.of(DataResult.error("Unknown element: " + resourceKey2));
            }
            return Optional.of(decoder.parse(dynamicOps, entry.data).setLifecycle(entry.lifecycle).map(object -> ParsedEntry.createWithId(object, entry.id)));
        }

        record Entry(JsonElement data, int id, Lifecycle lifecycle) {
        }
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

