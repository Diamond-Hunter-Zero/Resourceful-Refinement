package com.resourceful_refinement.content.mechanical_stamper;

import com.resourceful_refinement.content.manifold.ManifoldAssemblyAction;
import com.resourceful_refinement.content.manifold.ManifoldAssemblySession;
import com.resourceful_refinement.content.manifold.ManifoldBlockEntity;
import com.resourceful_refinement.content.mechanical_stamper.recipe.MechanicalStamperRecipe;
import com.resourceful_refinement.content.mechanical_stamper.recipe.MechanicalStamperRecipeInput;
import com.resourceful_refinement.content.research.ResearchRecipeGate;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.resourceful_refinement.registry.ModStressValues;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MechanicalStamperBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {
    public static final int TANK_CAPACITY = 2000;
    public static final int ENTITY_SCAN_INTERVAL = 10;
    public static final int PAIRED_WORK_DISTANCE = 2;
    public static final int PARTNER_DISTANCE = 4;

    public enum RunningState {
        IDLE,
        EXTENDING,
        IMPACTING,
        RETRACTING
    }

    public RunningState state = RunningState.IDLE;
    public int timer;
    public int prevTimer;
    public int scanCooldown = ENTITY_SCAN_INTERVAL;
    public float extensionProgress;

    private boolean activeController;
    private boolean isolatedCycle;
    private boolean processedThisCycle;
    private int cycleDuration = MechanicalStamperRecipe.DEFAULT_PROCESSING_TIME;
    private BlockPos partnerPos;
    private UUID targetEntityId;
    private BlockPos targetDepotPos;
    private MechanicalStamperRecipe lastRecipe;
    private ResourceLocation lastRecipeId;
    private boolean processingBeltItem;
    private int beltProcessSegment = -1;
    private boolean processingManifoldBlock;
    private BlockPos targetManifoldPos;
    private Direction targetManifoldFace;
    private ResourceLocation targetManifoldStampId;
    private ResourceLocation targetManifoldFillId;
    private boolean targetManifoldUsesFluid;

    public final ItemStackHandler stampInv = new ItemStackHandler(1) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncData();
        }
    };

    public final ItemStackHandler mediumInv = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncData();
        }
    };

    public final FluidTank fluidTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            syncData();
        }
    };

    public MechanicalStamperBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
    }

    public Direction getFacing() {
        if (getBlockState().hasProperty(MechanicalStamperBlock.HORIZONTAL_FACING)) {
            return getBlockState().getValue(MechanicalStamperBlock.HORIZONTAL_FACING);
        }
        return Direction.NORTH;
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return null;
        }

        Direction front = getFacing();
        Direction back = front.getOpposite();
        if (side == front) {
            return stampInv;
        }
        if (side != back) {
            return mediumInv;
        }
        return null;
    }

    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return null;
        }
        return side == getFacing().getOpposite() ? fluidTank : null;
    }

    public ItemStack getStampItem() {
        return stampInv.getStackInSlot(0);
    }

    public ItemStack getFillMedium() {
        return mediumInv.getStackInSlot(0);
    }

    public FluidStack getFillFluid() {
        return fluidTank.getFluid();
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }

        if (state == RunningState.IDLE) {
            prevTimer = 0;
            timer = 0;
            extensionProgress = 0;
            if (!level.isClientSide) {
                tickIdle();
            }
            return;
        }

        tickRunning();
    }

    private void tickIdle() {
        clearStaleRecipe();
        if (getSpeed() == 0) {
            return;
        }
        if (tryStartManifoldStampCycle()) {
            return;
        }
        if (scanCooldown-- > 0) {
            return;
        }
        scanCooldown = ENTITY_SCAN_INTERVAL;

        WorkTarget target = findWorldItemTarget();
        if (target == null) {
            return;
        }

        Optional<RecipeHolder<MechanicalStamperRecipe>> recipe = findRecipe(target.stack(), target.paired());
        recipe.ifPresent(holder -> startCycle(holder, target));
    }

    private void tickRunning() {
        if (processingBeltItem) {
            tickBeltCycleAnimation();
            return;
        }
        if (processingManifoldBlock) {
            tickManifoldBlockCycle();
            return;
        }

        if (!level.isClientSide) {
            pinTargetEntity();
        }

        int midpoint = Math.max(1, cycleDuration / 2);
        if (!processedThisCycle && timer >= midpoint) {
            state = RunningState.IMPACTING;
            extensionProgress = 1;
            if (!level.isClientSide && activeController) {
                processAtImpact();
            }
            processedThisCycle = true;
            state = RunningState.RETRACTING;
            if (!level.isClientSide) {
                sendData();
            }
        }

        if (!level.isClientSide && timer >= cycleDuration) {
            finishCycle();
            return;
        }

        if (!level.isClientSide && getEffectiveCycleSpeed() == 0) {
            return;
        }

        advanceAnimationTimer();
        updateExtensionProgress();
    }

    private void tickBeltCycleAnimation() {
        if (level.isClientSide) {
            advanceAnimationTimer();
            int midpoint = Math.max(1, cycleDuration / 2);
            state = timer >= midpoint ? RunningState.RETRACTING : RunningState.EXTENDING;
            updateExtensionProgress();
            return;
        }

        int midpoint = Math.max(1, cycleDuration / 2);
        if (!processedThisCycle && timer >= midpoint) {
            state = RunningState.IMPACTING;
            extensionProgress = 1;
            return;
        }

        if (processedThisCycle && timer >= cycleDuration) {
            finishCycle();
            return;
        }

        if (getEffectiveCycleSpeed() != 0) {
            advanceAnimationTimer();
        }
        state = processedThisCycle ? RunningState.RETRACTING : RunningState.EXTENDING;
        updateExtensionProgress();
    }

    private void tickManifoldBlockCycle() {
        if (level.isClientSide) {
            advanceAnimationTimer();
            int midpoint = Math.max(1, cycleDuration / 2);
            state = timer >= midpoint ? RunningState.RETRACTING : RunningState.EXTENDING;
            updateExtensionProgress();
            return;
        }

        if (targetManifoldPos == null || getSpeed() == 0
                || !(level.getBlockEntity(targetManifoldPos) instanceof ManifoldBlockEntity)) {
            finishCycle();
            return;
        }

        ManifoldAssemblySession.holdTarget(level, targetManifoldPos);

        int midpoint = Math.max(1, cycleDuration / 2);
        if (!processedThisCycle && timer >= midpoint) {
            state = RunningState.IMPACTING;
            extensionProgress = 1;
            processManifoldStampAtImpact();
            processedThisCycle = true;
            state = RunningState.RETRACTING;
            sendData();
        }

        if (timer >= cycleDuration) {
            finishCycle();
            return;
        }

        advanceAnimationTimer();
        updateExtensionProgress();
    }

    private void startCycle(RecipeHolder<MechanicalStamperRecipe> holder, WorkTarget target) {
        MechanicalStamperRecipe recipe = holder.value();
        boolean isolated = recipe.isIsolatedStamper();
        MechanicalStamperBlockEntity partner = null;
        if (!isolated) {
            partner = getAvailablePartner(recipe, target.stack());
            if (partner == null) {
                return;
            }
        }

        beginCycle(holder, target, partner == null ? null : partner.getBlockPos(), isolated, true);
        if (partner != null) {
            partner.beginCycle(holder, target, worldPosition, false, false);
        }
    }

    private void beginCycle(RecipeHolder<MechanicalStamperRecipe> holder, WorkTarget target, @Nullable BlockPos partnerPos,
                            boolean isolated, boolean activeController) {
        MechanicalStamperRecipe recipe = holder.value();
        this.lastRecipe = recipe;
        this.lastRecipeId = holder.id();
        this.targetEntityId = target.itemEntity() == null ? null : target.itemEntity().getUUID();
        this.targetDepotPos = target.depotPos();
        this.partnerPos = partnerPos;
        this.isolatedCycle = isolated;
        this.activeController = activeController;
        this.processedThisCycle = false;
        this.cycleDuration = Math.max(2, recipe.getProcessingTime());
        this.prevTimer = 0;
        this.timer = 0;
        this.extensionProgress = 0;
        this.state = RunningState.EXTENDING;
        sendData();
    }

    private void beginBeltCycle(RecipeHolder<MechanicalStamperRecipe> holder, int beltSegment, @Nullable BlockPos partnerPos,
                                boolean isolated, boolean activeController) {
        MechanicalStamperRecipe recipe = holder.value();
        this.lastRecipe = recipe;
        this.lastRecipeId = holder.id();
        this.targetEntityId = null;
        this.targetDepotPos = null;
        this.partnerPos = partnerPos;
        this.isolatedCycle = isolated;
        this.activeController = activeController;
        this.processedThisCycle = false;
        this.processingBeltItem = true;
        this.beltProcessSegment = beltSegment;
        this.cycleDuration = Math.max(2, recipe.getProcessingTime());
        this.prevTimer = 0;
        this.timer = 0;
        this.extensionProgress = 0;
        this.state = RunningState.EXTENDING;
        sendData();
    }

    private void beginManifoldBlockCycle(BlockPos targetPos, ManifoldAssemblyAction.Stamp action,
                                         boolean usesFluidFill) {
        this.targetEntityId = null;
        this.targetDepotPos = null;
        this.partnerPos = null;
        this.isolatedCycle = true;
        this.activeController = true;
        this.processedThisCycle = false;
        this.processingManifoldBlock = true;
        this.targetManifoldPos = targetPos;
        this.targetManifoldFace = action.face();
        this.targetManifoldStampId = action.stampItemId();
        this.targetManifoldFillId = action.fillId();
        this.targetManifoldUsesFluid = usesFluidFill;
        this.cycleDuration = ManifoldAssemblySession.DEFAULT_DURATION;
        this.prevTimer = 0;
        this.timer = 0;
        this.extensionProgress = 0;
        this.state = RunningState.EXTENDING;
        sendData();
    }

    public static Boolean handleBeltProcessing(BeltBlockEntity belt, TransportedItemStack transported,
                                               float nextOffset, boolean blocking) {
        if (belt == null || belt.getLevel() == null || belt.getLevel().isClientSide
                || transported == null || transported.stack.isEmpty()) {
            return null;
        }

        int segment = findBeltSegment(belt, transported, nextOffset);
        if (segment < 0) {
            return null;
        }

        MechanicalStamperBlockEntity stamper = findBeltStamper(belt, transported.stack, segment, true);
        if (stamper == null) {
            stamper = findBeltStamper(belt, transported.stack, segment, false);
        }
        if (stamper == null) {
            return null;
        }

        if (!stamper.processingBeltItem) {
            if (blocking || stamper.state != RunningState.IDLE || stamper.getSpeed() == 0) {
                return null;
            }
            if (!stamper.tryStartBeltCycle(transported.stack, segment)) {
                return null;
            }
        } else if (stamper.beltProcessSegment != segment || !stamper.canContinueBeltCycle(transported.stack)) {
            stamper.abortBeltCycle();
            return null;
        }

        stamper.holdBeltItemAtStamper(belt, transported);
        int midpoint = Math.max(1, stamper.cycleDuration / 2);
        if (!stamper.processedThisCycle && stamper.timer >= midpoint) {
            boolean removed = stamper.completeBeltProcessing(belt, transported);
            stamper.processedThisCycle = true;
            stamper.state = RunningState.RETRACTING;
            stamper.sendData();
            return removed;
        }

        if (stamper.processedThisCycle && stamper.timer >= stamper.cycleDuration) {
            transported.locked = false;
            stamper.finishCycle();
        }
        return false;
    }

    private static MechanicalStamperBlockEntity findBeltStamper(BeltBlockEntity belt, ItemStack stack, int segment,
                                                                boolean includeActiveCycles) {
        BlockPos workPos = BeltHelper.getPositionForOffset(belt, segment).above();

        if (includeActiveCycles) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                MechanicalStamperBlockEntity active = getStamperAtWorkPos(belt, workPos, direction, PAIRED_WORK_DISTANCE);
                if (active != null && active.processingBeltItem && active.activeController
                        && active.beltProcessSegment == segment) {
                    return active;
                }
                active = getStamperAtWorkPos(belt, workPos, direction, 1);
                if (active != null && active.processingBeltItem && active.activeController
                        && active.beltProcessSegment == segment) {
                    return active;
                }
            }
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            MechanicalStamperBlockEntity paired = getStamperAtWorkPos(belt, workPos, direction, PAIRED_WORK_DISTANCE);
            if (paired == null || !paired.canStartBeltRecipe(stack, true)) {
                continue;
            }
            Optional<RecipeHolder<MechanicalStamperRecipe>> recipe = paired.findRecipe(stack, true);
            MechanicalStamperBlockEntity partner = recipe
                    .map(holder -> paired.getAvailablePartner(holder.value(), stack))
                    .orElse(null);
            if (partner != null && paired.getBlockPos().asLong() < partner.getBlockPos().asLong()) {
                return paired;
            }
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            MechanicalStamperBlockEntity isolated = getStamperAtWorkPos(belt, workPos, direction, 1);
            if (isolated != null && isolated.canStartBeltRecipe(stack, false)) {
                return isolated;
            }
        }

        return null;
    }

    @Nullable
    private static MechanicalStamperBlockEntity getStamperAtWorkPos(BeltBlockEntity belt, BlockPos workPos,
                                                                    Direction facing, int distance) {
        BlockPos stamperPos = workPos.relative(facing.getOpposite(), distance);
        if (!(belt.getLevel().getBlockEntity(stamperPos) instanceof MechanicalStamperBlockEntity stamper)) {
            return null;
        }
        return stamper.getFacing() == facing ? stamper : null;
    }

    private boolean tryStartBeltCycle(ItemStack stack, int segment) {
        Optional<RecipeHolder<MechanicalStamperRecipe>> recipeHolder = findRecipe(stack, true);
        if (recipeHolder.isEmpty()) {
            return false;
        }

        MechanicalStamperRecipe recipe = recipeHolder.get().value();
        boolean isolated = recipe.isIsolatedStamper();
        MechanicalStamperBlockEntity partner = null;
        if (!isolated) {
            partner = getAvailablePartner(recipe, stack);
            if (partner == null) {
                return false;
            }
        }

        beginBeltCycle(recipeHolder.get(), segment, partner == null ? null : partner.getBlockPos(), isolated, true);
        if (partner != null) {
            partner.beginBeltCycle(recipeHolder.get(), segment, worldPosition, false, false);
        }
        return true;
    }

    private boolean tryStartManifoldStampCycle() {
        if (level == null || state != RunningState.IDLE || getPartner() != null) {
            return false;
        }

        BlockPos targetPos = getIsolatedWorkPos();
        if (!(level.getBlockEntity(targetPos) instanceof ManifoldBlockEntity manifold)) {
            return false;
        }

        ManifoldStampTarget target = createManifoldStampTarget(manifold);
        if (target == null || !ManifoldAssemblySession.canApply(level, targetPos, target.action())) {
            return false;
        }

        beginManifoldBlockCycle(targetPos, target.action(), target.usesFluidFill());
        return true;
    }

    private boolean canStartBeltRecipe(ItemStack stack, boolean paired) {
        if (state != RunningState.IDLE || getSpeed() == 0) {
            return false;
        }
        Optional<RecipeHolder<MechanicalStamperRecipe>> recipe = findRecipe(stack, paired);
        if (recipe.isEmpty()) {
            return false;
        }
        return paired != recipe.get().value().isIsolatedStamper();
    }

    private boolean canContinueBeltCycle(ItemStack stack) {
        if (lastRecipe == null || stack.isEmpty()) {
            return false;
        }
        if (!ResearchRecipeGate.canUseServerRecipe(level, lastRecipeId)) {
            return false;
        }
        boolean paired = !isolatedCycle;
        if (!lastRecipe.matches(createInput(stack, paired), level)) {
            return false;
        }
        MechanicalStamperBlockEntity partner = paired ? getValidPartner(lastRecipe, stack) : null;
        return !paired || partner != null && lastRecipe.matches(partner.createInput(stack, true), level);
    }

    private void processAtImpact() {
        if (level == null || lastRecipe == null) {
            return;
        }
        if (!ResearchRecipeGate.canUseServerRecipe(level, lastRecipeId)) {
            return;
        }

        ActiveTarget target = getActiveTarget();
        if (target == null || target.stack().isEmpty()) {
            return;
        }

        boolean paired = !isolatedCycle;
        if (paired && getValidPartner(lastRecipe, target.stack()) == null) {
            return;
        }

        MechanicalStamperRecipeInput input = createInput(target.stack(), paired);
        if (!lastRecipe.matches(input, level)) {
            return;
        }

        MechanicalStamperBlockEntity partner = paired ? getValidPartner(lastRecipe, target.stack()) : null;
        if (partner != null && !lastRecipe.matches(partner.createInput(target.stack(), true), level)) {
            return;
        }

        consumeRecipeInputs(lastRecipe);
        if (partner != null) {
            partner.consumeRecipeInputs(lastRecipe);
        }
        applyRecipeOutput(target, lastRecipe);
        syncData();
        if (partner != null) {
            partner.syncData();
        }
    }

    private void processManifoldStampAtImpact() {
        if (level == null || targetManifoldPos == null || targetManifoldFace == null
                || targetManifoldStampId == null || targetManifoldFillId == null) {
            return;
        }
        if (!(level.getBlockEntity(targetManifoldPos) instanceof ManifoldBlockEntity manifold)) {
            return;
        }
        ManifoldAssemblyAction.Stamp action = createManifoldStampAction(manifold, targetManifoldStampId,
                targetManifoldFillId, targetManifoldUsesFluid);
        if (action == null) {
            return;
        }
        if (!action.face().equals(targetManifoldFace)) {
            return;
        }

        boolean changed = ManifoldAssemblySession.apply(level, targetManifoldPos, action);
        if (changed) {
            consumeManifoldStampFill();
            syncData();
        }
    }

    private void applyRecipeOutput(ActiveTarget target, MechanicalStamperRecipe recipe) {
        if (target.itemEntity() != null) {
            applyRecipeOutput(target.itemEntity(), recipe);
            return;
        }

        if (target.depot() != null && target.depotPos() != null) {
            applyRecipeOutputToDepot(target.depot(), target.depotPos(), recipe);
        }
    }

    private void applyRecipeOutput(ItemEntity target, MechanicalStamperRecipe recipe) {
        ItemStack targetStack = target.getItem();
        Vec3 outputPos = target.position();
        targetStack.shrink(1);
        if (targetStack.isEmpty()) {
            target.discard();
        } else {
            target.setItem(targetStack);
        }

        for (ProcessingOutput output : recipe.getResults()) {
            ItemStack result = output.rollOutput(level.random);
            if (result.isEmpty()) {
                continue;
            }
            ItemEntity created = new ItemEntity(level, outputPos.x, outputPos.y, outputPos.z, result);
            created.setDefaultPickUpDelay();
            created.setDeltaMovement(VecHelper.offsetRandomly(Vec3.ZERO, level.random, .05f));
            level.addFreshEntity(created);
        }
    }

    private void applyRecipeOutputToDepot(DepotBehaviour depot, BlockPos depotPos, MechanicalStamperRecipe recipe) {
        ItemStack originalRemainder = depot.getHeldItemStack().copy();
        originalRemainder.shrink(1);

        List<ItemStack> outputs = recipe.getResults().stream()
                .map(output -> output.rollOutput(level.random))
                .filter(result -> !result.isEmpty())
                .toList();

        if (outputs.isEmpty()) {
            setDepotHeldItem(depot, originalRemainder);
            return;
        }

        setDepotHeldItem(depot, outputs.getFirst());
        addDepotOutput(depot, depotPos, originalRemainder);
        for (int i = 1; i < outputs.size(); i++) {
            addDepotOutput(depot, depotPos, outputs.get(i));
        }
    }

    private void setDepotHeldItem(DepotBehaviour depot, ItemStack stack) {
        if (stack.isEmpty()) {
            depot.removeHeldItem();
            return;
        }
        depot.setCenteredHeldItem(new TransportedItemStack(stack));
    }

    private void addDepotOutput(DepotBehaviour depot, BlockPos depotPos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemStack remainder = insertIntoDepotOutputBuffer(depot, stack.copy());
        if (!remainder.isEmpty()) {
            Containers.dropItemStack(level, depotPos.getX() + 0.5, depotPos.getY() + 1.125,
                    depotPos.getZ() + 0.5, remainder);
        }
    }

    private ItemStack insertIntoDepotOutputBuffer(DepotBehaviour depot, ItemStack stack) {
        try {
            Field outputBufferField = DepotBehaviour.class.getDeclaredField("processingOutputBuffer");
            outputBufferField.setAccessible(true);
            Object outputBuffer = outputBufferField.get(depot);
            if (outputBuffer instanceof ItemStackHandler handler) {
                return ItemHandlerHelper.insertItemStacked(handler, stack, false);
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return stack;
    }

    private boolean completeBeltProcessing(BeltBlockEntity belt, TransportedItemStack transported) {
        if (lastRecipe == null || !canContinueBeltCycle(transported.stack)) {
            abortBeltCycle();
            return false;
        }

        MechanicalStamperBlockEntity partner = isolatedCycle ? null : getValidPartner(lastRecipe, transported.stack);
        consumeRecipeInputs(lastRecipe);
        if (partner != null) {
            partner.consumeRecipeInputs(lastRecipe);
        }

        transported.clearFanProcessingData();
        ItemStack originalRemainder = transported.stack.copy();
        originalRemainder.shrink(1);

        List<ItemStack> outputs = lastRecipe.getResults().stream()
                .map(output -> output.rollOutput(level.random))
                .filter(result -> !result.isEmpty())
                .toList();

        if (outputs.isEmpty()) {
            transported.stack = originalRemainder;
            markPartnerBeltProcessed(partner);
            return transported.stack.isEmpty();
        }

        transported.stack = outputs.getFirst();
        placeBeltItemAtStamper(transported);

        BeltInventory inventory = getBeltInventory(belt);
        if (inventory != null) {
            if (!originalRemainder.isEmpty()) {
                TransportedItemStack remainderStack = transported.copy();
                remainderStack.stack = originalRemainder;
                placeBeltItemAtStamper(remainderStack);
                inventory.addItem(remainderStack);
            }
            for (int i = 1; i < outputs.size(); i++) {
                TransportedItemStack extra = transported.copy();
                extra.stack = outputs.get(i);
                placeBeltItemAtStamper(extra);
                inventory.addItem(extra);
            }
        }

        markPartnerBeltProcessed(partner);
        syncData();
        return false;
    }

    private void markPartnerBeltProcessed(@Nullable MechanicalStamperBlockEntity partner) {
        if (partner == null) {
            return;
        }
        partner.processedThisCycle = true;
        partner.state = RunningState.RETRACTING;
        partner.sendData();
    }

    private static BeltInventory getBeltInventory(BeltBlockEntity belt) {
        BeltBlockEntity controller = belt.getControllerBE();
        if (controller == null) {
            controller = belt;
        }
        return controller.getInventory();
    }

    private void holdBeltItemAtStamper(BeltBlockEntity belt, TransportedItemStack transported) {
        placeBeltItemAtStamper(transported);
        transported.locked = true;
        belt.notifyUpdate();
    }

    private void placeBeltItemAtStamper(TransportedItemStack transported) {
        float position = beltProcessSegment + 0.5F;
        transported.beltPosition = position;
        transported.prevBeltPosition = position;
    }

    private void abortBeltCycle() {
        MechanicalStamperBlockEntity partner = null;
        if (partnerPos != null && level != null
                && level.getBlockEntity(partnerPos) instanceof MechanicalStamperBlockEntity foundPartner) {
            partner = foundPartner;
        }
        finishCycle();
        if (partner != null) {
            partner.finishCycle();
        }
    }

    private static int findBeltSegment(BeltBlockEntity belt, TransportedItemStack transported, float nextOffset) {
        int currentSegment = (int) transported.beltPosition;
        int nextSegment = (int) nextOffset;
        int minSegment = Math.max(0, Math.min(currentSegment, nextSegment) - 1);
        int maxSegment = Math.min(belt.beltLength - 1, Math.max(currentSegment, nextSegment) + 1);

        for (int segment = minSegment; segment <= maxSegment; segment++) {
            float center = segment + 0.5F;
            float from = transported.beltPosition;
            float to = nextOffset;
            if (Math.abs(from - center) <= 0.51F || Math.min(from, to) <= center && Math.max(from, to) >= center) {
                return segment;
            }
        }
        return -1;
    }

    private void consumeRecipeInputs(MechanicalStamperRecipe recipe) {
        recipe.getStamp().ifPresent(stamp -> {
            if (level != null && level.random.nextFloat() <= stamp.consumptionChance()) {
                stampInv.extractItem(0, 1, false);
            }
        });
        recipe.getFillMedium().ifPresent(medium -> mediumInv.extractItem(0, medium.count(), false));
        recipe.getFillFluid().ifPresent(fluid -> fluidTank.drain(fluid.amount(), IFluidHandler.FluidAction.EXECUTE));
    }

    private void consumeManifoldStampFill() {
        if (targetManifoldUsesFluid) {
            fluidTank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        } else {
            mediumInv.extractItem(0, 1, false);
        }
    }

    private void finishCycle() {
        state = RunningState.IDLE;
        prevTimer = 0;
        timer = 0;
        extensionProgress = 0;
        activeController = false;
        isolatedCycle = false;
        processedThisCycle = false;
        processingBeltItem = false;
        processingManifoldBlock = false;
        beltProcessSegment = -1;
        targetEntityId = null;
        targetDepotPos = null;
        partnerPos = null;
        lastRecipe = null;
        lastRecipeId = null;
        targetManifoldPos = null;
        targetManifoldFace = null;
        targetManifoldStampId = null;
        targetManifoldFillId = null;
        targetManifoldUsesFluid = false;
        sendData();
    }

    private void updateExtensionProgress() {
        int midpoint = Math.max(1, cycleDuration / 2);
        if (timer <= midpoint) {
            extensionProgress = Mth.clamp(timer / (float) midpoint, 0, 1);
        } else {
            extensionProgress = Mth.clamp((cycleDuration - timer) / (float) Math.max(1, cycleDuration - midpoint), 0, 1);
        }
    }

    public float getRenderedHeadOffset(float partialTicks) {
        if (state == RunningState.IDLE) {
            return 0;
        }
        int midpoint = Math.max(1, cycleDuration / 2);
        float interpolatedTimer = Mth.lerp(partialTicks, prevTimer, timer);
        if (interpolatedTimer <= midpoint) {
            return Mth.clamp(interpolatedTimer / midpoint, 0, 1);
        }
        return Mth.clamp((cycleDuration - interpolatedTimer) / Math.max(1f, cycleDuration - midpoint), 0, 1);
    }

    private void advanceAnimationTimer() {
        prevTimer = timer;
        timer += getRunningTickSpeed();
    }

    private int getRunningTickSpeed() {
        float speed = getEffectiveCycleSpeed();
        if (speed == 0) {
            return 0;
        }
        return Math.max(1, (int) Mth.lerp(Mth.clamp(Math.abs(speed) / 512f, 0, 1), 1, 60));
    }

    private float getEffectiveCycleSpeed() {
        if (isolatedCycle || partnerPos == null || level == null) {
            return getSpeed();
        }
        if (!(level.getBlockEntity(partnerPos) instanceof MechanicalStamperBlockEntity partner)) {
            return 0;
        }
        float self = Math.abs(getSpeed());
        float other = Math.abs(partner.getSpeed());
        return Math.min(self, other);
    }

    private void clearStaleRecipe() {
        if (state == RunningState.IDLE) {
            lastRecipe = null;
            lastRecipeId = null;
            targetEntityId = null;
            targetDepotPos = null;
            partnerPos = null;
            processingManifoldBlock = false;
            targetManifoldPos = null;
            targetManifoldFace = null;
            targetManifoldStampId = null;
            targetManifoldFillId = null;
            targetManifoldUsesFluid = false;
        }
    }

    @Nullable
    private ManifoldStampTarget createManifoldStampTarget(ManifoldBlockEntity manifold) {
        ItemStack stamp = getStampItem();
        if (stamp.isEmpty()) {
            return null;
        }

        ResourceLocation stampId = BuiltInRegistries.ITEM.getKey(stamp.getItem());
        FluidStack fillFluid = getFillFluid();
        if (!fillFluid.isEmpty() && fillFluid.getAmount() >= 1000) {
            ResourceLocation fillId = BuiltInRegistries.FLUID.getKey(fillFluid.getFluid());
            ManifoldAssemblyAction.Stamp action = createManifoldStampAction(manifold, stampId, fillId, true);
            return action == null ? null : new ManifoldStampTarget(action, true);
        }

        ItemStack medium = getFillMedium();
        if (medium.isEmpty()) {
            return null;
        }

        ResourceLocation fillId = BuiltInRegistries.ITEM.getKey(medium.getItem());
        ManifoldAssemblyAction.Stamp action = createManifoldStampAction(manifold, stampId, fillId, false);
        return action == null ? null : new ManifoldStampTarget(action, false);
    }

    @Nullable
    private ManifoldAssemblyAction.Stamp createManifoldStampAction(ManifoldBlockEntity manifold, ResourceLocation stampId,
                                                                   ResourceLocation fillId, boolean usesFluidFill) {
        if (stampId == null || fillId == null || getStampItem().isEmpty()
                || !stampId.equals(BuiltInRegistries.ITEM.getKey(getStampItem().getItem()))) {
            return null;
        }
        if (usesFluidFill) {
            FluidStack fillFluid = getFillFluid();
            if (fillFluid.isEmpty() || fillFluid.getAmount() < 1000
                    || !fillId.equals(BuiltInRegistries.FLUID.getKey(fillFluid.getFluid()))) {
                return null;
            }
        } else {
            ItemStack medium = getFillMedium();
            if (medium.isEmpty() || !fillId.equals(BuiltInRegistries.ITEM.getKey(medium.getItem()))) {
                return null;
            }
        }

        Direction localFace = ManifoldAssemblySession.worldFaceToLocalFace(manifold.getBlockState(), getFacing().getOpposite());
        return new ManifoldAssemblyAction.Stamp(localFace, stampId, fillId);
    }

    @Nullable
    private WorkTarget findWorldItemTarget() {
        BlockPos workPos = getPotentialPairedWorkPos();
        AABB searchBox = new AABB(workPos).inflate(.35);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, searchBox)) {
            if (!entity.isAlive() || entity.getItem().isEmpty()) {
                continue;
            }
            Optional<RecipeHolder<MechanicalStamperRecipe>> recipe = findRecipe(entity.getItem(), true);
            if (recipe.isPresent() && !recipe.get().value().isIsolatedStamper()) {
                return new WorkTarget(entity, null, entity.getItem(), true);
            }
        }
        WorkTarget depotTarget = findDepotTarget(workPos);
        if (depotTarget != null) {
            Optional<RecipeHolder<MechanicalStamperRecipe>> recipe = findRecipe(depotTarget.stack(), true);
            if (recipe.isPresent() && !recipe.get().value().isIsolatedStamper()) {
                return depotTarget;
            }
        }

        BlockPos isolatedWorkPos = getIsolatedWorkPos();
        if (isolatedWorkPos.equals(workPos)) {
            return null;
        }
        searchBox = new AABB(isolatedWorkPos).inflate(.35);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, searchBox)) {
            if (!entity.isAlive() || entity.getItem().isEmpty()) {
                continue;
            }
            Optional<RecipeHolder<MechanicalStamperRecipe>> recipe = findRecipe(entity.getItem(), false);
            if (recipe.isPresent() && recipe.get().value().isIsolatedStamper()) {
                return new WorkTarget(entity, null, entity.getItem(), false);
            }
        }
        depotTarget = findDepotTarget(isolatedWorkPos);
        if (depotTarget != null) {
            Optional<RecipeHolder<MechanicalStamperRecipe>> recipe = findRecipe(depotTarget.stack(), false);
            if (recipe.isPresent() && recipe.get().value().isIsolatedStamper()) {
                return depotTarget;
            }
        }
        return null;
    }

    @Nullable
    private WorkTarget findDepotTarget(BlockPos workPos) {
        BlockPos depotPos = workPos.below();
        DepotBehaviour depot = BlockEntityBehaviour.get(level, depotPos, DepotBehaviour.TYPE);
        if (depot == null || depot.getHeldItemStack().isEmpty()) {
            return null;
        }
        return new WorkTarget(null, depotPos, depot.getHeldItemStack(), workPos.equals(getPotentialPairedWorkPos()));
    }

    private Optional<RecipeHolder<MechanicalStamperRecipe>> findRecipe(ItemStack target, boolean allowPaired) {
        MechanicalStamperRecipeInput input = createInput(target, allowPaired);
        return level.getRecipeManager()
                .getRecipeFor(ModRecipeTypes.MECHANICAL_STAMPING_TYPE.get(), input, level)
                .filter(holder -> ResearchRecipeGate.canUseServerRecipe(level, holder));
    }

    private MechanicalStamperRecipeInput createInput(ItemStack target, boolean paired) {
        return new MechanicalStamperRecipeInput(target, getStampItem(), getFillMedium(), getFillFluid(), paired);
    }

    @Nullable
    private MechanicalStamperBlockEntity getAvailablePartner(MechanicalStamperRecipe recipe, ItemStack target) {
        MechanicalStamperBlockEntity partner = getPartner();
        if (partner == null || partner.state != RunningState.IDLE || partner.getSpeed() == 0) {
            return null;
        }
        if (!recipe.matches(partner.createInput(target, true), level)) {
            return null;
        }
        return partner;
    }

    @Nullable
    private MechanicalStamperBlockEntity getValidPartner(MechanicalStamperRecipe recipe, ItemStack target) {
        if (partnerPos == null || level == null) {
            return null;
        }
        if (!(level.getBlockEntity(partnerPos) instanceof MechanicalStamperBlockEntity partner)) {
            return null;
        }
        if (!isValidPartnerBlock(partner) || partner.getSpeed() == 0) {
            return null;
        }
        return recipe.matches(partner.createInput(target, true), level) ? partner : null;
    }

    @Nullable
    private MechanicalStamperBlockEntity getPartner() {
        if (level == null) {
            return null;
        }
        BlockPos candidatePos = worldPosition.relative(getFacing(), PARTNER_DISTANCE);
        if (!(level.getBlockEntity(candidatePos) instanceof MechanicalStamperBlockEntity partner)) {
            return null;
        }
        return isValidPartnerBlock(partner) ? partner : null;
    }

    private boolean isValidPartnerBlock(MechanicalStamperBlockEntity partner) {
        BlockState state = partner.getBlockState();
        return state.getBlock() instanceof MechanicalStamperBlock && partner.getFacing() == getFacing().getOpposite();
    }

    private void pinTargetEntity() {
        if (targetDepotPos != null) {
            return;
        }
        ItemEntity target = getTargetEntity();
        if (target == null) {
            return;
        }
        Vec3 center = VecHelper.getCenterOf(getActiveWorkPos());
        target.setDeltaMovement(Vec3.ZERO);
        target.setPos(center.x, center.y, center.z);
    }

    @Nullable
    private ItemEntity getTargetEntity() {
        if (level == null || targetEntityId == null) {
            return null;
        }
        if (level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(targetEntityId);
            return entity instanceof ItemEntity itemEntity && itemEntity.isAlive() ? itemEntity : null;
        }
        AABB search = new AABB(getActiveWorkPos()).inflate(1);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, search)) {
            if (targetEntityId.equals(entity.getUUID())) {
                return entity;
            }
        }
        return null;
    }

    @Nullable
    private ActiveTarget getActiveTarget() {
        ItemEntity itemEntity = getTargetEntity();
        if (itemEntity != null && !itemEntity.getItem().isEmpty()) {
            return new ActiveTarget(itemEntity, null, null, itemEntity.getItem());
        }

        if (targetDepotPos == null || level == null) {
            return null;
        }
        DepotBehaviour depot = BlockEntityBehaviour.get(level, targetDepotPos, DepotBehaviour.TYPE);
        if (depot == null || depot.getHeldItemStack().isEmpty()) {
            return null;
        }
        return new ActiveTarget(null, targetDepotPos, depot, depot.getHeldItemStack());
    }

    private BlockPos getActiveWorkPos() {
        return isolatedCycle ? getIsolatedWorkPos() : getPotentialPairedWorkPos();
    }

    private BlockPos getPotentialPairedWorkPos() {
        return worldPosition.relative(getFacing(), PAIRED_WORK_DISTANCE);
    }

    private BlockPos getIsolatedWorkPos() {
        return worldPosition.relative(getFacing());
    }

    @Override
    public float calculateStressApplied() {
        return (float) ModStressValues.MECHANICAL_STAMPER_STRESS;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("StampInv", stampInv.serializeNBT(registries));
        tag.put("MediumInv", mediumInv.serializeNBT(registries));
        tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putString("RunningState", state.name());
        tag.putInt("Timer", timer);
        tag.putInt("PrevTimer", prevTimer);
        tag.putInt("ScanCooldown", scanCooldown);
        tag.putFloat("ExtensionProgress", extensionProgress);
        tag.putBoolean("ActiveController", activeController);
        tag.putBoolean("IsolatedCycle", isolatedCycle);
        tag.putBoolean("ProcessedThisCycle", processedThisCycle);
        tag.putInt("CycleDuration", cycleDuration);
        if (partnerPos != null) {
            tag.put("PartnerPos", NbtUtils.writeBlockPos(partnerPos));
        }
        if (targetEntityId != null) {
            tag.putUUID("TargetEntity", targetEntityId);
        }
        if (targetDepotPos != null) {
            tag.put("TargetDepotPos", NbtUtils.writeBlockPos(targetDepotPos));
        }
        tag.putBoolean("ProcessingBeltItem", processingBeltItem);
        tag.putInt("BeltProcessSegment", beltProcessSegment);
        tag.putBoolean("ProcessingManifoldBlock", processingManifoldBlock);
        if (targetManifoldPos != null) {
            tag.put("TargetManifoldPos", NbtUtils.writeBlockPos(targetManifoldPos));
        }
        if (targetManifoldFace != null) {
            tag.putString("TargetManifoldFace", targetManifoldFace.getSerializedName());
        }
        if (targetManifoldStampId != null) {
            tag.putString("TargetManifoldStamp", targetManifoldStampId.toString());
        }
        if (targetManifoldFillId != null) {
            tag.putString("TargetManifoldFill", targetManifoldFillId.toString());
        }
        tag.putBoolean("TargetManifoldUsesFluid", targetManifoldUsesFluid);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("StampInv")) {
            stampInv.deserializeNBT(registries, tag.getCompound("StampInv"));
        }
        if (tag.contains("MediumInv")) {
            mediumInv.deserializeNBT(registries, tag.getCompound("MediumInv"));
        }
        if (tag.contains("FluidTank")) {
            fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
        }
        if (tag.contains("RunningState")) {
            try {
                state = RunningState.valueOf(tag.getString("RunningState"));
            } catch (IllegalArgumentException ignored) {
                state = RunningState.IDLE;
            }
        }
        timer = tag.getInt("Timer");
        prevTimer = tag.contains("PrevTimer") ? tag.getInt("PrevTimer") : timer;
        if (clientPacket) {
            prevTimer = timer;
        }
        scanCooldown = tag.contains("ScanCooldown") ? tag.getInt("ScanCooldown") : ENTITY_SCAN_INTERVAL;
        extensionProgress = tag.getFloat("ExtensionProgress");
        activeController = tag.getBoolean("ActiveController");
        isolatedCycle = tag.getBoolean("IsolatedCycle");
        processedThisCycle = tag.getBoolean("ProcessedThisCycle");
        cycleDuration = tag.contains("CycleDuration") ? Math.max(2, tag.getInt("CycleDuration")) : MechanicalStamperRecipe.DEFAULT_PROCESSING_TIME;
        partnerPos = tag.contains("PartnerPos") ? NbtUtils.readBlockPos(tag, "PartnerPos").orElse(null) : null;
        targetEntityId = tag.hasUUID("TargetEntity") ? tag.getUUID("TargetEntity") : null;
        targetDepotPos = tag.contains("TargetDepotPos") ? NbtUtils.readBlockPos(tag, "TargetDepotPos").orElse(null) : null;
        processingBeltItem = tag.getBoolean("ProcessingBeltItem");
        beltProcessSegment = tag.contains("BeltProcessSegment") ? tag.getInt("BeltProcessSegment") : -1;
        processingManifoldBlock = tag.getBoolean("ProcessingManifoldBlock");
        targetManifoldPos = tag.contains("TargetManifoldPos") ? NbtUtils.readBlockPos(tag, "TargetManifoldPos").orElse(null) : null;
        targetManifoldFace = tag.contains("TargetManifoldFace")
                ? Direction.byName(tag.getString("TargetManifoldFace"))
                : null;
        targetManifoldStampId = tag.contains("TargetManifoldStamp")
                ? ResourceLocation.tryParse(tag.getString("TargetManifoldStamp"))
                : null;
        targetManifoldFillId = tag.contains("TargetManifoldFill")
                ? ResourceLocation.tryParse(tag.getString("TargetManifoldFill"))
                : null;
        targetManifoldUsesFluid = tag.getBoolean("TargetManifoldUsesFluid");
    }

    private void syncData() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void dropItemContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        dropStoredItem(stampInv.getStackInSlot(0));
        stampInv.setStackInSlot(0, ItemStack.EMPTY);
        dropStoredItem(mediumInv.getStackInSlot(0));
        mediumInv.setStackInSlot(0, ItemStack.EMPTY);
    }

    private void dropStoredItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5, stack.copy());
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("     Mechanical Stamper:"));
        ItemStack stamp = stampInv.getStackInSlot(0);
        tooltip.add(stamp.isEmpty()
                ? Component.literal("Stamp: empty")
                : Component.literal("Stamp: " + stamp.getCount() + " x " + stamp.getHoverName().getString()));

        ItemStack medium = mediumInv.getStackInSlot(0);
        tooltip.add(medium.isEmpty()
                ? Component.literal("Fill Medium: empty")
                : Component.literal("Fill Medium: " + medium.getCount() + " x " + medium.getHoverName().getString()));

        FluidStack fluid = fluidTank.getFluid();
        tooltip.add(fluid.isEmpty()
                ? Component.literal("Fill Fluid: empty")
                : Component.literal("Fill Fluid: " + fluid.getAmount() + "mb " + fluid.getHoverName().getString()));

        if (state != RunningState.IDLE) {
            tooltip.add(Component.literal("Processing: " + Math.min(timer, cycleDuration) + "/" + cycleDuration + "t"));
        }
        return true;
    }

    private record WorkTarget(@Nullable ItemEntity itemEntity, @Nullable BlockPos depotPos, ItemStack stack,
                              boolean paired) {
    }

    private record ActiveTarget(@Nullable ItemEntity itemEntity, @Nullable BlockPos depotPos,
                                @Nullable DepotBehaviour depot, ItemStack stack) {
    }

    private record ManifoldStampTarget(ManifoldAssemblyAction.Stamp action, boolean usesFluidFill) {
    }
}
