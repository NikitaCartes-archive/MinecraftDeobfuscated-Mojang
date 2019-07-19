package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

@Environment(EnvType.CLIENT)
public class LerpingBossEvent extends BossEvent {
	protected float targetPercent;
	protected long setTime;

	public LerpingBossEvent(ClientboundBossEventPacket clientboundBossEventPacket) {
		super(
			clientboundBossEventPacket.getId(), clientboundBossEventPacket.getName(), clientboundBossEventPacket.getColor(), clientboundBossEventPacket.getOverlay()
		);
		this.targetPercent = clientboundBossEventPacket.getPercent();
		this.percent = clientboundBossEventPacket.getPercent();
		this.setTime = Util.getMillis();
		this.setDarkenScreen(clientboundBossEventPacket.shouldDarkenScreen());
		this.setPlayBossMusic(clientboundBossEventPacket.shouldPlayMusic());
		this.setCreateWorldFog(clientboundBossEventPacket.shouldCreateWorldFog());
	}

	@Override
	public void setPercent(float f) {
		this.percent = this.getPercent();
		this.targetPercent = f;
		this.setTime = Util.getMillis();
	}

	@Override
	public float getPercent() {
		long l = Util.getMillis() - this.setTime;
		float f = Mth.clamp((float)l / 100.0F, 0.0F, 1.0F);
		return Mth.lerp(f, this.percent, this.targetPercent);
	}

	public void update(ClientboundBossEventPacket clientboundBossEventPacket) {
		switch (clientboundBossEventPacket.getOperation()) {
			case UPDATE_NAME:
				this.setName(clientboundBossEventPacket.getName());
				break;
			case UPDATE_PCT:
				this.setPercent(clientboundBossEventPacket.getPercent());
				break;
			case UPDATE_STYLE:
				this.setColor(clientboundBossEventPacket.getColor());
				this.setOverlay(clientboundBossEventPacket.getOverlay());
				break;
			case UPDATE_PROPERTIES:
				this.setDarkenScreen(clientboundBossEventPacket.shouldDarkenScreen());
				this.setPlayBossMusic(clientboundBossEventPacket.shouldPlayMusic());
		}
	}
}
