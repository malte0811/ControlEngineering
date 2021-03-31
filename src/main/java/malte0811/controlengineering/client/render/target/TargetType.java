package malte0811.controlengineering.client.render.target;

public enum TargetType {
    //Render using TER
    DYNAMIC,
    //Render as block model
    STATIC,
    //Only render in "special" context, e.g. item models
    //TODO rename or split
    SPECIAL,
}
