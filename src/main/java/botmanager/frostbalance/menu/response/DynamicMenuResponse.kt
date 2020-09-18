package botmanager.frostbalance.menu.response

abstract class DynamicMenuResponse(val emojiLambda: () -> String, val nameLambda: () -> String) : MenuResponse(emojiLambda(), nameLambda()) {

    fun updateValues() {
        emoji = emojiLambda()
        name = nameLambda()
    }

}
