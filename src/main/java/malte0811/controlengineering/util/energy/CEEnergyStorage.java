package malte0811.controlengineering.util.energy;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;

public class CEEnergyStorage implements IEnergyStorage {
    private final int capacity;
    private final int maxIn;
    private final int maxOut;
    private int energyStored;

    public CEEnergyStorage(int capacity, int maxIn, int maxOut) {
        this.capacity = capacity;
        this.maxIn = maxIn;
        this.maxOut = maxOut;
    }

    public void setEnergyStored(int energyStored) {
        this.energyStored = energyStored;
    }

    public INBT writeNBT() {
        return IntNBT.valueOf(energyStored);
    }

    public void readNBT(INBT nbt) {
        if (nbt instanceof NumberNBT) {
            setEnergyStored(((NumberNBT) nbt).getInt());
        } else {
            setEnergyStored(0);
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        final int received = Math.min(maxIn, Math.min(maxReceive, capacity - energyStored));
        if (!simulate) {
            energyStored += received;
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        final int extracted = Math.min(maxOut, Math.min(maxExtract, energyStored));
        if (!simulate) {
            energyStored -= extracted;
        }
        return extracted;
    }

    public boolean extractOrTrue(int amount) {
        if (energyStored < amount) {
            return true;
        } else {
            energyStored -= amount;
            return false;
        }
    }

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return maxOut > 0;
    }

    @Override
    public boolean canReceive() {
        return maxIn > 0;
    }

    @Nonnull
    public IEnergyStorage insertOnlyView() {
        return new ExtractOnlyEnergyWrapper(this);
    }
}
