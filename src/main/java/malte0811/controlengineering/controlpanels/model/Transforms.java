package malte0811.controlengineering.controlpanels.model;

import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;

@SuppressWarnings("deprecation")
public class Transforms {
    public static final ItemTransforms PANEL_TRANSFORMS = new ItemTransforms(
            new ItemTransform(
                    new Vector3f(75, 225, 0),
                    new Vector3f(0, 0, 0.125f),
                    new Vector3f(0.375F, 0.375F, 0.375F)
            ),
            new ItemTransform(
                    new Vector3f(75, 45, 0),
                    new Vector3f(0, 0, 0.125f),
                    new Vector3f(0.375F, 0.375F, 0.375F)
            ),
            new ItemTransform(new Vector3f(0, 225, 0), new Vector3f(1, 0, -1), new Vector3f(1, 1, 1)),
            new ItemTransform(new Vector3f(0, 45, 0), new Vector3f(1, 0, -1), new Vector3f(1, 1, 1)),
            ItemTransform.NO_TRANSFORM,
            new ItemTransform(
                    new Vector3f(30, 225, 0),
                    new Vector3f(0, 0, 0),
                    new Vector3f(0.625F, 0.625F, 0.625F)
            ),
            new ItemTransform(new Vector3f(0, 0, 0), new Vector3f(0, .375f, 0), new Vector3f(0.25f, 0.25f, 0.25f)),
            new ItemTransform(new Vector3f(-90, 0, 0), new Vector3f(0, 0, -.5f), new Vector3f(1, 1, 1))
    );

    public static final ItemTransforms PANEL_TOP_TRANSFORMS = new ItemTransforms(
            new ItemTransform(
                    new Vector3f(75, 225, 0),
                    new Vector3f(0, 0, 0.125f),
                    new Vector3f(0.375F, 0.375F, 0.375F)
            ),
            new ItemTransform(
                    new Vector3f(75, 45, 0),
                    new Vector3f(0, 0, 0.125f),
                    new Vector3f(0.375F, 0.375F, 0.375F)
            ),
            new ItemTransform(
                    new Vector3f(0, 225, 0),
                    new Vector3f(1, 0, -1),
                    new Vector3f(1, 1, 1)
            ),
            new ItemTransform(
                    new Vector3f(0, 45, 0),
                    new Vector3f(1, 0, -1),
                    new Vector3f(1, 1, 1)
            ),
            ItemTransform.NO_TRANSFORM,
            new ItemTransform(
                    new Vector3f(30, 225, 0),
                    new Vector3f(0, 0, 0),
                    new Vector3f(0.625F, 0.625F, 0.625F)
            ),
            new ItemTransform(
                    new Vector3f(0, 0, 0),
                    new Vector3f(0, .375f, 0),
                    new Vector3f(0.25f, 0.25f, 0.25f)
            ),
            new ItemTransform(
                    new Vector3f(-90, 0, 0),
                    new Vector3f(0, 0, -.5f),
                    new Vector3f(1, 1, 1)
            )
    );
}
