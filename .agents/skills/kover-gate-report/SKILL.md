---
name: kover-gate-report
description: Run KoverGate coverage gate and fix coverage gaps after writing or modifying tests
---

# koverGate Report Skill

After writing or modifying tests in a module, check coverage gaps and close them.

## Steps

1. **Run the coverage gate task** for the module you changed:
   ```bash
   ./gradlew :<module-name>:koverGateReport
   ```
   Replace `<module-name>` with the Gradle module path (e.g., `:app`, `:core:data`).

2. **Read the JSON gap report:**
   ```
   <module-name>/build/reports/kover/gap-report.json
   ```

3. **Interpret results:**
   - If `"passed": true` — coverage requirements are met. Done.
   - If `"passed": false` — examine the `files` array. Each entry lists:
     - `uncoveredLines`: line numbers with no coverage (`mi > 0`)
     - `missedBranchLines`: line numbers with partial branch coverage (`mb > 0`)

4. **Write targeted tests** to cover the identified lines and branches. Focus on:
   - The specific line numbers listed in `uncoveredLines`
   - The conditional paths at lines listed in `missedBranchLines`

5. **Re-run the task** to confirm gaps are closed:
   ```bash
   ./gradlew :<module-name>:koverGateReport
   ```
   Repeat until `"passed": true`.

## Notes

- Adjust thresholds in your module's `build.gradle.kts` via the `koverGate {}` block if 100% is not the right target for a specific metric.
- Disable metrics that don't apply with `disabledMetrics.set(setOf(CoverageMetric.BRANCH))`.
- The JSON report is always written (even on pass) at `build/reports/kover/gap-report.json`.

