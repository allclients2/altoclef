package adris.altoclef.util.math;

// Because net.minecraft.util.Pair didn't allow setLeft or setRight in older versions. It was just a dumb record basically..
public class Pair<A, B> {
    private A left;
    private B right;

    public Pair(A left, B right) {
        this.left = left;
        this.right = right;
    }

    public A getLeft() {
        return this.left;
    }
    public B getRight() {
        return this.right;
    }

    public void setLeft(A value) {
        this.left = value;
    }
    public void setRight(B value) {
        this.right = value;
    }
}