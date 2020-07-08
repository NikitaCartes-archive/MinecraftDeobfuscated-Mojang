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
	public static CompoundTag readCompressed(File file) throws IOException {
		InputStream inputStream = new FileInputStream(file);
		Throwable var2 = null;

		CompoundTag var3;
		try {
			var3 = readCompressed(inputStream);
		} catch (Throwable var12) {
			var2 = var12;
			throw var12;
		} finally {
			if (inputStream != null) {
				if (var2 != null) {
					try {
						inputStream.close();
					} catch (Throwable var11) {
						var2.addSuppressed(var11);
					}
				} else {
					inputStream.close();
				}
			}
		}

		return var3;
	}

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

	public static void writeCompressed(CompoundTag compoundTag, File file) throws IOException {
		OutputStream outputStream = new FileOutputStream(file);
		Throwable var3 = null;

		try {
			writeCompressed(compoundTag, outputStream);
		} catch (Throwable var12) {
			var3 = var12;
			throw var12;
		} finally {
			if (outputStream != null) {
				if (var3 != null) {
					try {
						outputStream.close();
					} catch (Throwable var11) {
						var3.addSuppressed(var11);
					}
				} else {
					outputStream.close();
				}
			}
		}
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
	public static void write(CompoundTag compoundTag, File file) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		Throwable var3 = null;

		try {
			DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
			Throwable var5 = null;

			try {
				write(compoundTag, dataOutputStream);
			} catch (Throwable var28) {
				var5 = var28;
				throw var28;
			} finally {
				if (dataOutputStream != null) {
					if (var5 != null) {
						try {
							dataOutputStream.close();
						} catch (Throwable var27) {
							var5.addSuppressed(var27);
						}
					} else {
						dataOutputStream.close();
					}
				}
			}
		} catch (Throwable var30) {
			var3 = var30;
			throw var30;
		} finally {
			if (fileOutputStream != null) {
				if (var3 != null) {
					try {
						fileOutputStream.close();
					} catch (Throwable var26) {
						var3.addSuppressed(var26);
					}
				} else {
					fileOutputStream.close();
				}
			}
		}
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public static CompoundTag read(File file) throws IOException {
		if (!file.exists()) {
			return null;
		} else {
			FileInputStream fileInputStream = new FileInputStream(file);
			Throwable var2 = null;

			CompoundTag var5;
			try {
				DataInputStream dataInputStream = new DataInputStream(fileInputStream);
				Throwable var4 = null;

				try {
					var5 = read(dataInputStream, NbtAccounter.UNLIMITED);
				} catch (Throwable var28) {
					var4 = var28;
					throw var28;
				} finally {
					if (dataInputStream != null) {
						if (var4 != null) {
							try {
								dataInputStream.close();
							} catch (Throwable var27) {
								var4.addSuppressed(var27);
							}
						} else {
							dataInputStream.close();
						}
					}
				}
			} catch (Throwable var30) {
				var2 = var30;
				throw var30;
			} finally {
				if (fileInputStream != null) {
					if (var2 != null) {
						try {
							fileInputStream.close();
						} catch (Throwable var26) {
							var2.addSuppressed(var26);
						}
					} else {
						fileInputStream.close();
					}
				}
			}

			return var5;
		}
	}

	public static CompoundTag read(DataInput dataInput) throws IOException {
		return read(dataInput, NbtAccounter.UNLIMITED);
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
			return EndTag.INSTANCE;
		} else {
			dataInput.readUTF();

			try {
				return TagTypes.getType(b).load(dataInput, i, nbtAccounter);
			} catch (IOException var7) {
				CrashReport crashReport = CrashReport.forThrowable(var7, "Loading NBT data");
				CrashReportCategory crashReportCategory = crashReport.addCategory("NBT Tag");
				crashReportCategory.setDetail("Tag type", b);
				throw new ReportedException(crashReport);
			}
		}
	}
}
