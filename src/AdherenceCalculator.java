//adherenceCalculator
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Computes how consistently medications were taken over a trailing window of days. */
public class AdherenceCalculator {

    public static class MedicationAdherence {
        public final String medicationName;
        public final int takenCount;
        public final int expectedCount;
        public final List<String> missedDoses;

        MedicationAdherence(String medicationName, int takenCount, int expectedCount, List<String> missedDoses) {
            this.medicationName = medicationName;
            this.takenCount = takenCount;
            this.expectedCount = expectedCount;
            this.missedDoses = missedDoses;
        }

        public double getPercentage() {
            return expectedCount == 0 ? 100.0 : (takenCount * 100.0) / expectedCount;
        }
    }

    public static List<MedicationAdherence> calculate(List<Medication> medications, int days) {
        List<MedicationAdherence> results = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        for (Medication m : medications) {
            int expected = 0;
            int taken = 0;
            List<String> missed = new ArrayList<>();
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                for (LocalTime t : m.getIntakeTimes()) {
                    boolean stillUpcoming = date.equals(today) && t.isAfter(now);
                    if (stillUpcoming) continue;
                    expected++;
                    if (m.isTakenAt(date, t)) {
                        taken++;
                    } else {
                        missed.add(date + " " + t);
                    }
                }
            }
            results.add(new MedicationAdherence(m.getName(), taken, expected, missed));
        }
        return results;
    }
}
