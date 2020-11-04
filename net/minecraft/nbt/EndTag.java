/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;

public class EndTag
implements Tag {
    public static final TagType<EndTag> TYPE = new TagType<EndTag>(){

        @Override
        public EndTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) {
            nbtAccounter.accountBits(64L);
            return INSTANCE;
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
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

