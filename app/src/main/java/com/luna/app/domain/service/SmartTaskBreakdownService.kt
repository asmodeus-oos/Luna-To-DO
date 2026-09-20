package com.luna.app.domain.service

import com.luna.app.domain.model.EnergyLevel
import com.luna.app.domain.model.Priority

data class BreakdownResult(
    val suggestedSubtasks: List<String>,
    val suggestedMinutes: Int,
    val suggestedEnergy: EnergyLevel,
    val suggestedPriority: Priority,
    val rationale: String
)

object SmartTaskBreakdownService {

    fun generateBreakdown(taskTitle: String): BreakdownResult {
        val lower = taskTitle.lowercase().trim()

        return when {
            // Software & Engineering
            lower.contains("bug") || lower.contains("fix") || lower.contains("issue") || lower.contains("crash") -> {
                BreakdownResult(
                    suggestedSubtasks = listOf(
                        "Reproduce bug with minimal steps",
                        "Locate root cause in code stack trace",
                        "Write unit or regression test",
                        "Implement fix & verify locally",
                        "Submit pull request & review"
                    ),
                    suggestedMinutes = 45,
                    suggestedEnergy = EnergyLevel.HIGH,
                    suggestedPriority = Priority.P1,
                    rationale = "High-urgency debugging workflow with regression protection."
                )
            }
            lower.contains("feature") || lower.contains("app") || lower.contains("build") || lower.contains("code") || lower.contains("api") || lower.contains("backend") -> {
                BreakdownResult(
                    suggestedSubtasks = listOf(
                        "Define specs, user flow & API contracts",
                        "Design Room SQLite schema / database models",
                        "Implement UI composables & state hoisting",
                        "Integrate repository & viewmodel logic",
                        "Verify edge cases & test on device"
                    ),
                    suggestedMinutes = 90,
                    suggestedEnergy = EnergyLevel.HIGH,
                    suggestedPriority = Priority.P1,
                    rationale = "Full-stack native implementation roadmap."
                )
            }

            // Design & UI/UX
            lower.contains("design") || lower.contains("ui") || lower.contains("ux") || lower.contains("mockup") || lower.contains("prototype") || lower.contains("figma") -> {
                BreakdownResult(
                    suggestedSubtasks = listOf(
                        "Gather visual references & moodboard",
                        "Draft low-fidelity wireframes & component hierarchy",
                        "Define color tokens, typography & spacing system",
                        "Craft high-fidelity mockups with dark/light themes",
                        "Prepare design export & assets handoff"
                    ),
                    suggestedMinutes = 60,
                    suggestedEnergy = EnergyLevel.HIGH,
                    suggestedPriority = Priority.P2,
                    rationale = "Structured iterative design process."
                )
            }

            // Business, Proposals & Client
            lower.contains("proposal") || lower.contains("client") || lower.contains("pitch") || lower.contains("deck") || lower.contains("contract") -> {
                BreakdownResult(
                    suggestedSubtasks = listOf(
                        "Clarify project scope, deliverables & constraints",
                        "Draft executive summary & value proposition",
                        "Estimate timeline, milestones & budget",
                        "Review formatting, typos & visual polish",
                        "Send proposal & schedule follow-up"
                    ),
                    suggestedMinutes = 60,
                    suggestedEnergy = EnergyLevel.HIGH,
                    suggestedPriority = Priority.P1,
                    rationale = "Client-facing deliverable breakdown."
                )
            }

            // Writing, Documentation & Articles
            lower.contains("write") || lower.contains("blog") || lower.contains("article") || lower.contains("doc") || lower.contains("readme") -> {
                BreakdownResult(
                    suggestedSubtasks = listOf(
                        "Brainstorm core thesis & outline main sections",
                        "Draft initial unedited version (fast flow)",
                        "Refine flow, tone, headers & clarity",
                        "Add diagrams, code snippets or callouts",
                        "Final proofread & publish"
                    ),
                    suggestedMinutes = 45,
                    suggestedEnergy = EnergyLevel.MEDIUM,
                    suggestedPriority = Priority.P2,
                    rationale = "Multi-pass writing and editing method."
                )
            }

            // Learning & Study
            lower.contains("study") || lower.contains("learn") || lower.contains("exam") || lower.contains("course") || lower.contains("read") -> {
                BreakdownResult(
                    suggestedSubtasks = listOf(
                        "Review syllabus & identify key focus topics",
                        "Deep-read chapter / documentation with active notes",
                        "Build hands-on sample project or flashcards",
                        "Self-test with practice questions or review",
                        "Summarize key takeaways in personal notebook"
                    ),
                    suggestedMinutes = 50,
                    suggestedEnergy = EnergyLevel.HIGH,
                    suggestedPriority = Priority.P2,
                    rationale = "Active recall & spaced learning sequence."
                )
            }

            // Planning & Travel
            lower.contains("trip") || lower.contains("travel") || lower.contains("vacation") || lower.contains("flight") || lower.contains("pack") -> {
                BreakdownResult(
                    suggestedSubtasks = listOf(
                        "Confirm dates, transportation & lodging",
                        "List key sights, activities & dining spots",
                        "Check weather, reservations & travel docs",
                        "Pack luggage with checklist",
                        "Double-check departure times & tickets"
                    ),
                    suggestedMinutes = 30,
                    suggestedEnergy = EnergyLevel.LOW,
                    suggestedPriority = Priority.P3,
                    rationale = "Stress-free travel planning checklist."
                )
            }

            // Finance & Taxes
            lower.contains("tax") || lower.contains("finance") || lower.contains("budget") || lower.contains("invoice") -> {
                BreakdownResult(
                    suggestedSubtasks = listOf(
                        "Gather receipts, invoices & bank statements",
                        "Categorize business & personal expenses",
                        "Calculate totals & verify deductions",
                        "File documents or send to accountant",
                        "Archive copies in secure storage"
                    ),
                    suggestedMinutes = 45,
                    suggestedEnergy = EnergyLevel.MEDIUM,
                    suggestedPriority = Priority.P1,
                    rationale = "Auditable accounting and filing sequence."
                )
            }

            // Default Dynamic Breakdown for any goal
            else -> {
                BreakdownResult(
                    suggestedSubtasks = listOf(
                        "Clarify definition of done for: $taskTitle",
                        "Prepare necessary tools, links & environment",
                        "Execute primary core deliverable",
                        "Review quality & verify against goals",
                        "Document or wrap up final results"
                    ),
                    suggestedMinutes = 30,
                    suggestedEnergy = EnergyLevel.MEDIUM,
                    suggestedPriority = Priority.P2,
                    rationale = "Universal progressive goal breakdown."
                )
            }
        }
    }
}
