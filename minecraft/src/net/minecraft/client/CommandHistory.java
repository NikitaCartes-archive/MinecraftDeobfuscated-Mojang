package net.minecraft.client;

import com.google.common.base.Charsets;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.ArrayListDeque;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class CommandHistory {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_PERSISTED_COMMAND_HISTORY = 50;
	private static final String PERSISTED_COMMANDS_FILE_NAME = "command_history.txt";
	private final Path commandsPath;
	private final ArrayListDeque<String> lastCommands = new ArrayListDeque<>(50);

	public CommandHistory(Path path) {
		this.commandsPath = path.resolve("command_history.txt");
		if (Files.exists(this.commandsPath, new LinkOption[0])) {
			try {
				BufferedReader bufferedReader = Files.newBufferedReader(this.commandsPath, Charsets.UTF_8);

				try {
					this.lastCommands.addAll(bufferedReader.lines().toList());
				} catch (Throwable var6) {
					if (bufferedReader != null) {
						try {
							bufferedReader.close();
						} catch (Throwable var5) {
							var6.addSuppressed(var5);
						}
					}

					throw var6;
				}

				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (Exception var7) {
				LOGGER.error("Failed to read {}, command history will be missing", "command_history.txt", var7);
			}
		}
	}

	public void addCommand(String string) {
		if (!string.equals(this.lastCommands.peekLast())) {
			if (this.lastCommands.size() >= 50) {
				this.lastCommands.removeFirst();
			}

			this.lastCommands.addLast(string);
			this.save();
		}
	}

	private void save() {
		try {
			BufferedWriter bufferedWriter = Files.newBufferedWriter(this.commandsPath, Charsets.UTF_8);

			try {
				for (String string : this.lastCommands) {
					bufferedWriter.write(string);
					bufferedWriter.newLine();
				}
			} catch (Throwable var5) {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (Throwable var4) {
						var5.addSuppressed(var4);
					}
				}

				throw var5;
			}

			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
		} catch (IOException var6) {
			LOGGER.error("Failed to write {}, command history will be missing", "command_history.txt", var6);
		}
	}

	public Collection<String> history() {
		return this.lastCommands;
	}
}
