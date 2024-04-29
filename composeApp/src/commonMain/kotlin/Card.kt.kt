data class Card(
    val cardElements: MutableList<CardElement> = mutableListOf()
) {
    fun toCsv(): String {
        var csv = ""

        return csv
    }

    companion object {
        fun fromCsv(csv: String): Card {
            val card = Card()

            return card
        }
    }
}