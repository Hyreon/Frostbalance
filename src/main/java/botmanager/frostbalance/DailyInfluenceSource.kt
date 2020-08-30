package botmanager.frostbalance

import java.time.LocalDate

class DailyInfluenceSource {

    fun yield(influenceRequested: Influence, forDay: Long? = null ): Influence {

        if (!isActive) { setDate(forDay) }

        return if (forDay?.let { it > dailyDate } == true) {
            throw IllegalStateException("Asked to yield influence for a day after today!")
        } else if (influenceLeft >= influenceRequested) { //cap doesn't affect anything
            influenceLeft = influenceLeft.subtract(influenceRequested)
            influenceRequested
        } else { //influence gained is over the cap
            val returnedInfluence = influenceLeft
            influenceLeft = Influence(0)
            returnedInfluence
        }
    }

    /**
     * If no parameter is set, the date is set to today.
     * This cannot set the date to be older than it currently is.
     */
    private fun setDate(date: Long? = null) {
        influenceLeft = DAILY_INFLUENCE_CAP
        dailyDate = date ?: LocalDate.now().toEpochDay() //don't change if the new date is older.
    }

    var influenceLeft: Influence
    var dailyDate: Long

    constructor() {
        influenceLeft = DAILY_INFLUENCE_CAP
        dailyDate = LocalDate.now().toEpochDay()
    }

    constructor(influenceTaken: Influence?, dailyDate: Long) {
        influenceLeft = DAILY_INFLUENCE_CAP.subtract(influenceTaken)
        this.dailyDate = dailyDate
    }

    val isActive: Boolean
        get() = LocalDate.now().toEpochDay() == dailyDate



    companion object {
        @JvmField
        val DAILY_INFLUENCE_CAP = Influence(1.00)
    }
}