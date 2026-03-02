
import os

file_path = r'c:\Users\pc\.gemini\antigravity\scratch\rajasthan_exam\app\src\main\java\com\rajasthanexams\ui\screens\PracticeScreen.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
show_resume_moved = False
yes_button_updated = False
dialog_added = False

# Logic to inject showResumeDialog at top
# Find "var currentQuestionIndex"
for i, line in enumerate(lines):
    new_lines.append(line)
    
    # 1. Move State Variable
    if "var currentQuestionIndex" in line and not show_resume_moved:
        # Check if already there (unlikely due to prev steps)
        # We append the variable here
        new_lines.append('    var showResumeDialog by remember { mutableStateOf(false) }\n')
        show_resume_moved = True

# remove lower declaration
final_lines = []
for line in new_lines:
    if "var showResumeDialog" in line and "currentQuestionIndex" not in lines[lines.index(line)-1] and "currentQuestionIndex" not in lines[lines.index(line)-2]:
        # This identifies the "lower" one, assuming the top one was just inserted near currentQuestionIndex
        # Wait, the logic above inserts it. So new_lines has it.
        # We need to filter out the ONE that was originally further down (around line 454)
        # The line content is "    var showResumeDialog by remember { mutableStateOf(false) }"
        # We can just skip it if we encounter it and we know we already inserted one at top?
        # But we need to distinguish.
        # The top one is newly added in new_lines.
        pass
    
# Better approach: Pass 2
lines = new_lines
new_lines = []

removed_count = 0
for line in lines:
    if "var showResumeDialog" in line:
        removed_count += 1
        if removed_count > 1:
            continue # Skip duplicates (the one we added is first, so we keep it. older one is second)
            # Wait, our loop added it AFTER currentQuestionIndex.
            # So it appears early.
            # The original one is deeper.
            # So if we see it again, skip it.
    new_lines.append(line)

lines = new_lines
new_lines = []

# 2. Update Yes Button
skip_until_brace = False
for i, line in enumerate(lines):
    if "Yes Button (Blue)" in line:
        new_lines.append(line)
        # Expect next few lines to be Button -> onClick
        continue
        
    if "onClick = {" in line and "Yes Button (Blue)" in lines[i-2]: # Context check
        new_lines.append(line)
        # Inject new logic
        new_lines.append('                                // Accumulate time\n')
        new_lines.append('                                val now = System.currentTimeMillis()\n')
        new_lines.append('                                val diff = now - questionStartTime\n')
        new_lines.append('                                val currentTotal = timeSpentPerQuestion.getOrDefault(currentQuestionIndex, 0L)\n')
        new_lines.append('                                timeSpentPerQuestion[currentQuestionIndex] = currentTotal + diff\n')
        new_lines.append('\n')
        new_lines.append('                                val answersMap = selectedAnswers.mapKeys { (index, _) -> questions[index].id }\n')
        new_lines.append('                                com.rajasthanexams.data.OfflineManager.saveUserAnswers(testId, answersMap)\n')
        new_lines.append('                                com.rajasthanexams.data.OfflineManager.saveTestProgress(testId, timeLeft, currentQuestionIndex)\n')
        new_lines.append('\n')
        new_lines.append('                                // Save Marked\n')
        new_lines.append('                                val markedIds = markedQuestions.map { questions[it].id }\n')
        new_lines.append('                                com.rajasthanexams.data.OfflineManager.saveMarkedQuestions(testId, markedIds)\n')
        new_lines.append('\n')
        new_lines.append('                                // Save Times\n')
        new_lines.append('                                val timeMap = timeSpentPerQuestion.mapKeys { (idx, _) -> questions[idx].id }\n')
        new_lines.append('                                com.rajasthanexams.data.OfflineManager.saveTimeSpent(testId, timeMap)\n')
        new_lines.append('\n')
        new_lines.append('                                showPauseDialog = false\n')
        new_lines.append('                                onPause()\n')
        
        skip_until_brace = True
        continue
        
    if skip_until_brace:
        if "}," in line: # Closing brace of onClick
            new_lines.append(line)
            skip_until_brace = False
        continue
    
    new_lines.append(line)

lines = new_lines
new_lines = []

# 3. Add ResumeDialog at end of PracticeScreen
# We look for "val context = androidx.compose.ui.platform.LocalContext.current" which is near bottom?
# Reference Step 76: "val context = ..." was at line 405 (chunk context) / 1060ish?
# Let's find "if (showReportDialog) {"
for line in lines:
    if "if (showReportDialog) {" in line and not dialog_added:
        # Insert ResumeDialog logic BEFORE this, or ensure it's in the Column/Box scope?
        # PracticeScreen uses Scaffold. showReportDialog is usually at top level (top of function or bottom).
        # We can put it right before showReportDialog.
        
        new_lines.append('    if (showResumeDialog) {\n')
        new_lines.append('        val attemptedCount = selectedAnswers.size\n')
        new_lines.append('        val markedCount = markedQuestions.size\n')
        new_lines.append('        val unattemptedCount = totalQuestions - attemptedCount\n')
        new_lines.append('        \n')
        new_lines.append('        com.rajasthanexams.ui.components.ResumeDialog(\n')
        new_lines.append('            timeLeft = formattedTime,\n')
        new_lines.append('            attempted = attemptedCount,\n')
        new_lines.append('            unattempted = unattemptedCount,\n')
        new_lines.append('            marked = markedCount,\n')
        new_lines.append('            onResume = { \n')
        new_lines.append('                showResumeDialog = false \n')
        new_lines.append('            },\n')
        new_lines.append('            onBack = {\n')
        new_lines.append('                onPause()\n')
        new_lines.append('            }\n')
        new_lines.append('        )\n')
        new_lines.append('    }\n')
        new_lines.append('\n')
        dialog_added = True
    new_lines.append(line)

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print("Modification complete.")
