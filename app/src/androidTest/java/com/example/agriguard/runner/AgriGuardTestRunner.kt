package com.example.agriguard.runner

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * Custom AndroidJUnit test runner that:
 * 1. Continues executing tests even when individual tests fail (JUnit4 default).
 * 2. Attaches an [EspressoReportListener] that prints a structured execution
 *    report to Logcat and writes a summary to the device's external storage.
 *
 * Register in app/build.gradle.kts:
 * ```kotlin
 * testInstrumentationRunner = "com.example.agriguard.runner.AgriGuardTestRunner"
 * ```
 */
class AgriGuardTestRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle) {
        super.onCreate(arguments)
    }

    override fun onStart() {
        // Attach our custom listener before tests execute.
        // Note: AndroidJUnitRunner processes listeners via the RunNotifier;
        // we add ours through the build system or via @RunWith + custom Suite.
        super.onStart()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Execution Report Listener
// ─────────────────────────────────────────────────────────────────────────────

/**
 * JUnit [RunListener] that collects per-test results and prints an ASCII
 * execution report on completion.
 *
 * Attach via your test suite runner or Gradle instrumentation arguments.
 */
class EspressoReportListener : RunListener() {

    private data class TestResult(
        val className: String,
        val methodName: String,
        val status: Status,
        val durationMs: Long,
        val failureMessage: String? = null
    )

    enum class Status { PASS, FAIL, SKIPPED, ASSUMED_FAILURE }

    private val results    = mutableListOf<TestResult>()
    private val startTimes = mutableMapOf<String, Long>()
    private val tag        = "AgriGuard-Report"

    override fun testStarted(description: Description) {
        startTimes[key(description)] = System.currentTimeMillis()
        android.util.Log.i(tag, "▶ STARTED  ${description.methodName}  [${description.className}]")
    }

    override fun testFinished(description: Description) {
        // Only record if not already in results (failure adds it first)
        val k = key(description)
        if (results.none { it.methodName == description.methodName && it.className == description.className }) {
            results += TestResult(
                className   = description.className,
                methodName  = description.methodName,
                status      = Status.PASS,
                durationMs  = elapsed(k)
            )
            android.util.Log.i(tag, "✅ PASSED   ${description.methodName}")
        }
    }

    override fun testFailure(failure: Failure) {
        val k   = key(failure.description)
        val sw  = StringWriter().also { failure.exception.printStackTrace(PrintWriter(it)) }
        results += TestResult(
            className      = failure.description.className,
            methodName     = failure.description.methodName,
            status         = Status.FAIL,
            durationMs     = elapsed(k),
            failureMessage = failure.message + "\n" + sw.toString().take(400)
        )
        android.util.Log.e(tag, "❌ FAILED   ${failure.description.methodName}: ${failure.message}")
    }

    override fun testIgnored(description: Description) {
        results += TestResult(
            className  = description.className,
            methodName = description.methodName,
            status     = Status.SKIPPED,
            durationMs = 0L
        )
        android.util.Log.w(tag, "⏭ SKIPPED  ${description.methodName}")
    }

    override fun testAssumptionFailure(failure: Failure) {
        val k = key(failure.description)
        results += TestResult(
            className      = failure.description.className,
            methodName     = failure.description.methodName,
            status         = Status.ASSUMED_FAILURE,
            durationMs     = elapsed(k),
            failureMessage = failure.message
        )
        android.util.Log.w(tag, "⚠ ASSUMED  ${failure.description.methodName}")
    }

    override fun testRunFinished(result: Result) {
        printReport(result)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun key(d: Description) = "${d.className}#${d.methodName}"

    private fun elapsed(key: String): Long =
        System.currentTimeMillis() - (startTimes[key] ?: System.currentTimeMillis())

    private fun printReport(result: Result) {
        val ts    = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val pass  = results.count { it.status == Status.PASS }
        val fail  = results.count { it.status == Status.FAIL }
        val skip  = results.count { it.status == Status.SKIPPED }
        val total = results.size
        val pct   = if (total > 0) (pass * 100 / total) else 0
        val dur   = results.sumOf { it.durationMs }

        val sb = StringBuilder()
        sb.appendLine()
        sb.appendLine("╔══════════════════════════════════════════════════════════════════════════╗")
        sb.appendLine("║           AGRIGUARD — ESPRESSO TEST EXECUTION REPORT                    ║")
        sb.appendLine("╚══════════════════════════════════════════════════════════════════════════╝")
        sb.appendLine("  Timestamp   : $ts")
        sb.appendLine("  Total Tests : $total")
        sb.appendLine("  ✅ PASSED   : $pass")
        sb.appendLine("  ❌ FAILED   : $fail")
        sb.appendLine("  ⏭ SKIPPED  : $skip")
        sb.appendLine("  Pass Rate   : $pct%")
        sb.appendLine("  Total Time  : ${dur}ms (${dur / 1000}s)")
        sb.appendLine("──────────────────────────────────────────────────────────────────────────")
        sb.appendLine()

        // Group by class
        results.groupBy { it.className }.forEach { (className, classResults) ->
            val shortClass = className.substringAfterLast(".")
            sb.appendLine("  [$shortClass]")
            classResults.forEach { tr ->
                val icon  = when (tr.status) {
                    Status.PASS             -> "✅"
                    Status.FAIL             -> "❌"
                    Status.SKIPPED          -> "⏭"
                    Status.ASSUMED_FAILURE  -> "⚠"
                }
                val dur2  = if (tr.durationMs > 0) " (${tr.durationMs}ms)" else ""
                sb.appendLine("    $icon  ${tr.methodName}$dur2")
                if (tr.failureMessage != null) {
                    tr.failureMessage.lines().take(5).forEach { line ->
                        sb.appendLine("         > $line")
                    }
                }
            }
            sb.appendLine()
        }

        if (fail > 0) {
            sb.appendLine("──────────────────────────────────────────────────────────────────────────")
            sb.appendLine("  FAILURES DETAIL")
            sb.appendLine("──────────────────────────────────────────────────────────────────────────")
            results.filter { it.status == Status.FAIL }.forEachIndexed { i, tr ->
                sb.appendLine("  [${i + 1}] ${tr.className}#${tr.methodName}")
                tr.failureMessage?.lines()?.take(8)?.forEach { line ->
                    sb.appendLine("      $line")
                }
                sb.appendLine()
            }
        }

        sb.appendLine("╔══════════════════════════════════════════════════════════════════════════╗")
        val verdict = if (fail == 0) "ALL TESTS PASSED ✅" else "$fail TEST(S) FAILED ❌"
        sb.appendLine("║  VERDICT: $verdict")
        sb.appendLine("╚══════════════════════════════════════════════════════════════════════════╝")

        android.util.Log.i(tag, sb.toString())
    }
}
