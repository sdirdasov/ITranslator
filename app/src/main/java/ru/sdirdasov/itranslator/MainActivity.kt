package ru.sdirdasov.itranslator

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import ru.sdirdasov.itranslator.post.PostListView

class MainActivity : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            actionOnTranslate()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.navigation_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_translate -> actionOnTranslate()
            R.id.action_dictionary -> actionOnDictionary()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun actionOnTranslate()
    {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment, BaseView())
        ft.commit()
    }

    private fun actionOnDictionary()
    {
        val ft = supportFragmentManager.beginTransaction()
        val listViewFragment = PostListView()
        ft.replace(R.id.fragment, listViewFragment)
        ft.commit()
    }
}
