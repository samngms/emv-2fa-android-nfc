package emv.mfa

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import emv.mfa.moshi.ExpiryDateAdapter
import emv.mfa.moshi.HexStringAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


class MainActivity : AppCompatActivity() {
    var nfcAdapter : NfcAdapter? = null;
    var pendingIntent : PendingIntent? = null;

    val INTENT_FILTER = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))

    val TECH_LIST = arrayOf(arrayOf(IsoDep::class.java.name))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        setupCaList()

        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, this.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, INTENT_FILTER, TECH_LIST);
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        var tag: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if ( null != tag ) {
            var handle = IsoDep.get(tag);
            handle.connect();
            val reader = EmvReader(handle)
            try {
                reader.process()
            } catch (e: Exception) {
                Log.e("Main", "Error", e)
            }
            findViewById<TextView>(R.id.my_text).text = "Sam Ng";
        }
    }

    fun setupCaList() {
        val text = this.resources.openRawResource(R.raw.rootca).bufferedReader().readText()
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
        RootCA.list = jsonAdapter.fromJson(text)
    }
}