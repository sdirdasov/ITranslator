package ru.sdirdasov.itranslator.post

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.TextView
import ru.sdirdasov.itranslator.Dictionary
import ru.sdirdasov.itranslator.R
import java.util.*

class PostAdapter(private val originalItems: ArrayList<Dictionary>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>()
{
    private var filteredItems: ArrayList<Dictionary> = arrayListOf()

    val filter: Filter
        get() = object : Filter()
        {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                when {
                    constraint.isEmpty() || constraint.toString().trim { it <= ' ' }.isEmpty() -> {
                        results.values = originalItems
                    }
                    else -> results.values = getFilteredList(constraint)
                }

                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                if (results.values != null) {
                    filteredItems = results.values as ArrayList<Dictionary>
                    notifyDataSetChanged()
                }
            }
        }

    init
    {
        filteredItems.addAll(originalItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PostViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_post, parent, false
        )
    )

    override fun onBindViewHolder(holder: PostViewHolder, position: Int)
    {
        holder.bind(position)
    }

    override fun getItemCount() = filteredItems.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var textView: TextView = itemView.findViewById(R.id.text)
        var translationView: TextView = itemView.findViewById(R.id.translation)
        var language: TextView = itemView.findViewById(R.id.languages)

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            val item = filteredItems[position]
            textView.text = item.word
            translationView.text = item.translation
            language.text = "${item.sourceLanguage}-${item.targetLanguage}"
        }
    }

    fun getFilteredList(constraint: CharSequence): ArrayList<Dictionary> {

        val filteredList = ArrayList<Dictionary>()
        val textToFilter = constraint.toString().toLowerCase(Locale.getDefault())

        for (word in originalItems) {
            if (word.word.length >= textToFilter.length && word.word.toLowerCase(Locale.getDefault()).contains(
                    textToFilter
                )
            ) {
                filteredList.add(word)
            }
        }
        return filteredList
    }
}
