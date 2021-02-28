package xyz.ckkpd.ewsparserapp

/**
 * FFT で緊急警報放送をデコードしようとした残骸です。Goertzel アルゴリズムの方がここでは精度が良かったため、利用されていません。
 */
@Deprecated("精度が低い", ReplaceWith("Goertzel"))
class FSKDecoder (
    val sampling_rate: Int,
    val threshold: Double = 100.0
) {
    fun decode(samples: DoubleArray) : EWSMark {
        val fft = FFT(sampling_rate, samples.size)
        fft.samples = samples
        val maxFreq = fft.getMaxFreq()

        if (maxFreq in ZERO_FREQ -threshold..ZERO_FREQ +threshold)
            return EWSMark.ZERO
        if (maxFreq in ONE_FREQ -threshold..ONE_FREQ +threshold)
            return EWSMark.ONE
        else return EWSMark.UNKNOWN
    }
}