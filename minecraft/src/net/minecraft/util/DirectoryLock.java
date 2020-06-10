package net.minecraft.util;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class DirectoryLock implements AutoCloseable {
	private final FileChannel lockFile;
	private final FileLock lock;
	private static final ByteBuffer DUMMY;

	public static DirectoryLock create(Path path) throws IOException {
		Path path2 = path.resolve("session.lock");
		if (!Files.isDirectory(path, new LinkOption[0])) {
			Files.createDirectories(path);
		}

		FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

		try {
			fileChannel.write(DUMMY.duplicate());
			fileChannel.force(true);
			FileLock fileLock = fileChannel.tryLock();
			if (fileLock == null) {
				throw DirectoryLock.LockException.alreadyLocked(path2);
			} else {
				return new DirectoryLock(fileChannel, fileLock);
			}
		} catch (IOException var6) {
			try {
				fileChannel.close();
			} catch (IOException var5) {
				var6.addSuppressed(var5);
			}

			throw var6;
		}
	}

	private DirectoryLock(FileChannel fileChannel, FileLock fileLock) {
		this.lockFile = fileChannel;
		this.lock = fileLock;
	}

	public void close() throws IOException {
		try {
			if (this.lock.isValid()) {
				this.lock.release();
			}
		} finally {
			if (this.lockFile.isOpen()) {
				this.lockFile.close();
			}
		}
	}

	public boolean isValid() {
		return this.lock.isValid();
	}

	@Environment(EnvType.CLIENT)
	public static boolean isLocked(Path path) throws IOException {
		Path path2 = path.resolve("session.lock");

		try {
			FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.WRITE);
			Throwable var3 = null;

			boolean var6;
			try {
				FileLock fileLock = fileChannel.tryLock();
				Throwable var5 = null;

				try {
					var6 = fileLock == null;
				} catch (Throwable var33) {
					var5 = var33;
					throw var33;
				} finally {
					if (fileLock != null) {
						if (var5 != null) {
							try {
								fileLock.close();
							} catch (Throwable var32) {
								var5.addSuppressed(var32);
							}
						} else {
							fileLock.close();
						}
					}
				}
			} catch (Throwable var35) {
				var3 = var35;
				throw var35;
			} finally {
				if (fileChannel != null) {
					if (var3 != null) {
						try {
							fileChannel.close();
						} catch (Throwable var31) {
							var3.addSuppressed(var31);
						}
					} else {
						fileChannel.close();
					}
				}
			}

			return var6;
		} catch (AccessDeniedException var37) {
			return true;
		} catch (NoSuchFileException var38) {
			return false;
		}
	}

	static {
		byte[] bs = "☃".getBytes(Charsets.UTF_8);
		DUMMY = ByteBuffer.allocateDirect(bs.length);
		DUMMY.put(bs);
		DUMMY.flip();
	}

	public static class LockException extends IOException {
		private LockException(Path path, String string) {
			super(path.toAbsolutePath() + ": " + string);
		}

		public static DirectoryLock.LockException alreadyLocked(Path path) {
			return new DirectoryLock.LockException(path, "already locked (possibly by other Minecraft instance?)");
		}
	}
}
