package malte0811.controlengineering.controlpanels.model;

import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemTransformVec3f;
import net.minecraft.util.math.vector.Vector3f;

@SuppressWarnings("deprecation")
public class Transforms {
    public static final ItemCameraTransforms PANEL_TRANSFORMS = new ItemCameraTransforms(
            new ItemTransformVec3f(
                    new Vector3f(75, 225, 0),
                    new Vector3f(0, 0, 0.125f),
                    new Vector3f(0.375F, 0.375F, 0.375F)
            ),
            new ItemTransformVec3f(
                    new Vector3f(75, 45, 0),
                    new Vector3f(0, 0, 0.125f),
                    new Vector3f(0.375F, 0.375F, 0.375F)
            ),
            new ItemTransformVec3f(new Vector3f(0, 225, 0), new Vector3f(1, 0, -1), new Vector3f(1, 1, 1)),
            new ItemTransformVec3f(new Vector3f(0, 45, 0), new Vector3f(1, 0, -1), new Vector3f(1, 1, 1)),
            ItemTransformVec3f.DEFAULT,
            new ItemTransformVec3f(
                    new Vector3f(30, 225, 0),
                    new Vector3f(0, 0, 0),
                    new Vector3f(0.625F, 0.625F, 0.625F)
            ),
            new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0, .375f, 0), new Vector3f(0.25f, 0.25f, 0.25f)),
            new ItemTransformVec3f(new Vector3f(-90, 0, 0), new Vector3f(0, 0, -.5f), new Vector3f(1, 1, 1))
    );

    public static final ItemCameraTransforms PANEL_TOP_TRANSFORMS = new ItemCameraTransforms(
            new ItemTransformVec3f(
                    new Vector3f(75, 225, 0),
                    new Vector3f(0, 0, 0.125f),
                    new Vector3f(0.375F, 0.375F, 0.375F)
            ),
            new ItemTransformVec3f(
                    new Vector3f(75, 45, 0),
                    new Vector3f(0, 0, 0.125f),
                    new Vector3f(0.375F, 0.375F, 0.375F)
            ),
            new ItemTransformVec3f(
                    new Vector3f(0, 225, 0),
                    new Vector3f(1, 0, -1),
                    new Vector3f(1, 1, 1)
            ),
            new ItemTransformVec3f(
                    new Vector3f(0, 45, 0),
                    new Vector3f(1, 0, -1),
                    new Vector3f(1, 1, 1)
            ),
            ItemTransformVec3f.DEFAULT,
            new ItemTransformVec3f(
                    new Vector3f(30, 225, 0),
                    new Vector3f(0, 0, 0),
                    new Vector3f(0.625F, 0.625F, 0.625F)
            ),
            new ItemTransformVec3f(
                    new Vector3f(0, 0, 0),
                    new Vector3f(0, .375f, 0),
                    new Vector3f(0.25f, 0.25f, 0.25f)
            ),
            new ItemTransformVec3f(
                    new Vector3f(-90, 0, 0),
                    new Vector3f(0, 0, -.5f),
                    new Vector3f(1, 1, 1)
            )
    );
}
