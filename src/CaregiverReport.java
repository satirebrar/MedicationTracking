//caregiverReport
import java.time.LocalDateTime;
import java.util.List;

/** Formats an adherence summary into shareable plain text for a caregiver or family member. */
public class CaregiverReport {
    public static String build(List<AdherenceCalculator.MedicationAdherence> report, int days) {
        StringBuilder sb = new StringBuilder();
        sb.append("Medication Adherence Report (last ").append(days).append(" days)\n");
        sb.append("Generated: ").append(LocalDateTime.now().withNano(0)).append("\n\n");
        for (AdherenceCalculator.MedicationAdherence a : report) {
            sb.append(String.format("%s: %d/%d doses taken (%.0f%%)%n",
                    a.medicationName, a.takenCount, a.expectedCount, a.getPercentage()));
            if (!a.missedDoses.isEmpty()) {
                sb.append("  Missed doses:\n");
                for (String missed : a.missedDoses) {
                    sb.append("    - ").append(missed).append("\n");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
