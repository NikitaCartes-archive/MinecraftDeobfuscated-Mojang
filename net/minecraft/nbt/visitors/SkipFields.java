/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.visitors.CollectToTag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.FieldTree;

public class SkipFields
extends CollectToTag {
    private final Deque<FieldTree> stack = new ArrayDeque<FieldTree>();

    public SkipFields(FieldSelector ... fieldSelectors) {
        FieldTree fieldTree = FieldTree.createRoot();
        for (FieldSelector fieldSelector : fieldSelectors) {
            fieldTree.addEntry(fieldSelector);
        }
        this.stack.push(fieldTree);
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType, String string) {
        FieldTree fieldTree2;
        FieldTree fieldTree = this.stack.element();
        if (fieldTree.isSelected(tagType, string)) {
            return StreamTagVisitor.EntryResult.SKIP;
        }
        if (tagType == CompoundTag.TYPE && (fieldTree2 = fieldTree.fieldsToRecurse().get(string)) != null) {
            this.stack.push(fieldTree2);
        }
        return super.visitEntry(tagType, string);
    }

    @Override
    public StreamTagVisitor.ValueResult visitContainerEnd() {
        if (this.depth() == this.stack.element().depth()) {
            this.stack.pop();
        }
        return super.visitContainerEnd();
    }
}

