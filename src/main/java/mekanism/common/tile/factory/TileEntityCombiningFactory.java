package mekanism.common.tile.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CombinerCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.common.base.ITileComponent;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.upgrade.CombinerUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntityCombiningFactory extends TileEntityItemToItemFactory<CombinerRecipe> {

    private final IInputHandler<@NonNull ItemStack> extraInputHandler;

    private InputInventorySlot extraSlot;

    public TileEntityCombiningFactory(IBlockProvider blockProvider) {
        super(blockProvider);
        extraInputHandler = InputHelper.getInputHandler(extraSlot);
    }

    @Override
    protected void addSlots(InventorySlotHelper builder) {
        super.addSlots(builder);
        builder.addSlot(extraSlot = InputInventorySlot.at(stack -> containsRecipe(recipe -> recipe.getExtraInput().testType(stack)), this, 7, 57));
        extraSlot.setSlotType(ContainerSlotType.EXTRA);
    }

    @Override
    public boolean isValidInputItem(@Nonnull ItemStack stack) {
        return containsRecipe(recipe -> recipe.getMainInput().testType(stack));
    }

    @Override
    public boolean inputProducesOutput(int process, @Nonnull ItemStack fallbackInput, @Nonnull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot,
          boolean updateCache) {
        if (outputSlot.isEmpty()) {
            return true;
        }
        CachedRecipe<CombinerRecipe> cached = getCachedRecipe(process);
        if (cached != null) {
            CombinerRecipe cachedRecipe = cached.getRecipe();
            if (cachedRecipe.getMainInput().testType(fallbackInput) && (extraSlot.isEmpty() || cachedRecipe.getExtraInput().testType(extraSlot.getStack()))) {
                //Our input matches the recipe we have cached for this slot
                return true;
            }
            //If there is no cached item input or it doesn't match our fallback then it is an out of date cache, so we ignore the fact that we have a cache
        }
        //TODO: Decide if recipe.getOutput *should* assume that it is given a valid input or not
        // Here we are using it as if it is not assuming it, but that is in part because it currently does not care about the value passed
        // and if something does have extra checking to check the input as long as it checks for invalid ones this should still work
        ItemStack extra = extraSlot.getStack();
        ItemStack output = outputSlot.getStack();
        CombinerRecipe foundRecipe = findFirstRecipe(recipe -> {
            if (recipe.getMainInput().testType(fallbackInput)) {
                if (extra.isEmpty() || recipe.getExtraInput().testType(extra)) {
                    return ItemHandlerHelper.canItemStacksStack(recipe.getOutput(fallbackInput, extra), output);
                }
            }
            return false;
        });
        if (foundRecipe == null) {
            //We could not find any valid recipe for the given item that matches the items in the current output slots
            return false;
        }
        if (updateCache) {
            //If we want to update the cache, then create a new cache with the recipe we found
            CachedRecipe<CombinerRecipe> newCachedRecipe = createNewCachedRecipe(foundRecipe, process);
            if (newCachedRecipe == null) {
                //If we want to update the cache but failed to create a new cache then return that the item is not valid for the slot as something goes wrong
                // I believe we can actually make createNewCachedRecipe Nonnull which will remove this if statement
                return false;
            }
            updateCachedRecipe(newCachedRecipe, process);
        }
        return true;
    }

    @Nonnull
    @Override
    public MekanismRecipeType<CombinerRecipe> getRecipeType() {
        return MekanismRecipeType.COMBINING;
    }

    @Nullable
    @Override
    public CombinerRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandlers[cacheIndex].getInput();
        if (stack.isEmpty()) {
            return null;
        }
        ItemStack extra = extraInputHandler.getInput();
        if (extra.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(stack, extra));
    }

    @Override
    public CachedRecipe<CombinerRecipe> createNewCachedRecipe(@Nonnull CombinerRecipe recipe, int cacheIndex) {
        return new CombinerCachedRecipe(recipe, inputHandlers[cacheIndex], extraInputHandler, outputHandlers[cacheIndex])
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty)
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks);
    }

    @Override
    public void parseUpgradeData(@Nonnull IUpgradeData upgradeData) {
        if (upgradeData instanceof CombinerUpgradeData) {
            CombinerUpgradeData data = (CombinerUpgradeData) upgradeData;
            redstone = data.redstone;
            setControlType(data.controlType);
            setEnergy(data.electricityStored);
            sorting = data.sorting;
            //TODO: Transfer recipe ticks?
            //TODO: Transfer operating ticks properly
            extraSlot.setStack(data.extraSlot.getStack());
            energySlot.setStack(data.energySlot.getStack());
            for (int i = 0; i < data.inputSlots.size(); i++) {
                inputSlots.get(i).setStack(data.inputSlots.get(i).getStack());
            }
            for (int i = 0; i < data.outputSlots.size(); i++) {
                outputSlots.get(i).setStack(data.outputSlots.get(i).getStack());
            }
            for (ITileComponent component : getComponents()) {
                component.read(data.components);
            }
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public CombinerUpgradeData getUpgradeData() {
        return new CombinerUpgradeData(redstone, getControlType(), getEnergy(), progress, energySlot, extraSlot, inputSlots, outputSlots, sorting, getComponents());
    }
}