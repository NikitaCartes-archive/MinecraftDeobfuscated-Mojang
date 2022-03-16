package net.minecraft.server.packs.resources;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.ExtraCodecs;
import org.slf4j.Logger;

public class ResourceFilterSection {
	static final Logger LOGGER = LogUtils.getLogger();
	static final Codec<ResourceFilterSection> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.list(ResourceFilterSection.ResourceLocationPattern.CODEC).fieldOf("block").forGetter(resourceFilterSection -> resourceFilterSection.blockList)
				)
				.apply(instance, ResourceFilterSection::new)
	);
	public static final MetadataSectionSerializer<ResourceFilterSection> SERIALIZER = new MetadataSectionSerializer<ResourceFilterSection>() {
		@Override
		public String getMetadataSectionName() {
			return "filter";
		}

		public ResourceFilterSection fromJson(JsonObject jsonObject) {
			return ResourceFilterSection.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow(false, ResourceFilterSection.LOGGER::error);
		}
	};
	private final List<ResourceFilterSection.ResourceLocationPattern> blockList;

	public ResourceFilterSection(List<ResourceFilterSection.ResourceLocationPattern> list) {
		this.blockList = List.copyOf(list);
	}

	public boolean isNamespaceFiltered(String string) {
		return this.blockList.stream().anyMatch(resourceLocationPattern -> resourceLocationPattern.namespacePredicate.test(string));
	}

	public boolean isPathFiltered(String string) {
		return this.blockList.stream().anyMatch(resourceLocationPattern -> resourceLocationPattern.pathPredicate.test(string));
	}

	static class ResourceLocationPattern implements Predicate<ResourceLocation> {
		static final Codec<ResourceFilterSection.ResourceLocationPattern> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.PATTERN.optionalFieldOf("namespace").forGetter(resourceLocationPattern -> resourceLocationPattern.namespacePattern),
						ExtraCodecs.PATTERN.optionalFieldOf("path").forGetter(resourceLocationPattern -> resourceLocationPattern.pathPattern)
					)
					.apply(instance, ResourceFilterSection.ResourceLocationPattern::new)
		);
		private final Optional<Pattern> namespacePattern;
		final Predicate<String> namespacePredicate;
		private final Optional<Pattern> pathPattern;
		final Predicate<String> pathPredicate;

		private ResourceLocationPattern(Optional<Pattern> optional, Optional<Pattern> optional2) {
			this.namespacePattern = optional;
			this.namespacePredicate = (Predicate<String>)optional.map(Pattern::asPredicate).orElse((Predicate)string -> true);
			this.pathPattern = optional2;
			this.pathPredicate = (Predicate<String>)optional2.map(Pattern::asPredicate).orElse((Predicate)string -> true);
		}

		public boolean test(ResourceLocation resourceLocation) {
			return this.namespacePredicate.test(resourceLocation.getNamespace()) && this.pathPredicate.test(resourceLocation.getPath());
		}
	}
}
