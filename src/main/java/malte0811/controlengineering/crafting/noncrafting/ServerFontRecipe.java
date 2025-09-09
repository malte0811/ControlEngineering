package malte0811.controlengineering.crafting.noncrafting;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.AbstractInt2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.crafting.CERecipeTypes;
import malte0811.controlengineering.util.RLUtils;
import malte0811.dualcodecs.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public class ServerFontRecipe extends BaseRecipe {
    public static final ResourceLocation LOCATION = RLUtils.ceLoc("server_font_width");

    private static final DualCodec<ByteBuf, Int2FloatMap.Entry> INNER_CODECS = DualCompositeCodecs.composite(
            DualCodecs.INT.fieldOf("character"), Int2FloatMap.Entry::getIntKey,
            DualCodecs.FLOAT.fieldOf("width"), Int2FloatMap.Entry::getFloatValue,
            AbstractInt2FloatMap.BasicEntry::new
    );
    public static final DualMapCodec<RegistryFriendlyByteBuf, ServerFontRecipe> CODECS = INNER_CODECS.listOf()
            .map(
                    entries -> {
                        Int2FloatMap result = new Int2FloatOpenHashMap();
                        for (var entry : entries) {
                            result.put(entry.getIntKey(), entry.getFloatValue());
                        }
                        return result;
                    },
                    m -> new ArrayList<>(m.int2FloatEntrySet())
            )
            .map(ServerFontRecipe::new, r -> r.widths)
            .<RegistryFriendlyByteBuf>castStream()
            .fieldOf("widths");

    public static boolean IN_UNIT_TEST = false;

    private final Int2FloatMap widths;

    public ServerFontRecipe(Int2FloatMap widths) {
        super(CERecipeSerializers.FONT_WIDTH, CERecipeTypes.SERVER_FONT.get());
        this.widths = widths;
    }

    public static float getWidth(Level level, String text) {
        if (IN_UNIT_TEST) {
            return text.length();
        }
        var recipe = level.getRecipeManager().byKey(LOCATION).orElse(null);
        if (recipe == null || !(recipe.value() instanceof ServerFontRecipe serverFont)) {
            return Float.POSITIVE_INFINITY;
        }
        return (float) text.chars()
                .mapToDouble(serverFont.widths::get)
                .sum();
    }
}
