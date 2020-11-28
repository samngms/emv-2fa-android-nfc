package emv.mfa

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import emv.mfa.moshi.ExpiryDateAdapter
import emv.mfa.moshi.HexStringAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val text = appContext.resources.openRawResource(R.raw.rootca).bufferedReader().readText()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(HexStringAdapter())
            .add(ExpiryDateAdapter())
            .build()
        val type = Types.newParameterizedType(
            MutableList::class.java,
            RootCert::class.java
        )
        val jsonAdapter = moshi.adapter<List<RootCert>>(type)
        val list = jsonAdapter.fromJson(text)

        assertEquals("com.example.myapplication2", appContext.packageName)
    }
}