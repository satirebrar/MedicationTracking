//medicationQueue
import java.util.LinkedList;
import java.util.Queue;

public class MedicationQueue {
    private Queue<MedicationDose> medicationQueue = new LinkedList<>();

    public void addMedication(MedicationDose dose) {
        medicationQueue.add(dose);
    }

    public MedicationDose takeNextMedication() {
        return medicationQueue.poll();
    }

    public void displayQueue() {
        for (MedicationDose dose : medicationQueue) {
            System.out.println(dose);
        }
    }

    public boolean isEmpty() {
        return medicationQueue.isEmpty();
    }
}
