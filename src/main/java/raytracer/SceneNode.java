package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static raytracer.Matrix4D.identity;
import static raytracer.Matrix4D.translation;

@AllArgsConstructor
@Getter
@ToString(exclude = "parent")
public class SceneNode {
    final SceneNode parent;
    final List<SceneNode> childs;
    final SceneObject sceneObject;
    boolean identity;
    final Matrix4D transform;
    final Matrix4D invTransform;

    public SceneNode(SceneNode parent, SceneObject sceneObject) {
        this.sceneObject = sceneObject;
        childs = new ArrayList<SceneNode>();
        if (parent != null) {
            parent.childs.add(this);
        }
        this.parent = parent;
        identity = true;
        transform = identity();
        invTransform = identity();
    }

    public SceneNode() {
        this(null, null);
    }

    public void translate(Vector3D offset) {
        transform.mulRight(translation(offset));
        invTransform.mulLeft(translation(offset.negate()));
        identity = false;
    }
}
