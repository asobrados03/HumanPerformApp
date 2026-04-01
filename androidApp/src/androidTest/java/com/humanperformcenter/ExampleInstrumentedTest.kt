package com.humanperformcenter

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Arrange
        // Context of the app under test.
        // Act
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Assert
        assertEquals("com.humanperformcenter", appContext.packageName)
    }
}
