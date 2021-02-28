package xyz.ckkpd.ewsparserapp

import kotlin.math.*

/**
 * 高速フーリエ変換を行うクラス
 */
@Deprecated("Goertzel アルゴリズムに比べて精度が低い", ReplaceWith("Goertzel"))
class FFT (_sampling_rate: Int, _sample_size: Int) {
    val sampling_rate: Int
    val sample_size: Int
    val FFT_size: Int
    val resolution: Double
    val dB_baseline: Double
    var samples: DoubleArray = DoubleArray(0)
        set(value) {
            field = value.copyOf(FFT_size)
        }

    /**
     * 与えられた整数以上であって最も小さい 2 のべき乗の値を返します。
     */
    private fun getSizeForFFT(n: Int) : Int = 2.0.pow(ceil(log2(n.toDouble()))).toInt()

    init {
        sampling_rate = _sampling_rate
        sample_size = _sample_size
        FFT_size = getSizeForFFT(sample_size)
        dB_baseline = 2.0.pow(15) * FFT_size * sqrt(2.0)
        resolution = SAMPLING_RATE / FFT_size.toDouble()
    }

    /**
     * 高速フーリエ変換を利用して、与えられたサンプルの中にある最も音量の大きい周波数を返します。
     */
    fun getMaxFreq() : Double {
        val fft4g = FFT4g(FFT_size)
        fft4g.rdft(1, samples)

        var dbfs =  DoubleArray(FFT_size/2)
        var max_db = -120.0
        var max_i: Int = 0
        for(i in 0 until FFT_size step 2) {
            dbfs[i / 2] =
                (20 * log10(sqrt(samples[i].pow(2) + samples[i + 1].pow(2)) / dB_baseline))
            if (max_db < dbfs[i / 2]) {
                max_db = dbfs[i / 2]
                max_i = i / 2
            }
        }
        return resolution * max_i
    }
}