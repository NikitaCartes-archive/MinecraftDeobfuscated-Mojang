/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.StructureBlockRenderer;
import net.minecraft.client.renderer.blockentity.SuspiciousSandRenderer;
import net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Environment(value=EnvType.CLIENT)
public class BlockEntityRenderers {
    private static final Map<BlockEntityType<?>, BlockEntityRendererProvider<?>> PROVIDERS = Maps.newHashMap();

    private static <T extends BlockEntity> void register(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T> blockEntityRendererProvider) {
        PROVIDERS.put(blockEntityType, blockEntityRendererProvider);
    }

    public static Map<BlockEntityType<?>, BlockEntityRenderer<?>> createEntityRenderers(BlockEntityRendererProvider.Context context) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        PROVIDERS.forEach((blockEntityType, blockEntityRendererProvider) -> {
            try {
                builder.put(blockEntityType, blockEntityRendererProvider.create(context));
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to create model for " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey((BlockEntityType<?>)blockEntityType), exception);
            }
        });
        return builder.build();
    }

    static {
        BlockEntityRenderers.register(BlockEntityType.SIGN, SignRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.HANGING_SIGN, HangingSignRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.MOB_SPAWNER, SpawnerRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.PISTON, PistonHeadRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.CHEST, ChestRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.ENDER_CHEST, ChestRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.TRAPPED_CHEST, ChestRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.ENCHANTING_TABLE, EnchantTableRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.LECTERN, LecternRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.END_PORTAL, TheEndPortalRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.END_GATEWAY, TheEndGatewayRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.BEACON, BeaconRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.SKULL, SkullBlockRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.BANNER, BannerRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.STRUCTURE_BLOCK, StructureBlockRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.SHULKER_BOX, ShulkerBoxRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.BED, BedRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.CONDUIT, ConduitRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.BELL, BellRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.CAMPFIRE, CampfireRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.SUSPICIOUS_SAND, SuspiciousSandRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.DECORATED_POT, DecoratedPotRenderer::new);
    }
}

