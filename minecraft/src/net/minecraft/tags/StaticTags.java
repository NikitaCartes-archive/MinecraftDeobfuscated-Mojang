package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StaticTags {
	private static final Set<ResourceKey<?>> HELPERS_IDS = Sets.<ResourceKey<?>>newHashSet();
	private static final List<StaticTagHelper<?>> HELPERS = Lists.<StaticTagHelper<?>>newArrayList();

	public static <T> StaticTagHelper<T> create(ResourceKey<? extends Registry<T>> resourceKey, String string) {
		if (!HELPERS_IDS.add(resourceKey)) {
			throw new IllegalStateException("Duplicate entry for static tag collection: " + resourceKey);
		} else {
			StaticTagHelper<T> staticTagHelper = new StaticTagHelper<>(resourceKey, string);
			HELPERS.add(staticTagHelper);
			return staticTagHelper;
		}
	}

	public static void resetAll(TagContainer tagContainer) {
		HELPERS.forEach(staticTagHelper -> staticTagHelper.reset(tagContainer));
	}

	public static void resetAllToEmpty() {
		HELPERS.forEach(StaticTagHelper::resetToEmpty);
	}

	public static Multimap<ResourceKey<? extends Registry<?>>, ResourceLocation> getAllMissingTags(TagContainer tagContainer) {
		Multimap<ResourceKey<? extends Registry<?>>, ResourceLocation> multimap = HashMultimap.create();
		HELPERS.forEach(staticTagHelper -> multimap.putAll(staticTagHelper.getKey(), staticTagHelper.getMissingTags(tagContainer)));
		return multimap;
	}

	public static void bootStrap() {
		makeSureAllKnownHelpersAreLoaded();
	}

	private static Set<StaticTagHelper<?>> getAllKnownHelpers() {
		return ImmutableSet.of(BlockTags.HELPER, ItemTags.HELPER, FluidTags.HELPER, EntityTypeTags.HELPER, GameEventTags.HELPER);
	}

	private static void makeSureAllKnownHelpersAreLoaded() {
		Set<ResourceKey<?>> set = (Set<ResourceKey<?>>)getAllKnownHelpers().stream().map(StaticTagHelper::getKey).collect(Collectors.toSet());
		if (!Sets.difference(HELPERS_IDS, set).isEmpty()) {
			throw new IllegalStateException("Missing helper registrations");
		}
	}

	public static void visitHelpers(Consumer<StaticTagHelper<?>> consumer) {
		HELPERS.forEach(consumer);
	}

	public static TagContainer createCollection() {
		TagContainer.Builder builder = new TagContainer.Builder();
		makeSureAllKnownHelpersAreLoaded();
		HELPERS.forEach(staticTagHelper -> staticTagHelper.addToCollection(builder));
		return builder.build();
	}
}
