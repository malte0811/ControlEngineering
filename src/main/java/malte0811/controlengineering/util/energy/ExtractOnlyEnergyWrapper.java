package malte0811.controlengineering.util.energy;

import net.minecraftforge.energy.IEnergyStorage;

public class ExtractOnlyEnergyWrapper implements IEnergyStorage {
    private final IEnergyStorage wrapped;

    public ExtractOnlyEnergyWrapper(IEnergyStorage wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return wrapped.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return wrapped.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return wrapped.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
