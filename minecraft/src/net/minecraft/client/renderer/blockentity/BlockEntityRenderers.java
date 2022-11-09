package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Environment(EnvType.CLIENT)
public class BlockEntityRenderers {
	private static final Map<BlockEntityType<?>, BlockEntityRendererProvider<?>> PROVIDERS = Maps.<BlockEntityType<?>, BlockEntityRendererProvider<?>>newHashMap();

	private static <T extends BlockEntity> void register(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T> blockEntityRendererProvider) {
		PROVIDERS.put(blockEntityType, blockEntityRendererProvider);
	}

	public static Map<BlockEntityType<?>, BlockEntityRenderer<?>> createEntityRenderers(BlockEntityRendererProvider.Context context) {
		Builder<BlockEntityType<?>, BlockEntityRenderer<?>> builder = ImmutableMap.builder();
		PROVIDERS.forEach((blockEntityType, blockEntityRendererProvider) -> {
			try {
				builder.put(blockEntityType, blockEntityRendererProvider.create(context));
			} catch (Exception var5) {
				throw new IllegalStateException("Failed to create model for " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType), var5);
			}
		});
		return builder.build();
	}

	static {
		register(BlockEntityType.SIGN, SignRenderer::new);
		register(BlockEntityType.HANGING_SIGN, HangingSignRenderer::new);
		register(BlockEntityType.MOB_SPAWNER, SpawnerRenderer::new);
		register(BlockEntityType.PISTON, PistonHeadRenderer::new);
		register(BlockEntityType.CHEST, ChestRenderer::new);
		register(BlockEntityType.ENDER_CHEST, ChestRenderer::new);
		register(BlockEntityType.TRAPPED_CHEST, ChestRenderer::new);
		register(BlockEntityType.ENCHANTING_TABLE, EnchantTableRenderer::new);
		register(BlockEntityType.LECTERN, LecternRenderer::new);
		register(BlockEntityType.END_PORTAL, TheEndPortalRenderer::new);
		register(BlockEntityType.END_GATEWAY, TheEndGatewayRenderer::new);
		register(BlockEntityType.BEACON, BeaconRenderer::new);
		register(BlockEntityType.SKULL, SkullBlockRenderer::new);
		register(BlockEntityType.BANNER, BannerRenderer::new);
		register(BlockEntityType.STRUCTURE_BLOCK, StructureBlockRenderer::new);
		register(BlockEntityType.SHULKER_BOX, ShulkerBoxRenderer::new);
		register(BlockEntityType.BED, BedRenderer::new);
		register(BlockEntityType.CONDUIT, ConduitRenderer::new);
		register(BlockEntityType.BELL, BellRenderer::new);
		register(BlockEntityType.CAMPFIRE, CampfireRenderer::new);
	}
}
