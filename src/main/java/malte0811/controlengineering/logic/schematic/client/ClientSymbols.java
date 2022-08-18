package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.SchematicNet;
import malte0811.controlengineering.logic.schematic.symbol.*;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static malte0811.controlengineering.logic.schematic.SchematicNet.SELECTED_WIRE_COLOR;
import static malte0811.controlengineering.logic.schematic.SchematicNet.WIRE_COLOR;

public class ClientSymbols {
    private static final Map<SchematicSymbol<?>, ClientSymbol<?, ?>> CLIENT_SYMBOLS = new HashMap<>();

    public static void init() {
        final int secondColumn = 13;
        register(SchematicSymbols.CONSTANT, ClientConstantSymbol::new);
        registerWithOverlay(SchematicSymbols.INPUT_PIN_ANALOG, ClientIOSymbol::new, "A", 4, 0);
        registerWithOverlay(SchematicSymbols.INPUT_PIN_DIGITAL, ClientIOSymbol::new, "D", 4, 0);
        register(SchematicSymbols.OUTPUT_PIN, ClientIOSymbol::new);

        register(SchematicSymbols.AND2, 0, 0);
        register(SchematicSymbols.AND3, 0, 14);
        register(SchematicSymbols.OR2, 0, 7);
        register(SchematicSymbols.OR3, 0, 21);
        register(SchematicSymbols.NAND2, secondColumn, 0);
        register(SchematicSymbols.NAND3, secondColumn, 14);
        register(SchematicSymbols.NOR2, secondColumn, 7);
        register(SchematicSymbols.NOR3, secondColumn, 21);
        register(SchematicSymbols.XOR2, 0, 28);
        register(SchematicSymbols.XOR3, 0, 35);
        register(SchematicSymbols.NOT, secondColumn, 28);
        register(SchematicSymbols.RS_LATCH, secondColumn, 49);
        register(SchematicSymbols.SCHMITT_TRIGGER, 0, 42);
        register(SchematicSymbols.DIGITIZER, secondColumn, 42);
        register(SchematicSymbols.COMPARATOR, secondColumn, 35);
        register(SchematicSymbols.D_LATCH, 0, 49);
        register(SchematicSymbols.DELAY_LINE, 0, 56);
        registerMUX(SchematicSymbols.ANALOG_MUX, "A");
        registerMUX(SchematicSymbols.DIGITAL_MUX, "D");
        register(SchematicSymbols.VOLTAGE_DIVIDER, ClientDividerSymbol::new);
        register(SchematicSymbols.ANALOG_ADDER, 24, 11);
        register(SchematicSymbols.INVERTING_AMPLIFIER, ClientInvAmpSymbol::new);
        register(SchematicSymbols.TEXT, ClientTextSymbol::new);
        register(SchematicSymbols.CONFIG_SWITCH, ClientConfigSwitch::new);
    }

    public static <State> void renderCenteredInBox(
            SymbolInstance<State> inst, PoseStack transform, int x, int y, int xSpace, int ySpace
    ) {
        final var level = Objects.requireNonNull(Minecraft.getInstance().level);
        final var type = inst.getType();
        final var width = type.getXSize(inst.getCurrentState(), level);
        final var height = type.getYSize(inst.getCurrentState(), level);
        final var scale = Math.max(1, Math.min(width / (float) xSpace, height / (float) ySpace));
        transform.pushPose();
        transform.translate(x + xSpace / 2., y + ySpace / 2., 0);
        transform.scale(scale, scale, 1);
        transform.translate(-width / 2., -height / 2., 0);
        render(inst, transform, 0, 0, 0xff);
        transform.popPose();
    }

    public static <State> void render(SymbolInstance<State> inst, PoseStack transform, int x, int y, int alpha) {
        render(inst.getType(), transform, x, y, inst.getCurrentState(), alpha);
    }

    public static <State>
    void render(SchematicSymbol<State> serverSymbol, PoseStack transform, int x, int y, State state, int alpha) {
        getClientSymbol(serverSymbol).render(transform, x, y, state, alpha);
    }

    public static void render(PlacedSymbol placed, PoseStack transform, int alpha) {
        render(placed.symbol(), transform, placed.position().x(), placed.position().y(), alpha);
    }

    public static void render(Schematic schematic, PoseStack stack, Vec2d mouse) {
        for (PlacedSymbol s : schematic.getSymbols()) {
            render(s, stack, 0xff);
        }
        for (SchematicNet net : schematic.getNets()) {
            final int color = net.contains(mouse.floor()) ? SELECTED_WIRE_COLOR : WIRE_COLOR;
            net.render(stack, color, schematic.getSymbols());
        }
    }

    public static <State>
    void createInstanceWithUI(
            SchematicSymbol<State> symbol, Consumer<? super SymbolInstance<State>> onDone, State initialState
    ) {
        getClientSymbol(symbol).createInstanceWithUI(onDone, initialState);
    }

    public static <State>
    void createInstanceWithUI(SchematicSymbol<State> symbol, Consumer<? super SymbolInstance<State>> onDone) {
        createInstanceWithUI(symbol, onDone, symbol.getInitialState());
    }


    @SuppressWarnings("unchecked")
    private static <State, Symbol extends SchematicSymbol<State>>
    ClientSymbol<State, Symbol> getClientSymbol(Symbol server) {
        return (ClientSymbol<State, Symbol>) CLIENT_SYMBOLS.get(server);
    }

    private static <Cfg> void register(CellSymbol<Cfg> cell, int uMin, int vMin) {
        register(cell, new ClientCellSymbol<>(cell, uMin, vMin));
    }

    private static <Cfg> void registerMUX(CellSymbol<Cfg> cell, String overlay) {
        register(cell, new ClientOverlaySymbol<>(new ClientCellSymbol<>(cell, 13, 56), overlay, 3, 3));
    }

    private static <State, Symbol extends SchematicSymbol<State>>
    void register(Symbol server, Function<Symbol, ClientSymbol<State, Symbol>> makeClient) {
        register(server, makeClient.apply(server));
    }

    private static <State, Symbol extends SchematicSymbol<State>>
    void registerWithOverlay(
            Symbol server,
            Function<Symbol, ClientSymbol<State, Symbol>> makeClient,
            String overlay, float xOff, float yOff
    ) {
        register(server, new ClientOverlaySymbol<>(makeClient.apply(server), overlay, xOff, yOff));
    }

    private static <State, Symbol extends SchematicSymbol<State>>
    void register(Symbol server, ClientSymbol<State, Symbol> client) {
        CLIENT_SYMBOLS.put(server, client);
    }
}
