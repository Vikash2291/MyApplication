package com.example.myapplication.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `valid email passes`() {
        assertThat(Validators.isValidEmail("user@example.com")).isTrue()
    }

    @Test
    fun `invalid email fails`() {
        assertThat(Validators.isValidEmail("user@")).isFalse()
        assertThat(Validators.isValidEmail("")).isFalse()
    }

    @Test
    fun `login validation reports missing password`() {
        assertThat(Validators.validateLogin("user@example.com", "")).isEqualTo("Password is required")
    }

    @Test
    fun `registration validation catches mismatched passwords`() {
        assertThat(
            Validators.validateRegistration("Alex", "user@example.com", "secret1", "secret2")
        ).isEqualTo("Passwords do not match")
    }

    @Test
    fun `valid registration returns null`() {
        assertThat(
            Validators.validateRegistration("Alex", "user@example.com", "secret1", "secret1")
        ).isNull()
    }
}
