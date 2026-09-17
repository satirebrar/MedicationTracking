//medicationStorage
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MedicationStorage {
    private static final String FILE_PATH = "data/medications.txt";
    private static final String HISTORY_FILE_PATH = "data/intake_history.txt";
    private static final int HISTORY_RETENTION_DAYS = 120;
    private static final String DELIMITER = "\\|";
    private static final String JOIN_DELIMITER = "|";

    public static void save(List<Medication> medications) {
        try {
            Path path = Paths.get(FILE_PATH);
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                for (Medication m : medications) {
                    String timesStr = m.getIntakeTimes().stream()
                            .map(LocalTime::toString)
                            .collect(Collectors.joining(","));
                    writer.write(String.join(JOIN_DELIMITER,
                            m.getName(), m.getDosage(), String.valueOf(m.getFrequency()),
                            m.getCategory(), m.getType(), timesStr,
                            String.valueOf(m.getNumberOfIntake())));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not save medications to file (" + e.getMessage() + ").");
        }
    }

    public static List<Medication> load() {
        List<Medication> medications = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) return medications;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(DELIMITER, -1);
                if (parts.length != 7) {
                    System.out.println("Warning: skipped a corrupted line in saved data.");
                    continue;
                }
                try {
                    String name = parts[0];
                    String dosage = parts[1];
                    int frequency = Integer.parseInt(parts[2]);
                    String category = parts[3];
                    String type = parts[4];
                    String intakeTimes = parts[5];
                    int numberOfIntake = Integer.parseInt(parts[6]);
                    medications.add(new Medication(name, dosage, frequency, category, type, intakeTimes, numberOfIntake));
                } catch (Exception e) {
                    System.out.println("Warning: skipped a corrupted line in saved data.");
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not load saved medications (" + e.getMessage() + ").");
        }
        return medications;
    }

    public static void saveHistory(List<Medication> medications) {
        try {
            Path path = Paths.get(HISTORY_FILE_PATH);
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            LocalDate cutoff = LocalDate.now().minusDays(HISTORY_RETENTION_DAYS);
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                for (Medication m : medications) {
                    for (Map.Entry<LocalDate, Set<LocalTime>> entry : m.getIntakeHistory().entrySet()) {
                        if (entry.getKey().isBefore(cutoff)) continue;
                        for (LocalTime t : entry.getValue()) {
                            writer.write(String.join(JOIN_DELIMITER, m.getName(), entry.getKey().toString(), t.toString()));
                            writer.newLine();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not save intake history (" + e.getMessage() + ").");
        }
    }

    public static void loadHistoryInto(List<Medication> medications) {
        Path path = Paths.get(HISTORY_FILE_PATH);
        if (!Files.exists(path)) return;
        Map<String, Medication> byName = new HashMap<>();
        for (Medication m : medications) byName.put(m.getName().toLowerCase(), m);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(DELIMITER, -1);
                if (parts.length != 3) continue;
                try {
                    Medication m = byName.get(parts[0].toLowerCase());
                    if (m == null) continue;
                    LocalDate date = LocalDate.parse(parts[1]);
                    LocalTime time = LocalTime.parse(parts[2]);
                    m.recordHistoricalIntake(date, time);
                } catch (Exception e) {
                    System.out.println("Warning: skipped a corrupted line in intake history.");
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not load intake history (" + e.getMessage() + ").");
        }
    }
}
