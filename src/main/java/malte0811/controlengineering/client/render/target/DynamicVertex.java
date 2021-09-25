package malte0811.controlengineering.client.render.target;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;

public class DynamicVertex {
    private final List<Step> steps;

    public DynamicVertex(List<Step> steps) {
        this.steps = steps;
    }

    public void accept(VertexConsumer out, int combinedLight, int combinedOverlay) {
        for (Step step : steps) {
            step.apply(out, combinedLight, combinedOverlay);
        }
        out.endVertex();
    }

    public interface Step {
        void apply(VertexConsumer out, int light, int overlay);
    }
}
