package com.mojang.realmsclient.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class UploadStatus {
	public volatile long bytesWritten;
	public volatile long totalBytes;
}
