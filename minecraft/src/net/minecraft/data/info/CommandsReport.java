package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.brigadier.CommandDispatcher;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.datafix.DataFixers;

public class CommandsReport implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private final DataGenerator generator;

	public CommandsReport(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	@Override
	public void run(HashCache hashCache) throws IOException {
		YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
		MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
		GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
		File file = new File(this.generator.getOutputFolder().toFile(), "tmp");
		GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(file, MinecraftServer.USERID_CACHE_FILE.getName()));
		DedicatedServerSettings dedicatedServerSettings = new DedicatedServerSettings(Paths.get("server.properties"));
		MinecraftServer minecraftServer = new DedicatedServer(
			file,
			dedicatedServerSettings,
			DataFixers.getDataFixer(),
			yggdrasilAuthenticationService,
			minecraftSessionService,
			gameProfileRepository,
			gameProfileCache,
			LoggerChunkProgressListener::new,
			dedicatedServerSettings.getProperties().levelName
		);
		Path path = this.generator.getOutputFolder().resolve("reports/commands.json");
		CommandDispatcher<CommandSourceStack> commandDispatcher = minecraftServer.getCommands().getDispatcher();
		DataProvider.save(GSON, hashCache, ArgumentTypes.serializeNodeToJson(commandDispatcher, commandDispatcher.getRoot()), path);
	}

	@Override
	public String getName() {
		return "Command Syntax";
	}
}
