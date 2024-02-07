package net.minecraft.network.syncher;

import java.util.List;

public interface SyncedDataHolder {
	void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor);

	void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> list);
}
