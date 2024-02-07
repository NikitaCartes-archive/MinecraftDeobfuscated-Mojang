package net.minecraft.client.multiplayer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;

@Environment(EnvType.CLIENT)
public class KnownPacksManager {
	private final PackRepository repository = ServerPacksSource.createVanillaTrustedRepository();
	private final Map<KnownPack, String> knownPackToId;

	public KnownPacksManager() {
		this.repository.reload();
		Builder<KnownPack, String> builder = ImmutableMap.builder();
		this.repository.getAvailablePacks().forEach(pack -> {
			PackLocationInfo packLocationInfo = pack.location();
			packLocationInfo.knownPackInfo().ifPresent(knownPack -> builder.put(knownPack, packLocationInfo.id()));
		});
		this.knownPackToId = builder.build();
	}

	public List<KnownPack> trySelectingPacks(List<KnownPack> list) {
		List<KnownPack> list2 = new ArrayList(list.size());
		List<String> list3 = new ArrayList(list.size());

		for (KnownPack knownPack : list) {
			String string = (String)this.knownPackToId.get(knownPack);
			if (string != null) {
				list3.add(string);
				list2.add(knownPack);
			}
		}

		this.repository.setSelected(list3);
		return list2;
	}

	public CloseableResourceManager createResourceManager() {
		List<PackResources> list = this.repository.openAllSelected();
		return new MultiPackResourceManager(PackType.SERVER_DATA, list);
	}
}
