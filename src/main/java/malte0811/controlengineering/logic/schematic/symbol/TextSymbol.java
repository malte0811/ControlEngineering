package malte0811.controlengineering.logic.schematic.symbol;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.noncrafting.ServerFontRecipe;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TextSymbol extends SchematicSymbol<String> {
    public static final String NAME_KEY = ControlEngineering.MODID + ".symbol.text";
    public static final double SCALE = 4 / 9.;

    public TextSymbol() {
        super("Test", MyCodecs.STRING);
    }

    @Override
    public int getXSize(String state, @Nonnull Level level) {
        return Mth.ceil(ServerFontRecipe.getWidth(level, state) * SCALE);
    }

    @Override
    public int getYSize(String state, @Nonnull Level level) {
        return 4;
    }

    @Override
    public List<SymbolPin> getPins(@Nullable String s) {
        return List.of();
    }

    @Override
    public Component getName() {
        return new TranslatableComponent(NAME_KEY);
    }
}
