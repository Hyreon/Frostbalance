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

            return getWeeklyInfluence(member, daily) + member.gainDailyInfluence(it, nextRequestDate - 1)

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
        val FALLOFF = Influence(0.15)
    }

}
