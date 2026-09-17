## Medication Tracking System

A small Java app for tracking medications, multiple daily intake times, and
remaining doses. Medications are kept in a binary search tree (sorted by
name); each one can have several intake times per day, and those individual
"doses" are what get queued, reminded, and marked as taken.

## Running

Compile once:

```
javac -d bin src/*.java
```

Then run either interface:

- **GUI (Swing)** — `java -cp bin MedicationApp`
- **Console** — `java -cp bin Main`

Both read from and save to the same files under `data/`, so data carries
over between the two and between runs.

## Features

- Add, edit, and delete medications, each with one or more daily intake times
  (e.g. `09:00,14:00,20:00`)
- Search by name (with suggestions for partial/misspelled names), type, or category
- Intake queue: auto-fill by time, or add a specific dose manually
- Remaining-dose tracking with low-stock / out-of-stock warnings
- Today's plan view showing Taken / Missed / Upcoming status per dose
- Background reminders at each dose's intake time
- Weekly adherence report (last 7 days, per medication, with a list of missed doses)
- Exportable caregiver report (`data/caregiver_report.txt`) to share adherence with family
- GUI-only: a system tray alert fires once when a dose goes unmarked past its time —
  a same-machine nudge for a caregiver, since the app has no email/SMS integration
- Data persists automatically to `data/medications.txt` and `data/intake_history.txt`

## Folder Structure

- `src`: Java source files
- `lib`: third-party dependencies (if any)
- `bin`: compiled output (generated, not checked in)
- `data`: saved medication and history data (generated, not checked in)
