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

        } ?: run {
            val ya = yieldedAmount
            yieldedAmount = Influence.none()
            return ya
        }

    }

    //TODO push immediately on border change for all users
    fun pushWeeklyInfluence(member: MemberWrapper, daily: DailyInfluenceSource, changeYield: Boolean? = null) {

        val cy = changeYield ?: yieldedAmount == Influence.none()

        nextRequestAmount(push = true).takeIf { it > 0 }?.let {

            member.gainDailyInfluence(Influence.none(), nextRequestDate - 1)
            if (cy) yieldedAmount += it
            pushWeeklyInfluence(member, daily, cy)

        }

    }

    private fun nextRequestAmount(push: Boolean = false): Influence {

        var request = Influence.none()

        if (!finished &&
                (nextRequestDate < LocalDate.now().toEpochDay() ||
                        (nextRequestDate == LocalDate.now().toEpochDay()
                                && !push))) {

            request += influenceToRequest
            if (!push) influenceToRequest = influenceToRequest.subtract(FALLOFF)
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
