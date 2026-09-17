//medicationBST
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MedicationBST {
    private BSTNode root;

    public boolean addMedication(Medication medication) {
        if (searchMedicationByName(medication.getName()) != null) return false;
        root = insert(root, medication);
        return true;
    }

    private BSTNode insert(BSTNode root, Medication medication) {
        if (root == null) return new BSTNode(medication);
        if (medication.compareTo(root.getMedication()) < 0)
            root.setLeft(insert(root.getLeft(), medication));
        else if (medication.compareTo(root.getMedication()) > 0)
            root.setRight(insert(root.getRight(), medication));
        return root;
    }

    public void inorderTraversal() {
        inorderTraversal(root);
    }

    private void inorderTraversal(BSTNode root) {
        if (root != null) {
            inorderTraversal(root.getLeft());
            System.out.println(root.getMedication());
            inorderTraversal(root.getRight());
        }
    }

    public List<MedicationDose> getDosesSortedByTime() {
        List<MedicationDose> doses = new ArrayList<>();
        for (Medication m : getAllMedications()) {
            for (LocalTime t : m.getIntakeTimes()) {
                doses.add(new MedicationDose(m, t));
            }
        }
        doses.sort(Comparator.comparing(MedicationDose::getTime));
        return doses;
    }

    private void collectMedications(BSTNode node, List<Medication> list) {
        if (node != null) {
            collectMedications(node.getLeft(), list);
            list.add(node.getMedication());
            collectMedications(node.getRight(), list);
        }
    }

    public Medication searchMedicationByName(String name) {
        return searchByName(root, name);
    }

    private Medication searchByName(BSTNode root, String name) {
        if (root == null) return null;
        int cmp = name.compareToIgnoreCase(root.getMedication().getName());
        if (cmp == 0) return root.getMedication();
        else if (cmp < 0) return searchByName(root.getLeft(), name);
        else return searchByName(root.getRight(), name);
    }

    public List<Medication> getAllMedications() {
        List<Medication> meds = new ArrayList<>();
        collectMedications(root, meds);
        return meds;
    }

    public List<Medication> findMatchesByName(String partialName) {
        List<Medication> matches = new ArrayList<>();
        String lower = partialName.toLowerCase();
        for (Medication m : getAllMedications()) {
            if (m.getName().toLowerCase().contains(lower)) matches.add(m);
        }
        return matches;
    }

    public boolean deleteMedication(String name) {
        if (searchMedicationByName(name) == null) return false;
        root = delete(root, name);
        return true;
    }

    private BSTNode delete(BSTNode node, String name) {
        if (node == null) return null;
        int cmp = name.compareToIgnoreCase(node.getMedication().getName());
        if (cmp < 0) {
            node.setLeft(delete(node.getLeft(), name));
        } else if (cmp > 0) {
            node.setRight(delete(node.getRight(), name));
        } else {
            if (node.getLeft() == null) return node.getRight();
            if (node.getRight() == null) return node.getLeft();
            BSTNode successor = findMin(node.getRight());
            node.setMedication(successor.getMedication());
            node.setRight(delete(node.getRight(), successor.getMedication().getName()));
        }
        return node;
    }

    private BSTNode findMin(BSTNode node) {
        while (node.getLeft() != null) node = node.getLeft();
        return node;
    }

    public boolean inorderTraversalByType(String type) {
        boolean[] found = new boolean[]{false};
        inorderTraversalByType(root, type, found);
        return found[0];
    }

    private void inorderTraversalByType(BSTNode root, String type, boolean[] found) {
        if (root != null) {
            inorderTraversalByType(root.getLeft(), type, found);
            if (type.equalsIgnoreCase(root.getMedication().getType())) {
                System.out.println(root.getMedication());
                found[0] = true;
            }
            inorderTraversalByType(root.getRight(), type, found);
        }
    }

    public boolean inorderTraversalByCategory(String category) {
        boolean[] found = new boolean[]{false};
        inorderTraversalByCategory(root, category, found);
        return found[0];
    }

    private void inorderTraversalByCategory(BSTNode root, String category, boolean[] found) {
        if (root != null) {
            inorderTraversalByCategory(root.getLeft(), category, found);
            if (category.equalsIgnoreCase(root.getMedication().getCategory())) {
                System.out.println(root.getMedication());
                found[0] = true;
            }
            inorderTraversalByCategory(root.getRight(), category, found);
        }
    }
}
