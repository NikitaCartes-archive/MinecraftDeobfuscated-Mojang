/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class ResourceLocationPattern {
    public static final Codec<ResourceLocationPattern> CODEC = RecordCodecBuilder.create(instance -> instance.group(ExtraCodecs.PATTERN.optionalFieldOf("namespace").forGetter(resourceLocationPattern -> resourceLocationPattern.namespacePattern), ExtraCodecs.PATTERN.optionalFieldOf("path").forGetter(resourceLocationPattern -> resourceLocationPattern.pathPattern)).apply((Applicative<ResourceLocationPattern, ?>)instance, ResourceLocationPattern::new));
    private final Optional<Pattern> namespacePattern;
    private final Predicate<String> namespacePredicate;
    private final Optional<Pattern> pathPattern;
    private final Predicate<String> pathPredicate;
    private final Predicate<ResourceLocation> locationPredicate;

    private ResourceLocationPattern(Optional<Pattern> optional, Optional<Pattern> optional2) {
        this.namespacePattern = optional;
        this.namespacePredicate = optional.map(Pattern::asPredicate).orElse(string -> true);
        this.pathPattern = optional2;
        this.pathPredicate = optional2.map(Pattern::asPredicate).orElse(string -> true);
        this.locationPredicate = resourceLocation -> this.namespacePredicate.test(resourceLocation.getNamespace()) && this.pathPredicate.test(resourceLocation.getPath());
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

