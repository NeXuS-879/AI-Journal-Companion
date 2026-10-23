# Assessment Test Report Maintenance

The working Word report is `docs/AI_Journal_Companion_Test_Report.docx`. It follows the supplied blank template retained unchanged at `docs/templates/AT2_Test_Report_Template.docx`.

The first populated version records verification completed through commit `a09afd8` on 23 October 2026. It contains 14 passed report cases and 11 planned cases marked `Not run`. These are grouped assessment cases, not the same count as the individual automated tests: recorded suites comprise 16 backend tests, 12 Android JVM tests and 13 emulator tests. Details and commands remain in the phase verification Markdown documents.

## Updating after development

The in-memory history increment was verified on 23 October 2026 against commit `a09afd8` plus working-tree changes. The report now contains 15 passed cases and 10 not run. The current Android retest passed 16 JVM tests and 15 emulator tests, including live multi-entry analysis and activity recreation. The 16 backend results are retained from the previous phase, not a new backend execution. See `docs/ANDROID_HISTORY_VERIFICATION.md`.

1. Run the checks appropriate to the implemented change and retain actual results, date, screenshots and defects. Do not infer passing status from implementation alone.
2. Edit `test_report_cases.json`. Preserve existing case IDs. Each case has five fields in order: ID, description, expected result, actual result and status (`Pass`, `Fail`, or `Not run`). Distinguish fixtures from real model calls and manual interaction.
3. Update the report's execution date and the verified source commit. Keep previous evidence in the phase reports so a retest does not erase the defect history.
4. Run `scripts/update_test_report.py` from the repository root using the configured bundled document Python runtime (requires `lxml`). It starts from the retained template and only changes `word/document.xml`, verifying all other package parts remain identical.
5. Render the resulting Word document and inspect every page before handing it over. Check continuation tables, header/logo clearance, wrapping, and complete case rows. The current QA used Word's read-only PDF export and bundled Poppler because bundled LibreOffice was unavailable.
6. Update screenshot/evidence references and commit the JSON, regenerated Word file, and relevant verification evidence together when requested.

The builder repeats the template's table structure on additional pages without changing its styles, branding or page geometry. Do not edit the retained template. If someone edits the working Word report directly, reconcile those changes with the JSON first: regeneration replaces the report from its source data.

Student name, ID, campus and developer-name fields remain placeholders. Supply those details before final submission. The original file in Downloads is unchanged.

## Evidence references

- `docs/ANDROID_UI_VERIFICATION.md`: initial input screen tests.
- `docs/ANDROID_NETWORK_VERIFICATION.md`: API client tests and live inference.
- `docs/ANDROID_VIEWMODEL_VERIFICATION.md`: state, UI integration, activity recreation and manual permission check.
- `docs/ANDROID_HISTORY_VERIFICATION.md`: in-memory entries, ordering, failure preservation, lazy scrolling and live history recreation.
- `docs/screenshots/journal-analysis.png`: successful Android analysis result (E1).
- `backend/tests`: automated backend contract and Ollama fixture tests.

The working report does not replace the final test/debug narrative, defect log, performance evaluation, user manual or technical documentation required by the assessment. Add further cases and evidence as those deliverables are completed.
