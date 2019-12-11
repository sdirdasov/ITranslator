package ru.sdirdasov.itranslator

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.jakewharton.rxbinding.widget.RxTextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.sdirdasov.itranslator.network.PostAPI
import ru.sdirdasov.itranslator.post.PostTranslated
import ru.sdirdasov.itranslator.utils.*
import java.util.*
import java.util.concurrent.TimeUnit

class BaseView : Fragment()
{
    lateinit var baseView: View
    lateinit var langaugeIn: Spinner
    lateinit var languageOut: Spinner
    lateinit var textToTranslate: EditText

    lateinit var translatedText: TextView
    private var noTranslate: Boolean = false

     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
     {
        baseView = inflater.inflate(R.layout.baseview_fragment, container, false)

        langaugeIn = baseView.findViewById<View>(R.id.languageIn) as Spinner
        languageOut = baseView.findViewById<View>(R.id.languageOut) as Spinner

        textToTranslate = baseView.findViewById<View>(R.id.textTranslate) as EditText
        textToTranslate.movementMethod = ScrollingMovementMethod()
        textToTranslate.isVerticalScrollBarEnabled = true

        translatedText = baseView.findViewById<View>(R.id.translatedText) as TextView
        translatedText.movementMethod = ScrollingMovementMethod()
        translatedText.isVerticalScrollBarEnabled = true

        setSharedPreferences()

        return baseView
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?)
     {
         val dataAdapter = ArrayAdapter(
             context!!,
             android.R.layout.simple_spinner_item, Languages.RU
         )

         dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

         langaugeIn.adapter = dataAdapter
         languageOut.adapter = dataAdapter
         languageOut.setSelection(1)

         RxTextView.textChanges(textToTranslate)
             .filter { charSequence -> charSequence.isNotEmpty() }
             .debounce(1000, TimeUnit.MILLISECONDS).subscribe { charSequence -> translate(charSequence.toString().trim { it <= ' ' }) }

         RxTextView.textChanges(textToTranslate)
             .filter { charSequence -> charSequence.isEmpty() }
             .subscribe { activity?.runOnUiThread {} }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView()
    {
        val sharedPref = activity?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()

        if (editor != null) {
            editor.putInt(PREF_FIRST_SELECTION, langaugeIn.selectedItemPosition)
            editor.putInt(PREF_SECOND_SELECTION, languageOut.selectedItemPosition)
            editor.putString(PREF_TEXT_TO_TRANSLATED, textToTranslate.text.toString())
            editor.putString(PREF_TRANSLATED_TEXT, translatedText.text.toString())
            editor.apply()
        }
        super.onDestroyView()
    }

    fun addToDictionary()
    {
        val text = textToTranslate.text.toString().trim { it <= ' ' }
        if (text.isNotEmpty()) {
            val dataBaseHelper = AppDatabase(baseView.context, DATABASE_DICTIONARY)
            dataBaseHelper.insertWord(
                Dictionary(
                    textToTranslate.text.toString().trim { it <= ' ' },
                    translatedText.text.toString(), langaugeIn.selectedItemPosition,
                    languageOut.selectedItemPosition
                )
            )
            dataBaseHelper.close()
        }
    }

    private fun setSharedPreferences()
    {
        val sharedPref = activity?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val text = sharedPref?.getString(PREF_TEXT_TO_TRANSLATED, "")?: ""
        val translation = sharedPref?.getString(PREF_TRANSLATED_TEXT, "")?: ""
        val selection1 = sharedPref?.getInt(PREF_FIRST_SELECTION, 0)?: 0
        val selection2 = sharedPref?.getInt(PREF_SECOND_SELECTION, 1)?: 1

        if (text.isNotEmpty()) {
            noTranslate = true
            textToTranslate.setText(text)
            langaugeIn.setSelection(selection1)
            languageOut.setSelection(selection2)
            translatedText.text = translation
        }
    }

    private fun translate(text: String)
    {
        if (noTranslate) {
            noTranslate = false
            return
        }

        val tokenAPI = TOKEN_API_YANDEX
        val firstLanguage = langaugeIn.selectedItem.toString()
        val secondLanguage = languageOut.selectedItem.toString()

        val query = Retrofit.Builder().baseUrl(BASE_URL_YANDEX)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val postAPI = query.create(PostAPI::class.java)
        val call = postAPI.getPosts(
            tokenAPI, text,
            "${getLanguageCode(firstLanguage)}-${getLanguageCode(secondLanguage)}"
        )

        call.enqueue(object : Callback<PostTranslated> {
            override fun onResponse(call: Call<PostTranslated>, response: Response<PostTranslated>) {
                if (response.isSuccessful) {
                    activity?.runOnUiThread {
                        translatedText.text = response.body()?.text!![0]
                        addToDictionary()
                    }
                }
            }

            override fun onFailure(call: Call<PostTranslated>, t: Throwable) {}
        })
    }

    private fun getLanguageCode(selectedLang: String): String?
    {
        var code: String? = null

        if (Locale.getDefault().language == "en") {
            for (i in Languages.EN.indices) {
                if (selectedLang == Languages.EN[i]) {
                    code = Languages.getCodeEN(i)
                }
            }
        } else {
            for (i in Languages.RU.indices) {
                if (selectedLang == Languages.RU[i]) {
                    code = Languages.getCodeRU(i)
                }
            }
        }
        return code
    }
}
