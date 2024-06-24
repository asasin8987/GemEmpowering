import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class GemEmpoweringStationSidedBlockEntity extends BlockEntity implements MenuProvider {
    public GemEmpoweringStationSidedBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.GEM_EMPOWERING_STATION_SIDED.get(), pPos, pBlockState);

        data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index)
                {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    case 2 -> energy;
                    case 3 -> maxEnergy;
                    case 4 -> fluid;
                    case 5 -> maxFluid;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index)
                {
                    case 0 -> progress = value;
                    case 1 -> maxProgress = value;
                    case 2 -> energy = value;
                    case 3 -> maxEnergy = value;
                    case 4 -> fluid = value;
                    case 5 -> maxFluid = value;
                }
            }

            @Override
            public int getCount() {
                return 6;
            }
        };
    }

    private final Component DISPLAY_NAME = Component.translatable(FirstMod.MODID + ".GemEmpoweringStationSided.BE");
    public static final int INPUT_SLOT = 0;
    public static final int FLUID_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int ENERGY_SLOT = 3;
    private final ItemStackHandler inputItemStackHandler = new ItemStackHandler(1);
    private final ItemStackHandler fluidItemStackHandler = new ItemStackHandler(1){
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() == Items.WATER_BUCKET ||
                    stack.getItem() == Items.LAVA_BUCKET  ||
                    stack.getItem() == Items.MILK_BUCKET;
        }
    };

    private final ItemStackHandler outputItemStackHandler = new ItemStackHandler(1){
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    };
    private final ItemStackHandler energyItemStackHandler = new ItemStackHandler() {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() == ModItems.grassShard.get();
        }
    };

    LazyOptional<ItemStackHandler> inputOptional = LazyOptional.empty();
    LazyOptional<ItemStackHandler> fluidOptional = LazyOptional.empty();
    LazyOptional<ItemStackHandler> outputOptional = LazyOptional.empty();
    LazyOptional<ItemStackHandler> energyOptional = LazyOptional.empty();
    public LazyOptional<ItemStackHandler> getInputOptional() {
        return inputOptional;
    }
    public LazyOptional<ItemStackHandler> getFluidOptional() {
        return fluidOptional;
    }
    public LazyOptional<ItemStackHandler> getEnergyOptional() {
        return energyOptional;
    }
    public LazyOptional<ItemStackHandler> getOutputOptional() {
        return outputOptional;
    }

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 78;
    private int energy = 0;
    private int maxEnergy = 2000;
    private int fluid = 0;
    private int maxFluid = 500;

    @Override
    public @NotNull Component getDisplayName() {
        return DISPLAY_NAME;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerID, Inventory inventory, Player player) {
        return new GemEmpoweringStationSidedMenu(containerID, inventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put(FirstMod.MODID +  ".input",  inputItemStackHandler.serializeNBT());
        pTag.put(FirstMod.MODID +  ".fluid",  fluidItemStackHandler.serializeNBT());
        pTag.put(FirstMod.MODID + ".energy", energyItemStackHandler.serializeNBT());
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
         inputItemStackHandler. deserializeNBT(pTag.getCompound(FirstMod.MODID + ".input"));
        outputItemStackHandler. deserializeNBT(pTag.getCompound(FirstMod.MODID + ".output"));
         fluidItemStackHandler. deserializeNBT(pTag.getCompound(FirstMod.MODID + ".fluid"));
        energyItemStackHandler. deserializeNBT(pTag.getCompound(FirstMod.MODID + ".energy"));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        inputOptional  = LazyOptional.of(() ->  inputItemStackHandler);
        outputOptional = LazyOptional.of(() ->  outputItemStackHandler);
        fluidOptional  = LazyOptional.of(() ->  fluidItemStackHandler);
        energyOptional = LazyOptional.of(() ->  energyItemStackHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputOptional.invalidate();
        outputOptional.invalidate();
        fluidOptional.invalidate();
        energyOptional.invalidate();
    }
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(4);
        int index = 0;
        inventory.setItem(index++,  inputItemStackHandler.getStackInSlot(0));
        inventory.setItem(index++, outputItemStackHandler.getStackInSlot(0));
        inventory.setItem(index++,  fluidItemStackHandler.getStackInSlot(0));
        inventory.setItem(index++, energyItemStackHandler.getStackInSlot(0));
        Containers.dropContents(Objects.requireNonNull(level), worldPosition, inventory);
    }

    public void tick(Level level, BlockPos pPos, BlockState pState) {
        if (isOutputSlotEmptyOrReceivable()) {
            increaseCraftingProcess();
            setChanged(level, pPos, pState);

            if (hasProgressFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }
    private void craftItem() {
        Optional<GemEmpoweringRecipe> recipe = getCurrentRecipe();
        ItemStack resultItem = recipe.get().getResultItem(null);

        inputItemStackHandler.extractItem(0, 1, false);
        outputItemStackHandler.setStackInSlot(0, new ItemStack(resultItem.getItem(),
                outputItemStackHandler.getStackInSlot(0).getCount() + resultItem.getCount()));
    }


    private void resetProgress() {
        this.progress = 0;
    }

    private boolean hasProgressFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseCraftingProcess() {
        this.progress++;
    }

    private boolean hasRecipe() {
        Optional<GemEmpoweringRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) return false;

        ItemStack resultItem = recipe.get().getResultItem(null);
        return canInsertAmountIntoOutputSlot(resultItem.getCount()) && canInsertItemIntoOutputSlot(resultItem.getItem());
    }

    private Optional<GemEmpoweringRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(4);
        /*for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }*/
        return level.getRecipeManager().getRecipeFor(GemEmpoweringRecipe.Type.INSTANCE, inventory, level);
    }

    private boolean canInsertItemIntoOutputSlot(@NotNull Item item) {
        return outputItemStackHandler.getStackInSlot(0).isEmpty() || outputItemStackHandler.getStackInSlot(0).is(item);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        return outputItemStackHandler.getStackInSlot(0).getMaxStackSize() >=
                outputItemStackHandler.getStackInSlot(0).getCount() + count;
    }

    private boolean isOutputSlotEmptyOrReceivable() {
        return outputItemStackHandler.getStackInSlot(0).isEmpty() ||
                outputItemStackHandler.getStackInSlot(0).getCount() < outputItemStackHandler.getStackInSlot(0).getMaxStackSize();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side == Direction.UP)
        {
            return inputOptional.cast();
        }
        else if (cap == ForgeCapabilities.ITEM_HANDLER && side == Direction.WEST)
        {
            return outputOptional.cast();
        }
        else if (cap == ForgeCapabilities.ITEM_HANDLER && side == Direction.EAST)
        {
            return energyOptional.cast();
        }
        else if (cap == ForgeCapabilities.ITEM_HANDLER && side == Direction.DOWN)
        {
            return fluidOptional.cast();
        }

        return super.getCapability(cap, side);
    }
}
