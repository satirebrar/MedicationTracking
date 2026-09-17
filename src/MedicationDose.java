//medicationDose
import java.time.LocalTime;

/** A single scheduled occurrence of a medication at one of its intake times. */
public class MedicationDose {
    private final Medication medication;
    private final LocalTime time;

    public MedicationDose(Medication medication, LocalTime time) {
        this.medication = medication;
        this.time = time;
    }

    public Medication getMedication() { return medication; }
    public LocalTime getTime() { return time; }

    @Override
    public String toString() {
        return time + " - " + medication.getName() + " (" + medication.getDosage() + ")";
    }
}
