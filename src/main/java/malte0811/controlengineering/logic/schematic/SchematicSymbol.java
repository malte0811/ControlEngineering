package malte0811.controlengineering.logic.schematic;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface SchematicSymbol {
    float BASE_SCALE = 3;

    void render(MatrixStack transform, int x, int y);

    int getXSize();

    int getYSize();

    boolean allowIntersecting();
}
