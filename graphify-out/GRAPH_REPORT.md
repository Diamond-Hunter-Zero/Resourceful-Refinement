# Graph Report - .  (2026-09-23)

## Corpus Check
- Large corpus: 1064 files · ~508,242 words. Semantic extraction will be expensive (many Claude tokens). Consider running on a subfolder.

## Summary
- 3375 nodes · 10905 edges · 146 communities (135 shown, 11 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 268 edges (avg confidence: 0.8)
- Token cost: 242,914 input · 0 output

## Community Hubs (Navigation)
- Combustion Chamber Logic
- Vanilla Chunk Generation
- Machine Block Items
- Machine Blocks (Interaction)
- Block Entity Renderers
- JEI Recipe Categories
- Entity Model Layers
- Fracking Pump Outlet Block
- Gel Blob Entity
- Gel Impact & Potion Fluids
- Radiator Heat Logic
- Noise Chunk Generation
- Mechanical Sieve Models
- Recipe Serializers
- Block Entity Constructors
- Gel Tracking Saved Data
- Brewer's Tap Logic
- Distillery Logic
- Jigsaw Structure Worldgen
- Advanced Pump Logic
- Milking Station Block
- GLARE Network Concepts
- Create Mixins
- Fuel Tank Block
- Refinery Access Port
- Hosegun Gloopy System
- Gel Splatter Block Entity
- Ponder Scenes
- Gel Fluid Tint Colours
- Forge Mould Block Entity
- Refinery Kinetic Proxy
- Item & Entity Renderers
- Block Entity Files
- Mechanical Forge Mould Block
- Milking Station Seat Entity
- Recipe Inputs
- Radiator Model & Heating
- Coating Recipe Category
- Fluid Refinery Recipe
- Fracking Pump Assembly
- Community 40
- Community 41
- Community 42
- Community 43
- Community 44
- Community 45
- Community 46
- Community 47
- Community 48
- Community 49
- Community 50
- Community 51
- Community 52
- Community 53
- Community 54
- Community 55
- Community 56
- Community 57
- Community 58
- Community 59
- Community 60
- Community 61
- Community 62
- Community 63
- Community 64
- Community 65
- Community 66
- Community 67
- Community 68
- Community 69
- Community 70
- Community 71
- Community 72
- Community 73
- Community 74
- Community 75
- Community 76
- Community 77
- Community 78
- Community 79
- Community 80
- Community 81
- Community 82
- Community 83
- Community 84
- Community 85
- Community 86
- Community 87
- Community 88
- Community 89
- Community 90
- Community 91
- Community 92
- Community 93
- Community 94
- Community 95
- Community 96
- Community 97
- Community 98
- Community 99
- Community 100
- Community 101
- Community 102
- Community 103
- Community 104
- Community 105
- Community 106
- Community 107
- Community 108
- Community 109
- Community 110
- Community 111
- Community 112
- Community 113
- Community 114
- Community 115
- Community 116
- Community 117
- Community 118
- Community 119
- Community 120
- Community 121
- Community 122
- Community 123
- Community 124
- Community 125
- Community 126
- Community 127
- Community 128
- Community 129
- Community 130
- Community 131
- Community 132
- Community 133
- Community 134
- Community 135
- Community 136
- Community 137
- Community 138
- Community 139
- Community 140
- Community 142
- Community 143
- Community 144

## God Nodes (most connected - your core abstractions)
1. `ResourcefulRefinementMain` - 82 edges
2. `CombustionChamberBlockEntity` - 73 edges
3. `RefineryAccessPortBlockEntity` - 71 edges
4. `ChunkGenerator` - 53 edges
5. `GelBlobEntity` - 52 edges
6. `BrewersTapBlockEntity` - 51 edges
7. `ModBlocks` - 50 edges
8. `DistilleryBlockEntity` - 47 edges
9. `MilkingStationBlockEntity` - 46 edges
10. `MechanicalFluidSieveBlockEntity` - 46 edges

## Surprising Connections (you probably didn't know these)
- `Mineral Deposit Node` --semantically_similar_to--> `Geyser`  [INFERRED] [semantically similar]
  Resourceful Refinement Design Docs/Bucket Excavator.md → README.md
- `NetherSurfaceJigsawStructure` --inherits--> `Structure`  [EXTRACTED]
  src/main/java/com/resourceful_refinement/worldgen/structure/NetherSurfaceJigsawStructure.java → tools/decompiled/GenerationContext.java
- `Fluid Processing Recipes` --references--> `Mechanical Fluid Sieve`  [EXTRACTED]
  Resourceful Refinement Design Docs/Fluid Processing Recipes.md → README.md
- `Carborax Fluids` --conceptually_related_to--> `Combustion Chamber`  [INFERRED]
  Resourceful Refinement Design Docs/Fluid Properties.md → README.md
- `Carborax Fluids` --conceptually_related_to--> `Carbonox Processing`  [INFERRED]
  Resourceful Refinement Design Docs/Fluid Properties.md → README.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Fluid Refinement Progression Tree** — resourceful_refinement_design_docs_fluid_properties_raw_molten_minerals, resourceful_refinement_design_docs_fluid_properties_catalysed_fluids, resourceful_refinement_design_docs_fluid_properties_alloyed_fluids, resourceful_refinement_design_docs_fluid_properties_purified_fluids [INFERRED 0.85]
- **Fluid Refinery Multiblock System** — resourceful_refinement_design_docs_fluid_refinery_fluid_refinery, resourceful_refinement_design_docs_fluid_refinery_refinery_access_port, resourceful_refinement_design_docs_blender_blade_blender_blade, resourceful_refinement_design_docs_blender_blade_refinery_structure_helper, resourceful_refinement_design_docs_detailed_fluid_refinery_implementation_plan_controller_proxy_pattern [EXTRACTED 0.90]
- **Geyser/Deposit Extraction Pattern** — readme_geyser, resourceful_refinement_design_docs_bucket_excavator_mineral_deposit, resourceful_refinement_design_docs_drill_pylon_crystal_fissure_bud [INFERRED 0.80]
- **GLARE Lux Power System** — resourceful_refinement_design_docs_glare_networks_glare_network, resourceful_refinement_design_docs_glare_networks_lux, resourceful_refinement_design_docs_glare_networks_iglareemitter, resourceful_refinement_design_docs_glare_networks_iglarereceiver, resourceful_refinement_design_docs_glare_networks_overload_state [EXTRACTED 0.90]
- **Telemetry Communication Flow** — resourceful_refinement_design_docs_glare_networks_telemetry_messaging, resourceful_refinement_design_docs_glare_networks_inbox_address, resourceful_refinement_design_docs_glare_networks_telemetry_terminal, resourceful_refinement_design_docs_telemetry_terminal_gui_terminal_gui [EXTRACTED 0.90]
- **Gel Projectile Pipeline** — resourceful_refinement_design_docs_hosegun_hosegun, resourceful_refinement_design_docs_hosegun_gel_blob, resourceful_refinement_design_docs_gel_splatter_gel_splatter, resourceful_refinement_design_docs_gel_splatter_gel_type, resourceful_refinement_design_docs_paint_nozzle_paint_nozzle [EXTRACTED 0.90]

## Communities (146 total, 11 thin omitted)

### Community 0 - "Combustion Chamber Logic"
Cohesion: 0.06
Nodes (17): com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection, com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity, com.simibubi.create.content.kinetics.base.IRotate, com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour, net.minecraft.core.Direction.Axis, net.neoforged.neoforge.fluids.capability.IFluidHandler, ScrollOptionBehaviour, ChainInputFluidHandler (+9 more)

### Community 1 - "Vanilla Chunk Generation"
Cohesion: 0.07
Nodes (35): ApiStatus.Internal, com.mojang.datafixers.util.Pair, it.unimi.dsi.fastutil.ints.IntSet, javax.annotation.Nullable, net.minecraft.core.Holder, net.minecraft.core.HolderSet, net.minecraft.core.RegistryAccess, net.minecraft.core.SectionPos (+27 more)

### Community 2 - "Machine Block Items"
Cohesion: 0.06
Nodes (34): Items, net.minecraft.world.food.FoodProperties, net.minecraft.world.item.BlockItem, net.minecraft.world.level.block.Block, net.neoforged.neoforge.client.extensions.common.IClientItemExtensions, net.neoforged.neoforge.fluids.SimpleFluidContent, CastingDepotItem, IClientItemExtensions (+26 more)

### Community 3 - "Machine Blocks (Interaction)"
Cohesion: 0.16
Nodes (25): com.mojang.serialization.MapCodec, com.simibubi.create.content.kinetics.simpleRelays.ICogWheel, com.simibubi.create.foundation.block.IBE, net.minecraft.world.entity.player.Player, net.minecraft.world.InteractionHand, net.minecraft.world.InteractionResult, net.minecraft.world.item.context.BlockPlaceContext, net.minecraft.world.ItemInteractionResult (+17 more)

### Community 4 - "Block Entity Renderers"
Cohesion: 0.10
Nodes (19): com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer, dev.engine_room.flywheel.lib.model.baked.PartialModel, net.createmod.catnip.render.SuperByteBuffer, net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer, net.minecraft.client.renderer.MultiBufferSource, net.minecraft.resources.ResourceLocation, net.minecraft.world.item.ItemDisplayContext, CastingDepotRenderer (+11 more)

### Community 5 - "JEI Recipe Categories"
Cohesion: 0.11
Nodes (18): mezz.jei.api.gui.builder.IRecipeLayoutBuilder, mezz.jei.api.gui.builder.ITooltipBuilder, mezz.jei.api.gui.drawable.IDrawable, mezz.jei.api.gui.ingredient.IRecipeSlotsView, mezz.jei.api.helpers.IGuiHelper, mezz.jei.api.recipe.category.IRecipeCategory, mezz.jei.api.recipe.IFocusGroup, mezz.jei.api.recipe.RecipeType (+10 more)

### Community 6 - "Entity Model Layers"
Cohesion: 0.06
Nodes (22): net.minecraft.client.animation.AnimationDefinition, net.minecraft.client.model.geom.ModelPart, net.minecraft.client.model.HierarchicalModel, ForgeMouldTubeModel, LayerDefinition, FrackingPumpBaseModel, LayerDefinition, Override (+14 more)

### Community 7 - "Fracking Pump Outlet Block"
Cohesion: 0.08
Nodes (11): net.minecraft.core.Direction, net.minecraft.world.level.Level, net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent, FrackingPumpOutletBlock, Axis, Builder, LevelReader, Override (+3 more)

### Community 8 - "Gel Blob Entity"
Cohesion: 0.08
Nodes (12): net.minecraft.world.entity.projectile.ThrowableItemProjectile, net.minecraft.world.phys.HitResult, GelBlobEntity, Block, Builder, CompoundTag, FluidStack, Override (+4 more)

### Community 9 - "Gel Impact & Potion Fluids"
Cohesion: 0.09
Nodes (21): BottleType, net.minecraft.util.RandomSource, net.minecraft.world.entity.Entity, net.minecraft.world.entity.LivingEntity, net.minecraft.world.item.alchemy.PotionContents, net.minecraft.world.phys.Vec3, net.neoforged.neoforge.fluids.FluidStack, GelImpactConstants (+13 more)

### Community 10 - "Radiator Heat Logic"
Cohesion: 0.06
Nodes (19): CachedCheck, HeatLevel, net.minecraft.world.item.crafting.SingleRecipeInput, net.minecraft.world.item.crafting.SmokingRecipe, SingleRecipeInput, CompoundTag, FluidAction, Override (+11 more)

### Community 11 - "Noise Chunk Generation"
Cohesion: 0.11
Nodes (22): com.google.common.annotations.VisibleForTesting, FluidPicker, MutableObject, net.minecraft.core.Registry, net.minecraft.server.level.WorldGenRegion, net.minecraft.world.level.biome.BiomeManager, net.minecraft.world.level.chunk.ChunkAccess, net.minecraft.world.level.levelgen.blending.Blender (+14 more)

### Community 12 - "Mechanical Sieve Models"
Cohesion: 0.07
Nodes (19): com.mojang.blaze3d.vertex.VertexConsumer, net.minecraft.client.renderer.texture.TextureAtlasSprite, LayerDefinition, MechanicalSieveCasingBottomModel, LayerDefinition, MechanicalSieveCasingMiddleModel, LayerDefinition, MechanicalSieveCasingModel (+11 more)

### Community 13 - "Recipe Serializers"
Cohesion: 0.14
Nodes (26): com.mojang.datafixers.util.Either, com.mojang.serialization.Codec, com.mojang.serialization.codecs.RecordCodecBuilder, com.simibubi.create.content.processing.recipe.ProcessingOutput, com.simibubi.create.content.processing.recipe.ProcessingRecipeParams, com.simibubi.create.content.processing.recipe.StandardProcessingRecipe, com.simibubi.create.foundation.recipe.IRecipeTypeInfo, net.minecraft.network.codec.StreamCodec (+18 more)

### Community 14 - "Block Entity Constructors"
Cohesion: 0.08
Nodes (10): com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock, net.minecraft.core.BlockPos, net.minecraft.world.level.block.entity.BlockEntityType, net.minecraft.world.level.LevelReader, CombustionChamberFanIntegration, BlenderBladeBlock, Axis, Override (+2 more)

### Community 15 - "Gel Tracking Saved Data"
Cohesion: 0.09
Nodes (13): Entry, it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap, ListTag, net.minecraft.nbt.ListTag, net.minecraft.server.level.ServerLevel, net.minecraft.world.level.chunk.LevelChunk, net.minecraft.world.level.saveddata.SavedData, GelTrackingSavedData (+5 more)

### Community 16 - "Brewer's Tap Logic"
Cohesion: 0.11
Nodes (10): com.simibubi.create.content.kinetics.belt.BeltBlockEntity, com.simibubi.create.content.kinetics.belt.transport.BeltInventory, com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack, BrewersTapBlockEntity, CompoundTag, ItemStack, Override, Provider (+2 more)

### Community 17 - "Distillery Logic"
Cohesion: 0.08
Nodes (10): com.simibubi.create.api.boiler.BoilerHeater, com.simibubi.create.content.processing.recipe.HeatCondition, DistilleryBlockEntity, DistilleryFilterValueBox, CompoundTag, FilteringBehaviour, Override, Provider (+2 more)

### Community 18 - "Jigsaw Structure Worldgen"
Cohesion: 0.10
Nodes (31): com.mojang.serialization.DataResult, net.minecraft.world.level.block.Rotation, net.minecraft.world.level.levelgen.heightproviders.HeightProvider, net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece, net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding, net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup, net.minecraft.world.level.levelgen.structure.pools.DimensionPadding, net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool (+23 more)

### Community 19 - "Advanced Pump Logic"
Cohesion: 0.10
Nodes (13): com.simibubi.create.content.fluids.FlowSource, net.createmod.catnip.data.Pair, net.createmod.catnip.math.BlockFace, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction, AdvancedPumpBlockEntity, BlockFace, Override, Provider (+5 more)

### Community 20 - "Milking Station Block"
Cohesion: 0.11
Nodes (9): net.minecraft.world.entity.Mob, net.minecraft.world.level.block.state.BlockState, Axis, Builder, Override, MilkingStationBlock, Builder, Override (+1 more)

### Community 21 - "GLARE Network Concepts"
Cohesion: 0.06
Nodes (43): Gel Splatter, Gel Type, LuxSocket Interface, Lux Transceiver, GLARE Chromatic-Transceiver, Colour Charge, GLARE Emitter Dish, GLARE Network (+35 more)

### Community 22 - "Create Mixins"
Cohesion: 0.09
Nodes (20): com.simibubi.create.content.fluids.OpenEndedPipe, net.minecraft.world.item.crafting.Recipe, net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement, net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate, org.spongepowered.asm.mixin.gen.Accessor, org.spongepowered.asm.mixin.injection.callback.CallbackInfo, org.spongepowered.asm.mixin.injection.Inject, org.spongepowered.asm.mixin.Mixin (+12 more)

### Community 23 - "Fuel Tank Block"
Cohesion: 0.10
Nodes (10): net.minecraft.world.level.BlockGetter, net.minecraft.world.phys.shapes.VoxelShape, FuelTankBlock, Builder, HitResult, LevelReader, Override, FluidRefillStationBlock (+2 more)

### Community 24 - "Refinery Access Port"
Cohesion: 0.10
Nodes (8): CompoundTag, FilteringBehaviour, Level, LevelAccessor, Override, Provider, RefineryAccessPortBlockEntity, RefineryFilterValueBox

### Community 25 - "Hosegun Gloopy System"
Cohesion: 0.12
Nodes (10): com.simibubi.create.content.processing.basin.BasinBlockEntity, net.minecraft.world.item.ItemStack, HosegunGloopy, HosegunGloopyRecipes, HosegunFluidHandler, HosegunItem, IClientItemExtensions, Override (+2 more)

### Community 26 - "Gel Splatter Block Entity"
Cohesion: 0.11
Nodes (7): Factory, net.minecraft.network.Connection, GelSplatterBlockEntity, CompoundTag, Override, Provider, GelTrackingService

### Community 27 - "Ponder Scenes"
Cohesion: 0.11
Nodes (13): net.createmod.ponder.api.scene.SceneBuilder, net.createmod.ponder.api.scene.SceneBuildingUtil, BrewersTapPonders, CombustionChamberPonders, DistilleryPonders, FluidPonders, ForgeAndCastingPonders, FrackingPonders (+5 more)

### Community 28 - "Gel Fluid Tint Colours"
Cohesion: 0.10
Nodes (8): net.minecraft.world.level.material.Fluid, net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions, org.joml.Vector3f, GelFluidTintColors, GelFluidTintColorsClient, GelSplatterBlockEntityAccess, GelSplatterBlocks, HosegunFluidColors

### Community 29 - "Forge Mould Block Entity"
Cohesion: 0.09
Nodes (14): ForgeMouldValueBox, AABB, Block, CompoundTag, FilteringBehaviour, Override, Provider, Recipe (+6 more)

### Community 30 - "Refinery Kinetic Proxy"
Cohesion: 0.10
Nodes (7): IBE, Axis, Override, RefineryKineticProxyBlock, Builder, Override, RefineryProxyBlock

### Community 31 - "Item & Entity Renderers"
Cohesion: 0.09
Nodes (15): net.minecraft.client.renderer.culling.Frustum, net.minecraft.client.renderer.entity.EntityRenderer, net.neoforged.api.distmarker.OnlyIn, net.neoforged.fml.common.Mod, Context, Override, MilkingStationSeatRenderer, ThrownPlungerRenderer (+7 more)

### Community 32 - "Block Entity Files"
Cohesion: 0.22
Nodes (17): com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation, com.simibubi.create.content.fluids.FluidTransportBehaviour, com.simibubi.create.content.kinetics.base.KineticBlockEntity, com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour, com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform, com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour, com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform, com.simibubi.create.foundation.blockEntity.SmartBlockEntity (+9 more)

### Community 33 - "Mechanical Forge Mould Block"
Cohesion: 0.10
Nodes (11): com.simibubi.create.content.kinetics.base.KineticBlock, Axis, Builder, LevelReader, Override, MechanicalForgeMouldBlock, Axis, Builder (+3 more)

### Community 34 - "Milking Station Seat Entity"
Cohesion: 0.11
Nodes (12): MoveFunction, net.minecraft.world.entity.EntityType, net.minecraft.world.entity.projectile.ThrownTrident, net.minecraft.world.phys.EntityHitResult, Builder, Override, Vec3, MilkingStationSeatEntity (+4 more)

### Community 35 - "Recipe Inputs"
Cohesion: 0.09
Nodes (11): net.minecraft.world.item.crafting.RecipeInput, DistilleryRecipeInput, Override, FrackingPumpRecipeInput, Override, Override, MilkingStationRecipeInput, FluidRefineryRecipeInput (+3 more)

### Community 36 - "Radiator Model & Heating"
Cohesion: 0.12
Nodes (13): mezz.jei.api.IModPlugin, mezz.jei.api.JeiPlugin, mezz.jei.api.registration.IRecipeCatalystRegistration, mezz.jei.api.registration.IRecipeCategoryRegistration, mezz.jei.api.registration.IRecipeRegistration, LayerDefinition, RadiatorModel, FluidStack (+5 more)

### Community 37 - "Coating Recipe Category"
Cohesion: 0.12
Nodes (9): net.minecraft.core.NonNullList, CoatingRecipeCategory, Override, CoatingRecipe, Item, ItemStack, MapCodec, Override (+1 more)

### Community 38 - "Fluid Refinery Recipe"
Cohesion: 0.12
Nodes (8): net.neoforged.neoforge.common.crafting.SizedIngredient, FluidRefineryRecipe, FilteringBehaviour, Override, SizedIngredient, Serializer, FluidRefineryRecipeCategory, Override

### Community 39 - "Fracking Pump Assembly"
Cohesion: 0.13
Nodes (7): AssemblyResult, FrackingPumpOutletBlockEntity, AABB, BlockPos, CompoundTag, Override, Provider

### Community 40 - "Community 40"
Cohesion: 0.09
Nodes (14): net.minecraft.client.renderer.blockentity.BlockEntityRenderer, net.minecraft.client.renderer.entity.ItemRenderer, BrewersTapRenderer, Context, Override, DistilleryRenderer, Context, Override (+6 more)

### Community 41 - "Community 41"
Cohesion: 0.16
Nodes (4): CompoundTag, Override, Provider, MilkingStationBlockEntity

### Community 42 - "Community 42"
Cohesion: 0.13
Nodes (6): CompoundTag, FilteringBehaviour, Override, Provider, MechanicalFluidSieveBlockEntity, FilteringBehaviour

### Community 43 - "Community 43"
Cohesion: 0.11
Nodes (8): com.simibubi.create.content.logistics.depot.DepotBlock, CastingDepotBlock, Builder, Override, FrackingPumpProxyBlock, Override, GeyserBlock, Override

### Community 44 - "Community 44"
Cohesion: 0.12
Nodes (12): Decoration, Instance, ModifiableStructureInfo, net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder, net.minecraft.world.level.levelgen.structure.TerrainAdjustment, net.neoforged.neoforge.common.world.ModifiableStructureInfo, BlockPos, Deprecated (+4 more)

### Community 45 - "Community 45"
Cohesion: 0.11
Nodes (16): net.neoforged.neoforge.event.TagsUpdatedEvent, GelPropertiesManager, GelType, BLESSED, BOUNCY, CLEANSE, CONCRETE, CURSED (+8 more)

### Community 46 - "Community 46"
Cohesion: 0.14
Nodes (7): MapCodec, Override, MechanicalForgeMouldRecipe, Override, MechanicalForgeMouldRecipeCategory, Override, MechanicalForgeMouldRecipeInput

### Community 47 - "Community 47"
Cohesion: 0.11
Nodes (10): com.mojang.math.Axis, ForgeMouldCasingModel, LayerDefinition, ForgeMouldItemRenderer, Override, ForgeMouldPressModel, LayerDefinition, ForgeMouldRenderer (+2 more)

### Community 48 - "Community 48"
Cohesion: 0.15
Nodes (6): net.minecraft.network.syncher.EntityDataAccessor, net.minecraft.world.damagesource.DamageSource, org.joml.Quaternionf, Builder, Override, SportsBallEntity

### Community 49 - "Community 49"
Cohesion: 0.15
Nodes (7): com.mojang.blaze3d.vertex.PoseStack, FrackingPumpOutletItemRenderer, Override, FrackingPumpOutletModel, LayerDefinition, FrackingPumpRenderer, Override

### Community 50 - "Community 50"
Cohesion: 0.13
Nodes (11): CompoundTag, Override, Provider, PaintNozzleBlockEntity, displayName(), fromOrdinal(), next(), PaintNozzleFlowSpeed (+3 more)

### Community 51 - "Community 51"
Cohesion: 0.11
Nodes (11): com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer, BlenderBladeItemRenderer, Override, BlenderBladeRenderer, Context, Override, LayerDefinition, RefineryBlenderModel (+3 more)

### Community 52 - "Community 52"
Cohesion: 0.17
Nodes (5): net.minecraft.world.MenuProvider, FluidRefillStationBlockEntity, CompoundTag, Override, Provider

### Community 53 - "Community 53"
Cohesion: 0.15
Nodes (5): DistilleryRecipe, Override, SizedIngredient, DistilleryRecipeCategory, Override

### Community 54 - "Community 54"
Cohesion: 0.14
Nodes (9): com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour, com.simibubi.create.foundation.fluid.SmartFluidTank, net.minecraft.client.player.LocalPlayer, org.spongepowered.asm.mixin.injection.ModifyConstant, org.spongepowered.asm.mixin.injection.Redirect, PouredCementPlacement, LocalPlayerMixin, HosePulleyFluidHandlerMixin (+1 more)

### Community 55 - "Community 55"
Cohesion: 0.14
Nodes (8): MultifaceSpreader, net.minecraft.world.level.block.MultifaceBlock, net.minecraft.world.level.block.MultifaceSpreader, net.minecraft.world.level.BlockAndTintGetter, GelSplatterBlock, Builder, Override, RandomSource

### Community 56 - "Community 56"
Cohesion: 0.13
Nodes (6): net.minecraft.client.model.geom.builders.LayerDefinition, HosegunItemRenderer, Override, HosegunModel, FluidRefillStationLayers, SportsBallDebugModel

### Community 57 - "Community 57"
Cohesion: 0.13
Nodes (9): net.minecraft.world.item.component.ItemAttributeModifiers, net.minecraft.world.item.TooltipFlag, net.minecraft.world.item.TridentItem, Override, TooltipContext, IClientItemExtensions, Override, TooltipContext (+1 more)

### Community 58 - "Community 58"
Cohesion: 0.15
Nodes (5): org.jetbrains.annotations.NotNull, CombustionChamberBlock, Axis, Builder, Override

### Community 59 - "Community 59"
Cohesion: 0.13
Nodes (8): Override, MilkingStationItemRenderer, LayerDefinition, Override, MilkingStationModel, Context, Override, MilkingStationRenderer

### Community 60 - "Community 60"
Cohesion: 0.13
Nodes (6): net.minecraft.resources.ResourceKey, net.minecraft.world.damagesource.DamageType, GelTrackingPurgeScheduler, PurgeQueue, Post, ModDamageTypes

### Community 61 - "Community 61"
Cohesion: 0.14
Nodes (9): com.simibubi.create.content.logistics.depot.DepotBehaviour, com.simibubi.create.content.logistics.depot.DepotBlockEntity, net.neoforged.neoforge.items.IItemHandler, CastingDepotBlockEntity, ClientboundBlockEntityDataPacket, CompoundTag, Override, Provider (+1 more)

### Community 62 - "Community 62"
Cohesion: 0.20
Nodes (8): com.simibubi.create.foundation.gui.widget.IconButton, Key, net.createmod.catnip.gui.AbstractSimiScreen, net.minecraft.client.gui.components.EditBox, net.minecraft.client.gui.screens.inventory.MenuAccess, net.minecraft.client.KeyMapping, FluidRefillStationScreen, Override

### Community 63 - "Community 63"
Cohesion: 0.16
Nodes (6): HorizontalDirectionalBlock, BrewersTapBlock, Block, Builder, Override, RenderShape

### Community 64 - "Community 64"
Cohesion: 0.13
Nodes (11): ModelResourceLocation, ModifyBakingResult, net.neoforged.bus.api.SubscribeEvent, net.neoforged.neoforge.client.event.RegisterMenuScreensEvent, RegisterAdditional, RegisterRenderers, Load, ClientModEvents (+3 more)

### Community 65 - "Community 65"
Cohesion: 0.10
Nodes (12): net.minecraft.util.StringRepresentable, CoatingType, CONDUCTION, DURASTEEL, GLOOPY, LIQUIDLUCK, OBSIDIANITE, QUICKSILVER (+4 more)

### Community 66 - "Community 66"
Cohesion: 0.18
Nodes (4): BrewersTapRecipe, Override, BrewersTapRecipeCategory, Override

### Community 67 - "Community 67"
Cohesion: 0.18
Nodes (4): Override, MilkingStationRecipe, Override, MilkingStationRecipeCategory

### Community 68 - "Community 68"
Cohesion: 0.14
Nodes (7): FluidRefillStationCasingModel, FluidRefillStationItemRenderer, Override, FluidRefillStationRenderer, Context, Override, Pose

### Community 69 - "Community 69"
Cohesion: 0.16
Nodes (8): com.simibubi.create.foundation.block.WrenchableDirectionalBlock, EntityBlock, SimpleWaterloggedBlock, Block, Builder, DirectionalBlock, Override, RadiatorBlock

### Community 70 - "Community 70"
Cohesion: 0.17
Nodes (6): DirectionalBlock, Block, Builder, Override, RenderShape, PaintNozzleBlock

### Community 71 - "Community 71"
Cohesion: 0.13
Nodes (16): net.minecraft.tags.TagKey, net.minecraft.world.effect.MobEffectInstance, createEffect(), FlavourType, CHILLED, COSMIC, FRUIT, SWEET (+8 more)

### Community 72 - "Community 72"
Cohesion: 0.18
Nodes (10): com.simibubi.create.api.behaviour.display.DisplaySource, net.minecraft.core.component.DataComponentType, net.minecraft.world.inventory.MenuType, net.minecraft.world.item.CreativeModeTab, net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent, net.neoforged.neoforge.registries.DeferredHolder, net.neoforged.neoforge.registries.DeferredRegister, ModCreativeTab (+2 more)

### Community 73 - "Community 73"
Cohesion: 0.30
Nodes (7): com.simibubi.create.foundation.ponder.CreateSceneBuilder, net.createmod.ponder.api.element.ElementLink, net.createmod.ponder.api.element.EntityElement, net.minecraft.world.item.Items, net.minecraft.world.level.block.Blocks, TrackedBlob, ModFluids

### Community 74 - "Community 74"
Cohesion: 0.14
Nodes (8): net.minecraft.client.model.geom.ModelLayerLocation, CastingDepotLayers, DistilleryModel, LayerDefinition, ForgeMouldLayers, FrackingPumpLayers, RefineryLayers, MechanicalSieveLayers

### Community 75 - "Community 75"
Cohesion: 0.15
Nodes (9): net.minecraft.client.model.Model, Override, PlushieItemRenderer, LayerDefinition, Override, PlushieModel, Context, Override (+1 more)

### Community 76 - "Community 76"
Cohesion: 0.14
Nodes (11): net.minecraft.world.InteractionResultHolder, net.minecraft.world.item.context.UseOnContext, net.minecraft.world.item.Item, net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack, SimpleFluidContent, FuelTankItemFluidHandler, MouldItem, IClientItemExtensions (+3 more)

### Community 77 - "Community 77"
Cohesion: 0.23
Nodes (11): com.simibubi.create.foundation.model.BakedModelWrapperWithData, net.minecraft.client.renderer.block.model.BakedQuad, net.minecraft.client.renderer.RenderType, net.minecraft.client.resources.model.BakedModel, net.minecraft.client.resources.model.ModelResourceLocation, net.neoforged.neoforge.client.ChunkRenderTypeSet, net.neoforged.neoforge.client.model.data.ModelData, net.neoforged.neoforge.client.model.data.ModelProperty (+3 more)

### Community 78 - "Community 78"
Cohesion: 0.15
Nodes (9): net.neoforged.fml.common.EventBusSubscriber, net.neoforged.neoforge.data.event.GatherDataEvent, net.neoforged.neoforge.event.entity.player.ItemTooltipEvent, BreakEvent, ModGelTrackingEvents, RegisterLayerDefinitions, PlungerClientEvents, ModDataGenerators (+1 more)

### Community 79 - "Community 79"
Cohesion: 0.21
Nodes (5): Vector3f, Quaternionf, GelPonders, Vec3, PonderGelSprayHelper

### Community 80 - "Community 80"
Cohesion: 0.21
Nodes (4): Override, MechanicalSieveRecipe, Override, MechanicalSieveRecipeCategory

### Community 81 - "Community 81"
Cohesion: 0.20
Nodes (7): com.simibubi.create.content.fluids.pump.PumpBlock, com.simibubi.create.content.fluids.pump.PumpBlockEntity, net.minecraft.world.level.block.state.properties.BooleanProperty, AdvancedPumpBlock, Builder, Override, SuppressWarnings

### Community 82 - "Community 82"
Cohesion: 0.13
Nodes (8): ComputeFogColor, net.neoforged.fml.event.lifecycle.FMLClientSetupEvent, net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent, RegisterItemDecorationsEvent, RenderFog, Block, Item, ModClientEvents

### Community 83 - "Community 83"
Cohesion: 0.23
Nodes (5): net.minecraft.client.resources.model.Material, FluidRefineryRenderer, HeatQuad, AABB, Override

### Community 84 - "Community 84"
Cohesion: 0.20
Nodes (7): net.minecraft.world.level.material.FluidState, net.neoforged.neoforge.fluids.BaseFlowingFluid, Flowing, GeneralizedFlowingFluid, Builder, Override, Source

### Community 85 - "Community 85"
Cohesion: 0.19
Nodes (5): GeyserBlockEntity, ClientboundBlockEntityDataPacket, CompoundTag, Override, Provider

### Community 86 - "Community 86"
Cohesion: 0.19
Nodes (3): FrackingPumpProxyBlockEntity, Override, Provider

### Community 87 - "Community 87"
Cohesion: 0.19
Nodes (6): Finish, net.neoforged.neoforge.event.entity.player.AttackEntityEvent, net.neoforged.neoforge.event.ItemAttributeModifierEvent, BreakEvent, Post, ModToolEvents

### Community 88 - "Community 88"
Cohesion: 0.17
Nodes (15): net.createmod.catnip.gui.element.ScreenElement, net.createmod.catnip.gui.TextureSheetSegment, getHeight(), getLocation(), getStartX(), getStartY(), getWidth(), Override (+7 more)

### Community 89 - "Community 89"
Cohesion: 0.22
Nodes (6): net.minecraft.world.entity.player.Inventory, net.minecraft.world.inventory.AbstractContainerMenu, net.minecraft.world.inventory.ContainerLevelAccess, FluidRefillStationMenu, ItemStack, Override

### Community 90 - "Community 90"
Cohesion: 0.17
Nodes (6): net.neoforged.neoforge.client.event.RenderArmEvent, HosegunArmPoses, Pre, HosegunClientEvents, Pre, RegisterLayerDefinitions

### Community 91 - "Community 91"
Cohesion: 0.22
Nodes (3): net.neoforged.neoforge.fluids.capability.IFluidHandlerItem, FluidRefillStationInteractions, FluidStack

### Community 92 - "Community 92"
Cohesion: 0.17
Nodes (6): CombustionChamberItemRenderer, Override, CombustionChamberModel, LayerDefinition, Override, Context

### Community 93 - "Community 93"
Cohesion: 0.17
Nodes (5): Override, PlungerItemRenderer, PlungerModel, Context, Override

### Community 94 - "Community 94"
Cohesion: 0.22
Nodes (4): BlockEntityBehaviour, Override, Provider, RefineryKineticProxyBlockEntity

### Community 95 - "Community 95"
Cohesion: 0.22
Nodes (9): net.minecraft.world.item.BucketItem, net.minecraft.world.level.block.LiquidBlock, net.minecraft.world.level.material.FlowingFluid, net.neoforged.neoforge.registries.DeferredBlock, net.neoforged.neoforge.registries.DeferredItem, Builder, Override, PouredCementBlock (+1 more)

### Community 96 - "Community 96"
Cohesion: 0.26
Nodes (6): net.minecraft.world.phys.AABB, BlenderBladeBlockEntity, AABB, Axis, Override, Vec3

### Community 97 - "Community 97"
Cohesion: 0.23
Nodes (3): Override, Provider, RefineryProxyBlockEntity

### Community 98 - "Community 98"
Cohesion: 0.14
Nodes (11): BucketItem, FluidGroup, ALLOYED, CARBORAX, CATALYSED, CONCRETE, DRINK, PAINT (+3 more)

### Community 99 - "Community 99"
Cohesion: 0.23
Nodes (3): DistilleryBlock, Builder, Override

### Community 101 - "Community 101"
Cohesion: 0.26
Nodes (5): net.minecraft.world.item.DyeColor, org.spongepowered.asm.mixin.gen.Invoker, PaintGelCollarHelper, CatCollarInvoker, WolfCollarInvoker

### Community 102 - "Community 102"
Cohesion: 0.22
Nodes (7): net.minecraft.world.level.levelgen.structure.StructureType, ModStructureTypes, GenerationStub, Override, StructureSettings, Types, NetherSurfaceJigsawStructure

### Community 103 - "Community 103"
Cohesion: 0.19
Nodes (5): CastingDepotItemRenderer, Override, CastingDepotModel, LayerDefinition, Context

### Community 104 - "Community 104"
Cohesion: 0.26
Nodes (5): net.minecraft.sounds.SoundEvent, net.minecraft.world.item.UseAnim, DrinkItem, Override, PlungerSounds

### Community 105 - "Community 105"
Cohesion: 0.23
Nodes (6): net.neoforged.neoforge.fluids.FluidType, GeneralizedFluidType, IClientFluidTypeExtensions, Override, SuppressWarnings, ModFluidTypes

### Community 106 - "Community 106"
Cohesion: 0.30
Nodes (4): FuelTankBlockEntity, CompoundTag, Override, Provider

### Community 107 - "Community 107"
Cohesion: 0.27
Nodes (3): Override, RenderType, TintedVertexConsumer

### Community 108 - "Community 108"
Cohesion: 0.35
Nodes (5): net.createmod.ponder.api.registration.PonderPlugin, net.createmod.ponder.api.registration.PonderSceneRegistrationHelper, net.createmod.ponder.api.registration.PonderTagRegistrationHelper, Override, ModPonders

### Community 109 - "Community 109"
Cohesion: 0.31
Nodes (4): net.minecraft.world.level.levelgen.heightproviders.HeightProviderType, net.minecraft.world.level.levelgen.VerticalAnchor, net.minecraft.world.level.levelgen.WorldGenerationContext, ConstantHeight

### Community 110 - "Community 110"
Cohesion: 0.22
Nodes (11): Geyser, bucketExcavationRecipe, Bucket Excavator, Mineral Deposit Node, Crystal Fissure Bud, Drill Pylon, DrillPylonRecipe, GLARE / Lux Network (+3 more)

### Community 111 - "Community 111"
Cohesion: 0.18
Nodes (10): DistilleryErrorCode, HEAT, INPUT_FLUID, INPUT_ITEM, NO_ERROR, NO_FILTER_MATCH, NO_RECIPE, OUTPUT_SPACE (+2 more)

### Community 112 - "Community 112"
Cohesion: 0.36
Nodes (6): com.simibubi.create.content.redstone.displayLink.DisplayLinkContext, com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource, com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats, net.minecraft.network.chat.MutableComponent, FluidRefillStationDisplaySource, Override

### Community 113 - "Community 113"
Cohesion: 0.27
Nodes (10): Blender Blade, Blender Entity Push & Contact Damage, KineticBlockEntity (Create), RefineryStructureHelper, Controller/Proxy Multiblock Pattern, Fluid Refinery Implementation Plan, Segmented BlockEntityRenderer, Refinery Catalysation Progression (+2 more)

### Community 114 - "Community 114"
Cohesion: 0.29
Nodes (4): GeyserRenderer, Context, Override, Pose

### Community 115 - "Community 115"
Cohesion: 0.31
Nodes (4): net.minecraft.data.loot.BlockLootSubProvider, Override, Provider, ModBlockLootProvider

### Community 116 - "Community 116"
Cohesion: 0.25
Nodes (9): Brewer's Tap, BrewingTapRecipe, Flavour Data Component, Coating, Coating Integrity (secondary durability), Coating Variants, Casting Depot, Mechanical Forge Mould (+1 more)

### Community 117 - "Community 117"
Cohesion: 0.36
Nodes (5): net.minecraft.data.PackOutput, net.neoforged.neoforge.client.model.generators.ItemModelProvider, net.neoforged.neoforge.common.data.ExistingFileHelper, Override, ModItemModelProvider

### Community 118 - "Community 118"
Cohesion: 0.32
Nodes (3): net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent, net.neoforged.neoforge.network.handling.IPayloadContext, ModNetworking

### Community 119 - "Community 119"
Cohesion: 0.29
Nodes (8): Carbonox Processing, Combustion Chamber, Fuel Tank, Alloyed Fluids, Carborax Fluids, Catalysed Fluids, Fluid Properties, Purified Fluids

### Community 121 - "Community 121"
Cohesion: 0.33
Nodes (3): net.createmod.catnip.gui.widget.AbstractSimiWidget, Override, RefillStationHoverButton

### Community 122 - "Community 122"
Cohesion: 0.43
Nodes (3): net.neoforged.bus.api.IEventBus, net.neoforged.fml.ModContainer, ModRegistries

### Community 123 - "Community 123"
Cohesion: 0.29
Nodes (7): Advanced Pump, Create (mod), Factory-game inspiration (Satisfactory/Factorio), JEI, Milking Station, NeoForge 1.21.1, Resourceful Refinement

### Community 124 - "Community 124"
Cohesion: 0.40
Nodes (5): DoubleValue, IntValue, net.neoforged.neoforge.common.ModConfigSpec, Builder, ServerConfig

### Community 126 - "Community 126"
Cohesion: 0.47
Nodes (4): net.minecraft.core.dispenser.BlockSource, net.minecraft.core.dispenser.DefaultDispenseItemBehavior, Override, SportsBallDispenseBehavior

### Community 127 - "Community 127"
Cohesion: 0.53
Nodes (4): net.minecraft.network.protocol.common.custom.CustomPacketPayload, Override, SetRefillStationTrackingIdPayload, Type

### Community 131 - "Community 131"
Cohesion: 0.40
Nodes (5): Hosegun, Paint Nozzle, Create Display Link, Fluid Refill Station, Gel Tracking Network

### Community 132 - "Community 132"
Cohesion: 0.40
Nodes (5): Mechanical Fluid Sieve, Ferrous Crystal, Fluid Processing Recipes, Molten Crimsite, Raw Molten Minerals

### Community 133 - "Community 133"
Cohesion: 0.40
Nodes (5): StandardProcessingRecipe (Create), ExtendedHeatCondition, Create HeatCondition, HeatUtilities, fluid_refinery recipe type

### Community 134 - "Community 134"
Cohesion: 0.40
Nodes (3): IClientItemExtensions, Override, TooltipContext

### Community 135 - "Community 135"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 137 - "Community 137"
Cohesion: 0.67
Nodes (3): Conveyor Belt, Create Mechanical Belt, Conveyor Rotator

### Community 138 - "Community 138"
Cohesion: 0.67
Nodes (3): Geyser Block, Fracking Pylon/Pump, Molten Mineral Fluids

### Community 139 - "Community 139"
Cohesion: 0.67
Nodes (3): Mechanical Sieve, mechanical_fluid_sieve Recipe Type, Sieve Stack Multiblock

## Knowledge Gaps
- **109 isolated node(s):** `FRUIT`, `SWEET`, `VEG`, `YEAST`, `CHILLED` (+104 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **11 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CombustionChamberBlockEntity` connect `Combustion Chamber Logic` to `Block Entity Files`, `Machine Blocks (Interaction)`, `Block Entity Renderers`, `Community 71`, `Block Entity Constructors`, `Community 58`, `Gel Fluid Tint Colours`, `Community 61`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **Why does `ResourcefulRefinementMain` connect `Block Entity Renderers` to `Machine Block Items`, `JEI Recipe Categories`, `Entity Model Layers`, `Fracking Pump Outlet Block`, `Recipe Serializers`, `Distillery Logic`, `Jigsaw Structure Worldgen`, `Hosegun Gloopy System`, `Item & Entity Renderers`, `Block Entity Files`, `Milking Station Seat Entity`, `Radiator Model & Heating`, `Community 43`, `Community 56`, `Community 60`, `Community 61`, `Community 64`, `Community 71`, `Community 72`, `Community 74`, `Community 75`, `Community 77`, `Community 78`, `Community 87`, `Community 88`, `Community 90`, `Community 95`, `Community 102`, `Community 105`, `Community 108`, `Community 117`, `Community 118`, `Community 122`, `Community 127`?**
  _High betweenness centrality (0.030) - this node is a cross-community bridge._
- **Why does `RefineryAccessPortBlockEntity` connect `Refinery Access Port` to `Combustion Chamber Logic`, `Block Entity Files`, `Community 97`, `Machine Blocks (Interaction)`, `Block Entity Renderers`, `Fluid Refinery Recipe`, `Community 73`, `Radiator Heat Logic`, `Mechanical Sieve Models`, `Block Entity Constructors`, `Community 83`, `Milking Station Block`, `Community 94`, `Community 61`, `Refinery Kinetic Proxy`, `Item & Entity Renderers`?**
  _High betweenness centrality (0.030) - this node is a cross-community bridge._
- **What connects `FRUIT`, `SWEET`, `VEG` to the rest of the system?**
  _109 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Combustion Chamber Logic` be split into smaller, more focused modules?**
  _Cohesion score 0.05747126436781609 - nodes in this community are weakly interconnected._
- **Should `Vanilla Chunk Generation` be split into smaller, more focused modules?**
  _Cohesion score 0.0741745816372682 - nodes in this community are weakly interconnected._
- **Should `Machine Block Items` be split into smaller, more focused modules?**
  _Cohesion score 0.06433566433566433 - nodes in this community are weakly interconnected._