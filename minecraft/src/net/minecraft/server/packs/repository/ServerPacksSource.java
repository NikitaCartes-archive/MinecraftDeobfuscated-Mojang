package net.minecraft.server.packs.repository;

import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

public class ServerPacksSource extends BuiltInPackSource {
	private static final PackMetadataSection VERSION_METADATA_SECTION = new PackMetadataSection(
		Component.translatable("dataPack.vanilla.description"), SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA)
	);
	private static final FeatureFlagsMetadataSection FEATURE_FLAGS_METADATA_SECTION = new FeatureFlagsMetadataSection(FeatureFlags.DEFAULT_FLAGS);
	private static final BuiltInMetadata BUILT_IN_METADATA = BuiltInMetadata.of(
		PackMetadataSection.TYPE, VERSION_METADATA_SECTION, FeatureFlagsMetadataSection.TYPE, FEATURE_FLAGS_METADATA_SECTION
	);
	private static final Component VANILLA_NAME = Component.translatable("dataPack.vanilla.name");
	private static final ResourceLocation PACKS_DIR = new ResourceLocation("minecraft", "datapacks");

	public ServerPacksSource() {
		super(PackType.SERVER_DATA, createVanillaPackSource(), PACKS_DIR);
	}

	private static VanillaPackResources createVanillaPackSource() {
		return new VanillaPackResourcesBuilder().setMetadata(BUILT_IN_METADATA).exposeNamespace("minecraft").applyDevelopmentConfig().pushJarResources().build();
	}

	@Override
	protected Component getPackTitle(String string) {
		return Component.literal(string);
	}

	@Nullable
	@Override
	protected Pack createVanillaPack(PackResources packResources) {
		return Pack.readMetaAndCreate("vanilla", VANILLA_NAME, false, string -> packResources, PackType.SERVER_DATA, Pack.Position.BOTTOM, PackSource.BUILT_IN);
	}

	@Nullable
	@Override
	protected Pack createBuiltinPack(String string, Pack.ResourcesSupplier resourcesSupplier, Component component) {
		return Pack.readMetaAndCreate(string, component, false, resourcesSupplier, PackType.SERVER_DATA, Pack.Position.TOP, PackSource.FEATURE);
	}

	public static PackRepository createPackRepository(Path path) {
		return new PackRepository(new ServerPacksSource(), new FolderRepositorySource(path, PackType.SERVER_DATA, PackSource.WORLD));
	}

	public static PackRepository createPackRepository(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		return createPackRepository(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR));
	}
}
