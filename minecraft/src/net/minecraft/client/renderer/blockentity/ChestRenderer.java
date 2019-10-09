package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Calendar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
	public static final ResourceLocation CHEST_LARGE_TRAP_LOCATION = new ResourceLocation("entity/chest/trapped_double");
	public static final ResourceLocation CHEST_LARGE_XMAS_LOCATION = new ResourceLocation("entity/chest/christmas_double");
	public static final ResourceLocation CHEST_LARGE_LOCATION = new ResourceLocation("entity/chest/normal_double");
	public static final ResourceLocation CHEST_TRAP_LOCATION = new ResourceLocation("entity/chest/trapped");
	public static final ResourceLocation CHEST_XMAS_LOCATION = new ResourceLocation("entity/chest/christmas");
	public static final ResourceLocation CHEST_LOCATION = new ResourceLocation("entity/chest/normal");
	public static final ResourceLocation ENDER_CHEST_LOCATION = new ResourceLocation("entity/chest/ender");
	private final ModelPart lid;
	private final ModelPart bottom;
	private final ModelPart lock;
	private final ModelPart doubleLid;
	private final ModelPart doubleBottom;
	private final ModelPart doubleLock;
	private boolean xmasTextures;

	public ChestRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
			this.xmasTextures = true;
		}

		this.bottom = new ModelPart(64, 64, 0, 19);
		this.bottom.addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, 0.0F);
		this.lid = new ModelPart(64, 64, 0, 0);
		this.lid.addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, 0.0F);
		this.lid.y = 9.0F;
		this.lid.z = 1.0F;
		this.lock = new ModelPart(64, 64, 0, 0);
		this.lock.addBox(7.0F, -2.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
		this.lock.y = 9.0F;
		this.doubleBottom = new ModelPart(128, 64, 0, 19);
		this.doubleBottom.addBox(1.0F, 0.0F, 1.0F, 30.0F, 10.0F, 14.0F, 0.0F);
		this.doubleLid = new ModelPart(128, 64, 0, 0);
		this.doubleLid.addBox(1.0F, 0.0F, 0.0F, 30.0F, 5.0F, 14.0F, 0.0F);
		this.doubleLid.y = 9.0F;
		this.doubleLid.z = 1.0F;
		this.doubleLock = new ModelPart(128, 64, 0, 0);
		this.doubleLock.addBox(15.0F, -2.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
		this.doubleLock.y = 9.0F;
	}

	@Override
	public void render(T blockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		BlockState blockState = blockEntity.hasLevel() ? blockEntity.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
		ChestType chestType = blockState.hasProperty((Property<T>)ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
		boolean bl = chestType != ChestType.SINGLE;
		ResourceLocation resourceLocation;
		if (this.xmasTextures) {
			resourceLocation = bl ? CHEST_LARGE_XMAS_LOCATION : CHEST_XMAS_LOCATION;
		} else if (blockEntity instanceof TrappedChestBlockEntity) {
			resourceLocation = bl ? CHEST_LARGE_TRAP_LOCATION : CHEST_TRAP_LOCATION;
		} else if (blockEntity instanceof EnderChestBlockEntity) {
			resourceLocation = ENDER_CHEST_LOCATION;
		} else {
			resourceLocation = bl ? CHEST_LARGE_LOCATION : CHEST_LOCATION;
		}

		poseStack.pushPose();
		float h = ((Direction)blockState.getValue(ChestBlock.FACING)).toYRot();
		poseStack.translate(0.5, 0.5, 0.5);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(-h));
		poseStack.translate(-0.5, -0.5, -0.5);
		float k = blockEntity.getOpenNess(g);
		k = 1.0F - k;
		k = 1.0F - k * k * k;
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
		TextureAtlasSprite textureAtlasSprite = this.getSprite(resourceLocation);
		if (bl) {
			if (chestType == ChestType.LEFT) {
				poseStack.translate(-1.0, 0.0, 0.0);
			}

			this.render(poseStack, vertexConsumer, this.doubleLid, this.doubleLock, this.doubleBottom, k, i, j, textureAtlasSprite);
		} else {
			this.render(poseStack, vertexConsumer, this.lid, this.lock, this.bottom, k, i, j, textureAtlasSprite);
		}

		poseStack.popPose();
	}

	private void render(
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		ModelPart modelPart,
		ModelPart modelPart2,
		ModelPart modelPart3,
		float f,
		int i,
		int j,
		TextureAtlasSprite textureAtlasSprite
	) {
		modelPart.xRot = -(f * (float) (Math.PI / 2));
		modelPart2.xRot = modelPart.xRot;
		modelPart.render(poseStack, vertexConsumer, 0.0625F, i, j, textureAtlasSprite);
		modelPart2.render(poseStack, vertexConsumer, 0.0625F, i, j, textureAtlasSprite);
		modelPart3.render(poseStack, vertexConsumer, 0.0625F, i, j, textureAtlasSprite);
	}
}
