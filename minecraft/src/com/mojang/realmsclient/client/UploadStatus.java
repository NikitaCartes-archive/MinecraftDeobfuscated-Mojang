package com.mojang.realmsclient.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class UploadStatus {
	public volatile Long bytesWritten = 0L;
	public volatile Long totalBytes = 0L;
}
