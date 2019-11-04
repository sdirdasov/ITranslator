package ru.sdirdasov.itranslator

import ru.sdirdasov.itranslator.utils.Languages
import java.util.*

class Dictionary internal constructor(
        var word: String?,
        var translation: String?,
        sourcePosition: Int,
        targetPosition: Int
    )
{
    var sourcePosition = -1
    var targetPosition = -1
    var sourceLanguage: String? = null
        private set
    var targetLanguage: String? = null
        private set

    val isEmpty: Boolean
        get() = word == null && targetPosition == -1 && sourcePosition == -1

    init
    {
        this.sourcePosition = sourcePosition
        this.targetPosition = targetPosition
        if (Locale.getDefault().language == "en") {
            sourceLanguage = getCodeENtoUpperCase(sourcePosition)
            targetLanguage = getCodeENtoUpperCase(targetPosition)
        } else {
            sourceLanguage = getCodeRUtoUpperCase(sourcePosition)
            targetLanguage = getCodeRUtoUpperCase(targetPosition)
        }
    }

    private fun getCodeENtoUpperCase(position: Int):String
    {
        return Languages.getCodeEN(position).toUpperCase(Locale.getDefault())
    }

    private fun getCodeRUtoUpperCase(position: Int):String
    {
        return Languages.getCodeRU(position).toUpperCase(Locale.getDefault())
    }
}
