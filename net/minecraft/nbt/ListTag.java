/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.nbt.TagVisitor;

public class ListTag
extends CollectionTag<Tag> {
    private static final int SELF_SIZE_IN_BITS = 296;
    public static final TagType<ListTag> TYPE = new TagType.VariableSize<ListTag>(){

        @Override
        public ListTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(296L);
            if (i > 512) {
                throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            }
            byte b = dataInput.readByte();
            int j = dataInput.readInt();
            if (b == 0 && j > 0) {
                throw new RuntimeException("Missing type on ListTag");
            }
            nbtAccounter.accountBits(32L * (long)j);
            TagType<?> tagType = TagTypes.getType(b);
            ArrayList<Tag> list = Lists.newArrayListWithCapacity(j);
            for (int k = 0; k < j; ++k) {
                list.add((Tag)tagType.load(dataInput, i + 1, nbtAccounter));
            }
            return new ListTag(list, b);
        }

        /*
         * Exception decompiling
         */
        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor) throws IOException {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [8[CASE], 4[SWITCH]], but top level block is 9[SWITCH]
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:538)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
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

        @Override
        public void skip(DataInput dataInput) throws IOException {
            TagType<?> tagType = TagTypes.getType(dataInput.readByte());
            int i = dataInput.readInt();
            tagType.skip(dataInput, i);
        }

        @Override
        public String getName() {
            return "LIST";
        }

        @Override
        public String getPrettyName() {
            return "TAG_List";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, i, nbtAccounter);
        }
    };
    private final List<Tag> list;
    private byte type;

    ListTag(List<Tag> list, byte b) {
        this.list = list;
        this.type = b;
    }

    public ListTag() {
        this(Lists.newArrayList(), 0);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        this.type = this.list.isEmpty() ? (byte)0 : this.list.get(0).getId();
        dataOutput.writeByte(this.type);
        dataOutput.writeInt(this.list.size());
        for (Tag tag : this.list) {
            tag.write(dataOutput);
        }
    }

    @Override
    public byte getId() {
        return 9;
    }

    public TagType<ListTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.getAsString();
    }

    private void updateTypeAfterRemove() {
        if (this.list.isEmpty()) {
            this.type = 0;
        }
    }

    @Override
    public Tag remove(int i) {
        Tag tag = this.list.remove(i);
        this.updateTypeAfterRemove();
        return tag;
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public CompoundTag getCompound(int i) {
        Tag tag;
        if (i >= 0 && i < this.list.size() && (tag = this.list.get(i)).getId() == 10) {
            return (CompoundTag)tag;
        }
        return new CompoundTag();
    }

    public ListTag getList(int i) {
        Tag tag;
        if (i >= 0 && i < this.list.size() && (tag = this.list.get(i)).getId() == 9) {
            return (ListTag)tag;
        }
        return new ListTag();
    }

    public short getShort(int i) {
        Tag tag;
        if (i >= 0 && i < this.list.size() && (tag = this.list.get(i)).getId() == 2) {
            return ((ShortTag)tag).getAsShort();
        }
        return 0;
    }

    public int getInt(int i) {
        Tag tag;
        if (i >= 0 && i < this.list.size() && (tag = this.list.get(i)).getId() == 3) {
            return ((IntTag)tag).getAsInt();
        }
        return 0;
    }

    public int[] getIntArray(int i) {
        Tag tag;
        if (i >= 0 && i < this.list.size() && (tag = this.list.get(i)).getId() == 11) {
            return ((IntArrayTag)tag).getAsIntArray();
        }
        return new int[0];
    }

    public long[] getLongArray(int i) {
        Tag tag;
        if (i >= 0 && i < this.list.size() && (tag = this.list.get(i)).getId() == 11) {
            return ((LongArrayTag)tag).getAsLongArray();
        }
        return new long[0];
    }

    public double getDouble(int i) {
        Tag tag;
        if (i >= 0 && i < this.list.size() && (tag = this.list.get(i)).getId() == 6) {
            return ((DoubleTag)tag).getAsDouble();
        }
        return 0.0;
    }

    public float getFloat(int i) {
        Tag tag;
        if (i >= 0 && i < this.list.size() && (tag = this.list.get(i)).getId() == 5) {
            return ((FloatTag)tag).getAsFloat();
        }
        return 0.0f;
    }

    public String getString(int i) {
        if (i < 0 || i >= this.list.size()) {
            return "";
        }
        Tag tag = this.list.get(i);
        if (tag.getId() == 8) {
            return tag.getAsString();
        }
        return tag.toString();
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public Tag get(int i) {
        return this.list.get(i);
    }

    @Override
    public Tag set(int i, Tag tag) {
        Tag tag2 = this.get(i);
        if (!this.setTag(i, tag)) {
            throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", tag.getId(), this.type));
        }
        return tag2;
    }

    @Override
    public void add(int i, Tag tag) {
        if (!this.addTag(i, tag)) {
            throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", tag.getId(), this.type));
        }
    }

    @Override
    public boolean setTag(int i, Tag tag) {
        if (this.updateType(tag)) {
            this.list.set(i, tag);
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int i, Tag tag) {
        if (this.updateType(tag)) {
            this.list.add(i, tag);
            return true;
        }
        return false;
    }

    private boolean updateType(Tag tag) {
        if (tag.getId() == 0) {
            return false;
        }
        if (this.type == 0) {
            this.type = tag.getId();
            return true;
        }
        return this.type == tag.getId();
    }

    @Override
    public ListTag copy() {
        List<Tag> iterable = TagTypes.getType(this.type).isValue() ? this.list : Iterables.transform(this.list, Tag::copy);
        ArrayList<Tag> list = Lists.newArrayList(iterable);
        return new ListTag(list, this.type);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof ListTag && Objects.equals(this.list, ((ListTag)object).list);
    }

    @Override
    public int hashCode() {
        return this.list.hashCode();
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitList(this);
    }

    @Override
    public byte getElementType() {
        return this.type;
    }

    @Override
    public void clear() {
        this.list.clear();
        this.type = 0;
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        switch (streamTagVisitor.visitList(TagTypes.getType(this.type), this.list.size())) {
            case HALT: {
                return StreamTagVisitor.ValueResult.HALT;
            }
            case BREAK: {
                return streamTagVisitor.visitContainerEnd();
            }
        }
        block13: for (int i = 0; i < this.list.size(); ++i) {
            Tag tag = this.list.get(i);
            switch (streamTagVisitor.visitElement(tag.getType(), i)) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case SKIP: {
                    continue block13;
                }
                case BREAK: {
                    return streamTagVisitor.visitContainerEnd();
                }
                default: {
                    switch (tag.accept(streamTagVisitor)) {
                        case HALT: {
                            return StreamTagVisitor.ValueResult.HALT;
                        }
                        case BREAK: {
                            return streamTagVisitor.visitContainerEnd();
                        }
                    }
                }
            }
        }
        return streamTagVisitor.visitContainerEnd();
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }

    @Override
    public /* synthetic */ Object remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ void add(int i, Object object) {
        this.add(i, (Tag)object);
    }

    @Override
    public /* synthetic */ Object set(int i, Object object) {
        return this.set(i, (Tag)object);
    }

    @Override
    public /* synthetic */ Object get(int i) {
        return this.get(i);
    }
}

