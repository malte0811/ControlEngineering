package malte0811.controlengineering.blockentity.logic;

import malte0811.controlengineering.logic.schematic.Schematic;

public interface ISchematicBE {
    Schematic getSchematic();

    void setSchematicChanged();
}
