package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Calendar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.LargeChestModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;

@Environment(EnvType.CLIENT)
public class ChestRenderer<T extends BlockEntity & LidBlockEntity> extends BlockEntityRenderer<T> {
	private static final ResourceLocation CHEST_LARGE_TRAP_LOCATION = new ResourceLocation("textures/entity/chest/trapped_double.png");
	private static final ResourceLocation CHEST_LARGE_XMAS_LOCATION = new ResourceLocation("textures/entity/chest/christmas_double.png");
	private static final ResourceLocation CHEST_LARGE_LOCATION = new ResourceLocation("textures/entity/chest/normal_double.png");
	private static final ResourceLocation CHEST_TRAP_LOCATION = new ResourceLocation("textures/entity/chest/trapped.png");
	private static final ResourceLocation CHEST_XMAS_LOCATION = new ResourceLocation("textures/entity/chest/christmas.png");
	private static final ResourceLocation CHEST_LOCATION = new ResourceLocation("textures/entity/chest/normal.png");
	private static final ResourceLocation ENDER_CHEST_LOCATION = new ResourceLocation("textures/entity/chest/ender.png");
	private final ChestModel chestModel = new ChestModel();
	private final ChestModel largeChestModel = new LargeChestModel();
	private boolean xmasTextures;

	public ChestRenderer() {
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
			this.xmasTextures = true;
		}
	}

	@Override
	public void render(T blockEntity, double d, double e, double f, float g, int i) {
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(515);
		RenderSystem.depthMask(true);
		BlockState blockState = blockEntity.hasLevel() ? blockEntity.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
		ChestType chestType = blockState.hasProperty((Property<T>)ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
		if (chestType != ChestType.LEFT) {
			boolean bl = chestType != ChestType.SINGLE;
			ChestModel chestModel = this.getChestModelAndBindTexture(blockEntity, i, bl);
			if (i >= 0) {
				RenderSystem.matrixMode(5890);
				RenderSystem.pushMatrix();
				RenderSystem.scalef(bl ? 8.0F : 4.0F, 4.0F, 1.0F);
				RenderSystem.translatef(0.0625F, 0.0625F, 0.0625F);
				RenderSystem.matrixMode(5888);
			} else {
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			}

			RenderSystem.pushMatrix();
			RenderSystem.enableRescaleNormal();
			RenderSystem.translatef((float)d, (float)e + 1.0F, (float)f + 1.0F);
			RenderSystem.scalef(1.0F, -1.0F, -1.0F);
			float h = ((Direction)blockState.getValue(ChestBlock.FACING)).toYRot();
			if ((double)Math.abs(h) > 1.0E-5) {
				RenderSystem.translatef(0.5F, 0.5F, 0.5F);
				RenderSystem.rotatef(h, 0.0F, 1.0F, 0.0F);
				RenderSystem.translatef(-0.5F, -0.5F, -0.5F);
			}

			this.rotateLid(blockEntity, g, chestModel);
			chestModel.render();
			RenderSystem.disableRescaleNormal();
			RenderSystem.popMatrix();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (i >= 0) {
				RenderSystem.matrixMode(5890);
				RenderSystem.popMatrix();
				RenderSystem.matrixMode(5888);
			}
		}
	}

	private ChestModel getChestModelAndBindTexture(T blockEntity, int i, boolean bl) {
		ResourceLocation resourceLocation;
		if (i >= 0) {
			resourceLocation = BREAKING_LOCATIONS[i];
		} else if (this.xmasTextures) {
			resourceLocation = bl ? CHEST_LARGE_XMAS_LOCATION : CHEST_XMAS_LOCATION;
		} else if (blockEntity instanceof TrappedChestBlockEntity) {
			resourceLocation = bl ? CHEST_LARGE_TRAP_LOCATION : CHEST_TRAP_LOCATION;
		} else if (blockEntity instanceof EnderChestBlockEntity) {
			resourceLocation = ENDER_CHEST_LOCATION;
		} else {
			resourceLocation = bl ? CHEST_LARGE_LOCATION : CHEST_LOCATION;
		}

		this.bindTexture(resourceLocation);
		return bl ? this.largeChestModel : this.chestModel;
	}

	private void rotateLid(T blockEntity, float f, ChestModel chestModel) {
		float g = blockEntity.getOpenNess(f);
		g = 1.0F - g;
		g = 1.0F - g * g * g;
		chestModel.getLid().xRot = -(g * (float) (Math.PI / 2));
	}
}
