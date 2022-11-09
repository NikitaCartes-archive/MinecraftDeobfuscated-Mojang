/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class RegistrySetBuilder {
    private final List<RegistryStub<?>> entries = new ArrayList();

    static <T> HolderGetter<T> wrapContextLookup(final HolderLookup.RegistryLookup<T> registryLookup) {
        return new EmptyTagLookup<T>(registryLookup){

            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
                return registryLookup.get(resourceKey);
            }
        };
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, RegistryBootstrap<T> registryBootstrap) {
        this.entries.add(new RegistryStub<T>(resourceKey, lifecycle, registryBootstrap));
        return this;
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends Registry<T>> resourceKey, RegistryBootstrap<T> registryBootstrap) {
        return this.add(resourceKey, Lifecycle.stable(), registryBootstrap);
    }

    private BuildState createState(RegistryAccess registryAccess) {
        BuildState buildState = BuildState.create(registryAccess, this.entries.stream().map(RegistryStub::key));
        this.entries.forEach(registryStub -> registryStub.apply(buildState));
        return buildState;
    }

    public HolderLookup.Provider build(RegistryAccess registryAccess) {
        BuildState buildState = this.createState(registryAccess);
        Stream<HolderLookup.RegistryLookup> stream = registryAccess.registries().map(registryEntry -> registryEntry.value().asLookup());
        Stream<HolderLookup.RegistryLookup> stream2 = this.entries.stream().map(registryStub -> registryStub.collectChanges(buildState).buildAsLookup());
        HolderLookup.Provider provider = HolderLookup.Provider.create(Stream.concat(stream, stream2.peek(buildState::addOwner)));
        buildState.reportRemainingUnreferencedValues();
        buildState.throwOnError();
        return provider;
    }

    public HolderLookup.Provider buildPatch(RegistryAccess registryAccess, HolderLookup.Provider provider) {
        BuildState buildState = this.createState(registryAccess);
        Stream<HolderLookup.RegistryLookup> stream = registryAccess.registries().map(registryEntry -> registryEntry.value().asLookup());
        Stream<HolderLookup.RegistryLookup> stream2 = this.entries.stream().map(registryStub -> registryStub.collectChanges(buildState).buildAsLookup());
        HolderLookup.Provider provider2 = HolderLookup.Provider.create(Stream.concat(stream, stream2.peek(buildState::addOwner)));
        buildState.fillMissingHolders(provider);
        buildState.reportRemainingUnreferencedValues();
        buildState.throwOnError();
        return provider2;
    }

    record RegistryStub<T>(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, RegistryBootstrap<T> bootstrap) {
        void apply(BuildState buildState) {
            this.bootstrap.run(buildState.bootstapContext());
        }

        public RegistryContents<T> collectChanges(BuildState buildState) {
            HashMap map = new HashMap();
            Iterator<Map.Entry<ResourceKey<?>, RegisteredValue<?>>> iterator = buildState.registeredValues.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ResourceKey<?>, RegisteredValue<?>> entry = iterator.next();
                ResourceKey<?> resourceKey = entry.getKey();
                if (!resourceKey.isFor(this.key)) continue;
                ResourceKey<?> resourceKey2 = resourceKey;
                RegisteredValue<?> registeredValue = entry.getValue();
                Holder.Reference<Object> reference = buildState.lookup.holders.remove(resourceKey);
                map.put(resourceKey2, new ValueAndHolder(registeredValue, Optional.ofNullable(reference)));
                iterator.remove();
            }
            return new RegistryContents(this, map);
        }
    }

    @FunctionalInterface
    public static interface RegistryBootstrap<T> {
        public void run(BootstapContext<T> var1);
    }

    record BuildState(CompositeOwner owner, UniversalLookup lookup, Map<ResourceLocation, HolderGetter<?>> registries, Map<ResourceKey<?>, RegisteredValue<?>> registeredValues, List<RuntimeException> errors) {
        public static BuildState create(RegistryAccess registryAccess, Stream<ResourceKey<? extends Registry<?>>> stream) {
            CompositeOwner compositeOwner = new CompositeOwner();
            ArrayList<RuntimeException> list = new ArrayList<RuntimeException>();
            UniversalLookup universalLookup = new UniversalLookup(compositeOwner);
            ImmutableMap.Builder builder = ImmutableMap.builder();
            registryAccess.registries().forEach(registryEntry -> builder.put(registryEntry.key().location(), RegistrySetBuilder.wrapContextLookup(registryEntry.value().asLookup())));
            stream.forEach(resourceKey -> builder.put(resourceKey.location(), universalLookup));
            return new BuildState(compositeOwner, universalLookup, builder.build(), new HashMap(), list);
        }

        public <T> BootstapContext<T> bootstapContext() {
            return new BootstapContext<T>(){

                @Override
                public Holder.Reference<T> register(ResourceKey<T> resourceKey, T object, Lifecycle lifecycle) {
                    RegisteredValue registeredValue = registeredValues.put(resourceKey, new RegisteredValue(object, lifecycle));
                    if (registeredValue != null) {
                        errors.add(new IllegalStateException("Duplicate registration for " + resourceKey + ", new=" + object + ", old=" + registeredValue.value));
                    }
                    return lookup.getOrCreate(resourceKey);
                }

                @Override
                public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> resourceKey) {
                    return registries.getOrDefault(resourceKey.location(), lookup);
                }
            };
        }

        public void reportRemainingUnreferencedValues() {
            for (ResourceKey<Object> resourceKey2 : this.lookup.holders.keySet()) {
                this.errors.add(new IllegalStateException("Unreferenced key: " + resourceKey2));
            }
            this.registeredValues.forEach((resourceKey, registeredValue) -> this.errors.add(new IllegalStateException("Orpaned value " + registeredValue.value + " for key " + resourceKey)));
        }

        public void throwOnError() {
            if (!this.errors.isEmpty()) {
                IllegalStateException illegalStateException = new IllegalStateException("Errors during registry creation");
                for (RuntimeException runtimeException : this.errors) {
                    illegalStateException.addSuppressed(runtimeException);
                }
                throw illegalStateException;
            }
        }

        public void addOwner(HolderOwner<?> holderOwner) {
            this.owner.add(holderOwner);
        }

        public void fillMissingHolders(HolderLookup.Provider provider) {
            HashMap<ResourceLocation, Optional> map = new HashMap<ResourceLocation, Optional>();
            Iterator<Map.Entry<ResourceKey<Object>, Holder.Reference<Object>>> iterator = this.lookup.holders.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ResourceKey<Object>, Holder.Reference<Object>> entry = iterator.next();
                ResourceKey<Object> resourceKey = entry.getKey();
                Holder.Reference<Object> reference = entry.getValue();
                map.computeIfAbsent(resourceKey.registry(), resourceLocation -> provider.lookup(ResourceKey.createRegistryKey(resourceLocation))).flatMap(holderLookup -> holderLookup.get(resourceKey)).ifPresent(reference2 -> {
                    reference.bindValue(reference2.value());
                    iterator.remove();
                });
            }
        }
    }

    record RegistryContents<T>(RegistryStub<T> stub, Map<ResourceKey<T>, ValueAndHolder<T>> values) {
        public HolderLookup.RegistryLookup<T> buildAsLookup() {
            return new HolderLookup.RegistryLookup<T>(){
                private final Map<ResourceKey<T>, Holder.Reference<T>> entries;
                {
                    this.entries = values.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> {
                        ValueAndHolder valueAndHolder = (ValueAndHolder)entry.getValue();
                        Holder.Reference reference = valueAndHolder.holder().orElseGet(() -> Holder.Reference.createStandAlone(this, (ResourceKey)entry.getKey()));
                        reference.bindValue(valueAndHolder.value().value());
                        return reference;
                    }));
                }

                @Override
                public ResourceKey<? extends Registry<? extends T>> key() {
                    return stub.key();
                }

                @Override
                public Lifecycle registryLifecycle() {
                    return stub.lifecycle();
                }

                @Override
                public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
                    return Optional.ofNullable(this.entries.get(resourceKey));
                }

                @Override
                public Stream<Holder.Reference<T>> listElements() {
                    return this.entries.values().stream();
                }

                @Override
                public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
                    return Optional.empty();
                }

                @Override
                public Stream<HolderSet.Named<T>> listTags() {
                    return Stream.empty();
                }
            };
        }
    }

    record ValueAndHolder<T>(RegisteredValue<T> value, Optional<Holder.Reference<T>> holder) {
    }

    record RegisteredValue<T>(T value, Lifecycle lifecycle) {
    }

    static class UniversalLookup
    extends EmptyTagLookup<Object> {
        final Map<ResourceKey<Object>, Holder.Reference<Object>> holders = new HashMap<ResourceKey<Object>, Holder.Reference<Object>>();

        public UniversalLookup(HolderOwner<Object> holderOwner) {
            super(holderOwner);
        }

        @Override
        public Optional<Holder.Reference<Object>> get(ResourceKey<Object> resourceKey) {
            return Optional.of(this.getOrCreate(resourceKey));
        }

        <T> Holder.Reference<T> getOrCreate(ResourceKey<T> resourceKey2) {
            return this.holders.computeIfAbsent(resourceKey2, resourceKey -> Holder.Reference.createStandAlone(this.owner, resourceKey));
        }
    }

    static class CompositeOwner
    implements HolderOwner<Object> {
        private final Set<HolderOwner<?>> owners = Sets.newIdentityHashSet();

        CompositeOwner() {
        }

        @Override
        public boolean canSerializeIn(HolderOwner<Object> holderOwner) {
            return this.owners.contains(holderOwner);
        }

        public void add(HolderOwner<?> holderOwner) {
            this.owners.add(holderOwner);
        }
    }

    static abstract class EmptyTagLookup<T>
    implements HolderGetter<T> {
        protected final HolderOwner<T> owner;

        protected EmptyTagLookup(HolderOwner<T> holderOwner) {
            this.owner = holderOwner;
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
            return Optional.of(HolderSet.emptyNamed(this.owner, tagKey));
        }
    }
}

