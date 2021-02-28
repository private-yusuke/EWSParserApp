package xyz.ckkpd.ewsparserapp

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import kotlinx.coroutines.*
import org.w3c.dom.Text
import java.lang.Exception

const val SAMPLING_RATE = 48000

var bufSize: Int = 0

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val micPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if(micPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(grantResults.size <= 0) return
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launch { startDecoding() }
        }
        else {
            findViewById<TextView>(R.id.decoded_string).text = "マイク読み取りの権限が与えられていません。システムの設定から権限を与えてください。"
        }
    }

    override fun onResume() {
        super.onResume()
        bufSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
    }

    /**
     * 緊急警報放送のパース結果や読み込めたビット列の流さを画面上のテキスト部分に反映します。
     */
    fun updateText(parsedString: String, decodedLength: Int) {
        findViewById<TextView>(R.id.decoded_string).text = parsedString
        findViewById<TextView>(R.id.decoded_length).text = decodedLength.toString()
    }

    /**
     * 緊急警報放送のデコードを行い、結果が出たら UI に反映します。
     */
    private suspend fun startDecoding() = withContext(Dispatchers.IO) {
        val audioRec = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLING_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()
                ).build()


        // 1つのビット分の音の波形を保持する配列
        var markSound = ShortArray(N_BPS)

        // 現在保持されている緊急警報放送のビットの列
        var currentSignals = mutableListOf<EWSMark>()

        audioRec.startRecording()

        while (true) {
            // マイクから読み取った音の波形をバッファ
            audioRec.read(markSound, 0, markSound.size)
            var samples = markSound.map { it.toDouble() }.toDoubleArray()

            var goertzelZero = Goertzel(SAMPLING_RATE, samples.size, ZERO_FREQ.toDouble())
            var goertzelOne = Goertzel(SAMPLING_RATE, samples.size, ONE_FREQ.toDouble())

            goertzelZero.samples = samples
            goertzelOne.samples = samples

            val optimized = false
            val magnitudeZero = goertzelZero.magnitude(optimized)
            val magnitudeOne = goertzelOne.magnitude(optimized)

            val mark: EWSMark

            // 1 か 0 のどちらかを読み取った場合、より可能性が高い方を得る
            if ((goertzelZero.hasFreq(optimized) || goertzelOne.hasFreq(optimized))) {
                if(magnitudeZero >= magnitudeOne)
                    mark = EWSMark.ZERO
                else mark = EWSMark.ONE
            } else mark = EWSMark.UNKNOWN

            // 今回読み取ったマークを履歴に追加
            if (mark != EWSMark.UNKNOWN)
                currentSignals.add(mark)

            // 緊急警報放送は信号の種類に関わらず先頭 100 ビットを正常に読み取れればそれで内容は確定するので、100 ビット読んだら結果をディスプレイ開始
            if(mark == EWSMark.UNKNOWN || currentSignals.size >= 100) {
                if(currentSignals.isNotEmpty()) {
                    Log.d("EWSCurrentSignal", currentSignals.joinToString(""))
                    Log.d("EWSCurrentSignal", "length: ${currentSignals.size.toString()}")
                    val parser = EWSParser(currentSignals)
                    var res: String
                    try {
                        res = parser.parse().toString()
                    } catch (e: Exception) {
                        res = "try again"
                    }
                    runOnUiThread {
                        updateText(res, currentSignals.size)
                    }
                }
                if(mark == EWSMark.UNKNOWN)
                    currentSignals.clear()
            } else {
                runOnUiThread {
                    updateText("listening...", currentSignals.size)
                }
            }
        }
    }
}