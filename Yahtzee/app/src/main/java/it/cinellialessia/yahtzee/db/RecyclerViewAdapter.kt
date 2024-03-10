package it.cinellialessia.yahtzee.db

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.cinellialessia.yahtzee.R

//This class provides access to the database data items and creates the view for each item in the retrieved dataset
class RecyclerViewAdapter(private val listener: RowClickListener): RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    private var items  = ArrayList<User>()

    fun setListData(data: ArrayList<User>) {
        this.items = data
    }

    // Create a ViewHolder from the XML layout that represents each row in the RecyclerView.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.item_db, parent, false)
        return MyViewHolder(inflater, listener)
    }

    // Returns the size of the list of objects and tells the Adapter the number of rows that the RecyclerView can contain
    override fun getItemCount(): Int {
        return items.size
    }

    // Method called for each of the visible rows displayed in the RecyclerView.
    // Thanks to the position variable, we'll retrieve the corresponding X object in our object list
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    //Describes a view of an element
    class MyViewHolder(view: View, private val listener: RowClickListener): RecyclerView.ViewHolder(view) {

        //A different variable is assigned to each component of the layout
        val tvScore: TextView = view.findViewById(R.id.tvScore)
        private val tvPlayer: TextView = view.findViewById(R.id.tvPlayer)
        private val tvMod: TextView = view.findViewById(R.id.tvMod)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val deleteUserID: ImageView = view.findViewById(R.id.deleteUserID)
        private val imgWinner: ImageView = view.findViewById(R.id.imgWinner)

        fun bind(data: User) {
            //We associate the data of the "user" table of the database to the corresponding layout objects
            tvScore.text = data.score
            tvPlayer.text = data.player
            tvMod.text = data.mod
            tvDate.text = data.date
            deleteUserID.setOnClickListener {
                listener.onUserClickListener(data)
            }

           //different icons are displayed depending on the game mode
            when(data.mod) {
                "Solitario"-> imgWinner.setBackgroundResource(R.drawable.single_player)
                "Single Player" -> imgWinner.setBackgroundResource(R.drawable.single_player)
                "Local Multiplayer" -> imgWinner.setBackgroundResource(R.drawable.player)
                "Multiplayer Locale" -> imgWinner.setBackgroundResource(R.drawable.player)
                "Only Tabs" -> imgWinner.setBackgroundResource(R.drawable.card1)
                "Tabella" -> imgWinner.setBackgroundResource(R.drawable.card1)
                "Only Tabs 2" -> imgWinner.setBackgroundResource(R.drawable.card2)
                "Tabelle" -> imgWinner.setBackgroundResource(R.drawable.card2)
                "Bluetooth Match" -> imgWinner.setBackgroundResource(R.drawable.bluetooth)
                "Bluetooth" -> imgWinner.setBackgroundResource(R.drawable.bluetooth)
                "VS Robot" -> imgWinner.setBackgroundResource(R.drawable.versus)
            }
        }
    }

    interface RowClickListener{
        fun onUserClickListener(user: User)
    }
}