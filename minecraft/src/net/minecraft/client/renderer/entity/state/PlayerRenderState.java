package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.item.ItemUseAnimation;

@Environment(EnvType.CLIENT)
public class PlayerRenderState extends HumanoidRenderState {
	public PlayerSkin skin = DefaultPlayerSkin.getDefaultSkin();
	public float capeFlap;
	public float capeLean;
	public float capeLean2;
	public int arrowCount;
	public int stingerCount;
	public int useItemRemainingTicks;
	public boolean isSpectator;
	public boolean showHat = true;
	public boolean showJacket = true;
	public boolean showLeftPants = true;
	public boolean showRightPants = true;
	public boolean showLeftSleeve = true;
	public boolean showRightSleeve = true;
	public boolean showCape = true;
	public float fallFlyingTimeInTicks;
	public boolean shouldApplyFlyingYRot;
	public float flyingYRot;
	public boolean swinging;
	public PlayerRenderState.HandState mainHandState = new PlayerRenderState.HandState();
	public PlayerRenderState.HandState offhandState = new PlayerRenderState.HandState();
	@Nullable
	public Component scoreText;
	@Nullable
	public Parrot.Variant parrotOnLeftShoulder;
	@Nullable
	public Parrot.Variant parrotOnRightShoulder;
	public int id;
	public String name = "Steve";

	public float fallFlyingScale() {
		return Mth.clamp(this.fallFlyingTimeInTicks * this.fallFlyingTimeInTicks / 100.0F, 0.0F, 1.0F);
	}

	@Environment(EnvType.CLIENT)
	public static class HandState {
		public boolean isEmpty = true;
		@Nullable
		public ItemUseAnimation useAnimation;
		public boolean holdsChargedCrossbow;
	}
}
