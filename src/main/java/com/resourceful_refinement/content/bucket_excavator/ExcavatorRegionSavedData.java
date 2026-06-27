package com.resourceful_refinement.content.bucket_excavator;

import com.resourceful_refinement.content.glare.*;
import com.resourceful_refinement.utilities.RegionExtents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class ExcavatorRegionSavedData extends SavedData {

    private static final String DATA_NAME = "resourceful_refinement_excavator_regions";

    public final Map<DimensionalNodePos, ExcavatorRecord> excavators = new HashMap<>();
    private SpatialExcavatorIndex spatialIndex = new SpatialExcavatorIndex();


    // -------------------------------------------------------------------------
    // SavedData Definition
    // -------------------------------------------------------------------------

    public static SavedData.Factory<ExcavatorRegionSavedData> factory() {
        return new SavedData.Factory<>(ExcavatorRegionSavedData::new, ExcavatorRegionSavedData::load);
    }

    public static ExcavatorRegionSavedData get(ServerLevel level) {
        return level.getServer().getLevel(level.dimension()).getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    private static ExcavatorRegionSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        ExcavatorRegionSavedData data = new ExcavatorRegionSavedData();
        data.read(tag);
        return data;
    }


    // -------------------------------------------------------------------------
    // System Utilities & Logic
    // -------------------------------------------------------------------------
    public static void RegisterOrUpdateExcavator(ServerLevel level, DimensionalNodePos pos, Direction facingDirection) {
        ExcavatorRegionSavedData data = get(level);
        ExcavatorRecord record = data.excavators.get(pos);

        Set<ExcavatorRecord> affected = new HashSet<>();

        if (record != null && record.excavationRegion != null) {
            affected.addAll(data.spatialIndex.findOverlapping(pos.levelKey(), record.excavationRegion, null));
            data.spatialIndex.removeRecord(record);
        }

        if (record == null) {
            record = new ExcavatorRecord(pos);
            data.excavators.put(pos, record);
        }

        record.facing = facingDirection;
        record.excavationRegion = RegionExtents.GetFaceExtendedRegion(
                pos.pos(),
                facingDirection,
                BucketExcavatorBlockEntity.EXCAVATION_REGION_WIDTH,
                BucketExcavatorBlockEntity.EXCAVATION_REGION_HEIGHT,
                BucketExcavatorBlockEntity.EXCAVATION_REGION_DEPTH
        );

        data.spatialIndex.addRecord(record);

        affected.add(record);
        affected.addAll(data.spatialIndex.findOverlapping(pos.levelKey(), record.excavationRegion, null));

        data.refreshClearFlags(level.getServer(), affected);
        data.setDirty();
    }

    public static void RemoveExcavator(ServerLevel level, DimensionalNodePos pos) {
        ExcavatorRegionSavedData data = get(level);
        ExcavatorRecord record = data.excavators.remove(pos);
        if (record == null) return;

        Set<ExcavatorRecord> affected = new HashSet<>();
        affected.addAll(data.spatialIndex.findOverlapping(pos.levelKey(), record.excavationRegion, record));

        data.spatialIndex.removeRecord(record);
        data.refreshClearFlags(level.getServer(), affected);
        data.setDirty();
    }

    private void refreshClearFlags(MinecraftServer server, Collection<ExcavatorRecord> affected) {
        for (ExcavatorRecord record : affected) {
            ServerLevel targetLevel = server.getLevel(record.pos.levelKey());
            if (targetLevel == null) continue;

            boolean clear = !spatialIndex.overlapsAny(record);
            if (targetLevel.getBlockEntity(record.pos.pos()) instanceof BucketExcavatorBlockEntity be) {
                be.setExcavatorClear(clear);
            }
        }
    }

    public static boolean IsBlockInsideExcavationRegion(ServerLevel level, DimensionalNodePos pos)
    {
        ServerLevel targetLevel = level.getServer().getLevel(pos.levelKey());
        if (targetLevel == null) return false;
        ExcavatorRegionSavedData saveData = ExcavatorRegionSavedData.get(targetLevel);
        return saveData.spatialIndex.contains(pos.levelKey(), pos.pos());
    }

    public static boolean IsExcavatorInsideExcavationRegion(ServerLevel level, ExcavatorRecord excavatorData)
    {
        ServerLevel targetLevel = level.getServer().getLevel(excavatorData.pos.levelKey());
        if (targetLevel == null) return false;
        ExcavatorRegionSavedData saveData = ExcavatorRegionSavedData.get(targetLevel);
        return saveData.spatialIndex.overlapsAny(excavatorData);
    }


    // -------------------------------------------------------------------------
    // Data persistence
    // -------------------------------------------------------------------------

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag nodeList = new ListTag();
        for (ExcavatorRecord node : excavators.values()) {
            nodeList.add(node.save());
        }
        compoundTag.put("Nodes", nodeList);

        return compoundTag;
    }

    private void read(CompoundTag tag) {
        excavators.clear();
        spatialIndex = new SpatialExcavatorIndex();

        ListTag nodeList = tag.getList("Nodes", Tag.TAG_COMPOUND);
        for (int i = 0; i < nodeList.size(); i++) {
            ExcavatorRecord node = ExcavatorRecord.load(nodeList.getCompound(i));
            excavators.put(node.pos, node);
            spatialIndex.addRecord(node);
        }
    }


    // -------------------------------------------------------------------------
    // Records
    // -------------------------------------------------------------------------

    public static class ExcavatorRecord {
        public final DimensionalNodePos pos;
        public Direction facing;
        public RegionExtents excavationRegion;

        ExcavatorRecord(DimensionalNodePos pos) {
            this.pos = pos;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            DimensionalNodePos.writePos(tag, "", pos);
            tag.putInt("Direction", facing.ordinal());

            return tag;
        }

        static ExcavatorRecord load(CompoundTag tag) {
            DimensionalNodePos pos = DimensionalNodePos.readPos(tag, "").orElseThrow();
            ExcavatorRecord record = new ExcavatorRecord(pos);
            record.facing = Direction.values()[Math.clamp(tag.getInt("Direction"),0, Direction.values().length-1)];

            record.excavationRegion = RegionExtents.GetFaceExtendedRegion(pos.pos(), record.facing,
                    BucketExcavatorBlockEntity.EXCAVATION_REGION_WIDTH,
                    BucketExcavatorBlockEntity.EXCAVATION_REGION_HEIGHT,
                    BucketExcavatorBlockEntity.EXCAVATION_REGION_DEPTH);

            return record;
        }
    }

}
