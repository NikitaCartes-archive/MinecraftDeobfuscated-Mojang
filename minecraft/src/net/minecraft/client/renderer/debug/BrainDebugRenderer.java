package net.minecraft.client.renderer.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.common.custom.BrainDebugPayload;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BrainDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final boolean SHOW_NAME_FOR_ALL = true;
	private static final boolean SHOW_PROFESSION_FOR_ALL = false;
	private static final boolean SHOW_BEHAVIORS_FOR_ALL = false;
	private static final boolean SHOW_ACTIVITIES_FOR_ALL = false;
	private static final boolean SHOW_INVENTORY_FOR_ALL = false;
	private static final boolean SHOW_GOSSIPS_FOR_ALL = false;
	private static final boolean SHOW_PATH_FOR_ALL = false;
	private static final boolean SHOW_HEALTH_FOR_ALL = false;
	private static final boolean SHOW_WANTS_GOLEM_FOR_ALL = true;
	private static final boolean SHOW_ANGER_LEVEL_FOR_ALL = false;
	private static final boolean SHOW_NAME_FOR_SELECTED = true;
	private static final boolean SHOW_PROFESSION_FOR_SELECTED = true;
	private static final boolean SHOW_BEHAVIORS_FOR_SELECTED = true;
	private static final boolean SHOW_ACTIVITIES_FOR_SELECTED = true;
	private static final boolean SHOW_MEMORIES_FOR_SELECTED = true;
	private static final boolean SHOW_INVENTORY_FOR_SELECTED = true;
	private static final boolean SHOW_GOSSIPS_FOR_SELECTED = true;
	private static final boolean SHOW_PATH_FOR_SELECTED = true;
	private static final boolean SHOW_HEALTH_FOR_SELECTED = true;
	private static final boolean SHOW_WANTS_GOLEM_FOR_SELECTED = true;
	private static final boolean SHOW_ANGER_LEVEL_FOR_SELECTED = true;
	private static final boolean SHOW_POI_INFO = true;
	private static final int MAX_RENDER_DIST_FOR_BRAIN_INFO = 30;
	private static final int MAX_RENDER_DIST_FOR_POI_INFO = 30;
	private static final int MAX_TARGETING_DIST = 8;
	private static final float TEXT_SCALE = 0.02F;
	private static final int WHITE = -1;
	private static final int YELLOW = -256;
	private static final int CYAN = -16711681;
	private static final int GREEN = -16711936;
	private static final int GRAY = -3355444;
	private static final int PINK = -98404;
	private static final int RED = -65536;
	private static final int ORANGE = -23296;
	private final Minecraft minecraft;
	private final Map<BlockPos, BrainDebugRenderer.PoiInfo> pois = Maps.<BlockPos, BrainDebugRenderer.PoiInfo>newHashMap();
	private final Map<UUID, BrainDebugPayload.BrainDump> brainDumpsPerEntity = Maps.<UUID, BrainDebugPayload.BrainDump>newHashMap();
	@Nullable
	private UUID lastLookedAtUuid;

	public BrainDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void clear() {
		this.pois.clear();
		this.brainDumpsPerEntity.clear();
		this.lastLookedAtUuid = null;
	}

	public void addPoi(BrainDebugRenderer.PoiInfo poiInfo) {
		this.pois.put(poiInfo.pos, poiInfo);
	}

	public void removePoi(BlockPos blockPos) {
		this.pois.remove(blockPos);
	}

	public void setFreeTicketCount(BlockPos blockPos, int i) {
		BrainDebugRenderer.PoiInfo poiInfo = (BrainDebugRenderer.PoiInfo)this.pois.get(blockPos);
		if (poiInfo == null) {
			LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: {}", blockPos);
		} else {
			poiInfo.freeTicketCount = i;
		}
	}

	public void addOrUpdateBrainDump(BrainDebugPayload.BrainDump brainDump) {
		this.brainDumpsPerEntity.put(brainDump.uuid(), brainDump);
	}

	public void removeBrainDump(int i) {
		this.brainDumpsPerEntity.values().removeIf(brainDump -> brainDump.id() == i);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		this.clearRemovedEntities();
		this.doRender(poseStack, multiBufferSource, d, e, f);
		if (!this.minecraft.player.isSpectator()) {
			this.updateLastLookedAtUuid();
		}
	}

	private void clearRemovedEntities() {
		this.brainDumpsPerEntity.entrySet().removeIf(entry -> {
			Entity entity = this.minecraft.level.getEntity(((BrainDebugPayload.BrainDump)entry.getValue()).id());
			return entity == null || entity.isRemoved();
		});
	}

	private void doRender(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		BlockPos blockPos = BlockPos.containing(d, e, f);
		this.brainDumpsPerEntity.values().forEach(brainDump -> {
			if (this.isPlayerCloseEnoughToMob(brainDump)) {
				this.renderBrainInfo(poseStack, multiBufferSource, brainDump, d, e, f);
			}
		});

		for (BlockPos blockPos2 : this.pois.keySet()) {
			if (blockPos.closerThan(blockPos2, 30.0)) {
				highlightPoi(poseStack, multiBufferSource, blockPos2);
			}
		}

		this.pois.values().forEach(poiInfo -> {
			if (blockPos.closerThan(poiInfo.pos, 30.0)) {
				this.renderPoiInfo(poseStack, multiBufferSource, poiInfo);
			}
		});
		this.getGhostPois().forEach((blockPos2x, list) -> {
			if (blockPos.closerThan(blockPos2x, 30.0)) {
				this.renderGhostPoi(poseStack, multiBufferSource, blockPos2x, list);
			}
		});
	}

	private static void highlightPoi(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos) {
		float f = 0.05F;
		DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
	}

	private void renderGhostPoi(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, List<String> list) {
		float f = 0.05F;
		DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
		renderTextOverPos(poseStack, multiBufferSource, list + "", blockPos, 0, -256);
		renderTextOverPos(poseStack, multiBufferSource, "Ghost POI", blockPos, 1, -65536);
	}

	private void renderPoiInfo(PoseStack poseStack, MultiBufferSource multiBufferSource, BrainDebugRenderer.PoiInfo poiInfo) {
		int i = 0;
		Set<String> set = this.getTicketHolderNames(poiInfo);
		if (set.size() < 4) {
			renderTextOverPoi(poseStack, multiBufferSource, "Owners: " + set, poiInfo, i, -256);
		} else {
			renderTextOverPoi(poseStack, multiBufferSource, set.size() + " ticket holders", poiInfo, i, -256);
		}

		i++;
		Set<String> set2 = this.getPotentialTicketHolderNames(poiInfo);
		if (set2.size() < 4) {
			renderTextOverPoi(poseStack, multiBufferSource, "Candidates: " + set2, poiInfo, i, -23296);
		} else {
			renderTextOverPoi(poseStack, multiBufferSource, set2.size() + " potential owners", poiInfo, i, -23296);
		}

		renderTextOverPoi(poseStack, multiBufferSource, "Free tickets: " + poiInfo.freeTicketCount, poiInfo, ++i, -256);
		renderTextOverPoi(poseStack, multiBufferSource, poiInfo.type, poiInfo, ++i, -1);
	}

	private void renderPath(PoseStack poseStack, MultiBufferSource multiBufferSource, BrainDebugPayload.BrainDump brainDump, double d, double e, double f) {
		if (brainDump.path() != null) {
			PathfindingRenderer.renderPath(poseStack, multiBufferSource, brainDump.path(), 0.5F, false, false, d, e, f);
		}
	}

	private void renderBrainInfo(PoseStack poseStack, MultiBufferSource multiBufferSource, BrainDebugPayload.BrainDump brainDump, double d, double e, double f) {
		boolean bl = this.isMobSelected(brainDump);
		int i = 0;
		renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), i, brainDump.name(), -1, 0.03F);
		i++;
		if (bl) {
			renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), i, brainDump.profession() + " " + brainDump.xp() + " xp", -1, 0.02F);
			i++;
		}

		if (bl) {
			int j = brainDump.health() < brainDump.maxHealth() ? -23296 : -1;
			renderTextOverMob(
				poseStack,
				multiBufferSource,
				brainDump.pos(),
				i,
				"health: " + String.format(Locale.ROOT, "%.1f", brainDump.health()) + " / " + String.format(Locale.ROOT, "%.1f", brainDump.maxHealth()),
				j,
				0.02F
			);
			i++;
		}

		if (bl && !brainDump.inventory().equals("")) {
			renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), i, brainDump.inventory(), -98404, 0.02F);
			i++;
		}

		if (bl) {
			for (String string : brainDump.behaviors()) {
				renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), i, string, -16711681, 0.02F);
				i++;
			}
		}

		if (bl) {
			for (String string : brainDump.activities()) {
				renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), i, string, -16711936, 0.02F);
				i++;
			}
		}

		if (brainDump.wantsGolem()) {
			renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), i, "Wants Golem", -23296, 0.02F);
			i++;
		}

		if (bl && brainDump.angerLevel() != -1) {
			renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), i, "Anger Level: " + brainDump.angerLevel(), -98404, 0.02F);
			i++;
		}

		if (bl) {
			for (String string : brainDump.gossips()) {
				if (string.startsWith(brainDump.name())) {
					renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), i, string, -1, 0.02F);
				} else {
					renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), i, string, -23296, 0.02F);
				}

				i++;
			}
		}

		if (bl) {
			for (String string : Lists.reverse(brainDump.memories())) {
				renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), i, string, -3355444, 0.02F);
				i++;
			}
		}

		if (bl) {
			this.renderPath(poseStack, multiBufferSource, brainDump, d, e, f);
		}
	}

	private static void renderTextOverPoi(
		PoseStack poseStack, MultiBufferSource multiBufferSource, String string, BrainDebugRenderer.PoiInfo poiInfo, int i, int j
	) {
		renderTextOverPos(poseStack, multiBufferSource, string, poiInfo.pos, i, j);
	}

	private static void renderTextOverPos(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, BlockPos blockPos, int i, int j) {
		double d = 1.3;
		double e = 0.2;
		double f = (double)blockPos.getX() + 0.5;
		double g = (double)blockPos.getY() + 1.3 + (double)i * 0.2;
		double h = (double)blockPos.getZ() + 0.5;
		DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, f, g, h, j, 0.02F, true, 0.0F, true);
	}

	private static void renderTextOverMob(PoseStack poseStack, MultiBufferSource multiBufferSource, Position position, int i, String string, int j, float f) {
		double d = 2.4;
		double e = 0.25;
		BlockPos blockPos = BlockPos.containing(position);
		double g = (double)blockPos.getX() + 0.5;
		double h = position.y() + 2.4 + (double)i * 0.25;
		double k = (double)blockPos.getZ() + 0.5;
		float l = 0.5F;
		DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, g, h, k, j, f, false, 0.5F, true);
	}

	private Set<String> getTicketHolderNames(BrainDebugRenderer.PoiInfo poiInfo) {
		return (Set<String>)this.getTicketHolders(poiInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
	}

	private Set<String> getPotentialTicketHolderNames(BrainDebugRenderer.PoiInfo poiInfo) {
		return (Set<String>)this.getPotentialTicketHolders(poiInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
	}

	private boolean isMobSelected(BrainDebugPayload.BrainDump brainDump) {
		return Objects.equals(this.lastLookedAtUuid, brainDump.uuid());
	}

	private boolean isPlayerCloseEnoughToMob(BrainDebugPayload.BrainDump brainDump) {
		Player player = this.minecraft.player;
		BlockPos blockPos = BlockPos.containing(player.getX(), brainDump.pos().y(), player.getZ());
		BlockPos blockPos2 = BlockPos.containing(brainDump.pos());
		return blockPos.closerThan(blockPos2, 30.0);
	}

	private Collection<UUID> getTicketHolders(BlockPos blockPos) {
		return (Collection<UUID>)this.brainDumpsPerEntity
			.values()
			.stream()
			.filter(brainDump -> brainDump.hasPoi(blockPos))
			.map(BrainDebugPayload.BrainDump::uuid)
			.collect(Collectors.toSet());
	}

	private Collection<UUID> getPotentialTicketHolders(BlockPos blockPos) {
		return (Collection<UUID>)this.brainDumpsPerEntity
			.values()
			.stream()
			.filter(brainDump -> brainDump.hasPotentialPoi(blockPos))
			.map(BrainDebugPayload.BrainDump::uuid)
			.collect(Collectors.toSet());
	}

	private Map<BlockPos, List<String>> getGhostPois() {
		Map<BlockPos, List<String>> map = Maps.<BlockPos, List<String>>newHashMap();

		for (BrainDebugPayload.BrainDump brainDump : this.brainDumpsPerEntity.values()) {
			for (BlockPos blockPos : Iterables.concat(brainDump.pois(), brainDump.potentialPois())) {
				if (!this.pois.containsKey(blockPos)) {
					((List)map.computeIfAbsent(blockPos, blockPosx -> Lists.newArrayList())).add(brainDump.name());
				}
			}
		}

		return map;
	}

	private void updateLastLookedAtUuid() {
		DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent(entity -> this.lastLookedAtUuid = entity.getUUID());
	}

	@Environment(EnvType.CLIENT)
	public static class PoiInfo {
		public final BlockPos pos;
		public final String type;
		public int freeTicketCount;

		public PoiInfo(BlockPos blockPos, String string, int i) {
			this.pos = blockPos;
			this.type = string;
			this.freeTicketCount = i;
		}
	}
}
