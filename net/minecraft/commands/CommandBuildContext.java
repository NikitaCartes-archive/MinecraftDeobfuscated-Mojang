/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;

public interface CommandBuildContext {
    public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> var1);

    public static CommandBuildContext simple(final HolderLookup.Provider provider, final FeatureFlagSet featureFlagSet) {
        return new CommandBuildContext(){

            @Override
            public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourceKey) {
                return provider.lookupOrThrow(resourceKey).filterFeatures(featureFlagSet);
            }
        };
    }

    public static Configurable configurable(final RegistryAccess registryAccess, final FeatureFlagSet featureFlagSet) {
        return new Configurable(){
            MissingTagAccessPolicy missingTagAccessPolicy = MissingTagAccessPolicy.FAIL;

            @Override
            public void missingTagAccessPolicy(MissingTagAccessPolicy missingTagAccessPolicy) {
                this.missingTagAccessPolicy = missingTagAccessPolicy;
            }

            @Override
            public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourceKey) {
                Registry registry = registryAccess.registryOrThrow(resourceKey);
                final HolderLookup.RegistryLookup registryLookup = registry.asLookup();
                final HolderLookup.RegistryLookup registryLookup2 = registry.asTagAddingLookup();
                HolderLookup.RegistryLookup.Delegate registryLookup3 = new HolderLookup.RegistryLookup.Delegate<T>(){

                    @Override
                    protected HolderLookup.RegistryLookup<T> parent() {
                        return switch (missingTagAccessPolicy) {
                            default -> throw new IncompatibleClassChangeError();
                            case MissingTagAccessPolicy.FAIL -> registryLookup;
                            case MissingTagAccessPolicy.CREATE_NEW -> registryLookup2;
                        };
                    }
                };
                return registryLookup3.filterFeatures(featureFlagSet);
            }
        };
    }

    public static interface Configurable
    extends CommandBuildContext {
        public void missingTagAccessPolicy(MissingTagAccessPolicy var1);
    }

    public static enum MissingTagAccessPolicy {
        CREATE_NEW,
        FAIL;

    }
}

