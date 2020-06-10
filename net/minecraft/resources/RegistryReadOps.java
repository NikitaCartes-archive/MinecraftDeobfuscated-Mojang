/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Codecs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistryReadOps<T>
extends DelegatingOps<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceManager resourceManager;
    private final RegistryAccess registryHolder;
    private final Map<ResourceKey<? extends Registry<?>>, ReadCache<?>> readCache = Maps.newIdentityHashMap();

    public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess registryAccess) {
        return new RegistryReadOps<T>(dynamicOps, resourceManager, registryAccess);
    }

    private RegistryReadOps(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess registryAccess) {
        super(dynamicOps);
        this.resourceManager = resourceManager;
        this.registryHolder = registryAccess;
    }

    protected <E> DataResult<Pair<java.util.function.Supplier<E>, T>> decodeElement(T object, ResourceKey<Registry<E>> resourceKey, MapCodec<E> mapCodec) {
        Optional<WritableRegistry<E>> optional = this.registryHolder.registry(resourceKey);
        if (!optional.isPresent()) {
            return DataResult.error("Unknown registry: " + resourceKey);
        }
        WritableRegistry writableRegistry = optional.get();
        DataResult dataResult = ResourceLocation.CODEC.decode(this.delegate, object);
        if (!dataResult.result().isPresent()) {
            return Codecs.withName(resourceKey, mapCodec).codec().decode(this.delegate, object).map(pair2 -> pair2.mapFirst(pair -> {
                writableRegistry.register((ResourceKey)pair.getFirst(), pair.getSecond());
                writableRegistry.setPersistent((ResourceKey)pair.getFirst());
                return pair::getSecond;
            }));
        }
        Pair pair = dataResult.result().get();
        ResourceLocation resourceLocation = (ResourceLocation)pair.getFirst();
        return this.readAndRegisterElement(resourceKey, writableRegistry, mapCodec, resourceLocation).map(supplier -> Pair.of(supplier, pair.getSecond()));
    }

    public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> mappedRegistry2, ResourceKey<Registry<E>> resourceKey, MapCodec<E> mapCodec) {
        ResourceLocation resourceLocation = resourceKey.location();
        Collection<ResourceLocation> collection = this.resourceManager.listResources(resourceLocation, string -> string.endsWith(".json"));
        DataResult<MappedRegistry<Object>> dataResult = DataResult.success(mappedRegistry2, Lifecycle.stable());
        for (ResourceLocation resourceLocation2 : collection) {
            String string2 = resourceLocation2.getPath();
            if (!string2.endsWith(".json")) {
                LOGGER.warn("Skipping resource {} since it is not a json file", (Object)resourceLocation2);
                continue;
            }
            if (!string2.startsWith(resourceLocation.getPath() + "/")) {
                LOGGER.warn("Skipping resource {} since it does not have a registry name prefix", (Object)resourceLocation2);
                continue;
            }
            String string22 = string2.substring(0, string2.length() - ".json".length()).substring(resourceLocation.getPath().length() + 1);
            int i = string22.indexOf(47);
            if (i < 0) {
                LOGGER.warn("Skipping resource {} since it does not have a namespace", (Object)resourceLocation2);
                continue;
            }
            String string3 = string22.substring(0, i);
            String string4 = string22.substring(i + 1);
            ResourceLocation resourceLocation3 = new ResourceLocation(string3, string4);
            dataResult = dataResult.flatMap(mappedRegistry -> this.readAndRegisterElement(resourceKey, (WritableRegistry)mappedRegistry, mapCodec, resourceLocation3).map(supplier -> mappedRegistry));
        }
        return dataResult.setPartial(mappedRegistry2);
    }

    private <E> DataResult<java.util.function.Supplier<E>> readAndRegisterElement(ResourceKey<Registry<E>> resourceKey, WritableRegistry<E> writableRegistry, MapCodec<E> mapCodec, ResourceLocation resourceLocation) {
        ResourceKey resourceKey2 = ResourceKey.create(resourceKey, resourceLocation);
        Object object2 = writableRegistry.get(resourceKey2);
        if (object2 != null) {
            return DataResult.success(() -> object2, Lifecycle.stable());
        }
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
        DataResult<E> dataResult2 = this.readElementFromFile(resourceKey, resourceKey2, mapCodec);
        dataResult2.result().ifPresent(object -> writableRegistry.register(resourceKey2, object));
        DataResult<java.util.function.Supplier<E>> dataResult3 = dataResult2.map(object -> () -> object);
        ((ReadCache)readCache).values.put(resourceKey2, dataResult3);
        return dataResult3;
    }

    /*
     * Exception decompiling
     */
    private <E> DataResult<E> readElementFromFile(ResourceKey<Registry<E>> resourceKey, ResourceKey<E> resourceKey2, MapCodec<E> mapCodec) {
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

    private <E> ReadCache<E> readCache(ResourceKey<Registry<E>> resourceKey2) {
        return this.readCache.computeIfAbsent(resourceKey2, resourceKey -> new ReadCache());
    }

    static final class ReadCache<E> {
        private final Map<ResourceKey<E>, DataResult<java.util.function.Supplier<E>>> values = Maps.newIdentityHashMap();

        private ReadCache() {
        }
    }
}

