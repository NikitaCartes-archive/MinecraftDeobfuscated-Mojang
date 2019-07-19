/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(value=EnvType.CLIENT)
public class LegacyStuffWrapper {
    /*
     * Exception decompiling
     */
    @Deprecated
    public static int[] getPixels(ResourceManager resourceManager, ResourceLocation resourceLocation) throws IOException {
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
}

