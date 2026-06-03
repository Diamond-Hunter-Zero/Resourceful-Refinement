package com.resourceful_refinement.content.fracking_pump;

import com.resourceful_refinement.registry.ModStressValues;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Blocks;
import com.simibubi.create.AllBlocks;
import com.resourceful_refinement.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import com.resourceful_refinement.content.fracking_pump.recipe.FrackingPumpRecipe;
import com.resourceful_refinement.content.fracking_pump.recipe.FrackingPumpRecipeInput;
import com.resourceful_refinement.content.geyser.GeyserBlockEntity;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import java.util.List;

public class FrackingPumpOutletBlockEntity extends KineticBlockEntity implements IBE<FrackingPumpOutletBlockEntity>, IHaveGoggleInformation {

    public record AssemblyResult(boolean success, String reason) {
        public static AssemblyResult fail(String reason) {
            return new AssemblyResult(false, reason);
        }

        public static AssemblyResult ok() {
            return new AssemblyResult(true, "");
        }
    }

    public static final int TANK_CAPACITY = 4000;

    public final FluidTank inputTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };

    public final FluidTank outputTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };

    private boolean assembled = false;
    private int h_pole = 0;
    private int h_ring = 0;
    public int renderFalsePylon = 0;

    public int timer = 0;
    private FrackingPumpRecipe lastRecipe;
    private ResourceLocation displayedRecipeId;
    private Block lastSourceBlock;
    private Fluid lastGeyserFluid;

    public static final float DROP_NORMALISED_DURATION = 0.065f;
    public static final float DROP_CYCLE_DURATION = 96f;
    public static final float DROP_PAUSE_NORMALISED_DURATION = 0.125f;


    public FrackingPumpOutletBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    public void tick() {
        super.tick();      

        if (level == null || level.isClientSide) return;
        if (!assembled) return;

        Block sourceBlock = level.getBlockState(worldPosition.below()).getBlock();
        Fluid geyserFluid = resolveGeyserFluid(worldPosition.below());

        if (sourceBlock != lastSourceBlock || lastGeyserFluid != geyserFluid) {
            lastSourceBlock = sourceBlock;
            lastGeyserFluid = geyserFluid;
            lastRecipe = null; // force recipe re-lookup
            updateDisplayedRecipeFromSource(sourceBlock, geyserFluid);
        }

        // 1. RPM Validation
        int rpmThreshold = getRequiredRPM();
        if (Math.abs(getSpeed()) < rpmThreshold) return;

        // 2. Fetch Recipe
        FrackingPumpRecipeInput input = new FrackingPumpRecipeInput(sourceBlock, inputTank.getFluid(), geyserFluid);

        if (lastRecipe == null || !lastRecipe.matches(input, level)) {
            var recipe = level.getRecipeManager()
                .getRecipeFor(com.resourceful_refinement.registry.ModRecipeTypes.FRACKING_PUMP_TYPE.get(), input, level);
            if (recipe.isPresent()) {
                lastRecipe = recipe.get().value();
                if (!recipe.get().id().equals(displayedRecipeId)) {
                    displayedRecipeId = recipe.get().id();
                    syncData();
                }
            } else {
                lastRecipe = null;
            }
        }

        // 3. Processing
        if (lastRecipe != null) {
            float processingRate = 1.0f + (h_ring - 1) * 0.25f;
            int baseDuration = lastRecipe.getProcessingDuration();
            if (baseDuration <= 0) baseDuration = 100; // Default 100 ticks
            
            int duration = (int) (baseDuration / processingRate);
            if (duration < 1) duration = 1;

            if (timer < duration) {
                timer++;
            }
            
            if (timer >= duration) {
                // Ensure space in output tank
                if (!lastRecipe.getFluidResults().isEmpty()) {
                    net.neoforged.neoforge.fluids.FluidStack output = lastRecipe.getFluidResults().get(0);
                    int filled = outputTank.fill(output.copy(), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);
                    if (filled < output.getAmount()) {
                        return; // Wait for space
                    }
                }

                // Consume input
                if (!lastRecipe.getFluidIngredients().isEmpty()) {
                    inputTank.drain(lastRecipe.getFluidIngredients().get(0).amount(), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                }
                
                // Produce output
                if (!lastRecipe.getFluidResults().isEmpty()) {
                    net.neoforged.neoforge.fluids.FluidStack output = lastRecipe.getFluidResults().get(0);
                    outputTank.fill(output.copy(), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                }
                
                timer = 0;
                syncData();
            }
        } else {
            timer = 0;
        }

        // 4. Animation Events (Sound & Particles)
        if (level != null && !level.isClientSide && Math.abs(getSpeed()) >= rpmThreshold) {
            long time = level.getGameTime();
            int cyclePos = (int) (time % DROP_CYCLE_DURATION);
            int hitPos = (int) (DROP_NORMALISED_DURATION * DROP_CYCLE_DURATION);
            
            if (cyclePos == hitPos) {
                // Play Sound
                level.playSound(null, worldPosition, SoundEvents.ANVIL_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 0.75f, 0.5f);
                level.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS, 0.7f, 0.8f);
                
                // Spawn Particles around the outlet
                for (int i = 0; i < 8; i++) {
                    double px = worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5)*1.5f;
                    double py = worldPosition.getY() + 1.4f + level.random.nextDouble()*0.25f;
                    double pz = worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5)*1.5f;
                    ((net.minecraft.server.level.ServerLevel)level).sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, px, py, pz, 1, 0, 0.1, 0, 0.02);
                }
            }
        }
    }

    private void updateDisplayedRecipeFromSource(Block sourceBlock, Fluid geyserFluid) {
        if (level == null || level.isClientSide) return;

        RecipeHolder<FrackingPumpRecipe> holder = findRecipeHolderForSource(sourceBlock, geyserFluid);
        if (holder != null) {
            if (!holder.id().equals(displayedRecipeId)) {
                displayedRecipeId = holder.id();
                syncData();
            }
            return;
        }

        if (displayedRecipeId != null) {
            displayedRecipeId = null;
            syncData();
        }
    }

    private RecipeHolder<FrackingPumpRecipe> findRecipeHolderForSource(Block sourceBlock, Fluid geyserFluid) {
        if (level == null) return null;

        for (var holder : level.getRecipeManager().getAllRecipesFor(com.resourceful_refinement.registry.ModRecipeTypes.FRACKING_PUMP_TYPE.get())) {
            FrackingPumpRecipe recipe = holder.value();
            if (recipe.getSourceBlock() != sourceBlock) continue;

            if (recipe.requiresGeyserFluid()) {
                if (geyserFluid == null || geyserFluid == Fluids.EMPTY) continue;
                if (recipe.getSourceFluid() != geyserFluid) continue;
            }

            return holder;
        }
        return null;
    }

    /**
     * If the block at {@code pos} is a Geyser, returns the fluid it stores.
     * Otherwise returns {@link Fluids#EMPTY}.
     */
    private Fluid resolveGeyserFluid(BlockPos pos) {
        if (level == null) return Fluids.EMPTY;
        if (level.getBlockEntity(pos) instanceof GeyserBlockEntity geyser) {
            Fluid f = geyser.getAssociatedFluid();
            return f != null ? f : Fluids.EMPTY;
        }
        return Fluids.EMPTY;
    }

    public int getProcessingSpeed() {
        return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
    }

    public int getRequiredRPM() {return 64 + (h_ring - 1) * 64;}

    // -------------------------------------------------------------------------
    // Multiblock Assembly
    // -------------------------------------------------------------------------
    public boolean isAssembled() {
        return assembled;
    }

    public int getPoleHeight() {
        return h_pole;
    }

    public int getRingHeight() {
        return h_ring;
    }

    public AssemblyResult tryAssemble() {
        // 1. Check we're not already assembled!
        if (isAssembled()) return AssemblyResult.ok();

        // 2. Check casings
        for (int i = 1; i <= 2; i++) {
            BlockState state = level.getBlockState(worldPosition.above(i));
            if (!AllBlocks.BRASS_CASING.has(state)) {
                return AssemblyResult.fail("Missing brass casing (layer " + i + " above outlet)");
            }
        }

        // 3. Count girders
        int poleCount = 0;
        BlockPos currentPos = worldPosition.above(3);
        while (AllBlocks.METAL_GIRDER.has(level.getBlockState(currentPos))) {
            poleCount++;
            currentPos = currentPos.above();
            if (poleCount > 8) {
                return AssemblyResult.fail("Girder pole too tall (maximum 8 blocks)");
            }
        }

        if (poleCount == 0) {
            return AssemblyResult.fail("Missing metal girder pole above casings");
        }
        if (poleCount % 2 != 0) {
            return AssemblyResult.fail("Girder pole height must be even (counterweight must be half the pole height)");
        }

        // 4. Check cap
        BlockState capState = level.getBlockState(currentPos);
        if (!AllBlocks.ANDESITE_ALLOY_BLOCK.has(capState)) {
            return AssemblyResult.fail("Missing andesite alloy block cap atop girder pole");
        }

        // 5. Check rings
        int ringCount = poleCount / 2;
        int poleTopY = currentPos.getY() - 1;
        int ringTopY = poleTopY;
        int ringBottomY = ringTopY - ringCount + 1;
        
        for (int y = ringBottomY; y <= ringTopY; y++) {
            int ringLayer = y - ringBottomY + 1;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue; // center hole
                    BlockPos ringPos = new BlockPos(worldPosition.getX() + x, y, worldPosition.getZ() + z);
                    if (!AllBlocks.INDUSTRIAL_IRON_BLOCK.has(level.getBlockState(ringPos))) {
                        return AssemblyResult.fail("Counterweight ring incomplete (layer " + ringLayer + " of " + ringCount + ")");
                    }
                }
            }
        }

        // 6. Assembly successful! Store data and convert blocks to proxies
        this.assembled = true;
        this.h_pole = poleCount;
        this.h_ring = ringCount;

        // Convert casings
        convertToProxy(worldPosition.above(1));
        convertToProxy(worldPosition.above(2));

        // Convert girders
        for (int i = 0; i < h_pole; i++) {
            convertToProxy(worldPosition.above(3 + i));
        }

        // Convert cap
        convertToProxy(currentPos);

        // Convert rings
        for (int y = ringBottomY; y <= ringTopY; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    BlockPos ringPos = new BlockPos(worldPosition.getX() + x, y, worldPosition.getZ() + z);
                    convertToProxy(ringPos);
                }
            }
        }

        syncData();
        Block sourceBlock = level.getBlockState(worldPosition.below()).getBlock();
        Fluid geyserFluid = resolveGeyserFluid(worldPosition.below());
        lastSourceBlock = sourceBlock;
        lastGeyserFluid = geyserFluid;
        updateDisplayedRecipeFromSource(sourceBlock, geyserFluid);
        notifyFluidInterfaceChanged();
        return AssemblyResult.ok();
    }

    private void convertToProxy(BlockPos pos) {
        BlockState oldState = level.getBlockState(pos);
        level.setBlock(pos, ModBlocks.FRACKING_PUMP_PROXY.get().defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof FrackingPumpProxyBlockEntity proxy) {
            proxy.setControllerData(worldPosition, pos.getX() - worldPosition.getX(), pos.getY() - worldPosition.getY(), pos.getZ() - worldPosition.getZ());
            proxy.setStoredState(oldState);
        }
    }

    public void disassemble() {
        if (!isAssembled()) return;

        this.assembled = false;
        
        // Restore blocks
        // Casings
        restoreFromProxy(worldPosition.above(1));
        restoreFromProxy(worldPosition.above(2));

        // Girders
        for (int i = 0; i < h_pole; i++) {
            restoreFromProxy(worldPosition.above(3 + i));
        }

        // Cap
        restoreFromProxy(worldPosition.above(3 + h_pole));

        // Rings
        int ringTopY = worldPosition.getY() + 2 + h_pole;
        int ringBottomY = ringTopY - h_ring + 1;
        
        for (int y = ringBottomY; y <= ringTopY; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    BlockPos ringPos = new BlockPos(worldPosition.getX() + x, y, worldPosition.getZ() + z);
                    restoreFromProxy(ringPos);
                }
            }
        }

        this.h_pole = 0;
        this.h_ring = 0;
        syncData();
        notifyFluidInterfaceChanged();
    }

    private void restoreFromProxy(BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof FrackingPumpProxyBlockEntity proxy) {
            BlockState stored = proxy.getStoredState();
            level.removeBlockEntity(pos);
            if (stored != null && !stored.isAir()) {
                level.setBlock(pos, stored, 3);
            } else {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        } else if (level.getBlockState(pos).is(ModBlocks.FRACKING_PUMP_PROXY.get())) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    // -------------------------------------------------------------------------
    // Data Management
    // -------------------------------------------------------------------------
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        tag.put("InputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("OutputTank", outputTank.writeToNBT(registries, new CompoundTag()));
        tag.putBoolean("Assembled", assembled);
        tag.putInt("HPole", h_pole);
        tag.putInt("HRing", h_ring);
        tag.putInt("Timer", timer);
        tag.putInt("RenderFalsePylon", renderFalsePylon);
        if (displayedRecipeId != null) {
            tag.putString("DisplayedRecipe", displayedRecipeId.toString());
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        inputTank.readFromNBT(registries, tag.getCompound("InputTank"));
        outputTank.readFromNBT(registries, tag.getCompound("OutputTank"));
        assembled = tag.getBoolean("Assembled");
        h_pole = tag.getInt("HPole");
        h_ring = tag.getInt("HRing");
        timer = tag.getInt("Timer");
        renderFalsePylon = tag.getInt("RenderFalsePylon");
        if (tag.contains("DisplayedRecipe")) {
            displayedRecipeId = ResourceLocation.tryParse(tag.getString("DisplayedRecipe"));
        } else {
            displayedRecipeId = null;
        }
    }

    // -------------------------------------------------------------------------
    // Network sync (for client-side rendering)
    // -------------------------------------------------------------------------
    private void syncData() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /** Re-evaluate fluid capabilities and tell neighbouring pipes to reconnect. */
    private void notifyFluidInterfaceChanged() {
        if (level == null || level.isClientSide) return;
        level.invalidateCapabilities(worldPosition);
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (!assembled) return super.getRenderBoundingBox();
        // Expand to encompass the whole pump (Y up to 3 + h_pole, X/Z by 1)
        return new AABB(worldPosition).expandTowards(0, 3 + h_pole, 0).inflate(1, 0, 1);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("     Fracking Performance:"));
        if (!assembled) {
            tooltip.add(Component.literal("§7Right-click to assemble when"));
            tooltip.add(Component.literal("§7valid structure is ready."));
            return true;
        }

        int rpmThreshold = getRequiredRPM();
        float speed = Math.abs(getSpeed());

        if (speed < rpmThreshold) {
            tooltip.add(Component.literal("     §cRequired RPM: §r" + rpmThreshold));
        } else {
            tooltip.add(Component.literal("     §5Current RPM: §r" + (int)speed));
        }
        tooltip.add(Component.literal("     §b" + (int)(speed * ModStressValues.FRACKING_STRESS) + "su §8at current speed"));
        tooltip.add(Component.literal(""));

        FrackingPumpRecipe recipeToDisplay = lastRecipe;
        if (recipeToDisplay == null && displayedRecipeId != null && level != null) {
            var holder = level.getRecipeManager().byKey(displayedRecipeId);
            if (holder.isPresent() && holder.get().value() instanceof FrackingPumpRecipe fpr) {
                recipeToDisplay = fpr;
            }
        }
        if (recipeToDisplay == null && level != null) {
            Block sourceBlock = level.getBlockState(worldPosition.below()).getBlock();
            Fluid geyserFluid = resolveGeyserFluid(worldPosition.below());
            RecipeHolder<FrackingPumpRecipe> holder = findRecipeHolderForSource(sourceBlock, geyserFluid);
            if (holder != null) {
                recipeToDisplay = holder.value();
            }
        }

        if (recipeToDisplay != null) {
            float processingRate = 1.0f + (h_ring - 1) * 0.25f;
            int baseDuration = recipeToDisplay.getProcessingDuration();
            if (baseDuration <= 0) baseDuration = 100;
            int duration = (int) (baseDuration / processingRate);
            if (duration < 1) duration = 1;

            // Input
            if (!recipeToDisplay.getFluidIngredients().isEmpty()) {
                var ingredient = recipeToDisplay.getFluidIngredients().get(0);
                float rate = ((float)ingredient.amount() / duration) * 20f;
                
                // Get fluid name from ingredient if tank is empty or doesn't match
                String fluidName = "Required Fluid";
                if (!inputTank.isEmpty() && ingredient.test(inputTank.getFluid())) {
                    fluidName = inputTank.getFluid().getHoverName().getString();
                } else {
                    var matching = ingredient.ingredient().getStacks();
                    if (matching.length > 0) {
                        fluidName = matching[0].getHoverName().getString();
                    }
                }

                boolean hasSufficientIntake = !inputTank.isEmpty()
                    && ingredient.test(inputTank.getFluid())
                    && inputTank.getFluidAmount() >= ingredient.amount();
                String intakeRateText = String.format("%.0f", rate) + "mB/s (" + fluidName + ")";
                tooltip.add(Component.literal("§9Intake: " + (hasSufficientIntake ? "§r" : "§c") + intakeRateText));
            }

            // Output
            if (!recipeToDisplay.getFluidResults().isEmpty()) {
                var result = recipeToDisplay.getFluidResults().get(0);
                float rate = ((float)result.getAmount() / duration) * 20f;
                tooltip.add(Component.literal("§6Outtake: §r" + String.format("%.0f", rate) + "mB/s (" + result.getHoverName().getString() + ")"));
            }
        } else {
            tooltip.add(Component.literal("§cMust be assembled on top of a valid Geyser block."));
        }

        return true;
    }

    @Override
    public Class<FrackingPumpOutletBlockEntity> getBlockEntityClass() {
        return FrackingPumpOutletBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FrackingPumpOutletBlockEntity> getBlockEntityType() {
        return com.resourceful_refinement.registry.ModBlockEntities.FRACKING_PUMP_OUTLET_BE.get();
    }

    public void setFalseRenderingLevel(int renderLevel)
    {
        renderFalsePylon = renderLevel;
        sendData();
    }
}
