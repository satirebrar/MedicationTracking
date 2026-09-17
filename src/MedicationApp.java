//medicationApp
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MedicationApp extends JFrame {
    private static final Font APP_FONT = new Font("SansSerif", Font.PLAIN, 16);
    private static final Color MISSED_COLOR = new Color(255, 205, 205);
    private static final Color TAKEN_COLOR = new Color(205, 235, 205);
    private static final Color UPCOMING_COLOR = Color.WHITE;
    private static final int ADHERENCE_WINDOW_DAYS = 7;

    private final MedicationBST bst = new MedicationBST();
    private final DoseTableModel tableModel = new DoseTableModel();
    private final JTable table = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel(" ");
    private final Set<String> missedNotified = new HashSet<>();
    private TrayIcon trayIcon;

    public MedicationApp() {
        super("Medication Tracker");
        loadData();
        buildUi();
        setupTray();
        startReminderTimer();
        refreshTable();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveData();
                dispose();
                System.exit(0);
            }
        });
        setSize(1000, 550);
        setLocationRelativeTo(null);
    }

    private void loadData() {
        List<Medication> loaded = MedicationStorage.load();
        if (loaded.isEmpty()) {
            for (Medication m : SampleData.create()) bst.addMedication(m);
            saveData();
        } else {
            for (Medication m : loaded) bst.addMedication(m);
            MedicationStorage.loadHistoryInto(bst.getAllMedications());
        }
    }

    private void saveData() {
        List<Medication> all = bst.getAllMedications();
        MedicationStorage.save(all);
        MedicationStorage.saveHistory(all);
    }

    private void buildUi() {
        setLayout(new BorderLayout(8, 8));

        table.setRowHeight(32);
        table.setFont(APP_FONT);
        table.getTableHeader().setFont(APP_FONT.deriveFont(Font.BOLD));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new StatusRowRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton addButton = bigButton("Add Medication");
        JButton editButton = bigButton("Edit Selected");
        JButton deleteButton = bigButton("Delete Selected");
        JButton takeButton = bigButton("Mark as Taken");
        JButton reportButton = bigButton("Weekly Report");
        JButton exportButton = bigButton("Export Caregiver Report");
        JButton refreshButton = bigButton("Refresh");

        addButton.addActionListener(e -> showMedicationDialog(null));
        editButton.addActionListener(e -> {
            Medication selected = getSelectedMedication();
            if (selected == null) {
                showStatus("Please select a medication first.");
                return;
            }
            showMedicationDialog(selected);
        });
        deleteButton.addActionListener(e -> deleteSelected());
        takeButton.addActionListener(e -> takeSelected());
        reportButton.addActionListener(e -> showWeeklyReport());
        exportButton.addActionListener(e -> exportCaregiverReport());
        refreshButton.addActionListener(e -> refreshTable());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(takeButton);
        buttonPanel.add(reportButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.NORTH);

        statusLabel.setFont(APP_FONT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JButton bigButton(String text) {
        JButton button = new JButton(text);
        button.setFont(APP_FONT);
        button.setMargin(new Insets(8, 14, 8, 14));
        return button;
    }

    private MedicationDose getSelectedDose() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return tableModel.getDoseAt(row);
    }

    private Medication getSelectedMedication() {
        MedicationDose dose = getSelectedDose();
        return dose == null ? null : dose.getMedication();
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
    }

    private void refreshTable() {
        tableModel.setDoses(bst.getDosesSortedByTime());
    }

    private void deleteSelected() {
        Medication selected = getSelectedMedication();
        if (selected == null) {
            showStatus("Please select a medication first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + selected.getName() + "\" (all its intake times)?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            bst.deleteMedication(selected.getName());
            saveData();
            refreshTable();
            showStatus("Deleted " + selected.getName() + ".");
        }
    }

    private void takeSelected() {
        MedicationDose dose = getSelectedDose();
        if (dose == null) {
            showStatus("Please select a medication dose first.");
            return;
        }
        Medication selected = dose.getMedication();
        selected.markAsTaken(dose.getTime());
        selected.decrementIntake();
        saveData();
        refreshTable();
        if (selected.isOutOfStock()) {
            JOptionPane.showMessageDialog(this,
                    "You are out of " + selected.getName() + ". Please refill soon.",
                    "Out of Stock", JOptionPane.WARNING_MESSAGE);
        } else if (selected.isLowStock()) {
            JOptionPane.showMessageDialog(this,
                    "Only " + selected.getNumberOfIntake() + " dose(s) of " + selected.getName() + " left.",
                    "Low Stock", JOptionPane.WARNING_MESSAGE);
        }
        showStatus("Marked " + selected.getName() + " (" + dose.getTime() + ") as taken.");
    }

    private void showMedicationDialog(Medication existing) {
        boolean isEdit = existing != null;
        JTextField nameField = new JTextField(isEdit ? existing.getName() : "", 18);
        nameField.setEnabled(!isEdit); // renaming not supported, keeps BST ordering simple
        JTextField dosageField = new JTextField(isEdit ? existing.getDosage() : "", 18);
        JTextField frequencyField = new JTextField(isEdit ? String.valueOf(existing.getFrequency()) : "", 18);
        JTextField categoryField = new JTextField(isEdit ? existing.getCategory() : "", 18);
        JTextField typeField = new JTextField(isEdit ? existing.getType() : "", 18);
        JTextField intakeTimesField = new JTextField(isEdit ? joinTimes(existing) : "", 18);
        JTextField numOfIntakesField = new JTextField(isEdit ? String.valueOf(existing.getNumberOfIntake()) : "", 18);

        JTextField[] fields = {nameField, dosageField, frequencyField, categoryField, typeField, intakeTimesField, numOfIntakesField};
        for (JTextField f : fields) f.setFont(APP_FONT);

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(labeled("Name:"));
        panel.add(nameField);
        panel.add(labeled("Dosage:"));
        panel.add(dosageField);
        panel.add(labeled("Frequency (times/day):"));
        panel.add(frequencyField);
        panel.add(labeled("Category:"));
        panel.add(categoryField);
        panel.add(labeled("Type:"));
        panel.add(typeField);
        panel.add(labeled("Intake times (comma-separated HH:mm):"));
        panel.add(intakeTimesField);
        panel.add(labeled("Number of doses left:"));
        panel.add(numOfIntakesField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                isEdit ? "Edit Medication" : "Add Medication",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        int frequency;
        int numOfIntakes;
        try {
            frequency = Integer.parseInt(frequencyField.getText().trim());
            numOfIntakes = Integer.parseInt(numOfIntakesField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Frequency and number of doses must be numbers.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isEdit) {
            try {
                existing.setDosage(dosageField.getText());
                existing.setFrequency(frequency);
                existing.setCategory(categoryField.getText());
                existing.setType(typeField.getText());
                existing.setIntakeTimes(intakeTimesField.getText().trim());
                existing.setNumberOfIntake(numOfIntakes);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid time format. Use HH:mm.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            saveData();
            refreshTable();
            showStatus("Updated " + existing.getName() + ".");
        } else {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Medication newMed;
            try {
                newMed = new Medication(name, dosageField.getText(), frequency, categoryField.getText(),
                        typeField.getText(), intakeTimesField.getText().trim(), numOfIntakes);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid time format. Use HH:mm.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!bst.addMedication(newMed)) {
                JOptionPane.showMessageDialog(this, "A medication with that name already exists.", "Duplicate Name", JOptionPane.ERROR_MESSAGE);
                return;
            }
            saveData();
            refreshTable();
            showStatus("Added " + newMed.getName() + ".");
        }
    }

    private String joinTimes(Medication m) {
        StringBuilder sb = new StringBuilder();
        List<LocalTime> times = m.getIntakeTimes();
        for (int i = 0; i < times.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(times.get(i));
        }
        return sb.toString();
    }

    private JLabel labeled(String text) {
        JLabel label = new JLabel(text);
        label.setFont(APP_FONT);
        return label;
    }

    private void showWeeklyReport() {
        List<AdherenceCalculator.MedicationAdherence> report =
                AdherenceCalculator.calculate(bst.getAllMedications(), ADHERENCE_WINDOW_DAYS);
        String text = CaregiverReport.build(report, ADHERENCE_WINDOW_DAYS);
        JTextArea textArea = new JTextArea(text, 20, 50);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea),
                "Weekly Adherence Report", JOptionPane.PLAIN_MESSAGE);
    }

    private void exportCaregiverReport() {
        List<AdherenceCalculator.MedicationAdherence> report =
                AdherenceCalculator.calculate(bst.getAllMedications(), ADHERENCE_WINDOW_DAYS);
        String text = CaregiverReport.build(report, ADHERENCE_WINDOW_DAYS);
        try {
            Path path = Paths.get("data/caregiver_report.txt");
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            Files.writeString(path, text);
            showStatus("Caregiver report saved to " + path.toAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Report saved to:\n" + path.toAbsolutePath() +
                            "\n\nShare this file with a caregiver or family member to review adherence.",
                    "Report Exported", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not save report: " + e.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupTray() {
        if (!SystemTray.isSupported()) return;
        try {
            SystemTray tray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(createTrayImage(), "Medication Tracker");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        } catch (AWTException e) {
            trayIcon = null;
        }
    }

    private Image createTrayImage() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(200, 30, 30));
        g.fillOval(0, 0, 16, 16);
        g.dispose();
        return image;
    }

    private void startReminderTimer() {
        Timer timer = new Timer(30_000, e -> checkReminders());
        timer.setInitialDelay(2_000);
        timer.start();
    }

    // On-time reminders prompt a yes/no dialog; doses whose time has already passed and
    // still aren't taken raise a one-time tray notification, so a caregiver nearby can notice too.
    private void checkReminders() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        for (MedicationDose dose : bst.getDosesSortedByTime()) {
            Medication m = dose.getMedication();
            LocalTime t = dose.getTime();
            if (m.isTakenAt(t)) continue;

            if (t.equals(now)) {
                int response = JOptionPane.showConfirmDialog(this,
                        "Time to take " + m.getName() + " (" + m.getDosage() + ") at " + t + ". Did you take it?",
                        "Medication Reminder", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    m.markAsTaken(t);
                    m.decrementIntake();
                    saveData();
                    refreshTable();
                }
            } else if (t.isBefore(now)) {
                String key = m.getName() + "|" + LocalDate.now() + "|" + t;
                if (missedNotified.add(key) && trayIcon != null) {
                    trayIcon.displayMessage("Missed Dose",
                            m.getName() + " was due at " + t + " and hasn't been taken yet.",
                            TrayIcon.MessageType.WARNING);
                }
            }
        }
    }

    private class StatusRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                MedicationDose dose = tableModel.getDoseAt(row);
                Medication m = dose.getMedication();
                if (m.isTakenAt(dose.getTime())) {
                    c.setBackground(TAKEN_COLOR);
                } else if (dose.getTime().isBefore(LocalTime.now())) {
                    c.setBackground(MISSED_COLOR);
                } else {
                    c.setBackground(UPCOMING_COLOR);
                }
            }
            return c;
        }
    }

    private static class DoseTableModel extends AbstractTableModel {
        private final String[] columns = {"Name", "Dosage", "Category", "Type", "Time", "Doses Left", "Status"};
        private List<MedicationDose> doses = List.of();

        void setDoses(List<MedicationDose> doses) {
            this.doses = doses;
            fireTableDataChanged();
        }

        MedicationDose getDoseAt(int row) {
            return doses.get(row);
        }

        @Override
        public int getRowCount() {
            return doses.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            MedicationDose dose = doses.get(rowIndex);
            Medication m = dose.getMedication();
            switch (columnIndex) {
                case 0: return m.getName();
                case 1: return m.getDosage();
                case 2: return m.getCategory();
                case 3: return m.getType();
                case 4: return dose.getTime().toString();
                case 5: return m.isOutOfStock() ? "OUT" : String.valueOf(m.getNumberOfIntake());
                case 6:
                    if (m.isTakenAt(dose.getTime())) return "Taken";
                    if (dose.getTime().isBefore(LocalTime.now())) return "MISSED";
                    return "Upcoming";
                default: return "";
            }
        }
    }

    public static void main(String[] args) {
        UIManager.put("Button.font", APP_FONT);
        UIManager.put("Label.font", APP_FONT);
        UIManager.put("TextField.font", APP_FONT);
        UIManager.put("Table.font", APP_FONT);
        UIManager.put("OptionPane.messageFont", APP_FONT);
        UIManager.put("OptionPane.buttonFont", APP_FONT);
        SwingUtilities.invokeLater(() -> new MedicationApp().setVisible(true));
    }
}
