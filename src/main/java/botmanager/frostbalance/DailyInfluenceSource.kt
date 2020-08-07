package botmanager.frostbalance

import java.time.LocalDate

class DailyInfluenceSource {

    fun yield(influenceRequested: Influence): Influence {

        if (!isActive) { reset() }

        return if (influenceLeft >= influenceRequested) { //cap doesn't affect anything
            influenceLeft = influenceLeft.subtract(influenceRequested)
            influenceRequested
        } else { //influence gained is over the cap
            val returnedInfluence = influenceLeft
            influenceLeft = Influence(0)
            returnedInfluence
        }
    }

    private fun reset() {
        influenceLeft = DAILY_INFLUENCE_CAP
        dailyDate = LocalDate.now().toEpochDay()
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