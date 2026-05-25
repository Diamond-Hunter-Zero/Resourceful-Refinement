/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Registry
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.LevelHeightAccessor
 *  net.minecraft.world.level.StructureManager
 *  net.minecraft.world.level.WorldGenLevel
 *  net.minecraft.world.level.block.Rotation
 *  net.minecraft.world.level.chunk.ChunkGenerator
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.RandomState
 *  net.minecraft.world.level.levelgen.WorldgenRandom
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece
 *  net.minecraft.world.level.levelgen.structure.Structure$GenerationContext
 *  net.minecraft.world.level.levelgen.structure.Structure$GenerationStub
 *  net.minecraft.world.level.levelgen.structure.StructurePiece
 *  net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder
 *  net.minecraft.world.level.levelgen.structure.pools.DimensionPadding
 *  net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement
 *  net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$PieceState
 *  net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer
 *  net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement
 *  net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
 *  net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup
 *  net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate$StructureBlockInfo
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.shapes.BooleanOp
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class JigsawPlacement {
    static final Logger LOGGER = LogUtils.getLogger();

    public static Optional<Structure.GenerationStub> addPieces(Structure.GenerationContext context, Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int maxDepth, BlockPos pos, boolean useExpansionHack, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter, PoolAliasLookup aliasLookup, DimensionPadding dimensionPadding, LiquidSettings liquidSettings) {
        BlockPos blockpos;
        RegistryAccess registryaccess = context.registryAccess();
        ChunkGenerator chunkgenerator = context.chunkGenerator();
        StructureTemplateManager structuretemplatemanager = context.structureTemplateManager();
        LevelHeightAccessor levelheightaccessor = context.heightAccessor();
        WorldgenRandom worldgenrandom = context.random();
        Registry registry = registryaccess.registryOrThrow(Registries.TEMPLATE_POOL);
        Rotation rotation = Rotation.getRandom((RandomSource)worldgenrandom);
        StructureTemplatePool structuretemplatepool = startPool.unwrapKey().flatMap(p_314915_ -> registry.getOptional(aliasLookup.lookup(p_314915_))).orElse((StructureTemplatePool)startPool.value());
        StructurePoolElement structurepoolelement = structuretemplatepool.getRandomTemplate((RandomSource)worldgenrandom);
        if (structurepoolelement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }
        if (startJigsawName.isPresent()) {
            ResourceLocation resourcelocation = startJigsawName.get();
            Optional<BlockPos> optional = JigsawPlacement.getRandomNamedJigsaw(structurepoolelement, resourcelocation, pos, rotation, structuretemplatemanager, worldgenrandom);
            if (optional.isEmpty()) {
                LOGGER.error("No starting jigsaw {} found in start pool {}", (Object)resourcelocation, (Object)startPool.unwrapKey().map(p_248484_ -> p_248484_.location().toString()).orElse("<unregistered>"));
                return Optional.empty();
            }
            blockpos = optional.get();
        } else {
            blockpos = pos;
        }
        BlockPos vec3i = blockpos.subtract((Vec3i)pos);
        BlockPos blockpos1 = pos.subtract((Vec3i)vec3i);
        PoolElementStructurePiece poolelementstructurepiece = new PoolElementStructurePiece(structuretemplatemanager, structurepoolelement, blockpos1, structurepoolelement.getGroundLevelDelta(), rotation, structurepoolelement.getBoundingBox(structuretemplatemanager, blockpos1, rotation), liquidSettings);
        BoundingBox boundingbox = poolelementstructurepiece.getBoundingBox();
        int i = (boundingbox.maxX() + boundingbox.minX()) / 2;
        int j = (boundingbox.maxZ() + boundingbox.minZ()) / 2;
        int k = projectStartToHeightmap.isPresent() ? pos.getY() + chunkgenerator.getFirstFreeHeight(i, j, projectStartToHeightmap.get(), levelheightaccessor, context.randomState()) : blockpos1.getY();
        int l = boundingbox.minY() + poolelementstructurepiece.getGroundLevelDelta();
        poolelementstructurepiece.move(0, k - l, 0);
        int i1 = k + vec3i.getY();
        return Optional.of(new Structure.GenerationStub(new BlockPos(i, i1, j), p_352014_ -> {
            ArrayList list = Lists.newArrayList();
            list.add(poolelementstructurepiece);
            if (maxDepth > 0) {
                AABB aabb = new AABB((double)(i - maxDistanceFromCenter), (double)Math.max(i1 - maxDistanceFromCenter, levelheightaccessor.getMinBuildHeight() + dimensionPadding.bottom()), (double)(j - maxDistanceFromCenter), (double)(i + maxDistanceFromCenter + 1), (double)Math.min(i1 + maxDistanceFromCenter + 1, levelheightaccessor.getMaxBuildHeight() - dimensionPadding.top()), (double)(j + maxDistanceFromCenter + 1));
                VoxelShape voxelshape = Shapes.join((VoxelShape)Shapes.create((AABB)aabb), (VoxelShape)Shapes.create((AABB)AABB.of((BoundingBox)boundingbox)), (BooleanOp)BooleanOp.ONLY_FIRST);
                JigsawPlacement.addPieces(context.randomState(), maxDepth, useExpansionHack, chunkgenerator, structuretemplatemanager, levelheightaccessor, (RandomSource)worldgenrandom, (Registry<StructureTemplatePool>)registry, poolelementstructurepiece, list, voxelshape, aliasLookup, liquidSettings);
                list.forEach(arg_0 -> ((StructurePiecesBuilder)p_352014_).addPiece(arg_0));
            }
        }));
    }

    private static Optional<BlockPos> getRandomNamedJigsaw(StructurePoolElement element, ResourceLocation startJigsawName, BlockPos pos, Rotation rotation, StructureTemplateManager structureTemplateManager, WorldgenRandom random) {
        List list = element.getShuffledJigsawBlocks(structureTemplateManager, pos, rotation, (RandomSource)random);
        Optional<BlockPos> optional = Optional.empty();
        for (StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : list) {
            ResourceLocation resourcelocation = ResourceLocation.tryParse((String)Objects.requireNonNull(structuretemplate$structureblockinfo.nbt(), () -> String.valueOf(structuretemplate$structureblockinfo) + " nbt was null").getString("name"));
            if (!startJigsawName.equals((Object)resourcelocation)) continue;
            optional = Optional.of(structuretemplate$structureblockinfo.pos());
            break;
        }
        return optional;
    }

    private static void addPieces(RandomState randomState, int maxDepth, boolean useExpansionHack, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, LevelHeightAccessor level, RandomSource random, Registry<StructureTemplatePool> pools, PoolElementStructurePiece startPiece, List<PoolElementStructurePiece> pieces, VoxelShape free, PoolAliasLookup aliasLookup, LiquidSettings liquidSettings) {
        Placer jigsawplacement$placer = new Placer(pools, maxDepth, chunkGenerator, structureTemplateManager, pieces, random);
        jigsawplacement$placer.tryPlacingChildren(startPiece, new MutableObject((Object)free), 0, useExpansionHack, level, randomState, aliasLookup, liquidSettings);
        while (jigsawplacement$placer.placing.hasNext()) {
            PieceState jigsawplacement$piecestate = (PieceState)jigsawplacement$placer.placing.next();
            jigsawplacement$placer.tryPlacingChildren(jigsawplacement$piecestate.piece, jigsawplacement$piecestate.free, jigsawplacement$piecestate.depth, useExpansionHack, level, randomState, aliasLookup, liquidSettings);
        }
    }

    public static boolean generateJigsaw(ServerLevel level, Holder<StructureTemplatePool> startPool, ResourceLocation startJigsawName, int maxDepth, BlockPos pos, boolean keepJigsaws) {
        ChunkGenerator chunkgenerator = level.getChunkSource().getGenerator();
        StructureTemplateManager structuretemplatemanager = level.getStructureManager();
        StructureManager structuremanager = level.structureManager();
        RandomSource randomsource = level.getRandom();
        Structure.GenerationContext structure$generationcontext = new Structure.GenerationContext(level.registryAccess(), chunkgenerator, chunkgenerator.getBiomeSource(), level.getChunkSource().randomState(), structuretemplatemanager, level.getSeed(), new ChunkPos(pos), (LevelHeightAccessor)level, p_227255_ -> true);
        Optional<Structure.GenerationStub> optional = JigsawPlacement.addPieces(structure$generationcontext, startPool, Optional.of(startJigsawName), maxDepth, pos, false, Optional.empty(), 128, PoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
        if (optional.isPresent()) {
            StructurePiecesBuilder structurepiecesbuilder = optional.get().getPiecesBuilder();
            for (StructurePiece structurepiece : structurepiecesbuilder.build().pieces()) {
                if (!(structurepiece instanceof PoolElementStructurePiece)) continue;
                PoolElementStructurePiece poolelementstructurepiece = (PoolElementStructurePiece)structurepiece;
                poolelementstructurepiece.place((WorldGenLevel)level, structuremanager, chunkgenerator, randomsource, BoundingBox.infinite(), pos, keepJigsaws);
            }
            return true;
        }
        return false;
    }
}
