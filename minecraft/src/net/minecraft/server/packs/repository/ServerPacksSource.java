package net.minecraft.server.packs.repository;

import com.google.common.annotations.VisibleForTesting;
import java.nio.file.Path;
import java.util.Optional;
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
import net.minecraft.world.level.validation.DirectoryValidator;

public class ServerPacksSource extends BuiltInPackSource {
	private static final PackMetadataSection VERSION_METADATA_SECTION = new PackMetadataSection(
		Component.translatable("dataPack.vanilla.description"), SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA), Optional.empty()
	);
	private static final FeatureFlagsMetadataSection FEATURE_FLAGS_METADATA_SECTION = new FeatureFlagsMetadataSection(FeatureFlags.DEFAULT_FLAGS);
	private static final BuiltInMetadata BUILT_IN_METADATA = BuiltInMetadata.of(
		PackMetadataSection.TYPE, VERSION_METADATA_SECTION, FeatureFlagsMetadataSection.TYPE, FEATURE_FLAGS_METADATA_SECTION
	);
	private static final Component VANILLA_NAME = Component.translatable("dataPack.vanilla.name");
	private static final ResourceLocation PACKS_DIR = new ResourceLocation("minecraft", "datapacks");

	public ServerPacksSource(DirectoryValidator directoryValidator) {
		super(PackType.SERVER_DATA, createVanillaPackSource(), PACKS_DIR, directoryValidator);
	}

	@VisibleForTesting
	public static VanillaPackResources createVanillaPackSource() {
		return new VanillaPackResourcesBuilder().setMetadata(BUILT_IN_METADATA).exposeNamespace("minecraft").applyDevelopmentConfig().pushJarResources().build();
	}

	@Override
	protected Component getPackTitle(String string) {
		return Component.literal(string);
	}

	@Nullable
	@Override
	protected Pack createVanillaPack(PackResources packResources) {
		return Pack.readMetaAndCreate("vanilla", VANILLA_NAME, false, fixedResources(packResources), PackType.SERVER_DATA, Pack.Position.BOTTOM, PackSource.BUILT_IN);
	}

	@Nullable
	@Override
	protected Pack createBuiltinPack(String string, Pack.ResourcesSupplier resourcesSupplier, Component component) {
		return Pack.readMetaAndCreate(string, component, false, resourcesSupplier, PackType.SERVER_DATA, Pack.Position.TOP, PackSource.FEATURE);
	}

	public static PackRepository createPackRepository(Path path, DirectoryValidator directoryValidator) {
		return new PackRepository(
			new ServerPacksSource(directoryValidator), new FolderRepositorySource(path, PackType.SERVER_DATA, PackSource.WORLD, directoryValidator)
		);
	}

	public static PackRepository createVanillaTrustedRepository() {
		return new PackRepository(new ServerPacksSource(new DirectoryValidator(path -> true)));
	}

	public static PackRepository createPackRepository(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		return createPackRepository(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR), levelStorageAccess.parent().getWorldDirValidator());
	}
}
