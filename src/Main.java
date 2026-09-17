//Main
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MedicationBST bst = new MedicationBST();
        MedicationQueue queue = new MedicationQueue();
        Scanner scanner = new Scanner(System.in);

        List<Medication> loaded = MedicationStorage.load();
        if (loaded.isEmpty()) {
            for (Medication m : SampleData.create()) bst.addMedication(m);
            saveState(bst);
            System.out.println("No saved data found. Starting with sample medications.");
        } else {
            for (Medication m : loaded) bst.addMedication(m);
            MedicationStorage.loadHistoryInto(bst.getAllMedications());
            System.out.println("Loaded " + loaded.size() + " medication(s) from saved data.");
        }

        while (true) {
            System.out.println("\n=== Medication Tracking System ===");
            System.out.println("1. Add Medication");
            System.out.println("2. Update Medication");
            System.out.println("3. Delete Medication");
            System.out.println("4. Display All Medications (Inorder)");
            System.out.println("5. Search Medication by Name");
            System.out.println("6. Search Medication by Type");
            System.out.println("7. Search Medication by Category");
            System.out.println("8. Add Medication Dose to Intake Queue Manually");
            System.out.println("9. Take Next Medication (From Queue)");
            System.out.println("10. Display Intake Queue");
            System.out.println("11. Auto-fill Queue by Intake Time");
            System.out.println("12. Start Real-Time Reminder");
            System.out.println("13. Show Today's Medication Plan");
            System.out.println("14. Show Weekly Adherence Report");
            System.out.println("15. Export Caregiver Report");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1: {
                    System.out.print("Enter medication name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter dosage: ");
                    String dosage = scanner.nextLine();
                    int frequency;
                    try {
                        System.out.print("Enter frequency (times/day): ");
                        frequency = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid frequency. Medication not added.");
                        break;
                    }
                    System.out.print("Enter category: ");
                    String category = scanner.nextLine();
                    System.out.print("Enter type: ");
                    String type = scanner.nextLine();
                    System.out.print("Enter intake times, comma-separated (e.g. 09:00,14:00,20:00): ");
                    String intakeTimes = scanner.nextLine();
                    int numOfIntakes;
                    try {
                        System.out.print("Enter number of intakes: ");
                        numOfIntakes = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number of intakes. Medication not added.");
                        break;
                    }
                    Medication newMed;
                    try {
                        newMed = new Medication(name, dosage, frequency, category, type, intakeTimes, numOfIntakes);
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid intake time format. Use HH:mm. Medication not added.");
                        break;
                    }
                    if (bst.addMedication(newMed)) {
                        saveState(bst);
                        System.out.println("Medication added successfully.");
                    } else {
                        System.out.println("A medication with that name already exists. Medication not added.");
                    }
                    break;
                }

                case 2: {
                    System.out.print("Enter medication name to update: ");
                    String query = scanner.nextLine();
                    Medication toUpdate = resolveMedication(bst, scanner, query);
                    if (toUpdate == null) {
                        System.out.println("Medication not found.");
                        break;
                    }
                    System.out.println("Updating \"" + toUpdate.getName() + "\". Press Enter to keep the current value.");

                    System.out.print("Dosage [" + toUpdate.getDosage() + "]: ");
                    String newDosage = scanner.nextLine();
                    if (!newDosage.isBlank()) toUpdate.setDosage(newDosage);

                    System.out.print("Frequency [" + toUpdate.getFrequency() + "]: ");
                    String newFrequency = scanner.nextLine();
                    if (!newFrequency.isBlank()) {
                        try {
                            toUpdate.setFrequency(Integer.parseInt(newFrequency.trim()));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid frequency. Keeping previous value.");
                        }
                    }

                    System.out.print("Category [" + toUpdate.getCategory() + "]: ");
                    String newCategory = scanner.nextLine();
                    if (!newCategory.isBlank()) toUpdate.setCategory(newCategory);

                    System.out.print("Type [" + toUpdate.getType() + "]: ");
                    String newType = scanner.nextLine();
                    if (!newType.isBlank()) toUpdate.setType(newType);

                    System.out.print("Intake times, comma-separated " + formatTimes(toUpdate) + ": ");
                    String newIntakeTimes = scanner.nextLine();
                    if (!newIntakeTimes.isBlank()) {
                        try {
                            toUpdate.setIntakeTimes(newIntakeTimes.trim());
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid time format. Keeping previous value.");
                        }
                    }

                    System.out.print("Number of intakes [" + toUpdate.getNumberOfIntake() + "]: ");
                    String newNumOfIntakes = scanner.nextLine();
                    if (!newNumOfIntakes.isBlank()) {
                        try {
                            toUpdate.setNumberOfIntake(Integer.parseInt(newNumOfIntakes.trim()));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number of intakes. Keeping previous value.");
                        }
                    }

                    saveState(bst);
                    System.out.println("Medication updated.");
                    break;
                }

                case 3: {
                    System.out.print("Enter medication name to delete: ");
                    String query = scanner.nextLine();
                    Medication toDelete = resolveMedication(bst, scanner, query);
                    if (toDelete == null) {
                        System.out.println("Medication not found.");
                        break;
                    }
                    System.out.print("Are you sure you want to delete \"" + toDelete.getName() + "\"? (y/n): ");
                    String confirm = scanner.nextLine();
                    if (confirm.equalsIgnoreCase("y")) {
                        bst.deleteMedication(toDelete.getName());
                        saveState(bst);
                        System.out.println("Medication deleted.");
                    } else {
                        System.out.println("Deletion cancelled.");
                    }
                    break;
                }

                case 4:
                    System.out.println("All Medications (Alphabetical):");
                    bst.inorderTraversal();
                    break;

                case 5: {
                    System.out.print("Enter name to search: ");
                    String searchName = scanner.nextLine();
                    Medication found = resolveMedication(bst, scanner, searchName);
                    if (found != null) {
                        System.out.println("Found: " + found);
                    } else {
                        System.out.println("Medication not found.");
                    }
                    break;
                }

                case 6: {
                    System.out.print("Enter type to search: ");
                    String searchType = scanner.nextLine();
                    if (!bst.inorderTraversalByType(searchType)) {
                        System.out.println("No medications found for that type.");
                    }
                    break;
                }

                case 7: {
                    System.out.print("Enter category to search: ");
                    String searchCategory = scanner.nextLine();
                    if (!bst.inorderTraversalByCategory(searchCategory)) {
                        System.out.println("No medications found for that category.");
                    }
                    break;
                }

                case 8: {
                    System.out.print("Enter medication name to add to intake queue: ");
                    String medName = scanner.nextLine();
                    Medication med = resolveMedication(bst, scanner, medName);
                    if (med == null) {
                        System.out.println("Medication not found.");
                        break;
                    }
                    LocalTime time = resolveTime(med, scanner);
                    if (time == null) {
                        System.out.println("No intake time selected.");
                        break;
                    }
                    queue.addMedication(new MedicationDose(med, time));
                    System.out.println("Medication dose added to queue.");
                    break;
                }

                case 9: {
                    if (!queue.isEmpty()) {
                        MedicationDose nextDose = queue.takeNextMedication();
                        Medication med = nextDose.getMedication();
                        med.markAsTaken(nextDose.getTime());
                        med.decrementIntake();
                        System.out.println("Take this medication now: " + nextDose);
                        warnIfLowStock(med);
                        saveState(bst);
                    } else {
                        System.out.println("Queue is empty.");
                    }
                    break;
                }

                case 10:
                    System.out.println("Medications in Intake Queue:");
                    queue.displayQueue();
                    break;

                case 11: {
                    queue = new MedicationQueue(); // reset
                    for (MedicationDose dose : bst.getDosesSortedByTime()) {
                        queue.addMedication(dose);
                    }
                    System.out.println("Queue filled based on intake times.");
                    break;
                }

                case 12: {
                    System.out.println("Real-time reminder started. Type 'q' instead of y/n at any prompt to stop and return to the menu.");
                    List<MedicationDose> doses = bst.getDosesSortedByTime();
                    boolean stopReminder = false;
                    while (!stopReminder) {
                        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
                        for (MedicationDose dose : doses) {
                            Medication m = dose.getMedication();
                            if (dose.getTime().equals(now) && !m.isTakenAt(dose.getTime())) {
                                System.out.println("🔔 Reminder: Take " + m.getName() + " (" + m.getDosage() + ") at " + dose.getTime());
                                System.out.print("Did you take it? (y/n, or 'q' to stop reminders): ");
                                String response = scanner.nextLine();
                                if (response.equalsIgnoreCase("q")) {
                                    stopReminder = true;
                                    break;
                                } else if (response.equalsIgnoreCase("y")) {
                                    m.markAsTaken(dose.getTime());
                                    m.decrementIntake();
                                    System.out.println("✅ Marked as taken.");
                                    warnIfLowStock(m);
                                    saveState(bst);
                                } else {
                                    System.out.println("⚠️ Not marked as taken.");
                                }
                            }
                        }
                        if (stopReminder) break;
                        try {
                            Thread.sleep(60000); // 1 minute
                        } catch (InterruptedException e) {
                            System.out.println("Reminder interrupted.");
                        }
                    }
                    System.out.println("Reminder stopped. Returning to menu.");
                    break;
                }

                case 13: {
                    System.out.println("🗓 Today's Medication Plan:");
                    List<MedicationDose> plan = bst.getDosesSortedByTime();
                    if (plan.isEmpty()) {
                        System.out.println("No medications found.");
                    } else {
                        LocalTime now = LocalTime.now();
                        for (MedicationDose dose : plan) {
                            Medication m = dose.getMedication();
                            String status;
                            if (m.isTakenAt(dose.getTime())) {
                                status = "✅ Taken";
                            } else if (dose.getTime().isBefore(now)) {
                                status = "⚠️ MISSED";
                            } else {
                                status = "⏳ Upcoming";
                            }
                            String stock = m.isOutOfStock() ? " [OUT OF STOCK]"
                                    : m.isLowStock() ? " [LOW STOCK: " + m.getNumberOfIntake() + " left]"
                                    : "";
                            System.out.println("⏰ " + dose.getTime() + " - " + m.getName() +
                                    " (" + m.getDosage() + ", " + m.getType() + ", " + m.getCategory() + ") - " + status + stock);
                        }
                    }
                    break;
                }

                case 14: {
                    List<AdherenceCalculator.MedicationAdherence> report = AdherenceCalculator.calculate(bst.getAllMedications(), 7);
                    System.out.println(CaregiverReport.build(report, 7));
                    break;
                }

                case 15: {
                    List<AdherenceCalculator.MedicationAdherence> report = AdherenceCalculator.calculate(bst.getAllMedications(), 7);
                    String text = CaregiverReport.build(report, 7);
                    try {
                        Path path = Paths.get("data/caregiver_report.txt");
                        if (path.getParent() != null) Files.createDirectories(path.getParent());
                        Files.writeString(path, text);
                        System.out.println("Caregiver report saved to " + path.toAbsolutePath());
                    } catch (Exception e) {
                        System.out.println("Could not save caregiver report (" + e.getMessage() + ").");
                    }
                    break;
                }

                case 0:
                    saveState(bst);
                    System.out.println("Exiting system. Goodbye!");
                    scanner.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void saveState(MedicationBST bst) {
        List<Medication> all = bst.getAllMedications();
        MedicationStorage.save(all);
        MedicationStorage.saveHistory(all);
    }

    private static void warnIfLowStock(Medication m) {
        if (m.isOutOfStock()) {
            System.out.println("⚠️ You are out of " + m.getName() + ". Please refill soon.");
        } else if (m.isLowStock()) {
            System.out.println("⚠️ Only " + m.getNumberOfIntake() + " dose(s) of " + m.getName() + " left.");
        }
    }

    private static String formatTimes(Medication m) {
        StringBuilder sb = new StringBuilder("[");
        List<LocalTime> times = m.getIntakeTimes();
        for (int i = 0; i < times.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(times.get(i));
        }
        return sb.append("]").toString();
    }

    // Lets the user pick which intake time to use when a medication has more than one per day.
    private static LocalTime resolveTime(Medication med, Scanner scanner) {
        List<LocalTime> times = med.getIntakeTimes();
        if (times.isEmpty()) return null;
        if (times.size() == 1) return times.get(0);

        System.out.println(med.getName() + " has multiple intake times:");
        for (int i = 0; i < times.size(); i++) {
            System.out.println((i + 1) + ". " + times.get(i));
        }
        System.out.print("Select a number (or press Enter to cancel): ");
        String selection = scanner.nextLine().trim();
        if (selection.isEmpty()) return null;
        try {
            int index = Integer.parseInt(selection) - 1;
            if (index >= 0 && index < times.size()) return times.get(index);
        } catch (NumberFormatException ignored) {
            // fall through to null
        }
        return null;
    }

    // Resolves a medication by exact name, falling back to a case-insensitive
    // partial-name match with a picker — helps users who don't recall the exact spelling.
    private static Medication resolveMedication(MedicationBST bst, Scanner scanner, String query) {
        Medication exact = bst.searchMedicationByName(query);
        if (exact != null) return exact;

        List<Medication> matches = bst.findMatchesByName(query);
        if (matches.isEmpty()) return null;
        if (matches.size() == 1) return matches.get(0);

        System.out.println("Multiple matches found:");
        for (int i = 0; i < matches.size(); i++) {
            System.out.println((i + 1) + ". " + matches.get(i).getName());
        }
        System.out.print("Select a number (or press Enter to cancel): ");
        String selection = scanner.nextLine().trim();
        if (selection.isEmpty()) return null;
        try {
            int index = Integer.parseInt(selection) - 1;
            if (index >= 0 && index < matches.size()) return matches.get(index);
        } catch (NumberFormatException ignored) {
            // fall through to null
        }
        return null;
    }
}
