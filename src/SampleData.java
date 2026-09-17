//sampleData
import java.util.ArrayList;
import java.util.List;

/** Sample medications used to seed the system on first run (no saved data yet). */
public class SampleData {
    public static List<Medication> create() {
        List<Medication> meds = new ArrayList<>();
        meds.add(new Medication("Aspirin", "500mg", 2, "Painkiller", "Pill", "09:00,21:00", 10));
        meds.add(new Medication("Parol", "500mg", 2, "Painkiller", "Pill", "09:00", 10));
        meds.add(new Medication("Ibuprofen", "400mg", 3, "Anti-inflammatory", "Pill", "12:00", 15));
        meds.add(new Medication("Calpol", "250mg", 1, "Fever", "Syrup", "18:30", 5));
        meds.add(new Medication("Metformin", "850mg", 2, "Diabetes", "Tablet", "08:00,20:00", 20));
        meds.add(new Medication("Amlodipine", "5mg", 1, "Blood Pressure", "Tablet", "08:00", 15));
        meds.add(new Medication("Omeprazole", "20mg", 1, "Stomach", "Capsule", "07:30", 10));
        meds.add(new Medication("Vitamin D", "1000IU", 1, "Supplement", "Tablet", "09:00", 30));
        meds.add(new Medication("Warfarin", "5mg", 1, "Blood Thinner", "Tablet", "18:00", 14));
        return meds;
    }
}
