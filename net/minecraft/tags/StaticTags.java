/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagContainer;

public class StaticTags {
    private static final Map<ResourceLocation, StaticTagHelper<?>> HELPERS = Maps.newHashMap();

    public static <T> StaticTagHelper<T> create(ResourceLocation resourceLocation, Function<TagContainer, TagCollection<T>> function) {
        StaticTagHelper<T> staticTagHelper = new StaticTagHelper<T>(function);
        StaticTagHelper<T> staticTagHelper2 = HELPERS.putIfAbsent(resourceLocation, staticTagHelper);
        if (staticTagHelper2 != null) {
            throw new IllegalStateException("Duplicate entry for static tag collection: " + resourceLocation);
        }
        return staticTagHelper;
    }

    public static void resetAll(TagContainer tagContainer) {
        HELPERS.values().forEach(staticTagHelper -> staticTagHelper.reset(tagContainer));
    }

    @Environment(value=EnvType.CLIENT)
    public static void resetAllToEmpty() {
        HELPERS.values().forEach(StaticTagHelper::resetToEmpty);
    }

    public static Multimap<ResourceLocation, ResourceLocation> getAllMissingTags(TagContainer tagContainer) {
        HashMultimap<ResourceLocation, ResourceLocation> multimap = HashMultimap.create();
        HELPERS.forEach((resourceLocation, staticTagHelper) -> multimap.putAll((ResourceLocation)resourceLocation, (Iterable<ResourceLocation>)staticTagHelper.getMissingTags(tagContainer)));
        return multimap;
    }

    public static void bootStrap() {
        StaticTagHelper[] staticTagHelpers = new StaticTagHelper[]{BlockTags.HELPER, ItemTags.HELPER, FluidTags.HELPER, EntityTypeTags.HELPER};
        boolean bl = Stream.of(staticTagHelpers).anyMatch(staticTagHelper -> !HELPERS.containsValue(staticTagHelper));
        if (bl) {
            throw new IllegalStateException("Missing helper registrations");
        }
    }
}

