package xyz.ckkpd.ewsparserapp

class EWSParseResult {
    var year: Int = 0
    var month: Int = 0
    var day: Int = 0
    var hour: Int = 0
    var monthDayToday: Boolean? = null
    var yearHourToday: Boolean? = null
    var region: EWSRegionSign? = null
    var signal: EWSSignal? = EWSSignal.UNKNOWN

    override fun toString(): String {
        return "$signal $region XXX$year/$month/$day $hour:XX"
    }
}