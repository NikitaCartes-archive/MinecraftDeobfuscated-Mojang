/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.pipeline;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.RenderCall;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class RenderPipeline {
    private final List<ConcurrentLinkedQueue<RenderCall>> renderCalls = ImmutableList.of(new ConcurrentLinkedQueue(), new ConcurrentLinkedQueue(), new ConcurrentLinkedQueue(), new ConcurrentLinkedQueue());
    private volatile boolean isRecording;
    private volatile int recordingBuffer = this.processedBuffer = this.renderingBuffer + 1;
    private volatile boolean isProcessing;
    private volatile int processedBuffer;
    private volatile int renderingBuffer;

    public boolean canBeginRecording() {
        return !this.isRecording && this.recordingBuffer == this.processedBuffer;
    }

    public boolean beginRecording() {
        if (this.isRecording) {
            throw new RuntimeException("ALREADY RECORDING !!!");
        }
        if (this.canBeginRecording()) {
            this.recordingBuffer = (this.processedBuffer + 1) % this.renderCalls.size();
            this.isRecording = true;
            return true;
        }
        return false;
    }

    public void recordRenderCall(RenderCall renderCall) {
        if (!this.isRecording) {
            throw new RuntimeException("NOT RECORDING !!!");
        }
        ConcurrentLinkedQueue<RenderCall> concurrentLinkedQueue = this.getRecordingQueue();
        concurrentLinkedQueue.add(renderCall);
    }

    public void endRecording() {
        if (!this.isRecording) {
            throw new RuntimeException("NOT RECORDING !!!");
        }
        this.isRecording = false;
    }

    public boolean canBeginProcessing() {
        return !this.isProcessing && this.recordingBuffer != this.processedBuffer;
    }

    public boolean beginProcessing() {
        if (this.isProcessing) {
            throw new RuntimeException("ALREADY PROCESSING !!!");
        }
        if (this.canBeginProcessing()) {
            this.isProcessing = true;
            return true;
        }
        return false;
    }

    public void processRecordedQueue() {
        if (!this.isProcessing) {
            throw new RuntimeException("NOT PROCESSING !!!");
        }
    }

    public void endProcessing() {
        if (!this.isProcessing) {
            throw new RuntimeException("NOT PROCESSING !!!");
        }
        this.isProcessing = false;
        this.renderingBuffer = this.processedBuffer;
        this.processedBuffer = this.recordingBuffer;
    }

    public ConcurrentLinkedQueue<RenderCall> startRendering() {
        return this.renderCalls.get(this.renderingBuffer);
    }

    public ConcurrentLinkedQueue<RenderCall> getRecordingQueue() {
        return this.renderCalls.get(this.recordingBuffer);
    }

    public ConcurrentLinkedQueue<RenderCall> getProcessedQueue() {
        return this.renderCalls.get(this.processedBuffer);
    }
}

