package botmanager.frostbalance

import java.time.LocalDate

class WeeklyInfluenceSource {

    private var influenceToRequest: Influence = DailyInfluenceSource.DAILY_INFLUENCE_CAP
    internal var nextRequestDate: Long = LocalDate.now().toEpochDay()

    var yieldedAmount: Influence = Influence.none()

    /**
     * Recursively gets weekly influence until it's caught up to today.
     */
    fun getWeeklyInfluence(member: MemberWrapper, daily: DailyInfluenceSource): Influence {

        nextRequestAmount().takeIf { it > 0 }?.let {

            //there was a major bug that got fixed by swapping the order of these two functions.
            //functional and object-oriented programming go about as well as salt and sugar.
            return member.gainDailyInfluence(it, nextRequestDate - 1) + getWeeklyInfluence(member, daily)

        } ?: return Influence.none()

    }

    private fun nextRequestAmount(): Influence {

        var request = Influence.none()

        if (nextRequestDate <= LocalDate.now().toEpochDay() && !finished) {

            request += influenceToRequest
            influenceToRequest = influenceToRequest.subtract(FALLOFF)
            if (influenceToRequest > 0) nextRequestDate++
        }

        return request

    }

    val finished: Boolean
        get() = influenceToRequest <= 0.0


    companion object {
        @JvmField
        val FALLOFF = Influence(0.00)
    }

}
