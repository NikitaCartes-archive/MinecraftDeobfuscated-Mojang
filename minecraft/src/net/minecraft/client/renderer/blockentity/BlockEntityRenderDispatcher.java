package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class BlockEntityRenderDispatcher {
	private final Map<Class<? extends BlockEntity>, BlockEntityRenderer<? extends BlockEntity>> renderers = Maps.<Class<? extends BlockEntity>, BlockEntityRenderer<? extends BlockEntity>>newHashMap();
	public static final BlockEntityRenderDispatcher instance = new BlockEntityRenderDispatcher();
	private Font font;
	public static double xOff;
	public static double yOff;
	public static double zOff;
	public TextureManager textureManager;
	public Level level;
	public Camera camera;
	public HitResult cameraHitResult;

	private BlockEntityRenderDispatcher() {
		this.renderers.put(SignBlockEntity.class, new SignRenderer());
		this.renderers.put(SpawnerBlockEntity.class, new SpawnerRenderer());
		this.renderers.put(PistonMovingBlockEntity.class, new PistonHeadRenderer());
		this.renderers.put(ChestBlockEntity.class, new ChestRenderer());
		this.renderers.put(EnderChestBlockEntity.class, new ChestRenderer());
		this.renderers.put(EnchantmentTableBlockEntity.class, new EnchantTableRenderer());
		this.renderers.put(LecternBlockEntity.class, new LecternRenderer());
		this.renderers.put(TheEndPortalBlockEntity.class, new TheEndPortalRenderer());
		this.renderers.put(TheEndGatewayBlockEntity.class, new TheEndGatewayRenderer());
		this.renderers.put(BeaconBlockEntity.class, new BeaconRenderer());
		this.renderers.put(SkullBlockEntity.class, new SkullBlockRenderer());
		this.renderers.put(BannerBlockEntity.class, new BannerRenderer());
		this.renderers.put(StructureBlockEntity.class, new StructureBlockRenderer());
		this.renderers.put(ShulkerBoxBlockEntity.class, new ShulkerBoxRenderer(new ShulkerModel()));
		this.renderers.put(BedBlockEntity.class, new BedRenderer());
		this.renderers.put(ConduitBlockEntity.class, new ConduitRenderer());
		this.renderers.put(BellBlockEntity.class, new BellRenderer());
		this.renderers.put(CampfireBlockEntity.class, new CampfireRenderer());

		for (BlockEntityRenderer<?> blockEntityRenderer : this.renderers.values()) {
			blockEntityRenderer.init(this);
		}
	}

	public <T extends BlockEntity> BlockEntityRenderer<T> getRenderer(Class<? extends BlockEntity> class_) {
		BlockEntityRenderer<? extends BlockEntity> blockEntityRenderer = (BlockEntityRenderer<? extends BlockEntity>)this.renderers.get(class_);
		if (blockEntityRenderer == null && class_ != BlockEntity.class) {
			blockEntityRenderer = this.getRenderer(class_.getSuperclass());
			this.renderers.put(class_, blockEntityRenderer);
		}

		return (BlockEntityRenderer<T>)blockEntityRenderer;
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityRenderer<T> getRenderer(@Nullable BlockEntity blockEntity) {
		return blockEntity == null ? null : this.getRenderer(blockEntity.getClass());
	}

	public void prepare(Level level, TextureManager textureManager, Font font, Camera camera, HitResult hitResult) {
		if (this.level != level) {
			this.setLevel(level);
		}

		this.textureManager = textureManager;
		this.camera = camera;
		this.font = font;
		this.cameraHitResult = hitResult;
	}

	public void render(BlockEntity blockEntity, float f, int i) {
		if (blockEntity.distanceToSqr(this.camera.getPosition().x, this.camera.getPosition().y, this.camera.getPosition().z) < blockEntity.getViewDistance()) {
			Lighting.turnOn();
			int j = this.level.getLightColor(blockEntity.getBlockPos());
			int k = j % 65536;
			int l = j / 65536;
			RenderSystem.glMultiTexCoord2f(33985, (float)k, (float)l);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			BlockPos blockPos = blockEntity.getBlockPos();
			this.render(blockEntity, (double)blockPos.getX() - xOff, (double)blockPos.getY() - yOff, (double)blockPos.getZ() - zOff, f, i, false);
		}
	}

	public void render(BlockEntity blockEntity, double d, double e, double f, float g) {
		this.render(blockEntity, d, e, f, g, -1, false);
	}

	public void renderItem(BlockEntity blockEntity) {
		this.render(blockEntity, 0.0, 0.0, 0.0, 0.0F, -1, true);
	}

	public void render(BlockEntity blockEntity, double d, double e, double f, float g, int i, boolean bl) {
		BlockEntityRenderer<BlockEntity> blockEntityRenderer = this.getRenderer(blockEntity);
		if (blockEntityRenderer != null) {
			try {
				if (bl || blockEntity.hasLevel() && blockEntity.getType().isValid(blockEntity.getBlockState().getBlock())) {
					blockEntityRenderer.render(blockEntity, d, e, f, g, i);
				}
			} catch (Throwable var15) {
				CrashReport crashReport = CrashReport.forThrowable(var15, "Rendering Block Entity");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Block Entity Details");
				blockEntity.fillCrashReportCategory(crashReportCategory);
				throw new ReportedException(crashReport);
			}
		}
	}

	public void setLevel(@Nullable Level level) {
		this.level = level;
		if (level == null) {
			this.camera = null;
		}
	}

	public Font getFont() {
		return this.font;
	}
}
