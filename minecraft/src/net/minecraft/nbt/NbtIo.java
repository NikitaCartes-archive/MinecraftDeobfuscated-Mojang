package net.minecraft.nbt;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.Util;
import net.minecraft.util.DelegateDataOutput;
import net.minecraft.util.FastBufferedInputStream;

public class NbtIo {
	private static final OpenOption[] SYNC_OUTPUT_OPTIONS = new OpenOption[]{
		StandardOpenOption.SYNC, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
	};

	public static CompoundTag readCompressed(Path path, NbtAccounter nbtAccounter) throws IOException {
		InputStream inputStream = Files.newInputStream(path);

		CompoundTag var3;
		try {
			var3 = readCompressed(inputStream, nbtAccounter);
		} catch (Throwable var6) {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Throwable var5) {
					var6.addSuppressed(var5);
				}
			}

			throw var6;
		}

		if (inputStream != null) {
			inputStream.close();
		}

		return var3;
	}

	private static DataInputStream createDecompressorStream(InputStream inputStream) throws IOException {
		return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(inputStream)));
	}

	private static DataOutputStream createCompressorStream(OutputStream outputStream) throws IOException {
		return new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputStream)));
	}

	public static CompoundTag readCompressed(InputStream inputStream, NbtAccounter nbtAccounter) throws IOException {
		DataInputStream dataInputStream = createDecompressorStream(inputStream);

		CompoundTag var3;
		try {
			var3 = read(dataInputStream, nbtAccounter);
		} catch (Throwable var6) {
			if (dataInputStream != null) {
				try {
					dataInputStream.close();
				} catch (Throwable var5) {
					var6.addSuppressed(var5);
				}
			}

			throw var6;
		}

		if (dataInputStream != null) {
			dataInputStream.close();
		}

		return var3;
	}

	public static void parseCompressed(Path path, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
		InputStream inputStream = Files.newInputStream(path);

		try {
			parseCompressed(inputStream, streamTagVisitor, nbtAccounter);
		} catch (Throwable var7) {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Throwable var6) {
					var7.addSuppressed(var6);
				}
			}

			throw var7;
		}

		if (inputStream != null) {
			inputStream.close();
		}
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

	public static byte[] writeToByteArrayCompressed(CompoundTag compoundTag) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = createCompressorStream(byteArrayOutputStream);

		try {
			write(compoundTag, dataOutputStream);
		} catch (Throwable var6) {
			if (dataOutputStream != null) {
				try {
					dataOutputStream.close();
				} catch (Throwable var5) {
					var6.addSuppressed(var5);
				}
			}

			throw var6;
		}

		if (dataOutputStream != null) {
			dataOutputStream.close();
		}

		return byteArrayOutputStream.toByteArray();
	}

	public static byte[] writeToByteArray(CompoundTag compoundTag) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

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
		return byteArrayOutputStream.toByteArray();
	}

	public static void writeCompressed(CompoundTag compoundTag, Path path) throws IOException {
		OutputStream outputStream = Files.newOutputStream(path, SYNC_OUTPUT_OPTIONS);

		try {
			OutputStream outputStream2 = new BufferedOutputStream(outputStream);

			try {
				writeCompressed(compoundTag, outputStream2);
			} catch (Throwable var8) {
				try {
					outputStream2.close();
				} catch (Throwable var7) {
					var8.addSuppressed(var7);
				}

				throw var8;
			}

			outputStream2.close();
		} catch (Throwable var9) {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Throwable var6) {
					var9.addSuppressed(var6);
				}
			}

			throw var9;
		}

		if (outputStream != null) {
			outputStream.close();
		}
	}

	public static void writeCompressed(CompoundTag compoundTag, OutputStream outputStream) throws IOException {
		DataOutputStream dataOutputStream = createCompressorStream(outputStream);

		try {
			write(compoundTag, dataOutputStream);
		} catch (Throwable var6) {
			if (dataOutputStream != null) {
				try {
					dataOutputStream.close();
				} catch (Throwable var5) {
					var6.addSuppressed(var5);
				}
			}

			throw var6;
		}

		if (dataOutputStream != null) {
			dataOutputStream.close();
		}
	}

	public static void write(CompoundTag compoundTag, Path path) throws IOException {
		OutputStream outputStream = Files.newOutputStream(path, SYNC_OUTPUT_OPTIONS);

		try {
			OutputStream outputStream2 = new BufferedOutputStream(outputStream);

			try {
				DataOutputStream dataOutputStream = new DataOutputStream(outputStream2);

				try {
					write(compoundTag, dataOutputStream);
				} catch (Throwable var10) {
					try {
						dataOutputStream.close();
					} catch (Throwable var9) {
						var10.addSuppressed(var9);
					}

					throw var10;
				}

				dataOutputStream.close();
			} catch (Throwable var11) {
				try {
					outputStream2.close();
				} catch (Throwable var8) {
					var11.addSuppressed(var8);
				}

				throw var11;
			}

			outputStream2.close();
		} catch (Throwable var12) {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Throwable var7) {
					var12.addSuppressed(var7);
				}
			}

			throw var12;
		}

		if (outputStream != null) {
			outputStream.close();
		}
	}

	@Nullable
	public static CompoundTag read(Path path) throws IOException {
		if (!Files.exists(path, new LinkOption[0])) {
			return null;
		} else {
			InputStream inputStream = Files.newInputStream(path);

			CompoundTag var3;
			try {
				DataInputStream dataInputStream = new DataInputStream(inputStream);

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
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var5) {
						var8.addSuppressed(var5);
					}
				}

				throw var8;
			}

			if (inputStream != null) {
				inputStream.close();
			}

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
		writeUnnamedTagWithFallback(compoundTag, dataOutput);
	}

	public static void parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
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

	public static void writeUnnamedTagWithFallback(Tag tag, DataOutput dataOutput) throws IOException {
		writeUnnamedTag(tag, new NbtIo.StringFallbackDataOutput(dataOutput));
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
			throw new ReportedNbtException(crashReport);
		}
	}

	public static class StringFallbackDataOutput extends DelegateDataOutput {
		public StringFallbackDataOutput(DataOutput dataOutput) {
			super(dataOutput);
		}

		@Override
		public void writeUTF(String string) throws IOException {
			try {
				super.writeUTF(string);
			} catch (UTFDataFormatException var3) {
				Util.logAndPauseIfInIde("Failed to write NBT String", var3);
				super.writeUTF("");
			}
		}
	}
}
