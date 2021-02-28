package xyz.ckkpd.ewsparserapp

import kotlin.math.*

/**
 * Goertzel アルゴリズムの実装
 * 参考: [The Goertzel Algorithm - Embedded.com](https://www.embedded.com/the-goertzel-algorithm/)
 */
class Goertzel (_sample_rate: Int, _sample_size: Int, _target_freq: Double) {
    /**
     * 入力するサンプルのサンプリング周波数
     */
    private val sample_rate: Int

    /**
     * 入力するサンプルの大きさ（一定）
     */
    private val sample_size: Int

    /**
     * 検知する対象の周波数（Hz）
     */
    private val target_freq: Double

    private val k: Int
    private val w: Double
    private val sine: Double
    private val cosine: Double
    private val coeff: Double

    private var Q = DoubleArray(3)

    /**
     * 処理対象のサンプル
     */
    var samples: DoubleArray = DoubleArray(0)
        set(value) {
            field = value
            Q[0] = 0.0
            Q[1] = 0.0
            Q[2] = 0.0
        }

    /**
     * 処理対象に検知対象の周波数が入っていたと判定するための閾値
     */
    var threshold = 1e5

    init {
        sample_rate = _sample_rate
        sample_size = _sample_size
        target_freq = _target_freq

        k = (0.5 + (sample_size * target_freq) / sample_rate).toInt()
        w = (2 * PI / sample_size) * k
        sine = sin(w)
        cosine = cos(w)
        coeff = 2 * cosine
    }

    /**
     * サンプルに含まれると予測される周波数の強さを返します。
     */
    fun magnitude(optimized: Boolean = true): Double {
        Q[0] = 0.0
        Q[1] = 0.0
        Q[2] = 0.0

        for(sample in samples) {
            Q[0] = coeff * Q[1] - Q[2] + sample
            Q[2] = Q[1]
            Q[1] = Q[0]
        }

        /**
         * [optimized] が true の場合、最適化された Goertzel アルゴリズムによるチェックを行う（精度は微小ながら低下）
         */
        if(optimized) {
            val magnitude2 = Q[1].pow(2) + Q[2].pow(2) - Q[1] * Q[2] * coeff
            return sqrt(magnitude2)
        }

        val real = Q[1] - Q[2] * cosine
        val imag = Q[2] * sine
        val magnitude2 = real.pow(2) + imag.pow(2)

        return sqrt(magnitude2)
    }

    /**
     * サンプルに対象の周波数が含まれていたら true、そうでなければ false を返します。
     * [optimized] が真の場合、最適化された Goertzel アルゴリズムによるチェックを行います（より高速）。
     */
    fun hasFreq(optimized: Boolean = true) : Boolean {
        return magnitude(optimized) >= threshold
    }
}