/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt.visitors;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.visitors.CollectToTag;

public class CollectFields
extends CollectToTag {
    private int fieldsToGetCount;
    private final Set<TagType<?>> wantedTypes;
    private final Deque<StackFrame> stack = new ArrayDeque<StackFrame>();

    public CollectFields(WantedField ... wantedFields) {
        this.fieldsToGetCount = wantedFields.length;
        ImmutableSet.Builder builder = ImmutableSet.builder();
        StackFrame stackFrame = new StackFrame(1);
        for (WantedField wantedField : wantedFields) {
            stackFrame.addEntry(wantedField);
            builder.add(wantedField.type);
        }
        this.stack.push(stackFrame);
        builder.add(CompoundTag.TYPE);
        this.wantedTypes = builder.build();
    }

    @Override
    public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagType) {
        if (tagType != CompoundTag.TYPE) {
            return StreamTagVisitor.ValueResult.HALT;
        }
        return super.visitRootEntry(tagType);
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType) {
        StackFrame stackFrame = this.stack.element();
        if (this.depth() > stackFrame.depth()) {
            return super.visitEntry(tagType);
        }
        if (this.fieldsToGetCount <= 0) {
            return StreamTagVisitor.EntryResult.HALT;
        }
        if (!this.wantedTypes.contains(tagType)) {
            return StreamTagVisitor.EntryResult.SKIP;
        }
        return super.visitEntry(tagType);
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType, String string) {
        StackFrame stackFrame2;
        StackFrame stackFrame = this.stack.element();
        if (this.depth() > stackFrame.depth()) {
            return super.visitEntry(tagType, string);
        }
        if (stackFrame.fieldsToGet.remove(string, tagType)) {
            --this.fieldsToGetCount;
            return super.visitEntry(tagType, string);
        }
        if (tagType == CompoundTag.TYPE && (stackFrame2 = stackFrame.fieldsToRecurse.get(string)) != null) {
            this.stack.push(stackFrame2);
            return super.visitEntry(tagType, string);
        }
        return StreamTagVisitor.EntryResult.SKIP;
    }

    @Override
    public StreamTagVisitor.ValueResult visitContainerEnd() {
        if (this.depth() == this.stack.element().depth()) {
            this.stack.pop();
        }
        return super.visitContainerEnd();
    }

    public int getMissingFieldCount() {
        return this.fieldsToGetCount;
    }

    record StackFrame(int depth, Map<String, TagType<?>> fieldsToGet, Map<String, StackFrame> fieldsToRecurse) {
        public StackFrame(int i) {
            this(i, new HashMap(), new HashMap<String, StackFrame>());
        }

        public void addEntry(WantedField wantedField) {
            if (this.depth <= wantedField.path.size()) {
                this.fieldsToRecurse.computeIfAbsent(wantedField.path.get(this.depth - 1), string -> new StackFrame(this.depth + 1)).addEntry(wantedField);
            } else {
                this.fieldsToGet.put(wantedField.name, wantedField.type);
            }
        }
    }

    public record WantedField(List<String> path, TagType<?> type, String name) {
        public WantedField(TagType<?> tagType, String string) {
            this(List.of(), tagType, string);
        }

        public WantedField(String string, TagType<?> tagType, String string2) {
            this(List.of(string), tagType, string2);
        }

        public WantedField(String string, String string2, TagType<?> tagType, String string3) {
            this(List.of(string, string2), tagType, string3);
        }
    }
}

