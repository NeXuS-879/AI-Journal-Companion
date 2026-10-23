"""Build the assessment test report while preserving the supplied DOCX package.

Update docs/testing/test_report_cases.json after actual verification, then run
this script with the bundled Python interpreter. Only document.xml is patched;
all template styles, headers, footers, logos and custom XML stay byte-identical.
"""

from copy import deepcopy
from hashlib import sha256
import json
from pathlib import Path
from zipfile import ZipFile

from lxml import etree


ROOT = Path(__file__).resolve().parents[1]
TEMPLATE = ROOT / "docs/templates/AT2_Test_Report_Template.docx"
OUTPUT = ROOT / "docs/AI_Journal_Companion_Test_Report.docx"
DATA = ROOT / "docs/testing/test_report_cases.json"
W = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"
NS = {"w": W[1:-1]}


def set_paragraph(paragraph, text):
    """Retain paragraph and first-run properties when filling a plain text slot."""
    run_properties = paragraph.find("w:r/w:rPr", NS)
    for child in list(paragraph):
        if child.tag != W + "pPr":
            paragraph.remove(child)
    run = etree.SubElement(paragraph, W + "r")
    if run_properties is not None:
        run.append(deepcopy(run_properties))
    node = etree.SubElement(run, W + "t")
    node.set("{http://www.w3.org/XML/1998/namespace}space", "preserve")
    node.text = text


def set_cell(cell, text):
    paragraphs = cell.findall("w:p", NS)
    set_paragraph(paragraphs[0], text)
    for paragraph in paragraphs[1:]:
        cell.remove(paragraph)


def main():
    data = json.loads(DATA.read_text(encoding="utf-8"))
    cases = data["cases"]
    assert len({case[0] for case in cases}) == len(cases), "Duplicate test IDs"
    assert all(len(case) == 5 and case[-1] in {"Pass", "Fail", "Not run"} for case in cases)

    with ZipFile(TEMPLATE) as source:
        original = {part.filename: source.read(part.filename) for part in source.infolist()}
        xml = etree.fromstring(original["word/document.xml"])
        body = xml.find("w:body", NS)
        paragraphs = body.findall("w:p", NS)
        set_paragraph(paragraphs[0], "Members Name & ID: [Student name and ID]")
        set_paragraph(paragraphs[1], "Campus: [Campus]")
        set_paragraph(paragraphs[2], f"Date: {data['updated']}")
        # Existing blank body slots hold the concise evidence key and update status.
        set_paragraph(paragraphs[3], "E1: docs/screenshots/journal-analysis.png. Evidence: docs/ANDROID_*_VERIFICATION.md and test suites.")
        set_paragraph(paragraphs[4], data["summary"] + " Build: " + data["source_commit"] + ".")

        table = body.find("w:tbl", NS)
        rows = table.findall("w:tr", NS)
        metadata = [
            (1, ["Project Name: AI Journal Companion", "Test Type: Unit, API and UI"]),
            (2, ["Description: Analysis pipeline and planned features", "Date: " + data["updated"]]),
            (3, ["Developer: [Student name]", "Tester: Automated suites and assisted checks"]),
        ]
        for index, values in metadata:
            for cell, value in zip(rows[index].findall("w:tc", NS), values):
                set_cell(cell, value)

        # Clone the template's blank case row for additional cases; never redesign it.
        prototype = deepcopy(rows[5])
        for row in rows[5:]:
            table.remove(row)
        for case in cases:
            row = deepcopy(prototype)
            cells = row.findall("w:tc", NS)
            assert len(cells) == 5, "Unexpected template case-row structure"
            for cell, value in zip(cells, case):
                set_cell(cell, value)
            # Keep a test case together and allow it to grow with text.
            properties = row.find("w:trPr", NS)
            if properties is None:
                properties = etree.Element(W + "trPr")
                row.insert(0, properties)
            etree.SubElement(properties, W + "cantSplit")
            table.append(row)
        # Word requires a contiguous header block; repeat the source's five header rows.
        for row in rows[:5]:
            properties = row.find("w:trPr", NS)
            if properties is None:
                properties = etree.Element(W + "trPr")
                row.insert(0, properties)
            etree.SubElement(properties, W + "tblHeader")

        # The template's decorative header extends below its top margin. Start
        # continuation tables after a spacer, avoiding overlap with that artwork.
        case_rows = table.findall("w:tr", NS)[5:]
        for row in case_rows[14:]:
            table.remove(row)
        previous = table
        for start in range(14, len(case_rows), 14):
            spacer = etree.Element(W + "p")
            properties = etree.SubElement(spacer, W + "pPr")
            etree.SubElement(properties, W + "pageBreakBefore")
            spacing = etree.SubElement(properties, W + "spacing")
            spacing.set(W + "after", "480")
            continuation = deepcopy(table)
            for row in continuation.findall("w:tr", NS)[5:]:
                continuation.remove(row)
            for row in case_rows[start:start + 14]:
                continuation.append(deepcopy(row))
            position = body.index(previous) + 1
            body.insert(position, spacer)
            body.insert(position + 1, continuation)
            previous = continuation

        output_xml = etree.tostring(xml, xml_declaration=True, encoding="UTF-8", standalone=True)
        with ZipFile(OUTPUT, "w") as destination:
            for part in source.infolist():
                destination.writestr(part, output_xml if part.filename == "word/document.xml" else original[part.filename])

    with ZipFile(OUTPUT) as result:
        changed = [name for name, content in original.items() if result.read(name) != content]
    assert changed == ["word/document.xml"], f"Unexpected modified package parts: {changed}"
    print(f"Created {OUTPUT}; {len(cases)} cases; template SHA256 {sha256(TEMPLATE.read_bytes()).hexdigest()}")


if __name__ == "__main__":
    main()
