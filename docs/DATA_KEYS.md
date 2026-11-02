# Rehab MW-Flow — Data Keys and Storage Contract
Version: draft 0.1 (2026-11-02)

## Privacy boundaries
- **GitHub (public):** generic program rules, exercise library, program templates, schema definitions, references and app code.
- **ChatGPT conversation:** personal context supplied by the user for discussion; not a guaranteed permanent database or backup.
- **Browser localStorage:** existing workout progress/history, stored on the user's device only; may be lost when browser data is cleared and does not sync automatically.
- Do not hardcode personal health profiles, measurements, or personally identifying information in the public repository.

## Proposed canonical schema keys (future migration; NOT deployed yet)
| Key | Type | Purpose |
| --- | --- | --- |
| `schemaVersion` | integer | Data contract revision |
| `programVersion` | string | Approved program revision |
| `programStatus` | enum | `draft`, `approved`, `retired` |
| `programRules` | object | Safety, session structure, progression |
| `exerciseLibrary` | object keyed by exercise ID | Exercise definitions and alternatives |
| `exerciseId` | string | Stable exercise identifier, not display name |
| `sessionTemplate` | object | Ordered warm-up, activation, main, cool-down |
| `equipmentRequirements` | array | Generic equipment types; no personal inventory |
| `contraindications` | array | Generic cautions, not diagnosis |
| `sourceRefs` | array of IDs | Links to references in SOURCES.md |
| `assetPath` | string or null | Illustration reference |
| `progressionRules` | object | Criteria to adjust training load |
| `sessionLog` | object | Locally stored workout record |
| `painScore` | integer 0–10 | User-entered symptom score |
| `sessionNotes` | string | User-entered notes; private local storage |

## Existing app storage (legacy; inspect code before migration)
The current app stores progress in browser localStorage. Its current storage keys are defined by the implementation in `index.html`, which remains the source of truth until an explicit migration. Do not rename or delete existing keys without a backwards-compatible migration and backup.

## Versioning and compatibility
- Add `schemaVersion` to new structured data.
- Exercise IDs must be stable across UI copy changes and illustration updates.
- Schema migrations must preserve prior logs and completed-day state.
- Never silently overwrite private user data when deploying app updates.
- Any export/import feature should clearly disclose its privacy and overwrite behavior.

## Planned files
- `docs/PROGRAM_RULES.md`: design/safety governance
- `docs/DATA_KEYS.md`: storage and schema contract
- `docs/SOURCES.md`: research and provenance registry
- Future: versioned, non-personal exercise/program JSON once plan approved
