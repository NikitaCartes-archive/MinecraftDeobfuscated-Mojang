/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;

public interface TagType<T extends Tag> {
    public T load(DataInput var1, int var2, NbtAccounter var3) throws IOException;

    public StreamTagVisitor.ValueResult parse(DataInput var1, StreamTagVisitor var2) throws IOException;

    default public void parseRoot(DataInput dataInput, StreamTagVisitor streamTagVisitor) throws IOException {
        switch (streamTagVisitor.visitRootEntry(this)) {
            case CONTINUE: {
                this.parse(dataInput, streamTagVisitor);
                break;
            }
            case HALT: {
                break;
            }
            case BREAK: {
                this.skip(dataInput);
            }
        }
    }

    public void skip(DataInput var1, int var2) throws IOException;

    public void skip(DataInput var1) throws IOException;

    default public boolean isValue() {
        return false;
    }

    public String getName();

    public String getPrettyName();

    public static TagType<EndTag> createInvalid(final int i) {
        return new TagType<EndTag>(){

            private IOException createException() {
                return new IOException("Invalid tag id: " + i);
            }

            @Override
            public EndTag load(DataInput dataInput, int i2, NbtAccounter nbtAccounter) throws IOException {
                throw this.createException();
            }

            @Override
            public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor) throws IOException {
                throw this.createException();
            }

            @Override
            public void skip(DataInput dataInput, int i2) throws IOException {
                throw this.createException();
            }

            @Override
            public void skip(DataInput dataInput) throws IOException {
                throw this.createException();
            }

            @Override
            public String getName() {
                return "INVALID[" + i + "]";
            }

            @Override
            public String getPrettyName() {
                return "UNKNOWN_" + i;
            }

            @Override
            public /* synthetic */ Tag load(DataInput dataInput, int i2, NbtAccounter nbtAccounter) throws IOException {
                return this.load(dataInput, i2, nbtAccounter);
            }
        };
    }

    public static interface VariableSize<T extends Tag>
    extends TagType<T> {
        @Override
        default public void skip(DataInput dataInput, int i) throws IOException {
            for (int j = 0; j < i; ++j) {
                this.skip(dataInput);
            }
        }
    }

    public static interface StaticSize<T extends Tag>
    extends TagType<T> {
        @Override
        default public void skip(DataInput dataInput) throws IOException {
            dataInput.skipBytes(this.size());
        }

        @Override
        default public void skip(DataInput dataInput, int i) throws IOException {
            dataInput.skipBytes(this.size() * i);
        }

        public int size();
    }
}

