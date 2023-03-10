/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.util.FastBufferedInputStream;
import org.jetbrains.annotations.Nullable;

public class NbtIo {
    public static CompoundTag readCompressed(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file);){
            CompoundTag compoundTag = NbtIo.readCompressed(inputStream);
            return compoundTag;
        }
    }

    private static DataInputStream createDecompressorStream(InputStream inputStream) throws IOException {
        return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(inputStream)));
    }

    public static CompoundTag readCompressed(InputStream inputStream) throws IOException {
        try (DataInputStream dataInputStream = NbtIo.createDecompressorStream(inputStream);){
            CompoundTag compoundTag = NbtIo.read(dataInputStream, NbtAccounter.UNLIMITED);
            return compoundTag;
        }
    }

    public static void parseCompressed(File file, StreamTagVisitor streamTagVisitor) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file);){
            NbtIo.parseCompressed(inputStream, streamTagVisitor);
        }
    }

    public static void parseCompressed(InputStream inputStream, StreamTagVisitor streamTagVisitor) throws IOException {
        try (DataInputStream dataInputStream = NbtIo.createDecompressorStream(inputStream);){
            NbtIo.parse(dataInputStream, streamTagVisitor);
        }
    }

    public static void writeCompressed(CompoundTag compoundTag, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file);){
            NbtIo.writeCompressed(compoundTag, outputStream);
        }
    }

    public static void writeCompressed(CompoundTag compoundTag, OutputStream outputStream) throws IOException {
        try (DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputStream)));){
            NbtIo.write(compoundTag, dataOutputStream);
        }
    }

    public static void write(CompoundTag compoundTag, File file) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);){
            NbtIo.write(compoundTag, dataOutputStream);
        }
    }

    @Nullable
    public static CompoundTag read(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream fileInputStream = new FileInputStream(file);){
            CompoundTag compoundTag;
            try (DataInputStream dataInputStream = new DataInputStream(fileInputStream);){
                compoundTag = NbtIo.read(dataInputStream, NbtAccounter.UNLIMITED);
            }
            return compoundTag;
        }
    }

    public static CompoundTag read(DataInput dataInput) throws IOException {
        return NbtIo.read(dataInput, NbtAccounter.UNLIMITED);
    }

    public static CompoundTag read(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
        Tag tag = NbtIo.readUnnamedTag(dataInput, 0, nbtAccounter);
        if (tag instanceof CompoundTag) {
            return (CompoundTag)tag;
        }
        throw new IOException("Root tag must be a named compound tag");
    }

    public static void write(CompoundTag compoundTag, DataOutput dataOutput) throws IOException {
        NbtIo.writeUnnamedTag(compoundTag, dataOutput);
    }

    public static void parse(DataInput dataInput, StreamTagVisitor streamTagVisitor) throws IOException {
        TagType<?> tagType = TagTypes.getType(dataInput.readByte());
        if (tagType == EndTag.TYPE) {
            if (streamTagVisitor.visitRootEntry(EndTag.TYPE) == StreamTagVisitor.ValueResult.CONTINUE) {
                streamTagVisitor.visitEnd();
            }
            return;
        }
        switch (streamTagVisitor.visitRootEntry(tagType)) {
            case HALT: {
                break;
            }
            case BREAK: {
                StringTag.skipString(dataInput);
                tagType.skip(dataInput);
                break;
            }
            case CONTINUE: {
                StringTag.skipString(dataInput);
                tagType.parse(dataInput, streamTagVisitor);
            }
        }
    }

    public static void writeUnnamedTag(Tag tag, DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(tag.getId());
        if (tag.getId() == 0) {
            return;
        }
        dataOutput.writeUTF("");
        tag.write(dataOutput);
    }

    private static Tag readUnnamedTag(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
        byte b = dataInput.readByte();
        if (b == 0) {
            return EndTag.INSTANCE;
        }
        StringTag.skipString(dataInput);
        try {
            return TagTypes.getType(b).load(dataInput, i, nbtAccounter);
        } catch (IOException iOException) {
            CrashReport crashReport = CrashReport.forThrowable(iOException, "Loading NBT data");
            CrashReportCategory crashReportCategory = crashReport.addCategory("NBT Tag");
            crashReportCategory.setDetail("Tag type", b);
            throw new ReportedException(crashReport);
        }
    }
}

