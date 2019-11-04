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
import ru.sdirdasov.itranslator.utils.DATABASE_DICTIONARY
import ru.sdirdasov.itranslator.utils.Languages
import java.util.*
import java.util.concurrent.TimeUnit

class BaseView : Fragment()
{
    private var viewBase: View? = null
    private var langaugeIn: Spinner? = null
    private var languageOut: Spinner? = null
    private var textToTranslate: EditText? = null

    private var translatedText: TextView? = null
    private var noTranslate: Boolean = false

     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
     {
        viewBase = inflater.inflate(R.layout.baseview_fragment, container, false)

        langaugeIn = viewBase!!.findViewById<View>(R.id.languageIn) as Spinner
        languageOut = viewBase!!.findViewById<View>(R.id.languageOut) as Spinner

        textToTranslate = viewBase!!.findViewById<View>(R.id.textTranslate) as EditText
        textToTranslate!!.movementMethod = ScrollingMovementMethod()
        textToTranslate!!.isVerticalScrollBarEnabled = true

        translatedText = viewBase!!.findViewById<View>(R.id.translatedText) as TextView
        translatedText!!.movementMethod = ScrollingMovementMethod()
        translatedText!!.isVerticalScrollBarEnabled = true

        setSharedPreferences()

        return viewBase
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?)
     {
         val dataAdapter = ArrayAdapter(
             context!!,
             android.R.layout.simple_spinner_item, Languages.RU
         )

         dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

         langaugeIn!!.adapter = dataAdapter
         languageOut!!.adapter = dataAdapter
         languageOut!!.setSelection(1)

         RxTextView.textChanges(textToTranslate!!)
             .filter { charSequence -> charSequence.isNotEmpty() }
             .debounce(1000, TimeUnit.MILLISECONDS).subscribe { charSequence -> translate(charSequence.toString().trim { it <= ' ' }) }

         RxTextView.textChanges(textToTranslate!!)
             .filter { charSequence -> charSequence.isEmpty() }
             .subscribe { activity!!.runOnUiThread {} }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView()
    {
        val sharedPref = activity!!.getSharedPreferences("default", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("selection1", langaugeIn!!.selectedItemPosition)
        editor.putInt("selection2", languageOut!!.selectedItemPosition)
        editor.putString("textToTranslate", textToTranslate!!.text.toString())
        editor.putString("translatedText", translatedText!!.text.toString())
        editor.apply()
        super.onDestroyView()
    }

    fun addToDictionary()
    {
        val text = textToTranslate!!.text.toString().trim { it <= ' ' }
        if (text != "") {
            val dataBaseHelper = AppDatabase(viewBase!!.context, DATABASE_DICTIONARY)
            dataBaseHelper.insertWord(
                Dictionary(
                    textToTranslate!!.text.toString().trim { it <= ' ' },
                    translatedText!!.text.toString(), langaugeIn!!.selectedItemPosition,
                    languageOut!!.selectedItemPosition
                )
            )
            dataBaseHelper.close()
        }
    }

    private fun setSharedPreferences()
    {
        val sharedPref = activity!!.getSharedPreferences("default", Context.MODE_PRIVATE)
        val text = sharedPref.getString("textToTranslate", "")
        val translation = sharedPref.getString("translatedText", "")
        val selection1 = sharedPref.getInt("selection1", 0)
        val selection2 = sharedPref.getInt("selection2", 1)

        if (text != "") {
            noTranslate = true
            textToTranslate!!.setText(text)
            langaugeIn!!.setSelection(selection1)
            languageOut!!.setSelection(selection2)
            translatedText!!.text = translation
        }
    }

    private fun translate(text: String)
    {
        if (noTranslate) {
            noTranslate = false
            return
        }

        val tokenAPI = "trnsl.1.1.20191101T120114Z.b016787b6c488297.28e0a49d6c835f41d3a8ced40f99c55ab3d33b37"
        val firstLanguage = langaugeIn!!.selectedItem.toString()
        val secondLanguage = languageOut!!.selectedItem.toString()

        val query = Retrofit.Builder().baseUrl("https://translate.yandex.net/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val postAPI = query.create(PostAPI::class.java)
        val call = postAPI.getPosts(
            tokenAPI, text,
            getLanguageCode(firstLanguage) + "-" + getLanguageCode(secondLanguage)
        )

        call.enqueue(object : Callback<PostTranslated> {
            override fun onResponse(call: Call<PostTranslated>, response: Response<PostTranslated>) {
                if (response.isSuccessful) {
                    activity!!.runOnUiThread {
                        translatedText!!.text = response.body()!!.text!![0]
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
