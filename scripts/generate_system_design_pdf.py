#!/usr/bin/env python3
"""Generate TechCare Services system design PDF with UML diagrams."""

import subprocess
import sys
from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm, mm
from reportlab.platypus import (
    Image,
    PageBreak,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)

ROOT = Path(__file__).resolve().parent.parent
DIAGRAMS_DIR = ROOT / "docs" / "diagrams"
OUTPUT_DIR = DIAGRAMS_DIR / "output"
PLANTUML_JAR = Path("/tmp/plantuml.jar")
PDF_PATH = ROOT / "docs" / "TechCare_System_Design.pdf"


def render_plantuml() -> dict[str, Path]:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    sources = {
        "use_case": (DIAGRAMS_DIR / "use_case_diagram.puml", "TechCare_Use_Case_Diagram.png"),
        "class": (DIAGRAMS_DIR / "class_diagram.puml", "TechCare_Class_Diagram.png"),
        "activity": (DIAGRAMS_DIR / "activity_diagram.puml", "TechCare_Activity_Diagram.png"),
        "er": (DIAGRAMS_DIR / "er_diagram.puml", "TechCare_ER_Diagram.png"),
        "relational_schema": (
            DIAGRAMS_DIR / "relational_schema_diagram.puml",
            "TechCare_Relational_Schema_Diagram.png",
        ),
    }
    images: dict[str, Path] = {}
    for key, (puml, png_name) in sources.items():
        out = OUTPUT_DIR / f"{key}_diagram.png"
        cmd = ["java", "-jar", str(PLANTUML_JAR), "-tpng", "-o", str(OUTPUT_DIR), str(puml)]
        subprocess.run(cmd, check=True, capture_output=True, cwd=str(DIAGRAMS_DIR))
        generated = OUTPUT_DIR / png_name
        if not generated.exists():
            generated = DIAGRAMS_DIR / png_name
        if not generated.exists():
            matches = list(OUTPUT_DIR.glob("*.png")) + list(DIAGRAMS_DIR.glob("TechCare_*.png"))
            raise FileNotFoundError(f"PlantUML did not produce image for {puml}: {matches}")
        if generated != out:
            out.write_bytes(generated.read_bytes())
            generated.unlink(missing_ok=True)
        images[key] = out
    return images


def schema_tables():
    """Normalized relational schema (3NF) derived from ER diagram."""
    return [
        {
            "title": "Table: users",
            "nf": "3NF — No transitive dependencies; role is functionally dependent on userId.",
            "rows": [
                ["Column", "Type", "Constraints"],
                ["userId", "INTEGER", "PRIMARY KEY, AUTOINCREMENT"],
                ["fullName", "TEXT", "NOT NULL"],
                ["email", "TEXT", "NOT NULL, UNIQUE"],
                ["phone", "TEXT", "NOT NULL, UNIQUE"],
                ["password", "TEXT", "NOT NULL (PBKDF2 hash)"],
                ["password_salt", "TEXT", "NOT NULL"],
                ["address", "TEXT", "NOT NULL"],
                ["role", "TEXT", "NOT NULL, DEFAULT 'CUSTOMER'"],
            ],
        },
        {
            "title": "Table: devices",
            "nf": "3NF — deviceType/brand/model depend only on deviceId; userId is FK only.",
            "rows": [
                ["Column", "Type", "Constraints"],
                ["deviceId", "INTEGER", "PRIMARY KEY, AUTOINCREMENT"],
                ["userId", "INTEGER", "NOT NULL, FK → users(userId) ON DELETE CASCADE"],
                ["deviceType", "TEXT", "NOT NULL"],
                ["brand", "TEXT", "NOT NULL"],
                ["model", "TEXT", "NOT NULL"],
            ],
        },
        {
            "title": "Table: services",
            "nf": "3NF — Catalog entity; all attributes depend on serviceId.",
            "rows": [
                ["Column", "Type", "Constraints"],
                ["serviceId", "INTEGER", "PRIMARY KEY, AUTOINCREMENT"],
                ["deviceType", "TEXT", "NOT NULL"],
                ["serviceName", "TEXT", "NOT NULL"],
                ["description", "TEXT", "NOT NULL"],
                ["estimatedPrice", "REAL", ""],
            ],
        },
        {
            "title": "Table: bookings",
            "nf": "3NF — Booking facts reference users/devices/services via FKs; no redundant user data.",
            "rows": [
                ["Column", "Type", "Constraints"],
                ["bookingId", "INTEGER", "PRIMARY KEY, AUTOINCREMENT"],
                ["userId", "INTEGER", "NOT NULL, FK → users(userId) ON DELETE CASCADE"],
                ["deviceId", "INTEGER", "NOT NULL, FK → devices(deviceId) ON DELETE CASCADE"],
                ["serviceId", "INTEGER", "NOT NULL, FK → services(serviceId) ON DELETE CASCADE"],
                ["issueDescription", "TEXT", "NOT NULL"],
                ["serviceMethod", "TEXT", "NOT NULL"],
                ["preferredDate", "TEXT", "NOT NULL"],
                ["preferredTime", "TEXT", "NOT NULL"],
                ["status", "TEXT", "NOT NULL"],
                ["technicianName", "TEXT", "NOT NULL"],
                ["estimatedCompletion", "TEXT", "NOT NULL"],
                ["createdAt", "TEXT", "NOT NULL"],
                ["photoPath", "TEXT", "NOT NULL, DEFAULT ''"],
                ["adminRemarks", "TEXT", "NOT NULL, DEFAULT ''"],
            ],
        },
        {
            "title": "Table: repair_status",
            "nf": "3NF — Status history normalized; each event depends on statusId.",
            "rows": [
                ["Column", "Type", "Constraints"],
                ["statusId", "INTEGER", "PRIMARY KEY, AUTOINCREMENT"],
                ["bookingId", "INTEGER", "NOT NULL, FK → bookings(bookingId) ON DELETE CASCADE"],
                ["status", "TEXT", "NOT NULL"],
                ["remarks", "TEXT", "NOT NULL"],
                ["updatedAt", "TEXT", "NOT NULL"],
                ["updatedBy", "TEXT", "NOT NULL, DEFAULT 'System'"],
            ],
        },
        {
            "title": "Table: faqs",
            "nf": "3NF — Independent FAQ content; question/answer depend on faqId.",
            "rows": [
                ["Column", "Type", "Constraints"],
                ["faqId", "INTEGER", "PRIMARY KEY, AUTOINCREMENT"],
                ["question", "TEXT", "NOT NULL"],
                ["answer", "TEXT", "NOT NULL"],
            ],
        },
        {
            "title": "Table: maintenance_tips",
            "nf": "3NF — Tips content normalized per tipId.",
            "rows": [
                ["Column", "Type", "Constraints"],
                ["tipId", "INTEGER", "PRIMARY KEY, AUTOINCREMENT"],
                ["deviceType", "TEXT", "NOT NULL"],
                ["title", "TEXT", "NOT NULL"],
                ["description", "TEXT", "NOT NULL"],
            ],
        },
        {
            "title": "Table: support_messages",
            "nf": "3NF — userName denormalized for display; message facts keyed by messageId.",
            "rows": [
                ["Column", "Type", "Constraints"],
                ["messageId", "INTEGER", "PRIMARY KEY, AUTOINCREMENT"],
                ["userId", "INTEGER", "NOT NULL, FK → users(userId) ON DELETE CASCADE"],
                ["userName", "TEXT", "NOT NULL"],
                ["message", "TEXT", "NOT NULL"],
                ["createdAt", "TEXT", "NOT NULL"],
            ],
        },
    ]


def build_pdf(images: dict[str, Path]) -> None:
    PDF_PATH.parent.mkdir(parents=True, exist_ok=True)
    doc = SimpleDocTemplate(
        str(PDF_PATH),
        pagesize=A4,
        rightMargin=2 * cm,
        leftMargin=2 * cm,
        topMargin=2 * cm,
        bottomMargin=2 * cm,
        title="TechCare Services System Design",
    )
    styles = getSampleStyleSheet()
    title_style = ParagraphStyle(
        "DocTitle",
        parent=styles["Title"],
        fontSize=22,
        spaceAfter=12,
        alignment=TA_CENTER,
    )
    heading_style = ParagraphStyle(
        "SectionHeading",
        parent=styles["Heading1"],
        fontSize=16,
        spaceBefore=6,
        spaceAfter=10,
        textColor=colors.HexColor("#1A5276"),
    )
    body_style = ParagraphStyle(
        "Body",
        parent=styles["Normal"],
        fontSize=10,
        leading=14,
        alignment=TA_LEFT,
    )
    subtitle_style = ParagraphStyle(
        "Subtitle",
        parent=styles["Normal"],
        fontSize=11,
        alignment=TA_CENTER,
        textColor=colors.HexColor("#566573"),
        spaceAfter=20,
    )

    story = []
    story.append(Paragraph("TechCare Services", title_style))
    story.append(
        Paragraph(
            "System Design Documentation<br/>Use Case · Class · Activity · ER · Relational Schema",
            subtitle_style,
        )
    )
    story.append(
        Paragraph(
            "Android repair-booking application (MVVM + Room/SQLite). "
            "Database file: <b>techcare.db</b> (version 4).",
            body_style,
        )
    )
    story.append(Spacer(1, 12))

    sections = [
        (
            "1. Use Case Diagram",
            "Actors: <b>Customer</b>, <b>Admin</b>, and <b>System</b>. "
            "Customers register devices, browse services, submit repair requests, "
            "track bookings, and contact support. Admins manage bookings, catalog content, "
            "and support messages.",
            images["use_case"],
            landscape(A4),
        ),
        (
            "2. Class Diagram",
            "MVVM architecture: Activities observe ViewModels, which delegate to Repositories "
            "and Room DAOs. Entities map to SQLite tables in AppDatabase.",
            images["class"],
            landscape(A4),
        ),
        (
            "3. Activity Diagram",
            "End-to-end repair booking flow from splash/login through submission, "
            "status tracking, and admin updates.",
            images["activity"],
            A4,
        ),
        (
            "4. Entity-Relationship Diagram",
            "Eight tables with foreign-key relationships. "
            "bookings is the central fact table joining users, devices, and services. "
            "repair_status stores the booking status timeline.",
            images["er"],
            landscape(A4),
        ),
    ]

    for title, desc, img_path, page_size in sections:
        story.append(PageBreak())
        story.append(Paragraph(title, heading_style))
        story.append(Paragraph(desc, body_style))
        story.append(Spacer(1, 8))
        page_w = page_size[0] - 4 * cm
        page_h = page_size[1] - 5 * cm
        story.append(
            Image(
                str(img_path),
                width=page_w,
                height=page_h,
                kind="proportional",
            )
        )

    story.append(PageBreak())
    story.append(Paragraph("5. Normalized Relational Schema", heading_style))
    story.append(
        Paragraph(
            "The schema below is in <b>Third Normal Form (3NF)</b>: "
            "each non-key attribute depends only on the primary key, "
            "and there are no transitive dependencies. "
            "Referential integrity uses ON DELETE CASCADE on all foreign keys.",
            body_style,
        )
    )
    story.append(Spacer(1, 10))

    story.append(Paragraph("<b>Relationship Summary</b>", body_style))
    rel_rows = [
        ["Relationship", "Cardinality", "FK / Rule"],
        ["users → devices", "1 : N", "devices.userId → users.userId"],
        ["users → bookings", "1 : N", "bookings.userId → users.userId"],
        ["users → support_messages", "1 : N", "support_messages.userId → users.userId"],
        ["devices → bookings", "1 : N", "bookings.deviceId → devices.deviceId"],
        ["services → bookings", "1 : N", "bookings.serviceId → services.serviceId"],
        ["bookings → repair_status", "1 : N", "repair_status.bookingId → bookings.bookingId"],
    ]
    rel_table = Table(rel_rows, colWidths=[5 * cm, 3 * cm, 9 * cm])
    rel_table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#1A5276")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
                ("FONTSIZE", (0, 0), (-1, -1), 8),
                ("GRID", (0, 0), (-1, -1), 0.5, colors.grey),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#EBF5FB")]),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 4),
                ("RIGHTPADDING", (0, 0), (-1, -1), 4),
            ]
        )
    )
    story.append(rel_table)
    story.append(Spacer(1, 14))

    for schema in schema_tables():
        story.append(Paragraph(schema["title"], body_style))
        story.append(
            Paragraph(
                f"<i>{schema['nf']}</i>",
                ParagraphStyle("NF", parent=body_style, fontSize=8, textColor=colors.grey),
            )
        )
        col_widths = [4.5 * cm, 3 * cm, 9.5 * cm]
        t = Table(schema["rows"], colWidths=col_widths, repeatRows=1)
        t.setStyle(
            TableStyle(
                [
                    ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#2874A6")),
                    ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                    ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
                    ("FONTSIZE", (0, 0), (-1, -1), 7.5),
                    ("GRID", (0, 0), (-1, -1), 0.4, colors.grey),
                    ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F4F6F7")]),
                    ("VALIGN", (0, 0), (-1, -1), "TOP"),
                    ("LEFTPADDING", (0, 0), (-1, -1), 3),
                ]
            )
        )
        story.append(t)
        story.append(Spacer(1, 10))

    doc.build(story)
    print(f"PDF created: {PDF_PATH}")


def main() -> int:
    if not PLANTUML_JAR.exists():
        print("PlantUML jar not found at /tmp/plantuml.jar", file=sys.stderr)
        return 1
    images = render_plantuml()
    build_pdf(images)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
