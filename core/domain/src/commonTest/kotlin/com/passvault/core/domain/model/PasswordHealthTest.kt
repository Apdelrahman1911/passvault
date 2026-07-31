package com.passvault.core.domain.model

import assertk.assertThat
import assertk.assertions.*
import kotlin.test.*

/**
 * Unit tests for PasswordHealth and PasswordScore.
 */
class PasswordHealthTest {

    @Test
    fun `unknown health has all flags false`() {
        val health = PasswordHealth.UNKNOWN

        assertThat(health.score).isEqualTo(PasswordScore.UNKNOWN)
        assertThat(health.isDuplicate).isFalse()
        assertThat(health.isWeak).isFalse()
        assertThat(health.isOld).isFalse()
        assertThat(health.ageDays).isNull()
    }

    @Test
    fun `password score enum has correct order`() {
        val scores = PasswordScore.entries

        assertThat(scores[0]).isEqualTo(PasswordScore.UNKNOWN)
        assertThat(scores[1]).isEqualTo(PasswordScore.VERY_WEAK)
        assertThat(scores[2]).isEqualTo(PasswordScore.WEAK)
        assertThat(scores[3]).isEqualTo(PasswordScore.FAIR)
        assertThat(scores[4]).isEqualTo(PasswordScore.GOOD)
        assertThat(scores[5]).isEqualTo(PasswordScore.STRONG)
        assertThat(scores[6]).isEqualTo(PasswordScore.VERY_STRONG)
    }

    @Test
    fun `password scores are comparable`() {
        assertThat(PasswordScore.VERY_STRONG > PasswordScore.STRONG).isTrue()
        assertThat(PasswordScore.STRONG > PasswordScore.GOOD).isTrue()
        assertThat(PasswordScore.GOOD > PasswordScore.FAIR).isTrue()
        assertThat(PasswordScore.FAIR > PasswordScore.WEAK).isTrue()
        assertThat(PasswordScore.WEAK > PasswordScore.VERY_WEAK).isTrue()
    }

    @Test
    fun `health can indicate weak password`() {
        val health = PasswordHealth(
            score = PasswordScore.WEAK,
            isDuplicate = false,
            isWeak = true,
            isOld = false,
            ageDays = null
        )

        assertThat(health.isWeak).isTrue()
    }

    @Test
    fun `health can indicate duplicate password`() {
        val health = PasswordHealth(
            score = PasswordScore.GOOD,
            isDuplicate = true,
            isWeak = false,
            isOld = false,
            ageDays = null
        )

        assertThat(health.isDuplicate).isTrue()
    }

    @Test
    fun `health can indicate old password`() {
        val health = PasswordHealth(
            score = PasswordScore.GOOD,
            isDuplicate = false,
            isWeak = false,
            isOld = true,
            ageDays = 365
        )

        assertThat(health.isOld).isTrue()
        assertThat(health.ageDays).isEqualTo(365)
    }

    @Test
    fun `health supports multiple issues`() {
        val health = PasswordHealth(
            score = PasswordScore.VERY_WEAK,
            isDuplicate = true,
            isWeak = true,
            isOld = true,
            ageDays = 400
        )

        assertThat(health.isWeak).isTrue()
        assertThat(health.isDuplicate).isTrue()
        assertThat(health.isOld).isTrue()
    }

    @Test
    fun `health is data class with equals`() {
        val health1 = PasswordHealth(
            score = PasswordScore.STRONG,
            isDuplicate = false,
            isWeak = false,
            isOld = false,
            ageDays = null
        )
        val health2 = PasswordHealth(
            score = PasswordScore.STRONG,
            isDuplicate = false,
            isWeak = false,
            isOld = false,
            ageDays = null
        )
        val health3 = PasswordHealth(
            score = PasswordScore.WEAK,
            isDuplicate = false,
            isWeak = false,
            isOld = false,
            ageDays = null
        )

        assertThat(health1).isEqualTo(health2)
        assertThat(health1).isNotEqualTo(health3)
    }

    @Test
    fun `health copy preserves values`() {
        val original = PasswordHealth(
            score = PasswordScore.GOOD,
            isDuplicate = false,
            isWeak = false,
            isOld = false,
            ageDays = null
        )

        val copy = original.copy(isOld = true, ageDays = 180)

        assertThat(copy.score).isEqualTo(PasswordScore.GOOD)
        assertThat(copy.isOld).isTrue()
        assertThat(copy.ageDays).isEqualTo(180)
    }

    @Test
    fun `password score names are descriptive`() {
        assertThat(PasswordScore.UNKNOWN.name).isEqualTo("UNKNOWN")
        assertThat(PasswordScore.VERY_WEAK.name).isEqualTo("VERY_WEAK")
        assertThat(PasswordScore.WEAK.name).isEqualTo("WEAK")
        assertThat(PasswordScore.FAIR.name).isEqualTo("FAIR")
        assertThat(PasswordScore.GOOD.name).isEqualTo("GOOD")
        assertThat(PasswordScore.STRONG.name).isEqualTo("STRONG")
        assertThat(PasswordScore.VERY_STRONG.name).isEqualTo("VERY_STRONG")
    }

    @Test
    fun `health can have null ageDays when not old`() {
        val health = PasswordHealth(
            score = PasswordScore.STRONG,
            isDuplicate = false,
            isWeak = false,
            isOld = false,
            ageDays = null
        )

        assertThat(health.isOld).isFalse()
        assertThat(health.ageDays).isNull()
    }

    @Test
    fun `health has total issue count`() {
        val health = PasswordHealth(
            score = PasswordScore.WEAK,
            isDuplicate = true,
            isWeak = true,
            isOld = true,
            ageDays = 365
        )

        val issueCount = listOf(
            health.isWeak,
            health.isDuplicate,
            health.isOld
        ).count { it }

        assertThat(issueCount).isEqualTo(3)
    }
}

/**
 * Password strength calculation tests.
 */
class PasswordStrengthCalculatorTest {

    @Test
    fun `very short password is very weak`() {
        val result = PasswordStrengthEvaluator.score("abc")

        assertThat(result).isEqualTo(PasswordScore.VERY_WEAK)
    }

    @Test
    fun `common password is weak`() {
        val result = PasswordStrengthEvaluator.score("password123")

        assertThat(result).isLessThanOrEqualTo(PasswordScore.WEAK)
    }

    @Test
    fun `long lowercase phrase is at least fair`() {
        val result = PasswordStrengthEvaluator.score("correcthorsebatterystaple")

        assertThat(result).isGreaterThanOrEqualTo(PasswordScore.FAIR)
    }

    @Test
    fun `password with mixed case is better`() {
        val mixed = PasswordStrengthEvaluator.score("UniqueMixedCasePhrase")
        val lowercase = PasswordStrengthEvaluator.score("uniquemixedcasephrase")

        assertThat(mixed).isGreaterThan(lowercase)
    }

    @Test
    fun `numbers improve an otherwise unique password`() {
        val withNumbers = PasswordStrengthEvaluator.score("UniquePhrase4826")
        val lettersOnly = PasswordStrengthEvaluator.score("UniquePhraseOnly")

        assertThat(withNumbers).isGreaterThanOrEqualTo(lettersOnly)
    }

    @Test
    fun `common leetspeak password is not reported as strong`() {
        val result = PasswordStrengthEvaluator.score("P@ssw0rd!")

        assertThat(result).isLessThanOrEqualTo(PasswordScore.WEAK)
    }

    @Test
    fun `long password with all types is very strong`() {
        val result = PasswordStrengthEvaluator.score("Violet!Harbor7Cactus$92")

        assertThat(result).isEqualTo(PasswordScore.VERY_STRONG)
    }

    @Test
    fun `empty password is very weak`() {
        val result = PasswordStrengthEvaluator.score("")

        assertThat(result).isEqualTo(PasswordScore.VERY_WEAK)
    }

    @Test
    fun `repeated characters are weak`() {
        val result = PasswordStrengthEvaluator.score("aaaaaaaaaa")

        assertThat(result).isEqualTo(PasswordScore.VERY_WEAK)
    }

    @Test
    fun `sequential characters are weak`() {
        val result = PasswordStrengthEvaluator.score("abcdefghij")

        assertThat(result).isLessThan(PasswordScore.GOOD)
    }
}
