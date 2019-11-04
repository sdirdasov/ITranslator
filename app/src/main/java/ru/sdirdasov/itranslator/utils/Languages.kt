package ru.sdirdasov.itranslator.utils

object Languages {

    val RU = arrayOf(
        "Английский",
        "Русский",
        "Японский"
    )

    private val CODE_RU = arrayOf(
        "en",
        "ru",
        "ja"
    )

    val EN = arrayOf(
        "English",
        "Japanese",
        "Russian"
    )

    private val CODE_EN = arrayOf(
        "en",
        "ja",
        "ru"
    )

    fun getCodeRU(i: Int): String {
        return CODE_RU[i]
    }

    fun getCodeEN(i: Int): String {
        return CODE_EN[i]
    }
}
