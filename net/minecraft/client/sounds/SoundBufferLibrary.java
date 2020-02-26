/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.audio.SoundBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(value=EnvType.CLIENT)
public class SoundBufferLibrary {
    private final ResourceManager resourceManager;
    private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> cache = Maps.newHashMap();

    public SoundBufferLibrary(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public CompletableFuture<SoundBuffer> getCompleteBuffer(ResourceLocation resourceLocation2) {
        return this.cache.computeIfAbsent(resourceLocation2, resourceLocation -> CompletableFuture.supplyAsync(() -> {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: Started 5 blocks at once
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.getAnalysis(Method.java:527)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:360)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:176)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:107)
             *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterHelper.applyForwards(ExpressionRewriterHelper.java:12)
             *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation.applyExpressionRewriterToArgs(StaticFunctionInvokation.java:103)
             *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation.applyExpressionRewriter(StaticFunctionInvokation.java:90)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:105)
             *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn.rewriteExpressions(StructuredReturn.java:99)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewrite(LambdaRewriter.java:90)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.rewriteLambdas(Op04StructuredStatement.java:1137)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:912)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:538)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
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
        }, Util.backgroundExecutor()));
    }

    public CompletableFuture<AudioStream> getStream(ResourceLocation resourceLocation, boolean bl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Resource resource = this.resourceManager.getResource(resourceLocation);
                InputStream inputStream = resource.getInputStream();
                return bl ? new LoopingAudioStream(OggAudioStream::new, inputStream) : new OggAudioStream(inputStream);
            } catch (IOException iOException) {
                throw new CompletionException(iOException);
            }
        }, Util.backgroundExecutor());
    }

    public void clear() {
        this.cache.values().forEach(completableFuture -> completableFuture.thenAccept(SoundBuffer::discardAlBuffer));
        this.cache.clear();
    }

    public CompletableFuture<?> preload(Collection<Sound> collection) {
        return CompletableFuture.allOf((CompletableFuture[])collection.stream().map(sound -> this.getCompleteBuffer(sound.getPath())).toArray(CompletableFuture[]::new));
    }
}

