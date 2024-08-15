package com.mojang.blaze3d.framegraph;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FrameGraphBuilder {
	private final List<FrameGraphBuilder.InternalVirtualResource<?>> internalResources = new ArrayList();
	private final List<FrameGraphBuilder.ExternalResource<?>> externalResources = new ArrayList();
	private final List<FrameGraphBuilder.Pass> passes = new ArrayList();

	public FramePass addPass(String string) {
		FrameGraphBuilder.Pass pass = new FrameGraphBuilder.Pass(this.passes.size(), string);
		this.passes.add(pass);
		return pass;
	}

	public <T> ResourceHandle<T> importExternal(String string, T object) {
		FrameGraphBuilder.ExternalResource<T> externalResource = new FrameGraphBuilder.ExternalResource<>(string, null, object);
		this.externalResources.add(externalResource);
		return externalResource.handle;
	}

	public <T> ResourceHandle<T> createInternal(String string, ResourceDescriptor<T> resourceDescriptor) {
		return this.createInternalResource(string, resourceDescriptor, null).handle;
	}

	<T> FrameGraphBuilder.InternalVirtualResource<T> createInternalResource(
		String string, ResourceDescriptor<T> resourceDescriptor, @Nullable FrameGraphBuilder.Pass pass
	) {
		int i = this.internalResources.size();
		FrameGraphBuilder.InternalVirtualResource<T> internalVirtualResource = new FrameGraphBuilder.InternalVirtualResource<>(i, string, pass, resourceDescriptor);
		this.internalResources.add(internalVirtualResource);
		return internalVirtualResource;
	}

	public void execute(GraphicsResourceAllocator graphicsResourceAllocator) {
		this.execute(graphicsResourceAllocator, FrameGraphBuilder.Inspector.NONE);
	}

	public void execute(GraphicsResourceAllocator graphicsResourceAllocator, FrameGraphBuilder.Inspector inspector) {
		BitSet bitSet = this.identifyPassesToKeep();
		List<FrameGraphBuilder.Pass> list = new ArrayList(bitSet.cardinality());
		BitSet bitSet2 = new BitSet(this.passes.size());

		for (FrameGraphBuilder.Pass pass : this.passes) {
			this.resolvePassOrder(pass, bitSet, bitSet2, list);
		}

		this.assignResourceLifetimes(list);

		for (FrameGraphBuilder.Pass pass : list) {
			for (FrameGraphBuilder.InternalVirtualResource<?> internalVirtualResource : pass.resourcesToAcquire) {
				inspector.acquireResource(internalVirtualResource.name);
				internalVirtualResource.acquire(graphicsResourceAllocator);
			}

			inspector.beforeExecutePass(pass.name);
			pass.task.run();
			inspector.afterExecutePass(pass.name);

			for (int i = pass.resourcesToRelease.nextSetBit(0); i >= 0; i = pass.resourcesToRelease.nextSetBit(i + 1)) {
				FrameGraphBuilder.InternalVirtualResource<?> internalVirtualResource = (FrameGraphBuilder.InternalVirtualResource<?>)this.internalResources.get(i);
				inspector.releaseResource(internalVirtualResource.name);
				internalVirtualResource.release(graphicsResourceAllocator);
			}
		}
	}

	private BitSet identifyPassesToKeep() {
		Deque<FrameGraphBuilder.Pass> deque = new ArrayDeque(this.passes.size());
		BitSet bitSet = new BitSet(this.passes.size());

		for (FrameGraphBuilder.VirtualResource<?> virtualResource : this.externalResources) {
			FrameGraphBuilder.Pass pass = virtualResource.handle.createdBy;
			if (pass != null) {
				this.discoverAllRequiredPasses(pass, bitSet, deque);
			}
		}

		for (FrameGraphBuilder.Pass pass2 : this.passes) {
			if (pass2.disableCulling) {
				this.discoverAllRequiredPasses(pass2, bitSet, deque);
			}
		}

		return bitSet;
	}

	private void discoverAllRequiredPasses(FrameGraphBuilder.Pass pass, BitSet bitSet, Deque<FrameGraphBuilder.Pass> deque) {
		deque.add(pass);

		while (!deque.isEmpty()) {
			FrameGraphBuilder.Pass pass2 = (FrameGraphBuilder.Pass)deque.poll();
			if (!bitSet.get(pass2.id)) {
				bitSet.set(pass2.id);

				for (int i = pass2.requiredPassIds.nextSetBit(0); i >= 0; i = pass2.requiredPassIds.nextSetBit(i + 1)) {
					deque.add((FrameGraphBuilder.Pass)this.passes.get(i));
				}
			}
		}
	}

	private void resolvePassOrder(FrameGraphBuilder.Pass pass, BitSet bitSet, BitSet bitSet2, List<FrameGraphBuilder.Pass> list) {
		if (bitSet2.get(pass.id)) {
			String string = (String)bitSet2.stream().mapToObj(ix -> ((FrameGraphBuilder.Pass)this.passes.get(ix)).name).collect(Collectors.joining(", "));
			throw new IllegalStateException("Frame graph cycle detected between " + string);
		} else if (bitSet.get(pass.id)) {
			bitSet2.set(pass.id);
			bitSet.clear(pass.id);

			for (int i = pass.requiredPassIds.nextSetBit(0); i >= 0; i = pass.requiredPassIds.nextSetBit(i + 1)) {
				this.resolvePassOrder((FrameGraphBuilder.Pass)this.passes.get(i), bitSet, bitSet2, list);
			}

			for (FrameGraphBuilder.Handle<?> handle : pass.writesFrom) {
				for (int j = handle.readBy.nextSetBit(0); j >= 0; j = handle.readBy.nextSetBit(j + 1)) {
					if (j != pass.id) {
						this.resolvePassOrder((FrameGraphBuilder.Pass)this.passes.get(j), bitSet, bitSet2, list);
					}
				}
			}

			list.add(pass);
			bitSet2.clear(pass.id);
		}
	}

	private void assignResourceLifetimes(Collection<FrameGraphBuilder.Pass> collection) {
		FrameGraphBuilder.Pass[] passs = new FrameGraphBuilder.Pass[this.internalResources.size()];

		for (FrameGraphBuilder.Pass pass : collection) {
			for (int i = pass.requiredResourceIds.nextSetBit(0); i >= 0; i = pass.requiredResourceIds.nextSetBit(i + 1)) {
				FrameGraphBuilder.InternalVirtualResource<?> internalVirtualResource = (FrameGraphBuilder.InternalVirtualResource<?>)this.internalResources.get(i);
				FrameGraphBuilder.Pass pass2 = passs[i];
				passs[i] = pass;
				if (pass2 == null) {
					pass.resourcesToAcquire.add(internalVirtualResource);
				} else {
					pass2.resourcesToRelease.clear(i);
				}

				pass.resourcesToRelease.set(i);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class ExternalResource<T> extends FrameGraphBuilder.VirtualResource<T> {
		private final T resource;

		public ExternalResource(String string, @Nullable FrameGraphBuilder.Pass pass, T object) {
			super(string, pass);
			this.resource = object;
		}

		@Override
		public T get() {
			return this.resource;
		}
	}

	@Environment(EnvType.CLIENT)
	static class Handle<T> implements ResourceHandle<T> {
		final FrameGraphBuilder.VirtualResource<T> holder;
		private final int version;
		@Nullable
		final FrameGraphBuilder.Pass createdBy;
		final BitSet readBy = new BitSet();
		@Nullable
		private FrameGraphBuilder.Handle<T> aliasedBy;

		Handle(FrameGraphBuilder.VirtualResource<T> virtualResource, int i, @Nullable FrameGraphBuilder.Pass pass) {
			this.holder = virtualResource;
			this.version = i;
			this.createdBy = pass;
		}

		@Override
		public T get() {
			return this.holder.get();
		}

		FrameGraphBuilder.Handle<T> writeAndAlias(FrameGraphBuilder.Pass pass) {
			if (this.holder.handle != this) {
				throw new IllegalStateException("Handle " + this + " is no longer valid, as its contents were moved into " + this.aliasedBy);
			} else {
				FrameGraphBuilder.Handle<T> handle = new FrameGraphBuilder.Handle<>(this.holder, this.version + 1, pass);
				this.holder.handle = handle;
				this.aliasedBy = handle;
				return handle;
			}
		}

		public String toString() {
			return this.createdBy != null ? this.holder + "#" + this.version + " (from " + this.createdBy + ")" : this.holder + "#" + this.version;
		}
	}

	@Environment(EnvType.CLIENT)
	public interface Inspector {
		FrameGraphBuilder.Inspector NONE = new FrameGraphBuilder.Inspector() {
		};

		default void acquireResource(String string) {
		}

		default void releaseResource(String string) {
		}

		default void beforeExecutePass(String string) {
		}

		default void afterExecutePass(String string) {
		}
	}

	@Environment(EnvType.CLIENT)
	static class InternalVirtualResource<T> extends FrameGraphBuilder.VirtualResource<T> {
		final int id;
		private final ResourceDescriptor<T> descriptor;
		@Nullable
		private T physicalResource;

		public InternalVirtualResource(int i, String string, @Nullable FrameGraphBuilder.Pass pass, ResourceDescriptor<T> resourceDescriptor) {
			super(string, pass);
			this.id = i;
			this.descriptor = resourceDescriptor;
		}

		@Override
		public T get() {
			return (T)Objects.requireNonNull(this.physicalResource, "Resource is not currently available");
		}

		public void acquire(GraphicsResourceAllocator graphicsResourceAllocator) {
			if (this.physicalResource != null) {
				throw new IllegalStateException("Tried to acquire physical resource, but it was already assigned");
			} else {
				this.physicalResource = graphicsResourceAllocator.acquire(this.descriptor);
			}
		}

		public void release(GraphicsResourceAllocator graphicsResourceAllocator) {
			if (this.physicalResource == null) {
				throw new IllegalStateException("Tried to release physical resource that was not allocated");
			} else {
				graphicsResourceAllocator.release(this.descriptor, this.physicalResource);
				this.physicalResource = null;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class Pass implements FramePass {
		final int id;
		final String name;
		final List<FrameGraphBuilder.Handle<?>> writesFrom = new ArrayList();
		final BitSet requiredResourceIds = new BitSet();
		final BitSet requiredPassIds = new BitSet();
		Runnable task = () -> {
		};
		final List<FrameGraphBuilder.InternalVirtualResource<?>> resourcesToAcquire = new ArrayList();
		final BitSet resourcesToRelease = new BitSet();
		boolean disableCulling;

		public Pass(final int i, final String string) {
			this.id = i;
			this.name = string;
		}

		private <T> void markResourceRequired(FrameGraphBuilder.Handle<T> handle) {
			if (handle.holder instanceof FrameGraphBuilder.InternalVirtualResource<?> internalVirtualResource) {
				this.requiredResourceIds.set(internalVirtualResource.id);
			}
		}

		private void markPassRequired(FrameGraphBuilder.Pass pass) {
			this.requiredPassIds.set(pass.id);
		}

		@Override
		public <T> ResourceHandle<T> createsInternal(String string, ResourceDescriptor<T> resourceDescriptor) {
			FrameGraphBuilder.InternalVirtualResource<T> internalVirtualResource = FrameGraphBuilder.this.createInternalResource(string, resourceDescriptor, this);
			this.requiredResourceIds.set(internalVirtualResource.id);
			return internalVirtualResource.handle;
		}

		@Override
		public <T> void reads(ResourceHandle<T> resourceHandle) {
			this._reads((FrameGraphBuilder.Handle<T>)resourceHandle);
		}

		private <T> void _reads(FrameGraphBuilder.Handle<T> handle) {
			this.markResourceRequired(handle);
			if (handle.createdBy != null) {
				this.markPassRequired(handle.createdBy);
			}

			handle.readBy.set(this.id);
		}

		@Override
		public <T> ResourceHandle<T> readsAndWrites(ResourceHandle<T> resourceHandle) {
			return this._readsAndWrites((FrameGraphBuilder.Handle<T>)resourceHandle);
		}

		@Override
		public void requires(FramePass framePass) {
			this.requiredPassIds.set(((FrameGraphBuilder.Pass)framePass).id);
		}

		@Override
		public void disableCulling() {
			this.disableCulling = true;
		}

		private <T> FrameGraphBuilder.Handle<T> _readsAndWrites(FrameGraphBuilder.Handle<T> handle) {
			this.writesFrom.add(handle);
			this._reads(handle);
			return handle.writeAndAlias(this);
		}

		@Override
		public void executes(Runnable runnable) {
			this.task = runnable;
		}

		public String toString() {
			return this.name;
		}
	}

	@Environment(EnvType.CLIENT)
	abstract static class VirtualResource<T> {
		public final String name;
		public FrameGraphBuilder.Handle<T> handle;

		public VirtualResource(String string, @Nullable FrameGraphBuilder.Pass pass) {
			this.name = string;
			this.handle = new FrameGraphBuilder.Handle<>(this, 0, pass);
		}

		public abstract T get();

		public String toString() {
			return this.name;
		}
	}
}
