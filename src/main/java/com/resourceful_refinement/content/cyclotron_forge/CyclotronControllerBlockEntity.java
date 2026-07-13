package com.resourceful_refinement.content.cyclotron_forge;

import com.resourceful_refinement.content.glare.DimensionalNodePos;
import com.resourceful_refinement.content.glare.GlareOperationStatus;
import com.resourceful_refinement.content.glare.GlareLuxCalculator;
import com.resourceful_refinement.content.glare.GlareService;
import com.resourceful_refinement.content.glare.IGlareNode;
import com.resourceful_refinement.content.glare.IGlareReceiver;
import com.resourceful_refinement.content.glare.lux.LuxTransceiverBlockEntity;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshot;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshotProvider;
import com.resourceful_refinement.content.cyclotron_forge.recipe.CyclotronForgeRecipe;
import com.resourceful_refinement.content.cyclotron_forge.recipe.CyclotronForgeRecipeInput;
import com.resourceful_refinement.content.research.ResearchRecipeGate;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CyclotronControllerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IGlareNode, IGlareReceiver, GlareNetworkSnapshotProvider {
    public record AssemblyResult(boolean success, String reason) {
        public static AssemblyResult ok() {
            return new AssemblyResult(true, "");
        }

        public static AssemblyResult fail(String reason) {
            return new AssemblyResult(false, reason);
        }
    }

    public static final int MAX_TOTAL_LENGTH = 16;
    public static final int MIN_COIL_LENGTH = 1;
    public static final int MAX_COIL_LENGTH = MAX_TOTAL_LENGTH - 2;
    public static final int INPUT_TANK_CAPACITY = 8000;
    public static final int OUTPUT_TANK_CAPACITY = 8000;
    public static final int ITEM_INPUT_SLOTS = 2;
    public static final int ITEM_OUTPUT_SLOTS = 4;

    private boolean assembled;
    private int coilLength;
    private Direction facing = Direction.NORTH;
    private int allocatedLux;
    private int craftingProgress;
    private UUID networkId;
    private GlareOperationStatus operationStatus = GlareOperationStatus.ONLINE;
    private int syncedLuxCapacity;
    private int syncedLuxAllocated;
    private boolean syncedOverloaded;
    private int[] syncedLuxHistory = new int[0];
    private CyclotronForgeRecipe lastRecipe;
    private ResourceLocation displayedRecipeId;
    private String processErrorString = "";
    private boolean isProcessing = false;
    private CyclotronKineticProxyBlockEntity kineticProxy;

    public final ItemStackHandler inputInv = new ItemStackHandler(ITEM_INPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            syncData();
        }
    };

    public final ItemStackHandler outputInv = new ItemStackHandler(ITEM_OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            syncData();
        }
    };

    public final FluidTank inputTankA = new FluidTank(INPUT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };

    public final FluidTank inputTankB = new FluidTank(INPUT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };

    public final FluidTank outputTank = new FluidTank(OUTPUT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };

    private final IItemHandler inputSlotA = new SingleSlotInputHandler(0);
    private final IItemHandler inputSlotB = new SingleSlotInputHandler(1);
    private final IItemHandler outputItems = new OutputItemHandler();
    private final IFluidHandler inputTankAHandler = new FillOnlyFluidHandler(inputTankA);
    private final IFluidHandler inputTankBHandler = new FillOnlyFluidHandler(inputTankB);
    private final IFluidHandler outputTankHandler = new DrainOnlyFluidHandler(outputTank);

    public CyclotronControllerBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CYCLOTRON_CONTROLLER_BE.get(), pos, state);
    }

    public CyclotronControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;
        isProcessing = false;

        if (!assembled) {
            resetProcessingState();
            return;
        }

        CyclotronForgeRecipeInput input = createRecipeInput();
        if (input.isEmpty()) {
            resetProcessingState();
            return;
        }

        RecipeSelection selection = findRecipe(input);
        if (selection == null) {
            lastRecipe = null;
            displayedRecipeId = null;
            craftingProgress = 0;
            processErrorString = "No matching recipe";
            setAllocatedLux(0);
            syncData();
            return;
        }

        if (displayedRecipeId != null && !displayedRecipeId.equals(selection.id())) {
            craftingProgress = 0;
        }
        lastRecipe = selection.recipe();
        displayedRecipeId = selection.id();

        if (!selection.lengthMatches()) {
            craftingProgress = 0;
            processErrorString = "Recipe requires exactly " + lastRecipe.getCoilLength() + " coil segment(s)";
            setAllocatedLux(0);
            syncData();
            return;
        }

        int duration = getRecipeDuration(lastRecipe);
        int requiredLux = GlareLuxCalculator.sampleVariableLux(lastRecipe.getLuxCurveArray(), craftingProgress, duration);
        setAllocatedLux(requiredLux);

        if (!canProcess(lastRecipe)) {
            syncData();
            return;
        }

        processErrorString = "";
        craftingProgress++;
        isProcessing = true;
        if (craftingProgress < duration) {
            syncData();
            return;
        }

        process(lastRecipe);
        craftingProgress = 0;
        syncData();
    }

    public boolean isAssembled() {
        return assembled;
    }

    public boolean isProcessing() {return isProcessing;}

    public int getCoilLength() {
        return coilLength;
    }

    public Direction getFacing() {
        return facing;
    }

    public @Nullable CyclotronKineticProxyBlockEntity getKineticProxy() {
        return kineticProxy;
    }

    void cacheKineticProxy(CyclotronKineticProxyBlockEntity proxy) {
        if (proxy != null && proxy.getControllerPos().equals(worldPosition)) {
            kineticProxy = proxy;
        }
    }

    public AssemblyResult tryAssemble() {
        if (assembled) return AssemblyResult.ok();
        if (level == null) return AssemblyResult.fail("No level is available");
        if (!getBlockState().hasProperty(CyclotronControllerBlock.FACING)) {
            return AssemblyResult.fail("Controller is missing a facing direction");
        }

        facing = getBlockState().getValue(CyclotronControllerBlock.FACING);
        for (int candidateLength = MIN_COIL_LENGTH; candidateLength <= MAX_COIL_LENGTH; candidateLength++) {
            AssemblyResult validation = validateStructure(candidateLength);
            if (validation.success()) {
                assembled = true;
                coilLength = candidateLength;
                convertStructureToProxies(candidateLength);
                if (level instanceof ServerLevel server) {
                    GlareService.onNodeLoaded(server, this);
                }
                refreshAdjacentLuxTransceivers(candidateLength);
                updateAssembledBlockState(true);
                syncData();
                return AssemblyResult.ok();
            }
        }
        return AssemblyResult.fail("Expected output cap, 1-14 coil slices, and input cap behind the controller");
    }

    private AssemblyResult validateStructure(int candidateCoilLength) {
        AssemblyResult output = validateOutputCap();
        if (!output.success()) return output;

        for (int depth = 1; depth <= candidateCoilLength; depth++) {
            AssemblyResult coil = validateCoilSlice(depth);
            if (!coil.success()) return coil;
        }

        return validateInputCap(candidateCoilLength + 1);
    }

    private AssemblyResult validateOutputCap() {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) continue;
                BlockPos pos = slicePos(0, x, y);
                if (!AllBlocks.BRASS_CASING.has(level.getBlockState(pos))) {
                    return AssemblyResult.fail("Output cap needs brass casing at " + describeOffset(0, x, y));
                }
            }
        }
        return AssemblyResult.ok();
    }

    private AssemblyResult validateCoilSlice(int depth) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                BlockPos pos = slicePos(depth, x, y);
                BlockState state = level.getBlockState(pos);
                if (x == 0 && y == 0) {
                    if (!state.isAir()) {
                        return AssemblyResult.fail("Coil slice " + depth + " needs an air gap in the center");
                    }
                } else if (!state.is(ModBlocks.HEAVY_PLATE_SHIELDING.get())) {
                    return AssemblyResult.fail("Coil slice " + depth + " needs heavy plate shielding at " + describeOffset(depth, x, y));
                }
            }
        }
        return AssemblyResult.ok();
    }

    private AssemblyResult validateInputCap(int depth) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                BlockPos pos = slicePos(depth, x, y);
                BlockState state = level.getBlockState(pos);
                if (isInputItemVault(x, y)) {
                    if (!AllBlocks.ITEM_VAULT.has(state)) {
                        return AssemblyResult.fail("Input cap needs item vaults in the top corners");
                    }
                } else if (isInputFluidTank(x, y)) {
                    if (!AllBlocks.FLUID_TANK.has(state)) {
                        return AssemblyResult.fail("Input cap needs fluid tanks in the bottom corners");
                    }
                } else if (!AllBlocks.BRASS_CASING.has(state)) {
                    return AssemblyResult.fail("Input cap needs brass casing at " + describeOffset(depth, x, y));
                }
            }
        }
        return AssemblyResult.ok();
    }

    private void convertStructureToProxies(int assembledCoilLength) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) continue;
                convertToProxy(slicePos(0, x, y), roleForOutputCap(x, y));
            }
        }

        for (int depth = 1; depth <= assembledCoilLength; depth++) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == 0 && y == 0) continue;
                    convertToProxy(slicePos(depth, x, y), CyclotronProxyRole.STRUCTURE);
                }
            }
        }

        int inputDepth = assembledCoilLength + 1;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                BlockPos pos = slicePos(inputDepth, x, y);
                CyclotronProxyRole role = roleForInputCap(x, y);
                if (role == CyclotronProxyRole.KINETIC_INPUT) {
                    convertToKineticProxy(pos);
                } else {
                    convertToProxy(pos, role);
                }
            }
        }
    }

    public void disassemble() {
        disassemble(true);
    }

    public void disassemble(boolean updateControllerState) {
        if (!assembled || level == null) return;

        int assembledCoilLength = coilLength;
        assembled = false;
        coilLength = 0;
        kineticProxy = null;
        craftingProgress = 0;
        lastRecipe = null;
        displayedRecipeId = null;
        processErrorString = "";
        setAllocatedLux(0);
        if (level instanceof ServerLevel server) {
            GlareService.removeAllLinks(server, getGlareNodePos());
            GlareService.onNodeRemoved(server, getGlareNodePos());
        }
        if (updateControllerState) {
            updateAssembledBlockState(false);
        }

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) continue;
                restoreFromProxy(slicePos(0, x, y));
            }
        }

        for (int depth = 1; depth <= assembledCoilLength; depth++) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == 0 && y == 0) continue;
                    restoreFromProxy(slicePos(depth, x, y));
                }
            }
        }

        int inputDepth = assembledCoilLength + 1;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                restoreFromProxy(slicePos(inputDepth, x, y));
            }
        }

        refreshAdjacentLuxTransceivers(assembledCoilLength);
        syncData();
    }

    public IItemHandler getItemHandlerForProxy(CyclotronProxyBlockEntity proxy, Direction side) {
        if (!assembled || side == null || !isInputOutputSide(proxy, side)) return null;
        return switch (proxy.getRole()) {
            case ITEM_INPUT_0 -> inputSlotA;
            case ITEM_INPUT_1 -> inputSlotB;
            case ITEM_OUTPUT -> outputItems;
            default -> null;
        };
    }

    public IFluidHandler getFluidHandlerForProxy(CyclotronProxyBlockEntity proxy, Direction side) {
        if (!assembled || side == null || !isInputOutputSide(proxy, side)) return null;
        return switch (proxy.getRole()) {
            case FLUID_INPUT_0 -> inputTankAHandler;
            case FLUID_INPUT_1 -> inputTankBHandler;
            case FLUID_OUTPUT -> outputTankHandler;
            default -> null;
        };
    }

    private boolean isInputOutputSide(CyclotronProxyBlockEntity proxy, Direction side) {
        return switch (proxy.getRole()) {
            case ITEM_INPUT_0, ITEM_INPUT_1, FLUID_INPUT_0, FLUID_INPUT_1 -> side == facing.getOpposite();
            case ITEM_OUTPUT, FLUID_OUTPUT -> side == facing;
            default -> false;
        };
    }

    public boolean isLuxSocketSide(CyclotronProxyBlockEntity proxy, Direction side) {
        if (!assembled || side == null) return false;
        int depth = localDepth(proxy.getDx(), proxy.getDz());
        int lateral = localLateral(proxy.getDx(), proxy.getDz());
        int vertical = proxy.getDy();
        if ((depth != 0 && depth != coilLength+1) || Math.abs(lateral) + Math.abs(vertical) != 1) return false;
        if (lateral == 1) return side == facing.getClockWise();
        if (lateral == -1) return side == facing.getCounterClockWise();
        if (vertical == 1) return side == Direction.UP;
        if (vertical == -1) return side == Direction.DOWN;
        return false;
    }

    public float getProxySpeed() {
        return assembled && kineticProxy != null ? Math.abs(kineticProxy.getSpeed()) : 0;
    }

    private CyclotronForgeRecipeInput createRecipeInput() {
        List<ItemStack> items = new ArrayList<>();
        for (int slot = 0; slot < inputInv.getSlots(); slot++) {
            ItemStack stack = inputInv.getStackInSlot(slot);
            if (!stack.isEmpty()) items.add(stack);
        }

        List<FluidStack> fluids = new ArrayList<>();
        if (!inputTankA.getFluid().isEmpty()) fluids.add(inputTankA.getFluid());
        if (!inputTankB.getFluid().isEmpty()) fluids.add(inputTankB.getFluid());
        return new CyclotronForgeRecipeInput(items, fluids);
    }

    private RecipeSelection findRecipe(CyclotronForgeRecipeInput input) {
        RecipeSelection wrongLengthMatch = null;
        for (RecipeHolder<CyclotronForgeRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CYCLOTRON_FORGE_TYPE.get())) {
            CyclotronForgeRecipe recipe = holder.value();
            if (!recipe.matches(input, level)) continue;
            if (!ResearchRecipeGate.canUseServerRecipe(level, holder)) continue;
            RecipeSelection selection = new RecipeSelection(holder.id(), recipe, recipe.getCoilLength() == coilLength);
            if (selection.lengthMatches()) return selection;
            if (wrongLengthMatch == null) wrongLengthMatch = selection;
        }
        return wrongLengthMatch;
    }

    private boolean canProcess(CyclotronForgeRecipe recipe) {
        if (recipe == null) {
            processErrorString = "No recipe";
            return false;
        }
        if (recipe.getCoilLength() != coilLength) {
            processErrorString = "Recipe requires exactly " + recipe.getCoilLength() + " coil segment(s)";
            return false;
        }
        if (getProxySpeed() < recipe.getMinRpm()) {
            processErrorString = "Required RPM: " + recipe.getMinRpm();
            return false;
        }
        if (!hasLuxReady(recipe)) {
            processErrorString = "Waiting for Lux";
            return false;
        }
        if (!canFitOutputs(recipe)) {
            processErrorString = "Output inventory or tank is full";
            return false;
        }
        processErrorString = "";
        return true;
    }

    private boolean hasLuxReady(CyclotronForgeRecipe recipe) {
        if (!recipe.requiresLux()) return true;
        if (!(level instanceof ServerLevel server)) return false;
        if (networkId == null || operationStatus != GlareOperationStatus.ONLINE) return false;
        return !GlareService.getAllLinks(server, getGlareNodePos()).isEmpty();
    }

    private boolean canFitOutputs(CyclotronForgeRecipe recipe) {
        ItemStackHandler trialInv = new ItemStackHandler(ITEM_OUTPUT_SLOTS);
        for (int slot = 0; slot < ITEM_OUTPUT_SLOTS; slot++) {
            trialInv.setStackInSlot(slot, outputInv.getStackInSlot(slot).copy());
        }
        for (ProcessingOutput output : recipe.getRollableResults()) {
            ItemStack stack = output.getStack();
            if (!stack.isEmpty() && !ItemHandlerHelper.insertItemStacked(trialInv, stack.copy(), false).isEmpty()) {
                return false;
            }
        }

        FluidTank trialTank = new FluidTank(OUTPUT_TANK_CAPACITY);
        trialTank.setFluid(outputTank.getFluid().copy());
        for (FluidStack result : recipe.getFluidResults()) {
            FluidStack stack = result.copy();
            if (!stack.isEmpty() && trialTank.fill(stack, IFluidHandler.FluidAction.EXECUTE) != stack.getAmount()) {
                return false;
            }
        }
        return true;
    }

    private void process(CyclotronForgeRecipe recipe) {
        consumeItems(recipe);
        consumeFluids(recipe);

        for (FluidStack result : recipe.getFluidResults()) {
            if (!result.isEmpty()) {
                outputTank.fill(result.copy(), IFluidHandler.FluidAction.EXECUTE);
            }
        }
        for (ItemStack result : recipe.rollResults(level.random)) {
            if (!result.isEmpty()) {
                ItemHandlerHelper.insertItemStacked(outputInv, result.copy(), false);
            }
        }
    }

    private void consumeItems(CyclotronForgeRecipe recipe) {
        for (net.neoforged.neoforge.common.crafting.SizedIngredient ingredient : recipe.getCombinedIngredients()) {
            int remaining = ingredient.count();
            for (int slot = 0; slot < inputInv.getSlots(); slot++) {
                ItemStack stack = inputInv.getStackInSlot(slot);
                if (!stack.isEmpty() && ingredient.ingredient().test(stack)) {
                    int drained = Math.min(remaining, stack.getCount());
                    inputInv.extractItem(slot, drained, false);
                    remaining -= drained;
                    if (remaining <= 0) break;
                }
            }
        }
    }

    private void consumeFluids(CyclotronForgeRecipe recipe) {
        for (SizedFluidIngredient ingredient : recipe.getFluidIngredients()) {
            if (ingredient.test(inputTankA.getFluid())) {
                inputTankA.drain(ingredient.amount(), IFluidHandler.FluidAction.EXECUTE);
            } else if (ingredient.test(inputTankB.getFluid())) {
                inputTankB.drain(ingredient.amount(), IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    private int getRecipeDuration(CyclotronForgeRecipe recipe) {
        int duration = recipe.getProcessingDuration();
        return duration <= 0 ? 100 : duration;
    }

    private void resetProcessingState() {
        if (craftingProgress == 0 && lastRecipe == null && displayedRecipeId == null && allocatedLux == 0
                && processErrorString.isEmpty()) {
            return;
        }
        craftingProgress = 0;
        lastRecipe = null;
        displayedRecipeId = null;
        processErrorString = "";
        setAllocatedLux(0);
        syncData();
    }

    private CyclotronProxyRole roleForOutputCap(int x, int y) {
        if (y == 0 && x == -1) return CyclotronProxyRole.ITEM_OUTPUT;
        if (y == 0 && x == 1) return CyclotronProxyRole.FLUID_OUTPUT;
        if ((Math.abs(x) == 1 && y == 0) || (x == 0 && Math.abs(y) == 1)) return CyclotronProxyRole.LUX_SOCKET;
        return CyclotronProxyRole.STRUCTURE;
    }

    private CyclotronProxyRole roleForInputCap(int x, int y) {
        if (y == 1 && x == -1) return CyclotronProxyRole.ITEM_INPUT_0;
        if (y == 1 && x == 1) return CyclotronProxyRole.ITEM_INPUT_1;
        if (y == -1 && x == -1) return CyclotronProxyRole.FLUID_INPUT_0;
        if (y == -1 && x == 1) return CyclotronProxyRole.FLUID_INPUT_1;
        if (x == 0 && y == 0) return CyclotronProxyRole.KINETIC_INPUT;
        return CyclotronProxyRole.STRUCTURE;
    }

    private boolean isInputItemVault(int x, int y) {
        return y == 1 && Math.abs(x) == 1;
    }

    private boolean isInputFluidTank(int x, int y) {
        return y == -1 && Math.abs(x) == 1;
    }

    private void convertToProxy(BlockPos pos, CyclotronProxyRole role) {
        BlockState oldState = level.getBlockState(pos);
        level.setBlock(pos, ModBlocks.CYCLOTRON_PROXY.get().defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof CyclotronProxyBlockEntity proxy) {
            proxy.setControllerData(worldPosition, pos.getX() - worldPosition.getX(),
                    pos.getY() - worldPosition.getY(), pos.getZ() - worldPosition.getZ(), role);
            proxy.setStoredState(oldState);
        }
    }

    private void convertToKineticProxy(BlockPos pos) {
        BlockState oldState = level.getBlockState(pos);
        BlockState newState = ModBlocks.CYCLOTRON_KINETIC_PROXY.get().defaultBlockState()
                .setValue(RotatedPillarKineticBlock.AXIS, facing.getAxis());
        level.setBlock(pos, newState, 3);
        if (level.getBlockEntity(pos) instanceof CyclotronKineticProxyBlockEntity proxy) {
            proxy.setControllerData(worldPosition, pos.getX() - worldPosition.getX(),
                    pos.getY() - worldPosition.getY(), pos.getZ() - worldPosition.getZ());
            proxy.setStoredState(oldState);
            kineticProxy = proxy;
        }
    }

    private void restoreFromProxy(BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CyclotronProxyBlockEntity proxy) {
            BlockState stored = proxy.getStoredState();
            level.removeBlockEntity(pos);
            level.setBlock(pos, stored == null || stored.isAir() ? Blocks.AIR.defaultBlockState() : stored, 3);
        } else if (level.getBlockEntity(pos) instanceof CyclotronKineticProxyBlockEntity proxy) {
            BlockState stored = proxy.getStoredState();
            level.removeBlockEntity(pos);
            level.setBlock(pos, stored == null || stored.isAir() ? Blocks.AIR.defaultBlockState() : stored, 3);
        } else if (level.getBlockState(pos).is(ModBlocks.CYCLOTRON_PROXY.get())
                || level.getBlockState(pos).is(ModBlocks.CYCLOTRON_KINETIC_PROXY.get())) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private int localDepth(int dx, int dz) {
        Direction back = facing.getOpposite();
        return dx * back.getStepX() + dz * back.getStepZ();
    }

    private int localLateral(int dx, int dz) {
        Direction right = facing.getClockWise();
        return dx * right.getStepX() + dz * right.getStepZ();
    }

    private BlockPos slicePos(int depth, int x, int y) {
        Direction back = facing.getOpposite();
        Direction right = facing.getClockWise();
        return worldPosition.relative(back, depth).relative(right, x).above(y);
    }

    private void refreshAdjacentLuxTransceivers() {
        refreshAdjacentLuxTransceivers(coilLength);
    }

    private void refreshAdjacentLuxTransceivers(int assembledCoilLength) {
        if (!(level instanceof ServerLevel server)) return;
        refreshAdjacentLuxTransceiversForCap(server, 0);
        if (assembledCoilLength > 0) {
            refreshAdjacentLuxTransceiversForCap(server, assembledCoilLength + 1);
        }
    }

    private void refreshAdjacentLuxTransceiversForCap(ServerLevel server, int depth) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (Math.abs(x) + Math.abs(y) != 1) continue;
                BlockPos socketPos = slicePos(depth, x, y);
                Direction outward = x == 1 ? facing.getClockWise()
                        : x == -1 ? facing.getCounterClockWise()
                        : y == 1 ? Direction.UP
                        : Direction.DOWN;
                BlockPos transceiverPos = socketPos.relative(outward);
                if (server.isLoaded(transceiverPos)
                        && server.getBlockEntity(transceiverPos) instanceof LuxTransceiverBlockEntity transceiver) {
                    transceiver.refreshSocketLink();
                }
            }
        }
    }

    private String describeOffset(int depth, int x, int y) {
        return "depth " + depth + ", lateral " + x + ", vertical " + y;
    }

    private void updateAssembledBlockState(boolean value) {
        if (level == null || !getBlockState().hasProperty(CyclotronControllerBlock.ASSEMBLED)) return;
        BlockState state = getBlockState();
        if (state.getValue(CyclotronControllerBlock.ASSEMBLED) != value) {
            level.setBlock(worldPosition, state.setValue(CyclotronControllerBlock.ASSEMBLED, value), 3);
        }
    }

    private void cacheKineticProxyFromWorld() {
        kineticProxy = null;
        if (level == null || !assembled || coilLength <= 0) return;
        BlockPos kineticPos = slicePos(coilLength + 1, 0, 0);
        if (level.getBlockEntity(kineticPos) instanceof CyclotronKineticProxyBlockEntity proxy) {
            kineticProxy = proxy;
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putBoolean("Assembled", assembled);
        tag.putInt("CoilLength", coilLength);
        tag.putString("Facing", facing.getName());
        tag.putInt("AllocatedLux", allocatedLux);
        tag.putInt("CraftingProgress", craftingProgress);
        tag.putBoolean("IsProcessing", isProcessing);
        tag.putString("OperationStatus", operationStatus.name());
        tag.putInt("GlareLuxCapacity", syncedLuxCapacity);
        tag.putInt("GlareLuxAllocated", syncedLuxAllocated);
        tag.putBoolean("GlareOverloaded", syncedOverloaded);
        tag.putIntArray("GlareLuxHistory", syncedLuxHistory);
        tag.put("InputInv", inputInv.serializeNBT(registries));
        tag.put("OutputInv", outputInv.serializeNBT(registries));
        tag.put("InputTankA", inputTankA.writeToNBT(registries, new CompoundTag()));
        tag.put("InputTankB", inputTankB.writeToNBT(registries, new CompoundTag()));
        tag.put("OutputTank", outputTank.writeToNBT(registries, new CompoundTag()));
        if (networkId != null) tag.putUUID("GlareNetwork", networkId);
        if (displayedRecipeId != null) tag.putString("DisplayedRecipe", displayedRecipeId.toString());
        if (!processErrorString.isEmpty()) tag.putString("ProcessError", processErrorString);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        assembled = tag.getBoolean("Assembled");
        coilLength = tag.getInt("CoilLength");
        facing = Direction.byName(tag.getString("Facing"));
        if (facing == null || facing.getAxis() == Direction.Axis.Y) facing = Direction.NORTH;
        allocatedLux = tag.getInt("AllocatedLux");
        craftingProgress = tag.getInt("CraftingProgress");
        isProcessing = tag.getBoolean("IsProcessing");
        try {
            operationStatus = GlareOperationStatus.valueOf(tag.getString("OperationStatus"));
        } catch (IllegalArgumentException ignored) {
            operationStatus = GlareOperationStatus.ONLINE;
        }
        syncedLuxCapacity = tag.getInt("GlareLuxCapacity");
        syncedLuxAllocated = tag.getInt("GlareLuxAllocated");
        syncedOverloaded = tag.getBoolean("GlareOverloaded");
        syncedLuxHistory = tag.getIntArray("GlareLuxHistory");
        if (tag.contains("InputInv")) inputInv.deserializeNBT(registries, tag.getCompound("InputInv"));
        if (tag.contains("OutputInv")) outputInv.deserializeNBT(registries, tag.getCompound("OutputInv"));
        if (tag.contains("InputTankA")) inputTankA.readFromNBT(registries, tag.getCompound("InputTankA"));
        if (tag.contains("InputTankB")) inputTankB.readFromNBT(registries, tag.getCompound("InputTankB"));
        if (tag.contains("OutputTank")) outputTank.readFromNBT(registries, tag.getCompound("OutputTank"));
        networkId = tag.hasUUID("GlareNetwork") ? tag.getUUID("GlareNetwork") : null;
        displayedRecipeId = tag.contains("DisplayedRecipe") ? ResourceLocation.tryParse(tag.getString("DisplayedRecipe")) : null;
        processErrorString = tag.getString("ProcessError");
        cacheKineticProxyFromWorld();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        cacheKineticProxyFromWorld();
        if (assembled && level instanceof ServerLevel server) {
            GlareService.onNodeLoaded(server, this);
            refreshSyncedGlareSummary(server);
            refreshAdjacentLuxTransceivers();
        }
    }

    @Override
    public int getMaxGlareLinks() {
        return 1;
    }

    @Override
    public boolean allowsManualGlareLinks() {
        return false;
    }

    @Override
    public DimensionalNodePos getGlareNodePos() {
        return DimensionalNodePos.of(level, worldPosition);
    }

    @Override
    public Vec3 getGlareLinkEndpoint() {
        return Vec3.atCenterOf(worldPosition);
    }

    @Override
    public void onGlareNetworkChanged(ServerLevel level, UUID networkId) {
        this.networkId = networkId;
        refreshSyncedGlareSummary(level);
        syncData();
    }

    @Override
    public void onGlareLinksChanged(ServerLevel level) {
        refreshSyncedGlareSummary(level);
        syncData();
    }

    @Override
    public int getAllocatedLux() {
        return assembled ? allocatedLux : 0;
    }

    public void setAllocatedLux(int value) {
        int next = Math.max(0, value);
        if (allocatedLux == next) return;
        allocatedLux = next;
        if (level instanceof ServerLevel server && assembled) {
            GlareService.updateNodeState(server, this);
            refreshSyncedGlareSummary(server);
        }
        syncData();
    }

    @Override
    public GlareOperationStatus getGlareOperationStatus() {
        return operationStatus;
    }

    @Override
    public void setGlareOperationStatus(GlareOperationStatus status) {
        operationStatus = status == null ? GlareOperationStatus.ONLINE : status;
        if (level instanceof ServerLevel server && assembled) {
            GlareService.updateNodeState(server, this);
            refreshSyncedGlareSummary(server);
        }
        syncData();
    }

    @Override
    public void applyGlareOperationStatusFromNetwork(GlareOperationStatus status) {
        operationStatus = status == null ? GlareOperationStatus.ONLINE : status;
        if (level instanceof ServerLevel server) refreshSyncedGlareSummary(server);
        syncData();
    }

    private void refreshSyncedGlareSummary(ServerLevel server) {
        syncedLuxCapacity = 0;
        syncedLuxAllocated = 0;
        syncedOverloaded = false;
        syncedLuxHistory = new int[0];
        if (networkId != null) {
            GlareService.getNetwork(server, networkId).ifPresent(network -> {
                syncedLuxCapacity = network.luxCapacity;
                syncedLuxAllocated = network.luxAllocated;
                syncedOverloaded = network.overloaded;
                syncedLuxHistory = network.luxHistory.stream().mapToInt(Integer::intValue).toArray();
            });
        }
    }

    @Override
    public GlareNetworkSnapshot getSyncedGlareNetworkSnapshot() {
        return GlareNetworkSnapshot.of(networkId != null, syncedLuxAllocated, syncedLuxCapacity, syncedLuxHistory,
                syncedOverloaded ? GlareOperationStatus.OVERLOADED : operationStatus, syncedOverloaded);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("     Cyclotron Forge:"));
        if (!assembled) {
            tooltip.add(Component.literal("     \u00a77Unassembled; right-click when built."));
            return true;
        }
        tooltip.add(Component.literal("     \u00a75Coil Length: \u00a7r" + coilLength));
        tooltip.add(Component.literal("     \u00a75Current RPM: \u00a7r" + (int) getProxySpeed()));
        tooltip.add(Component.literal("     \u00a7dLux: \u00a7r" + allocatedLux + " ("
                + operationStatus.name().toLowerCase(Locale.ROOT) + ")"));
        if (lastRecipe != null) {
            tooltip.add(Component.literal("     \u00a78Progress: " + craftingProgress + " / " + getRecipeDuration(lastRecipe)));
        }
        if (!processErrorString.isEmpty()) {
            tooltip.add(Component.literal("     \u00a7c" + processErrorString));
        }
        return true;
    }

    private void syncData() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private final class SingleSlotInputHandler implements IItemHandler {
        private final int slotIndex;

        private SingleSlotInputHandler(int slotIndex) {
            this.slotIndex = slotIndex;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? inputInv.getStackInSlot(slotIndex) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot == 0 ? inputInv.insertItem(slotIndex, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? inputInv.getSlotLimit(slotIndex) : 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && inputInv.isItemValid(slotIndex, stack);
        }
    }

    private final class OutputItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return outputInv.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return outputInv.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return outputInv.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return outputInv.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }

    private static final class FillOnlyFluidHandler implements IFluidHandler {
        private final FluidTank tank;

        private FillOnlyFluidHandler(FluidTank tank) {
            this.tank = tank;
        }

        @Override
        public int getTanks() {
            return tank.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluidInTank(tankIndex);
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return tank.getTankCapacity(tankIndex);
        }

        @Override
        public boolean isFluidValid(int tankIndex, FluidStack stack) {
            return tank.isFluidValid(tankIndex, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return tank.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }

    private static final class DrainOnlyFluidHandler implements IFluidHandler {
        private final FluidTank tank;

        private DrainOnlyFluidHandler(FluidTank tank) {
            this.tank = tank;
        }

        @Override
        public int getTanks() {
            return tank.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluidInTank(tankIndex);
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return tank.getTankCapacity(tankIndex);
        }

        @Override
        public boolean isFluidValid(int tankIndex, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return tank.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return tank.drain(maxDrain, action);
        }
    }

    private record RecipeSelection(ResourceLocation id, CyclotronForgeRecipe recipe, boolean lengthMatches) {
    }
}
