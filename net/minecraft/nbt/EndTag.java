/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;

public class EndTag
implements Tag {
    private static final int SELF_SIZE_IN_BYTES = 8;
    public static final TagType<EndTag> TYPE = new TagType<EndTag>(){

        @Override
        public EndTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) {
            nbtAccounter.accountBytes(8L);
            return INSTANCE;
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor) {
            return streamTagVisitor.visitEnd();
        }

        @Override
        public void skip(DataInput dataInput, int i) {
        }

        @Override
        public void skip(DataInput dataInput) {
        }

        @Override
        public String getName() {
            return "END";
        }

        @Override
        public String getPrettyName() {
            return "TAG_End";
        }

        @Override
        public boolean isValue() {
            return true;
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, i, nbtAccounter);
        }
    };
    public static final EndTag INSTANCE = new EndTag();

    private EndTag() {
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
    }

    @Override
    public int sizeInBytes() {
        return 8;
    }

    @Override
    public byte getId() {
        return 0;
    }

    public TagType<EndTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.getAsString();
    }

    @Override
    public EndTag copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitEnd(this);
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        return streamTagVisitor.visitEnd();
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

