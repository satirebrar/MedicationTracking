//BSTnode
public class BSTNode {
    private Medication medication;
    private BSTNode left, right;

    public BSTNode(Medication medication) {
        this.medication = medication;
    }

    public Medication getMedication() { return medication; }
    public void setMedication(Medication medication) { this.medication = medication; }
    public BSTNode getLeft() { return left; }
    public void setLeft(BSTNode left) { this.left = left; }
    public BSTNode getRight() { return right; }
    public void setRight(BSTNode right) { this.right = right; }
}