package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class VillageDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft minecraft;
	private final Map<BlockPos, VillageDebugRenderer.PoiInfo> pois = Maps.<BlockPos, VillageDebugRenderer.PoiInfo>newHashMap();
	private final Set<SectionPos> villageSections = Sets.<SectionPos>newHashSet();
	private final Map<UUID, VillageDebugRenderer.BrainDump> brainDumpsPerEntity = Maps.<UUID, VillageDebugRenderer.BrainDump>newHashMap();
	private UUID lastLookedAtUuid;

	public VillageDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void clear() {
		this.pois.clear();
		this.villageSections.clear();
		this.brainDumpsPerEntity.clear();
		this.lastLookedAtUuid = null;
	}

	public void addPoi(VillageDebugRenderer.PoiInfo poiInfo) {
		this.pois.put(poiInfo.pos, poiInfo);
	}

	public void removePoi(BlockPos blockPos) {
		this.pois.remove(blockPos);
	}

	public void setFreeTicketCount(BlockPos blockPos, int i) {
		VillageDebugRenderer.PoiInfo poiInfo = (VillageDebugRenderer.PoiInfo)this.pois.get(blockPos);
		if (poiInfo == null) {
			LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: " + blockPos);
		} else {
			poiInfo.freeTicketCount = i;
		}
	}

	public void setVillageSection(SectionPos sectionPos) {
		this.villageSections.add(sectionPos);
	}

	public void setNotVillageSection(SectionPos sectionPos) {
		this.villageSections.remove(sectionPos);
	}

	public void addOrUpdateBrainDump(VillageDebugRenderer.BrainDump brainDump) {
		this.brainDumpsPerEntity.put(brainDump.uuid, brainDump);
	}

	@Environment(EnvType.CLIENT)
	public static class BrainDump {
		public final UUID uuid;
		public final int id;
		public final String name;
		public final String profession;
		public final int xp;
		public final Position pos;
		public final String inventory;
		public final Path path;
		public final boolean wantsGolem;
		public final List<String> activities = Lists.<String>newArrayList();
		public final List<String> behaviors = Lists.<String>newArrayList();
		public final List<String> memories = Lists.<String>newArrayList();
		public final List<String> gossips = Lists.<String>newArrayList();
		public final Set<BlockPos> pois = Sets.<BlockPos>newHashSet();

		public BrainDump(UUID uUID, int i, String string, String string2, int j, Position position, String string3, @Nullable Path path, boolean bl) {
			this.uuid = uUID;
			this.id = i;
			this.name = string;
			this.profession = string2;
			this.xp = j;
			this.pos = position;
			this.inventory = string3;
			this.path = path;
			this.wantsGolem = bl;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class PoiInfo {
		public final BlockPos pos;
		public String type;
		public int freeTicketCount;

		public PoiInfo(BlockPos blockPos, String string, int i) {
			this.pos = blockPos;
			this.type = string;
			this.freeTicketCount = i;
		}
	}
}
