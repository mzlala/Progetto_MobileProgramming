package it.cinellialessia.yahtzee.db

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import it.cinellialessia.yahtzee.*
import it.cinellialessia.yahtzee.databinding.DatabaseBinding
import it.cinellialessia.yahtzee.databinding.ItemDbBinding
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivityDataB : AppCompatActivity(), RecyclerViewAdapter.RowClickListener, View.OnClickListener {

    private lateinit var binding : DatabaseBinding
    private lateinit var bindingItem : ItemDbBinding
    lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var viewModel: MainActivityViewModel
    private val sharedPrefFile = "PreferenceKotlin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DatabaseBinding.inflate(layoutInflater)
        bindingItem = ItemDbBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setData()
        setUI()
        binding.btnBarChart.setOnClickListener(this)
    }

    //start the BarChartActivity
    override fun onClick(view: View?) {
        if(view != null){
            when(view.id){
                binding.btnBarChart.id -> intent = Intent(this, BarChartActivity::class.java)
            }
            soundTap()
            startActivity(intent)
        }
    }

    //By clicking on the specific button the data set is deleted
    override fun onUserClickListener(user: User) {
        viewModel.deleteUserInfo(user)
    }

    //Function to take the date
    fun date(): String {
        val date = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time).toString()
        return date.substring(0, date.length -3)
    }

    private fun setData() {
        viewModel =
            ViewModelProvider(this@MainActivityDataB).get(MainActivityViewModel::class.java)
        viewModel.getAllUsersObservers()
            .observe(this@MainActivityDataB, {  //the list containing the data set is observed
                recyclerViewAdapter.setListData(ArrayList(it))
                recyclerViewAdapter.notifyDataSetChanged()
            })
    }

    private fun setUI() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivityDataB)
            recyclerViewAdapter = RecyclerViewAdapter(this@MainActivityDataB)
            adapter = recyclerViewAdapter
            val divider = DividerItemDecoration(applicationContext, VERTICAL)
            addItemDecoration(divider)
        }

        val listener = MyQueryListener(viewModel)
        binding.etSearch.setOnQueryTextListener(listener)
        binding.etSearch.setQuery("", true)
    }

    //To be able to filter by game mode
    class MyQueryListener(private val model: MainActivityViewModel) :
        SearchView.OnQueryTextListener{
        override fun onQueryTextSubmit(p0: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(p0: String?): Boolean {
            model.filterMod(p0)
            return false
        }
    }

    private fun soundTap(){
        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        if(sharedPreferences.getInt("Sound", 0) == 0){
            val mp = MediaPlayer.create(this, R.raw.tap)
            if (mp.isPlaying) {
                mp.reset()
            }
            mp.start()
        }
    }
}