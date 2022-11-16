package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationPattern {
	public static final Codec<ResourceLocationPattern> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.PATTERN.optionalFieldOf("namespace").forGetter(resourceLocationPattern -> resourceLocationPattern.namespacePattern),
					ExtraCodecs.PATTERN.optionalFieldOf("path").forGetter(resourceLocationPattern -> resourceLocationPattern.pathPattern)
				)
				.apply(instance, ResourceLocationPattern::new)
	);
	private final Optional<Pattern> namespacePattern;
	private final Predicate<String> namespacePredicate;
	private final Optional<Pattern> pathPattern;
	private final Predicate<String> pathPredicate;
	private final Predicate<ResourceLocation> locationPredicate;

	private ResourceLocationPattern(Optional<Pattern> optional, Optional<Pattern> optional2) {
		this.namespacePattern = optional;
		this.namespacePredicate = (Predicate<String>)optional.map(Pattern::asPredicate).orElse((Predicate)string -> true);
		this.pathPattern = optional2;
		this.pathPredicate = (Predicate<String>)optional2.map(Pattern::asPredicate).orElse((Predicate)string -> true);
		this.locationPredicate = resourceLocation -> this.namespacePredicate.test(resourceLocation.getNamespace())
				&& this.pathPredicate.test(resourceLocation.getPath());
	}

	public Predicate<String> namespacePredicate() {
		return this.namespacePredicate;
	}

	public Predicate<String> pathPredicate() {
		return this.pathPredicate;
	}

	public Predicate<ResourceLocation> locationPredicate() {
		return this.locationPredicate;
	}
}
