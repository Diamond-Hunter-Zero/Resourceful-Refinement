package com.resourceful_refinement.content.bucket_excavator;

import com.resourceful_refinement.utilities.RegionExtents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.*;

import com.resourceful_refinement.content.glare.DimensionalNodePos;
import com.resourceful_refinement.content.bucket_excavator.ExcavatorRegionSavedData.ExcavatorRecord;

public class SpatialExcavatorIndex {
    // Shifts coordinates by 4 bits to instantly divide by 16 (Size of a spatial bin)
    private static final int BIN_SHIFT = 4;

    // Outer map routes queries directly to the correct dimension.
    // Inner map routes 3D spatial bins inside that isolated dimension.
    private final Map<ResourceKey<Level>, Map<Long, List<ExcavatorRecord>>> dimensionGrids = new HashMap<>();


    public SpatialExcavatorIndex() {
    }

    /// Constructs and populates the spatial optimization index
    public SpatialExcavatorIndex(Map<DimensionalNodePos, ExcavatorRecord> sourceMap) {
        for (ExcavatorRecord record : sourceMap.values()) {
            addRecord(record);
        }
    }

    public void addRecord(ExcavatorRecord record) {
        RegionExtents region = record.excavationRegion;
        if (region == null) return;
        ResourceKey<Level> dimension = record.pos.levelKey();

        int minBinX = region.min().getX() >> BIN_SHIFT;
        int minBinY = region.min().getY() >> BIN_SHIFT;
        int minBinZ = region.min().getZ() >> BIN_SHIFT;

        int maxBinX = region.max().getX() >> BIN_SHIFT;
        int maxBinY = region.max().getY() >> BIN_SHIFT;
        int maxBinZ = region.max().getZ() >> BIN_SHIFT;

        // Step 1: Isolate the spatial grid specific to this dimension context
        Map<Long, List<ExcavatorRecord>> spatialGrid = dimensionGrids.computeIfAbsent(dimension, d -> new HashMap<>());

        // Step 2: Map this record to every spatial block bucket it overlaps
        for (int bx = minBinX; bx <= maxBinX; bx++) {
            for (int by = minBinY; by <= maxBinY; by++) {
                for (int bz = minBinZ; bz <= maxBinZ; bz++) {
                    long binKey = getBinKey(bx, by, bz);
                    List<ExcavatorRecord> records = spatialGrid.computeIfAbsent(binKey, k -> new ArrayList<>());
                    if (!records.contains(record)) {
                        records.add(record);
                    }
                }
            }
        }
    }

    public void removeRecord(ExcavatorRecord record) {
        RegionExtents region = record.excavationRegion;
        if (region == null) return;
        ResourceKey<Level> dimension = record.pos.levelKey();

        int minBinX = region.min().getX() >> BIN_SHIFT;
        int minBinY = region.min().getY() >> BIN_SHIFT;
        int minBinZ = region.min().getZ() >> BIN_SHIFT;

        int maxBinX = region.max().getX() >> BIN_SHIFT;
        int maxBinY = region.max().getY() >> BIN_SHIFT;
        int maxBinZ = region.max().getZ() >> BIN_SHIFT;

        Map<Long, List<ExcavatorRecord>> spatialGrid = dimensionGrids.get(dimension);
        if (spatialGrid == null) return;

        for (int bx = minBinX; bx <= maxBinX; bx++) {
            for (int by = minBinY; by <= maxBinY; by++) {
                for (int bz = minBinZ; bz <= maxBinZ; bz++) {
                    long binKey = getBinKey(bx, by, bz);
                    List<ExcavatorRecord> records = spatialGrid.get(binKey);
                    if (records != null) {
                        records.remove(record);
                        if (records.isEmpty()) {
                            spatialGrid.remove(binKey);
                        }
                    }
                }
            }
        }
    }

    /**
     * Performs a multi-dimensional, sub-chunk optimized look-up query.
     * @param dimension The ResourceKey of the level to query within.
     * @param pos       The absolute coordinate being checked.
     * @return True if the block sits inside any active Excavator region in that world.
     */
    public boolean contains(ResourceKey<Level> dimension, BlockPos pos) {
        // Find the active index matching the incoming dimension
        Map<Long, List<ExcavatorRecord>> spatialGrid = dimensionGrids.get(dimension);
        if (spatialGrid == null) return false; // Dimension has no active excavators

        int tx = pos.getX();
        int ty = pos.getY();
        int tz = pos.getZ();

        // Convert world blocks into a 64-bit spatial coordinate hash
        long binKey = getBinKey(tx >> BIN_SHIFT, ty >> BIN_SHIFT, tz >> BIN_SHIFT);
        List<ExcavatorRecord> candidates = spatialGrid.get(binKey);

        if (candidates == null) return false;

        // Micro-optimized loop through only the specific records occupying this 16x16x16 space
        for (int i = 0; i < candidates.size(); i++) {
            ExcavatorRecord record = candidates.get(i);
            RegionExtents region = record.excavationRegion;
            BlockPos min = region.min();
            BlockPos max = region.max();

            // Horizontal spatial axes checked first to drop non-matching candidates quickly
            if (tx >= min.getX() && tx <= max.getX() &&
                    tz >= min.getZ() && tz <= max.getZ() &&
                    ty >= min.getY() && ty <= max.getY()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the region owned by the given record overlaps with any other excavator's region,
     * excluding the record itself by reference identity.
     * <p>
     * Uses the spatial bin grid to cheaply narrow down candidates before doing precise AABB intersection.
     *
     * @param record The ExcavatorRecord whose region is being tested for overlaps.
     * @return True if any other record's region intersects with this record's region.
     */
    public boolean overlapsAny(ExcavatorRecord record) {
        RegionExtents region = record.excavationRegion;
        if (region == null) return false;

        ResourceKey<Level> dimension = record.pos.levelKey();
        Map<Long, List<ExcavatorRecord>> spatialGrid = dimensionGrids.get(dimension);
        if (spatialGrid == null) return false;

        int minBinX = region.min().getX() >> BIN_SHIFT;
        int minBinY = region.min().getY() >> BIN_SHIFT;
        int minBinZ = region.min().getZ() >> BIN_SHIFT;

        int maxBinX = region.max().getX() >> BIN_SHIFT;
        int maxBinY = region.max().getY() >> BIN_SHIFT;
        int maxBinZ = region.max().getZ() >> BIN_SHIFT;

        // Pre-cache the querying record's AABB components for tight inner-loop comparisons
        int rMinX = region.min().getX(), rMaxX = region.max().getX();
        int rMinY = region.min().getY(), rMaxY = region.max().getY();
        int rMinZ = region.min().getZ(), rMaxZ = region.max().getZ();

        // Guard against double-testing the same candidate when it spans multiple shared bins
        java.util.Set<ExcavatorRecord> tested = new java.util.HashSet<>();

        for (int bx = minBinX; bx <= maxBinX; bx++) {
            for (int by = minBinY; by <= maxBinY; by++) {
                for (int bz = minBinZ; bz <= maxBinZ; bz++) {
                    List<ExcavatorRecord> candidates = spatialGrid.get(getBinKey(bx, by, bz));
                    if (candidates == null) continue;

                    for (int i = 0; i < candidates.size(); i++) {
                        ExcavatorRecord candidate = candidates.get(i);

                        // Skip self and already-tested candidates
                        if (candidate == record || !tested.add(candidate)) continue;

                        RegionExtents other = candidate.excavationRegion;
                        if (other == null) continue;

                        // AABB vs AABB intersection: overlap on all three axes simultaneously
                        if (rMaxX >= other.min().getX() && rMinX <= other.max().getX() &&
                                rMaxZ >= other.min().getZ() && rMinZ <= other.max().getZ() &&
                                rMaxY >= other.min().getY() && rMinY <= other.max().getY()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Finds all overlapping records for the specified region, excluding the passed record
     *
     * @return A Set of ExcavatorRecord containing all overlapping excavator records.
     */
    public Set<ExcavatorRecord> findOverlapping(ResourceKey<Level> dimension, RegionExtents region, ExcavatorRecord exclude) {
        Set<ExcavatorRecord> matches = new HashSet<>();
        if (region == null) return matches;

        Map<Long, List<ExcavatorRecord>> spatialGrid = dimensionGrids.get(dimension);
        if (spatialGrid == null) return matches;

        int minBinX = region.min().getX() >> BIN_SHIFT;
        int minBinY = region.min().getY() >> BIN_SHIFT;
        int minBinZ = region.min().getZ() >> BIN_SHIFT;
        int maxBinX = region.max().getX() >> BIN_SHIFT;
        int maxBinY = region.max().getY() >> BIN_SHIFT;
        int maxBinZ = region.max().getZ() >> BIN_SHIFT;

        for (int bx = minBinX; bx <= maxBinX; bx++) {
            for (int by = minBinY; by <= maxBinY; by++) {
                for (int bz = minBinZ; bz <= maxBinZ; bz++) {
                    List<ExcavatorRecord> candidates = spatialGrid.get(getBinKey(bx, by, bz));
                    if (candidates == null) continue;

                    for (ExcavatorRecord candidate : candidates) {
                        if (candidate == exclude || candidate.excavationRegion == null) continue;
                        if (intersects(region, candidate.excavationRegion)) {
                            matches.add(candidate);
                        }
                    }
                }
            }
        }

        return matches;
    }

    private static boolean intersects(RegionExtents a, RegionExtents b) {
        return a.max().getX() >= b.min().getX() && a.min().getX() <= b.max().getX()
                && a.max().getY() >= b.min().getY() && a.min().getY() <= b.max().getY()
                && a.max().getZ() >= b.min().getZ() && a.min().getZ() <= b.max().getZ();
    }

    /// Compresses 3 dimension-relative integer sub-chunk values into a 64-bit long
    private static long getBinKey(int bx, int by, int bz) {
        return ((long) bx & 0xFFFFFFL) | (((long) by & 0xFFFFL) << 24) | (((long) bz & 0xFFFFFFL) << 40);
    }
}
