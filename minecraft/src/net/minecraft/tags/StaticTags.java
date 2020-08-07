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

public class StaticTags {
	private static final Map<ResourceLocation, StaticTagHelper<?>> HELPERS = Maps.<ResourceLocation, StaticTagHelper<?>>newHashMap();

	public static <T> StaticTagHelper<T> create(ResourceLocation resourceLocation, Function<TagContainer, TagCollection<T>> function) {
		StaticTagHelper<T> staticTagHelper = new StaticTagHelper<>(function);
		StaticTagHelper<?> staticTagHelper2 = (StaticTagHelper<?>)HELPERS.putIfAbsent(resourceLocation, staticTagHelper);
		if (staticTagHelper2 != null) {
			throw new IllegalStateException("Duplicate entry for static tag collection: " + resourceLocation);
		} else {
			return staticTagHelper;
		}
	}

	public static void resetAll(TagContainer tagContainer) {
		HELPERS.values().forEach(staticTagHelper -> staticTagHelper.reset(tagContainer));
	}

	@Environment(EnvType.CLIENT)
	public static void resetAllToEmpty() {
		HELPERS.values().forEach(StaticTagHelper::resetToEmpty);
	}

	public static Multimap<ResourceLocation, ResourceLocation> getAllMissingTags(TagContainer tagContainer) {
		Multimap<ResourceLocation, ResourceLocation> multimap = HashMultimap.create();
		HELPERS.forEach((resourceLocation, staticTagHelper) -> multimap.putAll(resourceLocation, staticTagHelper.getMissingTags(tagContainer)));
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
