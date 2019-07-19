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
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class NbtIo {
	public static CompoundTag readCompressed(InputStream inputStream) throws IOException {
		DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(inputStream)));
		Throwable var2 = null;

		CompoundTag var3;
		try {
			var3 = read(dataInputStream, NbtAccounter.UNLIMITED);
		} catch (Throwable var12) {
			var2 = var12;
			throw var12;
		} finally {
			if (dataInputStream != null) {
				if (var2 != null) {
					try {
						dataInputStream.close();
					} catch (Throwable var11) {
						var2.addSuppressed(var11);
					}
				} else {
					dataInputStream.close();
				}
			}
		}

		return var3;
	}

	public static void writeCompressed(CompoundTag compoundTag, OutputStream outputStream) throws IOException {
		DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputStream)));
		Throwable var3 = null;

		try {
			write(compoundTag, dataOutputStream);
		} catch (Throwable var12) {
			var3 = var12;
			throw var12;
		} finally {
			if (dataOutputStream != null) {
				if (var3 != null) {
					try {
						dataOutputStream.close();
					} catch (Throwable var11) {
						var3.addSuppressed(var11);
					}
				} else {
					dataOutputStream.close();
				}
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static void safeWrite(CompoundTag compoundTag, File file) throws IOException {
		File file2 = new File(file.getAbsolutePath() + "_tmp");
		if (file2.exists()) {
			file2.delete();
		}

		write(compoundTag, file2);
		if (file.exists()) {
			file.delete();
		}

		if (file.exists()) {
			throw new IOException("Failed to delete " + file);
		} else {
			file2.renameTo(file);
		}
	}

	@Environment(EnvType.CLIENT)
	public static void write(CompoundTag compoundTag, File file) throws IOException {
		DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));

		try {
			write(compoundTag, dataOutputStream);
		} finally {
			dataOutputStream.close();
		}
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public static CompoundTag read(File file) throws IOException {
		if (!file.exists()) {
			return null;
		} else {
			DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));

			CompoundTag var2;
			try {
				var2 = read(dataInputStream, NbtAccounter.UNLIMITED);
			} finally {
				dataInputStream.close();
			}

			return var2;
		}
	}

	public static CompoundTag read(DataInputStream dataInputStream) throws IOException {
		return read(dataInputStream, NbtAccounter.UNLIMITED);
	}

	public static CompoundTag read(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
		Tag tag = readUnnamedTag(dataInput, 0, nbtAccounter);
		if (tag instanceof CompoundTag) {
			return (CompoundTag)tag;
		} else {
			throw new IOException("Root tag must be a named compound tag");
		}
	}

	public static void write(CompoundTag compoundTag, DataOutput dataOutput) throws IOException {
		writeUnnamedTag(compoundTag, dataOutput);
	}

	private static void writeUnnamedTag(Tag tag, DataOutput dataOutput) throws IOException {
		dataOutput.writeByte(tag.getId());
		if (tag.getId() != 0) {
			dataOutput.writeUTF("");
			tag.write(dataOutput);
		}
	}

	private static Tag readUnnamedTag(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
		byte b = dataInput.readByte();
		if (b == 0) {
			return new EndTag();
		} else {
			dataInput.readUTF();
			Tag tag = Tag.newTag(b);

			try {
				tag.load(dataInput, i, nbtAccounter);
				return tag;
			} catch (IOException var8) {
				CrashReport crashReport = CrashReport.forThrowable(var8, "Loading NBT data");
				CrashReportCategory crashReportCategory = crashReport.addCategory("NBT Tag");
				crashReportCategory.setDetail("Tag type", b);
				throw new ReportedException(crashReport);
			}
		}
	}
}
