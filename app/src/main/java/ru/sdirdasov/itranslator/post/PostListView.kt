package ru.sdirdasov.itranslator.post

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import ru.sdirdasov.itranslator.AppDatabase
import ru.sdirdasov.itranslator.R
import ru.sdirdasov.itranslator.utils.DATABASE_DICTIONARY

class PostListView : Fragment()
{
    private var actionBar: ActionBar? = null
    private var adapter: PostAdapter? = null
    private var presentView: View? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        actionBar = (activity as AppCompatActivity).supportActionBar
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View?
    {
        presentView = inflater.inflate(R.layout.listview_post, container, false)

        val database = AppDatabase(context, DATABASE_DICTIONARY)
        val arrayList = database.allWords

        adapter = PostAdapter(arrayList)

        if (arrayList.isEmpty())
        {
            setEmptyFragment()
        }
        else
        {
            setCompletedFragment()
        }

        val search = presentView!!.findViewById<View>(R.id.search) as EditText
        search.setHint(R.string.dictionary_search_hint)

        return presentView
    }

    private fun setEmptyFragment()
    {
        val emptyText = presentView!!.findViewById<View>(R.id.emptyTextView) as TextView
        val search = presentView!!.findViewById<View>(R.id.search) as EditText

        emptyText.visibility = View.VISIBLE
        search.visibility = View.INVISIBLE

        actionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_TITLE

        emptyText.setText(R.string.empty_dictionary)
        actionBar!!.setTitle(R.string.dictionary)
    }

    private fun setCompletedFragment()
    {
        actionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar!!.setTitle(R.string.dictionary)

        val listView = presentView!!.findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(context)

        listView.layoutManager = layoutManager
        listView.setHasFixedSize(true)
        listView.adapter = adapter

        val search = presentView!!.findViewById<View>(R.id.search) as EditText
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                adapter!!.filter.filter(s.toString())
            }
        })
    }

    override fun onDestroy()
    {
        actionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_TITLE
        actionBar!!.setTitle(R.string.app_name)
        super.onDestroy()
    }
}
