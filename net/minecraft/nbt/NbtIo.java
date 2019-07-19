/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.io.BufferedInputStream;
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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

public class NbtIo {
    public static CompoundTag readCompressed(InputStream inputStream) throws IOException {
        try (DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(inputStream)));){
            CompoundTag compoundTag = NbtIo.read(dataInputStream, NbtAccounter.UNLIMITED);
            return compoundTag;
        }
    }

    public static void writeCompressed(CompoundTag compoundTag, OutputStream outputStream) throws IOException {
        try (DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputStream)));){
            NbtIo.write(compoundTag, dataOutputStream);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static void safeWrite(CompoundTag compoundTag, File file) throws IOException {
        File file2 = new File(file.getAbsolutePath() + "_tmp");
        if (file2.exists()) {
            file2.delete();
        }
        NbtIo.write(compoundTag, file2);
        if (file.exists()) {
            file.delete();
        }
        if (file.exists()) {
            throw new IOException("Failed to delete " + file);
        }
        file2.renameTo(file);
    }

    @Environment(value=EnvType.CLIENT)
    public static void write(CompoundTag compoundTag, File file) throws IOException {
        try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));){
            NbtIo.write(compoundTag, dataOutputStream);
        }
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public static CompoundTag read(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));){
            CompoundTag compoundTag = NbtIo.read(dataInputStream, NbtAccounter.UNLIMITED);
            return compoundTag;
        }
    }

    public static CompoundTag read(DataInputStream dataInputStream) throws IOException {
        return NbtIo.read(dataInputStream, NbtAccounter.UNLIMITED);
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

    private static void writeUnnamedTag(Tag tag, DataOutput dataOutput) throws IOException {
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
            return new EndTag();
        }
        dataInput.readUTF();
        Tag tag = Tag.newTag(b);
        try {
            tag.load(dataInput, i, nbtAccounter);
        } catch (IOException iOException) {
            CrashReport crashReport = CrashReport.forThrowable(iOException, "Loading NBT data");
            CrashReportCategory crashReportCategory = crashReport.addCategory("NBT Tag");
            crashReportCategory.setDetail("Tag type", b);
            throw new ReportedException(crashReport);
        }
        return tag;
    }
}

