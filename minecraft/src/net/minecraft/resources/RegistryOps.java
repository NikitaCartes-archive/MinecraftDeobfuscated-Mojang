package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T> extends DelegatingOps<T> {
	private final RegistryOps.RegistryInfoLookup lookupProvider;

	private static RegistryOps.RegistryInfoLookup memoizeLookup(RegistryOps.RegistryInfoLookup registryInfoLookup) {
		return new RegistryOps.RegistryInfoLookup() {
			private final Map<ResourceKey<? extends Registry<?>>, Optional<? extends RegistryOps.RegistryInfo<?>>> lookups = new HashMap();

			@Override
			public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
				return (Optional<RegistryOps.RegistryInfo<T>>)this.lookups.computeIfAbsent(resourceKey, registryInfoLookup::lookup);
			}
		};
	}

	public static <T> RegistryOps<T> create(DynamicOps<T> dynamicOps, HolderLookup.Provider provider) {
		return create(
			dynamicOps,
			memoizeLookup(
				new RegistryOps.RegistryInfoLookup() {
					@Override
					public <E> Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> resourceKey) {
						return provider.lookup(resourceKey)
							.map(registryLookup -> new RegistryOps.RegistryInfo<>(registryLookup, registryLookup, registryLookup.registryLifecycle()));
					}
				}
			)
		);
	}

	public static <T> RegistryOps<T> create(DynamicOps<T> dynamicOps, RegistryOps.RegistryInfoLookup registryInfoLookup) {
		return new RegistryOps<>(dynamicOps, registryInfoLookup);
	}

	public static <T> Dynamic<T> injectRegistryContext(Dynamic<T> dynamic, HolderLookup.Provider provider) {
		return new Dynamic<>(create(dynamic.getOps(), provider), dynamic.getValue());
	}

	private RegistryOps(DynamicOps<T> dynamicOps, RegistryOps.RegistryInfoLookup registryInfoLookup) {
		super(dynamicOps);
		this.lookupProvider = registryInfoLookup;
	}

	public <E> Optional<HolderOwner<E>> owner(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		return this.lookupProvider.lookup(resourceKey).map(RegistryOps.RegistryInfo::owner);
	}

	public <E> Optional<HolderGetter<E>> getter(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		return this.lookupProvider.lookup(resourceKey).map(RegistryOps.RegistryInfo::getter);
	}

	public static <E, O> RecordCodecBuilder<O, HolderGetter<E>> retrieveGetter(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		return ExtraCodecs.<HolderGetter<E>>retrieveContext(
				dynamicOps -> dynamicOps instanceof RegistryOps<?> registryOps
						? (DataResult)registryOps.lookupProvider
							.lookup(resourceKey)
							.map(registryInfo -> DataResult.success(registryInfo.getter(), registryInfo.elementsLifecycle()))
							.orElseGet(() -> DataResult.error(() -> "Unknown registry: " + resourceKey))
						: DataResult.error(() -> "Not a registry ops")
			)
			.forGetter(object -> null);
	}

	public static <E, O> RecordCodecBuilder<O, Holder.Reference<E>> retrieveElement(ResourceKey<E> resourceKey) {
		ResourceKey<? extends Registry<E>> resourceKey2 = ResourceKey.createRegistryKey(resourceKey.registry());
		return ExtraCodecs.<Holder.Reference<E>>retrieveContext(
				dynamicOps -> dynamicOps instanceof RegistryOps<?> registryOps
						? (DataResult)registryOps.lookupProvider
							.lookup(resourceKey2)
							.flatMap(registryInfo -> registryInfo.getter().get(resourceKey))
							.map(DataResult::success)
							.orElseGet(() -> DataResult.error(() -> "Can't find value: " + resourceKey))
						: DataResult.error(() -> "Not a registry ops")
			)
			.forGetter(object -> null);
	}

	public static record RegistryInfo<T>(HolderOwner<T> owner, HolderGetter<T> getter, Lifecycle elementsLifecycle) {
	}

	public interface RegistryInfoLookup {
		<T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey);
	}
}
