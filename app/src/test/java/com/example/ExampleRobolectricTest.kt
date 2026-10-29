package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ClaudeModels
import com.example.util.ClaudeParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Claude", appName)
    }

    @Test
    fun `test Claude models availability and presets`() {
        val models = ClaudeModels.ALL_MODELS
        assertEquals(6, models.size)
        val opus55 = ClaudeModels.getById("claude-opus-5-5")
        assertEquals("Claude Opus 5.5", opus55.name)
        assertTrue(opus55.supportsThinking)

        val fable51 = ClaudeModels.getById("fable-5-1-creative")
        assertEquals("Fable 5.1 AI", fable51.name)
        assertEquals("Creative Genius", fable51.tag)

        val thinkingModel = ClaudeModels.getById("claude-3-7-sonnet-thinking")
        assertTrue(thinkingModel.supportsThinking)
    }

    @Test
    fun `test ClaudeParser extracts thinking and code artifacts`() {
        val rawResponse = """
            <thinking>
            1. Step one analysis.
            2. Step two optimization.
            </thinking>
            Here is the requested solution:
            ```html
            <!DOCTYPE html>
            <html>
            <body><h1>Live Game</h1></body>
            </html>
            ```
            Hope this helps!
        """.trimIndent()

        val parsed = ClaudeParser.parse(rawResponse)
        assertNotNull(parsed.thinking)
        assertTrue(parsed.thinking!!.contains("Step one analysis"))
        assertEquals(1, parsed.artifacts.size)
        assertEquals("html", parsed.artifacts[0].type)
        assertTrue(parsed.artifacts[0].content.contains("Live Game"))
        assertTrue(parsed.cleanContent.contains("Here is the requested solution"))
    }

    @Test
    fun `test launch MainActivity`() {
        val activityController = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
        activityController.setup()
        assertNotNull(activityController.get())
    }
}
