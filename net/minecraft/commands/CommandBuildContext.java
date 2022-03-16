/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public final class CommandBuildContext {
    private final RegistryAccess registryAccess;
    MissingTagAccessPolicy missingTagAccessPolicy = MissingTagAccessPolicy.FAIL;

    public CommandBuildContext(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    public void missingTagAccessPolicy(MissingTagAccessPolicy missingTagAccessPolicy) {
        this.missingTagAccessPolicy = missingTagAccessPolicy;
    }

    public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourceKey) {
        return new HolderLookup.RegistryLookup<T>(this.registryAccess.registryOrThrow(resourceKey)){

            @Override
            public Optional<? extends HolderSet<T>> get(TagKey<T> tagKey) {
                return switch (CommandBuildContext.this.missingTagAccessPolicy) {
                    default -> throw new IncompatibleClassChangeError();
                    case MissingTagAccessPolicy.FAIL -> this.registry.getTag(tagKey);
                    case MissingTagAccessPolicy.CREATE_NEW -> Optional.of(this.registry.getOrCreateTag(tagKey));
                    case MissingTagAccessPolicy.RETURN_EMPTY -> {
                        Optional optional = this.registry.getTag(tagKey);
                        yield Optional.of(optional.isPresent() ? (HolderSet.Direct)((Object)optional.get()) : HolderSet.direct(new Holder[0]));
                    }
                };
            }
        };
    }

    public static enum MissingTagAccessPolicy {
        CREATE_NEW,
        RETURN_EMPTY,
        FAIL;

    }
}

