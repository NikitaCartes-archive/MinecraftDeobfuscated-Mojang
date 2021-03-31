package net.minecraft.client.gui.components;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

@Environment(EnvType.CLIENT)
public class LerpingBossEvent extends BossEvent {
	private static final long LERP_MILLISECONDS = 100L;
	protected float targetPercent;
	protected long setTime;

	public LerpingBossEvent(
		UUID uUID, Component component, float f, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay, boolean bl, boolean bl2, boolean bl3
	) {
		super(uUID, component, bossBarColor, bossBarOverlay);
		this.targetPercent = f;
		this.progress = f;
		this.setTime = Util.getMillis();
		this.setDarkenScreen(bl);
		this.setPlayBossMusic(bl2);
		this.setCreateWorldFog(bl3);
	}

	@Override
	public void setProgress(float f) {
		this.progress = this.getProgress();
		this.targetPercent = f;
		this.setTime = Util.getMillis();
	}

	@Override
	public float getProgress() {
		long l = Util.getMillis() - this.setTime;
		float f = Mth.clamp((float)l / 100.0F, 0.0F, 1.0F);
		return Mth.lerp(f, this.progress, this.targetPercent);
	}
}
