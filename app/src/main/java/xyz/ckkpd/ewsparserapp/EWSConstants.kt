package xyz.ckkpd.ewsparserapp

/**
 * 0 に対応する周波数
 */
const val ZERO_FREQ = 640
/**
 * 1 に対応する周波数
 */
const val ONE_FREQ = 1024

/**
 * 緊急警報信号で一秒間に送信されるマーク列の流さ
 */
const val BPS = 64
const val N_BPS = SAMPLING_RATE / BPS

/**
 * 緊急警報信号で送信されるマーク
 */
enum class EWSMark(val symbol: Char) {
    ZERO('0'),
    ONE('1'),
    UNKNOWN('?');

    override fun toString(): String {
        return this.symbol.toString()
    }
}

/**
 * 緊急警報信号の種類
 */
enum class EWSSignal(val bits: Int) {
    /**
     * 第一種開始信号
     */
    FIRST(0b001),
    /**
     * 第二種開始信号
     */
    SECOND(0b010),
    /**
     * 終了信号
     */
    END(0b100),
    /**
     * 第一種開始信号及び第二種開始信号
     */
    FIRST_OR_SECOND(0b011),
    /**
     * 第一種開始信号及び終了信号
     */
    FIRST_OR_END(0b101),
    /**
     * 不明
     */
    UNKNOWN(0b111);

    override fun toString(): String {
        return when(this) {
            FIRST -> "第一種開始信号"
            SECOND -> "第二種開始信号"
            END -> "終了信号"
            FIRST_OR_SECOND -> "第一種開始信号及び第二種開始信号"
            FIRST_OR_END -> "第一種開始信号及び終了信号"
            UNKNOWN -> "不明"
        }
    }
    companion object {
        fun valueOf(bits: Int) = values().firstOrNull { it.bits == bits }
    }

    infix fun or(rhs: EWSSignal): EWSSignal? = valueOf(this.bits or rhs.bits)
    infix fun and(rhs: EWSSignal): EWSSignal? = valueOf(this.bits and rhs.bits)
}

/**
 * 緊急警報信号のマークの列から整数に変換
 */
fun Collection<EWSMark>.toInt(): Int {
    var res = 0

    for (i in this) {
        res = res shl 1
        if(i == EWSMark.ZERO) res += 0
        if(i == EWSMark.ONE) res += 1
    }

    return res
}

/**
 * バイト列から緊急警報信号のマークの列に変換
 */
fun Collection<Byte>.toEWSMarks(): List<EWSMark> {
    var res = mutableListOf<EWSMark>()

    for (i in this) {
        res.add(when(i) {
            0.toByte() -> EWSMark.ZERO
            1.toByte() -> EWSMark.ONE
            else -> error("not implemented")
        })
    }

    return res.toList()
}

/**
 * 地域区分符号
 * @param code 対応する符号
 * @param region 対応する地域の名前
 */
enum class EWSRegionSign(val code: Int, val region: String) {
    COMMON(0b001101001101, "地域共通符号"),
    KANTO(0b010110100101, "関東広域圏"),
    CHUKYO(0b011100101010, "中京広域圏"),
    KINKI(0b100011010101, "近畿広域圏"),
    TOTTORI_SHIMANE(0b011010011001, "鳥取・島根圏"),
    OKAYAMA_KAGAWA(0b010101010011, "岡山・香川圏");

    companion object {
        fun valueOf(code: Int) = EWSRegionSign.values().firstOrNull { it.code ==  code}
    }

    override fun toString(): String {
        return this.region
    }
}

/**
 * 緊急警報放送のそれぞれの符号の種類
 */
enum class EWSSignalPart {
    PREFIX,
    FIXED,
    REGION_PREFIX,
    REGION_BODY,
    REGION_SUFFIX,
    MONTH_DAY_PREFIX,
    DAY,
    MONTH_DAY_TODAY,
    MONTH,
    MONTH_DAY_SUFFIX,
    YEAR_HOUR_PREFIX,
    HOUR,
    YEAR_HOUR_TODAY,
    YEAR,
    YEAR_HOUR_SUFFIX;
}

/**
 * 緊急警報信号を先頭から読み出したときに先頭からこの順で並んでいる符号の列
 */
val EWSSignalPartBasicList = listOf<EWSSignalPart>(
        EWSSignalPart.PREFIX,
        EWSSignalPart.FIXED,
        EWSSignalPart.REGION_PREFIX,
        EWSSignalPart.REGION_BODY,
        EWSSignalPart.REGION_SUFFIX,
        EWSSignalPart.FIXED,
        EWSSignalPart.MONTH_DAY_PREFIX,
        EWSSignalPart.DAY,
        EWSSignalPart.MONTH_DAY_TODAY,
        EWSSignalPart.MONTH,
        EWSSignalPart.MONTH_DAY_SUFFIX,
        EWSSignalPart.FIXED,
        EWSSignalPart.YEAR_HOUR_PREFIX,
        EWSSignalPart.HOUR,
        EWSSignalPart.YEAR_HOUR_TODAY,
        EWSSignalPart.YEAR,
        EWSSignalPart.YEAR_HOUR_SUFFIX
)

/**
 * 緊急警報信号の中でその種類の判別に利用することが可能な符号の列
 */
val EWSSignalPartVerificationPartList = listOf<EWSSignalPart>(
        EWSSignalPart.PREFIX,
        EWSSignalPart.FIXED,
        EWSSignalPart.REGION_PREFIX,
        EWSSignalPart.REGION_SUFFIX,
        EWSSignalPart.MONTH_DAY_PREFIX,
        EWSSignalPart.MONTH_DAY_SUFFIX,
        EWSSignalPart.YEAR_HOUR_PREFIX,
        EWSSignalPart.YEAR_HOUR_SUFFIX
)

/**
 * 緊急警報信号のそれぞれの符号のビット列の流さ
 */
val EWSSignalPartLength = mapOf<EWSSignalPart, Int>(
    EWSSignalPart.PREFIX to 4,
    EWSSignalPart.FIXED to 16,
    EWSSignalPart.REGION_PREFIX to 2,
    EWSSignalPart.REGION_BODY to 12,
    EWSSignalPart.REGION_SUFFIX to 2,
    EWSSignalPart.MONTH_DAY_PREFIX to 3,
    EWSSignalPart.DAY to 5,
    EWSSignalPart.MONTH_DAY_TODAY to 1,
    EWSSignalPart.MONTH to 5,
    EWSSignalPart.MONTH_DAY_SUFFIX to 2,
    EWSSignalPart.YEAR_HOUR_PREFIX to 3,
    EWSSignalPart.YEAR to 5,
    EWSSignalPart.YEAR_HOUR_TODAY to 1,
    EWSSignalPart.HOUR to 5,
    EWSSignalPart.YEAR_HOUR_SUFFIX to 2
 )

/**
 * 前置符号
 */
val EWSSignalPrefix = mapOf(
    0b1100 to (EWSSignal.FIRST or EWSSignal.SECOND),
    0b0011 to EWSSignal.END
)

/**
 * 固定符号
 */
val EWSSignalFixed = mapOf(
    0b0000111001101101 to (EWSSignal.FIRST or EWSSignal.END),
    0b1111000110010010 to EWSSignal.SECOND
)

/**
 * 地域区分符号の先頭
 */
val EWSSignalRegionSignPrefix = mapOf(
    0b10 to (EWSSignal.FIRST or EWSSignal.SECOND),
    0b01 to EWSSignal.END
)

/**
 * 地域区分符号の末尾
 */
val EWSSignalRegionSignSuffix = mapOf(
    0b00 to (EWSSignal.FIRST or EWSSignal.SECOND),
    0b11 to EWSSignal.END
)

/**
 * 月日区分符号の先頭
 */
val EWSSignalMonthDaySignPrefix = mapOf(
    0b010 to (EWSSignal.FIRST or EWSSignal.SECOND),
    0b100 to EWSSignal.END
)

/**
 * 月日区分符号の末尾
 */
val EWSSignalMonthDaySignSuffix = mapOf(
    0b00 to (EWSSignal.FIRST or EWSSignal.SECOND),
    0b11 to EWSSignal.END
)

/**
 * 年時区分符号の先頭
 */
val EWSSignalYearHourSignPrefix = mapOf(
    0b011 to (EWSSignal.FIRST or EWSSignal.SECOND),
    0b101 to EWSSignal.END
)

/**
 * 年時区分符号の末尾
 */
val EWSSignalYearHourSignSuffix = mapOf(
    0b00 to (EWSSignal.FIRST or EWSSignal.SECOND),
    0b11 to EWSSignal.END
)

/**
 * 年時区分符号に含まれる時符号
 */
val EWSHourSign = mapOf(
    0b00011 to 0,
    0b10011 to 1,
    0b01011 to 2,
    0b11011 to 3,
    0b00111 to 4,
    0b10111 to 5,
    0b01111 to 6,
    0b11111 to 7,
    0b00001 to 8,
    0b10001 to 9,
    0b01001 to 10,
    0b11001 to 11,
    0b00101 to 12,
    0b10101 to 13,
    0b01101 to 14,
    0b11101 to 15,
    0b00010 to 16,
    0b10010 to 17,
    0b01010 to 18,
    0b11010 to 19,
    0b00110 to 20,
    0b10110 to 21,
    0b01110 to 22,
    0b11110 to 23
)

/**
 * 月日区分符号に含まれる日符号
 */
val EWSDaySign = mapOf(
    0b10000 to 1,
    0b01000 to 2,
    0b11000 to 3,
    0b00100 to 4,
    0b10100 to 5,
    0b01100 to 6,
    0b11100 to 7,
    0b00010 to 8,
    0b10010 to 9,
    0b01010 to 10,
    0b11010 to 11,
    0b00110 to 12,
    0b10110 to 13,
    0b01110 to 14,
    0b11110 to 15,
    0b00001 to 16,
    0b10001 to 17,
    0b01001 to 18,
    0b11001 to 19,
    0b00101 to 20,
    0b10101 to 21,
    0b01101 to 22,
    0b11101 to 23,
    0b00011 to 24,
    0b10011 to 25,
    0b01011 to 26,
    0b11011 to 27,
    0b00111 to 28,
    0b10111 to 29,
    0b01111 to 30,
    0b11111 to 31
)

/**
 * 月日区分符号に含まれる月符号
 */
val EWSMonthSign = mapOf(
    0b10001 to 1,
    0b01001 to 2,
    0b11001 to 3,
    0b00101 to 4,
    0b10101 to 5,
    0b01101 to 6,
    0b11101 to 7,
    0b00011 to 8,
    0b10011 to 9,
    0b01011 to 10,
    0b11011 to 11,
    0b00111 to 12
)

/**
 * 年時区分符号に含まれる年符号
 */
val EWSYearSign = mapOf(
    0b10101 to 0,
    0b01101 to 1,
    0b11101 to 2,
    0b00011 to 3,
    0b10011 to 4,
    0b01011 to 5,
    0b10001 to 6,
    0b01001 to 7,
    0b11001 to 8,
    0b00101 to 9
)

/**
 * 与えられた昭和1年から数えた年の下1桁を西暦の下1桁に変換する。
 */
fun showaToChristian(n: Int) = (n + 5) % 10