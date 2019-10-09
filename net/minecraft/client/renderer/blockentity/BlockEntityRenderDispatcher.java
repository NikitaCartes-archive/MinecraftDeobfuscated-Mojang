/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.StructureBlockRenderer;
import net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockEntityRenderDispatcher {
    private final Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = Maps.newHashMap();
    public static final BlockEntityRenderDispatcher instance = new BlockEntityRenderDispatcher();
    private final BufferBuilder singleRenderBuffer = new BufferBuilder(256);
    private Font font;
    public TextureManager textureManager;
    public Level level;
    public Camera camera;
    public HitResult cameraHitResult;

    private BlockEntityRenderDispatcher() {
        this.register(BlockEntityType.SIGN, new SignRenderer(this));
        this.register(BlockEntityType.MOB_SPAWNER, new SpawnerRenderer(this));
        this.register(BlockEntityType.PISTON, new PistonHeadRenderer(this));
        this.register(BlockEntityType.CHEST, new ChestRenderer(this));
        this.register(BlockEntityType.ENDER_CHEST, new ChestRenderer(this));
        this.register(BlockEntityType.TRAPPED_CHEST, new ChestRenderer(this));
        this.register(BlockEntityType.ENCHANTING_TABLE, new EnchantTableRenderer(this));
        this.register(BlockEntityType.LECTERN, new LecternRenderer(this));
        this.register(BlockEntityType.END_PORTAL, new TheEndPortalRenderer(this));
        this.register(BlockEntityType.END_GATEWAY, new TheEndGatewayRenderer(this));
        this.register(BlockEntityType.BEACON, new BeaconRenderer(this));
        this.register(BlockEntityType.SKULL, new SkullBlockRenderer(this));
        this.register(BlockEntityType.BANNER, new BannerRenderer(this));
        this.register(BlockEntityType.STRUCTURE_BLOCK, new StructureBlockRenderer(this));
        this.register(BlockEntityType.SHULKER_BOX, new ShulkerBoxRenderer(new ShulkerModel(), this));
        this.register(BlockEntityType.BED, new BedRenderer(this));
        this.register(BlockEntityType.CONDUIT, new ConduitRenderer(this));
        this.register(BlockEntityType.BELL, new BellRenderer(this));
        this.register(BlockEntityType.CAMPFIRE, new CampfireRenderer(this));
    }

    private <E extends BlockEntity> void register(BlockEntityType<E> blockEntityType, BlockEntityRenderer<E> blockEntityRenderer) {
        this.renderers.put(blockEntityType, blockEntityRenderer);
    }

    @Nullable
    public <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E blockEntity) {
        return this.renderers.get(blockEntity.getType());
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

    public <E extends BlockEntity> void render(E blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double g) {
        if (!(blockEntity.distanceToSqr(this.camera.getPosition().x, this.camera.getPosition().y, this.camera.getPosition().z) < blockEntity.getViewDistance())) {
            return;
        }
        BlockEntityRenderer blockEntityRenderer = this.getRenderer(blockEntity);
        if (blockEntityRenderer == null) {
            return;
        }
        if (!blockEntity.hasLevel() || !blockEntity.getType().isValid(blockEntity.getBlockState().getBlock())) {
            return;
        }
        BlockPos blockPos = blockEntity.getBlockPos();
        BlockEntityRenderDispatcher.tryRender(blockEntity, () -> BlockEntityRenderDispatcher.setupAndRender(blockEntityRenderer, blockEntity, (double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - g, f, poseStack, multiBufferSource));
    }

    private static <T extends BlockEntity> void setupAndRender(BlockEntityRenderer<T> blockEntityRenderer, T blockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        Level level = blockEntity.getLevel();
        int i = level != null ? level.getLightColor(blockEntity.getBlockPos()) : 0xF000F0;
        blockEntityRenderer.render(blockEntity, d, e, f, g, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
    }

    @Deprecated
    public <E extends BlockEntity> void renderItem(E blockEntity, PoseStack poseStack) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(this.singleRenderBuffer);
        this.renderItem(blockEntity, poseStack, bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY);
        bufferSource.endBatch();
    }

    public <E extends BlockEntity> boolean renderItem(E blockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        BlockEntityRenderer blockEntityRenderer = this.getRenderer(blockEntity);
        if (blockEntityRenderer == null) {
            return true;
        }
        BlockEntityRenderDispatcher.tryRender(blockEntity, () -> blockEntityRenderer.render(blockEntity, 0.0, 0.0, 0.0, 0.0f, poseStack, multiBufferSource, i, j));
        return false;
    }

    private static void tryRender(BlockEntity blockEntity, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering Block Entity");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block Entity Details");
            blockEntity.fillCrashReportCategory(crashReportCategory);
            throw new ReportedException(crashReport);
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

