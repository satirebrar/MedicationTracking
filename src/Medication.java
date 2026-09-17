//medication
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Medication implements Comparable<Medication> {
    private static final int LOW_STOCK_THRESHOLD = 2;

    private String name;
    private String dosage;
    private int frequency;
    private String category;
    private String type;
    private List<LocalTime> intakeTimes;
    private int numberOfIntake;
    private Map<LocalDate, Set<LocalTime>> intakeHistory = new HashMap<>();

    public Medication(String name, String dosage, int frequency, String category, String type, String intakeTimesStr, int numberOfIntake) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.category = category;
        this.type = type;
        this.intakeTimes = parseTimes(intakeTimesStr);
        this.numberOfIntake = numberOfIntake;
    }

    private static List<LocalTime> parseTimes(String intakeTimesStr) {
        List<LocalTime> times = new ArrayList<>();
        for (String part : intakeTimesStr.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) times.add(LocalTime.parse(trimmed));
        }
        Collections.sort(times);
        return times;
    }

    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public int getFrequency() { return frequency; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public List<LocalTime> getIntakeTimes() { return Collections.unmodifiableList(intakeTimes); }
    public int getNumberOfIntake() { return numberOfIntake; }

    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setFrequency(int frequency) { this.frequency = frequency; }
    public void setCategory(String category) { this.category = category; }
    public void setType(String type) { this.type = type; }
    public void setIntakeTimes(String intakeTimesStr) { this.intakeTimes = parseTimes(intakeTimesStr); }
    public void setNumberOfIntake(int numberOfIntake) { this.numberOfIntake = numberOfIntake; }

    public boolean isTakenAt(LocalDate date, LocalTime time) {
        return intakeHistory.getOrDefault(date, Set.of()).contains(time);
    }

    public boolean isTakenAt(LocalTime time) {
        return isTakenAt(LocalDate.now(), time);
    }

    public void markAsTaken(LocalTime time) {
        intakeHistory.computeIfAbsent(LocalDate.now(), d -> new HashSet<>()).add(time);
    }

    /** Repopulates history from persisted storage; does not represent a "just taken" event. */
    public void recordHistoricalIntake(LocalDate date, LocalTime time) {
        intakeHistory.computeIfAbsent(date, d -> new HashSet<>()).add(time);
    }

    public boolean isFullyTakenToday() {
        if (intakeTimes.isEmpty()) return false;
        Set<LocalTime> takenToday = intakeHistory.getOrDefault(LocalDate.now(), Set.of());
        return takenToday.containsAll(intakeTimes);
    }

    public Map<LocalDate, Set<LocalTime>> getIntakeHistory() {
        return intakeHistory;
    }

    public void decrementIntake() {
        if (numberOfIntake > 0) numberOfIntake--;
    }

    public boolean isOutOfStock() {
        return numberOfIntake <= 0;
    }

    public boolean isLowStock() {
        return numberOfIntake > 0 && numberOfIntake <= LOW_STOCK_THRESHOLD;
    }

    @Override
    public int compareTo(Medication other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return name + " - " + dosage + " - " + frequency + " - " + category + " - " + type + " - " + intakeTimes + " - " + numberOfIntake;
    }
}
