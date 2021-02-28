package xyz.ckkpd.ewsparserapp

class EWSParser(val marks: List<EWSMark>) {
    /**
     * 与えられた緊急警報放送と思われる符号の列をどこまで見たか保持します。
     */
    var p: Int = 0

    /**
     * 緊急警報放送をパースした結果を保持します。
     */
    private var parseResult: EWSParseResult = EWSParseResult()

    /**
     * 与えられた緊急警報放送と思われる符号の列をパースします。
     */
    fun parse(): EWSParseResult {
        for (part in EWSSignalPartBasicList) {
            parseEWSSignalPart(part)
        }

        return parseResult
    }

    /**
     * 与えられた緊急警報放送の符号の一部分をパースします。
     */
    private fun parseEWSSignalPart(part: EWSSignalPart) {
        /*
         * パースする部分が信号の種類を判別するためのものであった場合、それを元に信号の種類を特定しようとします。
         */
        if(EWSSignalPartVerificationPartList.contains(part))
            parseResult.signal = parseResult.signal?.and(parseEWSSignalCheckPart(part))
        else {
            // パースする列
            val markPart = marks.slice(p until p + EWSSignalPartLength[part]!!).toInt()

            when(part) {
                EWSSignalPart.REGION_BODY -> parseResult.region = EWSRegionSign.valueOf(markPart)
                EWSSignalPart.DAY -> parseResult.day = EWSDaySign[markPart]!!
                EWSSignalPart.MONTH_DAY_TODAY -> parseResult.monthDayToday = markPart == 1
                EWSSignalPart.MONTH -> parseResult.month = EWSMonthSign[markPart]!!
                EWSSignalPart.YEAR_HOUR_TODAY -> parseResult.yearHourToday = markPart == 1
                EWSSignalPart.YEAR -> parseResult.year = showaToChristian(EWSYearSign[markPart]!!)
                EWSSignalPart.HOUR -> parseResult.hour = EWSHourSign[markPart]!!
                else -> error("not implemented")
            }
            p += EWSSignalPartLength[part]!!
        }
    }

    /**
     * 与えられた緊急警報放送の符号の種類の判別部分をパースします。
     */
    private fun parseEWSSignalCheckPart(part: EWSSignalPart): EWSSignal {
        val markPart = marks.slice(p until p + EWSSignalPartLength[part]!!).toInt()
        p += EWSSignalPartLength[part]!!

        val list = when(part) {
            EWSSignalPart.PREFIX -> EWSSignalPrefix
            EWSSignalPart.FIXED -> EWSSignalFixed
            EWSSignalPart.REGION_PREFIX -> EWSSignalRegionSignPrefix
            EWSSignalPart.REGION_SUFFIX -> EWSSignalRegionSignSuffix
            EWSSignalPart.MONTH_DAY_PREFIX -> EWSSignalMonthDaySignPrefix
            EWSSignalPart.MONTH_DAY_SUFFIX -> EWSSignalMonthDaySignSuffix
            EWSSignalPart.YEAR_HOUR_PREFIX -> EWSSignalYearHourSignPrefix
            EWSSignalPart.YEAR_HOUR_SUFFIX -> EWSSignalYearHourSignSuffix
            else -> null
        }

        if(list.isNullOrEmpty()) return EWSSignal.UNKNOWN
        return list.getOrDefault(markPart, EWSSignal.UNKNOWN)!!
    }
}