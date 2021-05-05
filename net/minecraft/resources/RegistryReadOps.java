/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistryReadOps<T>
extends DelegatingOps<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String JSON = ".json";
    private final ResourceAccess resources;
    private final RegistryAccess registryAccess;
    private final Map<ResourceKey<? extends Registry<?>>, ReadCache<?>> readCache;
    private final RegistryReadOps<JsonElement> jsonOps;

    public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess registryAccess) {
        return RegistryReadOps.createAndLoad(dynamicOps, ResourceAccess.forResourceManager(resourceManager), registryAccess);
    }

    public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> dynamicOps, ResourceAccess resourceAccess, RegistryAccess registryAccess) {
        RegistryReadOps<T> registryReadOps = new RegistryReadOps<T>(dynamicOps, resourceAccess, registryAccess, Maps.newIdentityHashMap());
        RegistryAccess.load(registryAccess, registryReadOps);
        return registryReadOps;
    }

    public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess registryAccess) {
        return RegistryReadOps.create(dynamicOps, ResourceAccess.forResourceManager(resourceManager), registryAccess);
    }

    public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, ResourceAccess resourceAccess, RegistryAccess registryAccess) {
        return new RegistryReadOps<T>(dynamicOps, resourceAccess, registryAccess, Maps.newIdentityHashMap());
    }

    private RegistryReadOps(DynamicOps<T> dynamicOps, ResourceAccess resourceAccess, RegistryAccess registryAccess, IdentityHashMap<ResourceKey<? extends Registry<?>>, ReadCache<?>> identityHashMap) {
        super(dynamicOps);
        this.resources = resourceAccess;
        this.registryAccess = registryAccess;
        this.readCache = identityHashMap;
        this.jsonOps = dynamicOps == JsonOps.INSTANCE ? this : new RegistryReadOps<JsonElement>(JsonOps.INSTANCE, resourceAccess, registryAccess, identityHashMap);
    }

    protected <E> DataResult<Pair<java.util.function.Supplier<E>, T>> decodeElement(T object, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
        Optional optional = this.registryAccess.ownedRegistry(resourceKey);
        if (!optional.isPresent()) {
            return DataResult.error("Unknown registry: " + resourceKey);
        }
        WritableRegistry writableRegistry = optional.get();
        DataResult dataResult = ResourceLocation.CODEC.decode(this.delegate, object);
        if (!dataResult.result().isPresent()) {
            if (!bl) {
                return DataResult.error("Inline definitions not allowed here");
            }
            return codec.decode(this, object).map(pair -> pair.mapFirst(object -> () -> object));
        }
        Pair pair2 = dataResult.result().get();
        ResourceLocation resourceLocation = (ResourceLocation)pair2.getFirst();
        return this.readAndRegisterElement(resourceKey, writableRegistry, codec, resourceLocation).map(supplier -> Pair.of(supplier, pair2.getSecond()));
    }

    public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> mappedRegistry2, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        Collection<ResourceLocation> collection = this.resources.listResources(resourceKey);
        DataResult<MappedRegistry<Object>> dataResult = DataResult.success(mappedRegistry2, Lifecycle.stable());
        String string = resourceKey.location().getPath() + "/";
        for (ResourceLocation resourceLocation : collection) {
            String string2 = resourceLocation.getPath();
            if (!string2.endsWith(JSON)) {
                LOGGER.warn("Skipping resource {} since it is not a json file", (Object)resourceLocation);
                continue;
            }
            if (!string2.startsWith(string)) {
                LOGGER.warn("Skipping resource {} since it does not have a registry name prefix", (Object)resourceLocation);
                continue;
            }
            String string3 = string2.substring(string.length(), string2.length() - JSON.length());
            ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), string3);
            dataResult = dataResult.flatMap(mappedRegistry -> this.readAndRegisterElement(resourceKey, (WritableRegistry)mappedRegistry, codec, resourceLocation2).map(supplier -> mappedRegistry));
        }
        return dataResult.setPartial(mappedRegistry2);
    }

    private <E> DataResult<java.util.function.Supplier<E>> readAndRegisterElement(ResourceKey<? extends Registry<E>> resourceKey, WritableRegistry<E> writableRegistry, Codec<E> codec, ResourceLocation resourceLocation) {
        ResourceKey resourceKey2 = ResourceKey.create(resourceKey, resourceLocation);
        ReadCache<E> readCache = this.readCache(resourceKey);
        DataResult dataResult = (DataResult)((ReadCache)readCache).values.get(resourceKey2);
        if (dataResult != null) {
            return dataResult;
        }
        Supplier<Object> supplier = Suppliers.memoize(() -> {
            Object object = writableRegistry.get(resourceKey2);
            if (object == null) {
                throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + resourceKey2);
            }
            return object;
        });
        ((ReadCache)readCache).values.put(resourceKey2, DataResult.success(supplier));
        DataResult<Pair<java.util.function.Supplier, OptionalInt>> dataResult2 = this.resources.parseElement(this.jsonOps, resourceKey, resourceKey2, codec);
        Optional optional = dataResult2.result();
        if (optional.isPresent()) {
            Pair pair2 = optional.get();
            writableRegistry.registerOrOverride(pair2.getSecond(), resourceKey2, pair2.getFirst(), dataResult2.lifecycle());
        }
        DataResult<java.util.function.Supplier<Object>> dataResult3 = !optional.isPresent() && writableRegistry.get(resourceKey2) != null ? DataResult.success(() -> writableRegistry.get(resourceKey2), Lifecycle.stable()) : dataResult2.map(pair -> () -> writableRegistry.get(resourceKey2));
        ((ReadCache)readCache).values.put(resourceKey2, dataResult3);
        return dataResult3;
    }

    private <E> ReadCache<E> readCache(ResourceKey<? extends Registry<E>> resourceKey2) {
        return this.readCache.computeIfAbsent(resourceKey2, resourceKey -> new ReadCache());
    }

    protected <E> DataResult<Registry<E>> registry(ResourceKey<? extends Registry<E>> resourceKey) {
        return this.registryAccess.ownedRegistry(resourceKey).map(writableRegistry -> DataResult.success(writableRegistry, writableRegistry.elementsLifecycle())).orElseGet(() -> DataResult.error("Unknown registry: " + resourceKey));
    }

    public static interface ResourceAccess {
        public Collection<ResourceLocation> listResources(ResourceKey<? extends Registry<?>> var1);

        public <E> DataResult<Pair<E, OptionalInt>> parseElement(DynamicOps<JsonElement> var1, ResourceKey<? extends Registry<E>> var2, ResourceKey<E> var3, Decoder<E> var4);

        public static ResourceAccess forResourceManager(final ResourceManager resourceManager) {
            return new ResourceAccess(){

                @Override
                public Collection<ResourceLocation> listResources(ResourceKey<? extends Registry<?>> resourceKey) {
                    return resourceManager.listResources(resourceKey.location().getPath(), string -> string.endsWith(RegistryReadOps.JSON));
                }

                /*
                 * Exception decompiling
                 */
                @Override
                public <E> DataResult<Pair<E, OptionalInt>> parseElement(DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder) {
                    /*
                     * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                     * 
                     * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
                     *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
                     *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
                     *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                     *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                     *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                     *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                     *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                     *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:538)
                     *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
                     *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                     *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                     *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                     *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                     *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
                     *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:261)
                     *     at org.benf.cfr.reader.Driver.doJar(Driver.java:143)
                     *     at net.fabricmc.loom.decompilers.cfr.LoomCFRDecompiler.decompile(LoomCFRDecompiler.java:89)
                     *     at net.fabricmc.loom.task.GenerateSourcesTask$DecompileAction.doDecompile(GenerateSourcesTask.java:269)
                     *     at net.fabricmc.loom.task.GenerateSourcesTask$DecompileAction.execute(GenerateSourcesTask.java:234)
                     *     at org.gradle.workers.internal.DefaultWorkerServer.execute(DefaultWorkerServer.java:63)
                     *     at org.gradle.workers.internal.AbstractClassLoaderWorker$1.create(AbstractClassLoaderWorker.java:49)
                     *     at org.gradle.workers.internal.AbstractClassLoaderWorker$1.create(AbstractClassLoaderWorker.java:43)
                     *     at org.gradle.internal.classloader.ClassLoaderUtils.executeInClassloader(ClassLoaderUtils.java:100)
                     *     at org.gradle.workers.internal.AbstractClassLoaderWorker.executeInClassLoader(AbstractClassLoaderWorker.java:43)
                     *     at org.gradle.workers.internal.IsolatedClassloaderWorker.run(IsolatedClassloaderWorker.java:49)
                     *     at org.gradle.workers.internal.IsolatedClassloaderWorker.run(IsolatedClassloaderWorker.java:30)
                     *     at org.gradle.workers.internal.WorkerDaemonServer.run(WorkerDaemonServer.java:87)
                     *     at org.gradle.workers.internal.WorkerDaemonServer.run(WorkerDaemonServer.java:56)
                     *     at org.gradle.process.internal.worker.request.WorkerAction$1.call(WorkerAction.java:138)
                     *     at org.gradle.process.internal.worker.child.WorkerLogEventListener.withWorkerLoggingProtocol(WorkerLogEventListener.java:41)
                     *     at org.gradle.process.internal.worker.request.WorkerAction.run(WorkerAction.java:135)
                     *     at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
                     *     at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
                     *     at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
                     *     at java.base/java.lang.reflect.Method.invoke(Method.java:568)
                     *     at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:36)
                     *     at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:24)
                     *     at org.gradle.internal.remote.internal.hub.MessageHubBackedObjectConnection$DispatchWrapper.dispatch(MessageHubBackedObjectConnection.java:182)
                     *     at org.gradle.internal.remote.internal.hub.MessageHubBackedObjectConnection$DispatchWrapper.dispatch(MessageHubBackedObjectConnection.java:164)
                     *     at org.gradle.internal.remote.internal.hub.MessageHub$Handler.run(MessageHub.java:414)
                     *     at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
                     *     at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:49)
                     *     at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
                     *     at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
                     *     at java.base/java.lang.Thread.run(Thread.java:833)
                     */
                    throw new IllegalStateException("Decompilation failed");
                }

                public String toString() {
                    return "ResourceAccess[" + resourceManager + "]";
                }

                private static /* synthetic */ Pair method_31157(Object object) {
                    return Pair.of(object, OptionalInt.empty());
                }
            };
        }

        public static final class MemoryMap
        implements ResourceAccess {
            private final Map<ResourceKey<?>, JsonElement> data = Maps.newIdentityHashMap();
            private final Object2IntMap<ResourceKey<?>> ids = new Object2IntOpenCustomHashMap(Util.identityStrategy());
            private final Map<ResourceKey<?>, Lifecycle> lifecycles = Maps.newIdentityHashMap();

            public <E> void add(RegistryAccess.RegistryHolder registryHolder, ResourceKey<E> resourceKey, Encoder<E> encoder, int i, E object, Lifecycle lifecycle) {
                DataResult<JsonElement> dataResult = encoder.encodeStart(RegistryWriteOps.create(JsonOps.INSTANCE, registryHolder), object);
                Optional<DataResult.PartialResult<JsonElement>> optional = dataResult.error();
                if (optional.isPresent()) {
                    LOGGER.error("Error adding element: {}", (Object)optional.get().message());
                    return;
                }
                this.data.put(resourceKey, dataResult.result().get());
                this.ids.put((ResourceKey<?>)resourceKey, i);
                this.lifecycles.put(resourceKey, lifecycle);
            }

            @Override
            public Collection<ResourceLocation> listResources(ResourceKey<? extends Registry<?>> resourceKey) {
                return this.data.keySet().stream().filter(resourceKey2 -> resourceKey2.isFor(resourceKey)).map(resourceKey2 -> new ResourceLocation(resourceKey2.location().getNamespace(), resourceKey.location().getPath() + "/" + resourceKey2.location().getPath() + RegistryReadOps.JSON)).collect(Collectors.toList());
            }

            @Override
            public <E> DataResult<Pair<E, OptionalInt>> parseElement(DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder) {
                JsonElement jsonElement = this.data.get(resourceKey2);
                if (jsonElement == null) {
                    return DataResult.error("Unknown element: " + resourceKey2);
                }
                return decoder.parse(dynamicOps, jsonElement).setLifecycle(this.lifecycles.get(resourceKey2)).map(object -> Pair.of(object, OptionalInt.of(this.ids.getInt(resourceKey2))));
            }
        }
    }

    static final class ReadCache<E> {
        private final Map<ResourceKey<E>, DataResult<java.util.function.Supplier<E>>> values = Maps.newIdentityHashMap();

        private ReadCache() {
        }
    }
}

