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
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.util.FastBufferedInputStream;

public class NbtIo {
	public static CompoundTag readCompressed(File file) throws IOException {
		InputStream inputStream = new FileInputStream(file);

		CompoundTag var2;
		try {
			var2 = readCompressed(inputStream);
		} catch (Throwable var5) {
			try {
				inputStream.close();
			} catch (Throwable var4) {
				var5.addSuppressed(var4);
			}

			throw var5;
		}

		inputStream.close();
		return var2;
	}

	private static DataInputStream createDecompressorStream(InputStream inputStream) throws IOException {
		return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(inputStream)));
	}

	public static CompoundTag readCompressed(InputStream inputStream) throws IOException {
		DataInputStream dataInputStream = createDecompressorStream(inputStream);

		CompoundTag var2;
		try {
			var2 = read(dataInputStream, NbtAccounter.unlimitedHeap());
		} catch (Throwable var5) {
			if (dataInputStream != null) {
				try {
					dataInputStream.close();
				} catch (Throwable var4) {
					var5.addSuppressed(var4);
				}
			}

			throw var5;
		}

		if (dataInputStream != null) {
			dataInputStream.close();
		}

		return var2;
	}

	public static void parseCompressed(File file, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
		InputStream inputStream = new FileInputStream(file);

		try {
			parseCompressed(inputStream, streamTagVisitor, nbtAccounter);
		} catch (Throwable var7) {
			try {
				inputStream.close();
			} catch (Throwable var6) {
				var7.addSuppressed(var6);
			}

			throw var7;
		}

		inputStream.close();
	}

	public static void parseCompressed(InputStream inputStream, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
		DataInputStream dataInputStream = createDecompressorStream(inputStream);

		try {
			parse(dataInputStream, streamTagVisitor, nbtAccounter);
		} catch (Throwable var7) {
			if (dataInputStream != null) {
				try {
					dataInputStream.close();
				} catch (Throwable var6) {
					var7.addSuppressed(var6);
				}
			}

			throw var7;
		}

		if (dataInputStream != null) {
			dataInputStream.close();
		}
	}

	public static void writeCompressed(CompoundTag compoundTag, File file) throws IOException {
		OutputStream outputStream = new FileOutputStream(file);

		try {
			writeCompressed(compoundTag, outputStream);
		} catch (Throwable var6) {
			try {
				outputStream.close();
			} catch (Throwable var5) {
				var6.addSuppressed(var5);
			}

			throw var6;
		}

		outputStream.close();
	}

	public static void writeCompressed(CompoundTag compoundTag, OutputStream outputStream) throws IOException {
		DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputStream)));

		try {
			write(compoundTag, dataOutputStream);
		} catch (Throwable var6) {
			try {
				dataOutputStream.close();
			} catch (Throwable var5) {
				var6.addSuppressed(var5);
			}

			throw var6;
		}

		dataOutputStream.close();
	}

	public static void write(CompoundTag compoundTag, File file) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(file);

		try {
			DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

			try {
				write(compoundTag, dataOutputStream);
			} catch (Throwable var8) {
				try {
					dataOutputStream.close();
				} catch (Throwable var7) {
					var8.addSuppressed(var7);
				}

				throw var8;
			}

			dataOutputStream.close();
		} catch (Throwable var9) {
			try {
				fileOutputStream.close();
			} catch (Throwable var6) {
				var9.addSuppressed(var6);
			}

			throw var9;
		}

		fileOutputStream.close();
	}

	@Nullable
	public static CompoundTag read(File file) throws IOException {
		if (!file.exists()) {
			return null;
		} else {
			FileInputStream fileInputStream = new FileInputStream(file);

			CompoundTag var3;
			try {
				DataInputStream dataInputStream = new DataInputStream(fileInputStream);

				try {
					var3 = read(dataInputStream, NbtAccounter.unlimitedHeap());
				} catch (Throwable var7) {
					try {
						dataInputStream.close();
					} catch (Throwable var6) {
						var7.addSuppressed(var6);
					}

					throw var7;
				}

				dataInputStream.close();
			} catch (Throwable var8) {
				try {
					fileInputStream.close();
				} catch (Throwable var5) {
					var8.addSuppressed(var5);
				}

				throw var8;
			}

			fileInputStream.close();
			return var3;
		}
	}

	public static CompoundTag read(DataInput dataInput) throws IOException {
		return read(dataInput, NbtAccounter.unlimitedHeap());
	}

	public static CompoundTag read(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
		Tag tag = readUnnamedTag(dataInput, nbtAccounter);
		if (tag instanceof CompoundTag) {
			return (CompoundTag)tag;
		} else {
			throw new IOException("Root tag must be a named compound tag");
		}
	}

	public static void write(CompoundTag compoundTag, DataOutput dataOutput) throws IOException {
		writeUnnamedTag(compoundTag, dataOutput);
	}

	public static void parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
		nbtAccounter.accountBytes(8L);
		TagType<?> tagType = TagTypes.getType(dataInput.readByte());
		if (tagType == EndTag.TYPE) {
			if (streamTagVisitor.visitRootEntry(EndTag.TYPE) == StreamTagVisitor.ValueResult.CONTINUE) {
				streamTagVisitor.visitEnd();
			}
		} else {
			switch (streamTagVisitor.visitRootEntry(tagType)) {
				case HALT:
				default:
					break;
				case BREAK:
					StringTag.skipString(dataInput);
					tagType.skip(dataInput, nbtAccounter);
					break;
				case CONTINUE:
					StringTag.skipString(dataInput);
					tagType.parse(dataInput, streamTagVisitor, nbtAccounter);
			}
		}
	}

	public static Tag readAnyTag(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
		byte b = dataInput.readByte();
		return (Tag)(b == 0 ? EndTag.INSTANCE : readTagSafe(dataInput, nbtAccounter, b));
	}

	public static void writeAnyTag(Tag tag, DataOutput dataOutput) throws IOException {
		dataOutput.writeByte(tag.getId());
		if (tag.getId() != 0) {
			tag.write(dataOutput);
		}
	}

	public static void writeUnnamedTag(Tag tag, DataOutput dataOutput) throws IOException {
		dataOutput.writeByte(tag.getId());
		if (tag.getId() != 0) {
			dataOutput.writeUTF("");
			tag.write(dataOutput);
		}
	}

	private static Tag readUnnamedTag(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
		byte b = dataInput.readByte();
		if (b == 0) {
			return EndTag.INSTANCE;
		} else {
			StringTag.skipString(dataInput);
			return readTagSafe(dataInput, nbtAccounter, b);
		}
	}

	private static Tag readTagSafe(DataInput dataInput, NbtAccounter nbtAccounter, byte b) {
		try {
			return TagTypes.getType(b).load(dataInput, nbtAccounter);
		} catch (IOException var6) {
			CrashReport crashReport = CrashReport.forThrowable(var6, "Loading NBT data");
			CrashReportCategory crashReportCategory = crashReport.addCategory("NBT Tag");
			crashReportCategory.setDetail("Tag type", b);
			throw new ReportedException(crashReport);
		}
	}
}
