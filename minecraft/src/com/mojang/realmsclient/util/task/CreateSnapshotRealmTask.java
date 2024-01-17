package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class CreateSnapshotRealmTask extends LongRunningTask {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component TITLE = Component.translatable("mco.snapshot.creating");
	private final long parentId;
	private final WorldGenerationInfo generationInfo;
	private final String name;
	private final String description;
	private final RealmsMainScreen realmsMainScreen;
	@Nullable
	private RealmCreationTask creationTask;
	@Nullable
	private ResettingGeneratedWorldTask generateWorldTask;

	public CreateSnapshotRealmTask(RealmsMainScreen realmsMainScreen, long l, WorldGenerationInfo worldGenerationInfo, String string, String string2) {
		this.parentId = l;
		this.generationInfo = worldGenerationInfo;
		this.name = string;
		this.description = string2;
		this.realmsMainScreen = realmsMainScreen;
	}

	public void run() {
		RealmsClient realmsClient = RealmsClient.create();

		try {
			RealmsServer realmsServer = realmsClient.createSnapshotRealm(this.parentId);
			this.creationTask = new RealmCreationTask(realmsServer.id, this.name, this.description);
			this.generateWorldTask = new ResettingGeneratedWorldTask(
				this.generationInfo,
				realmsServer.id,
				RealmsResetWorldScreen.CREATE_WORLD_RESET_TASK_TITLE,
				() -> Minecraft.getInstance().execute(() -> RealmsMainScreen.play(realmsServer, this.realmsMainScreen, true))
			);
			if (this.aborted()) {
				return;
			}

			this.creationTask.run();
			if (this.aborted()) {
				return;
			}

			this.generateWorldTask.run();
		} catch (RealmsServiceException var3) {
			LOGGER.error("Couldn't create snapshot world", (Throwable)var3);
			this.error(var3);
		} catch (Exception var4) {
			LOGGER.error("Couldn't create snapshot world", (Throwable)var4);
			this.error(var4);
		}
	}

	@Override
	public Component getTitle() {
		return TITLE;
	}

	@Override
	public void abortTask() {
		super.abortTask();
		if (this.creationTask != null) {
			this.creationTask.abortTask();
		}

		if (this.generateWorldTask != null) {
			this.generateWorldTask.abortTask();
		}
	}
}
