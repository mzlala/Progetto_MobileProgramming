package it.cinellialessia.yahtzee

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import it.cinellialessia.yahtzee.databinding.*
import it.cinellialessia.yahtzee.db.*
import kotlin.random.Random

class SinglePlayer: AppCompatActivity() {

    private lateinit var binding : SinglePlayerBinding
    private lateinit var bindingItem : ItemDbBinding
    private lateinit var bindingWin : PopupEndSpBinding
    private lateinit var bindingExit: PopupExitBinding
    private lateinit var bindingSetting: SettingBinding
    private val sharedPrefFile = "PreferenceKotlin"
    private lateinit var viewModel: MainActivityViewModel
    //playedDice -> elements correspond to the boxes, and they take value true if they have been played
    private var playedDice = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
    private var arrayLocked = arrayListOf(false,false,false,false,false)
    private var arrayList = arrayListOf(0,0,0,0,0)
    private var launch : Int = 0
    private var ind : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingItem = ItemDbBinding.inflate(layoutInflater)
        binding = SinglePlayerBinding.inflate(layoutInflater)
        bindingWin = PopupEndSpBinding.inflate(layoutInflater)
        bindingExit = PopupExitBinding.inflate(layoutInflater)
        bindingSetting = SettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        binding.tvPointBonus.text = getString(R.string.set_bonus)
        binding.tvPointBonus.textSize = 14F
        binding.play.setBackgroundColor(Color.LTGRAY)
        binding.play.isEnabled = false

        launch = 3
        binding.launch.text = (getString(R.string.launch)+ "       " + launch.toString())
        binding.launch.setBackgroundColor(getColor(R.color.button))

        for (d in abbreviationDice()){
            d.isVisible = false
        }

        Extra().resetDice(abbreviationDice(),arrayLocked)     // Unlock All Dice and make them invisible
        Extra().lockTvPoint(abbreviation())     //Lock all text view

        val diceSelected = sharedPreferences.getInt("DefaultDice", 0)     //Default Dice color = White
        val listDice = Extra().colorDiceDefault(abbreviationDice(), diceSelected)     // Set the selected Dice color
        threeLaunch(listDice) // start the game
        soundEffect(sharedPreferences)

        binding.imageBack.setOnClickListener {
            showDialogExit() //  exit dialog
            playSounds("tap")
        }
    }

    private fun abbreviation(): List<TextView>{
        return listOf(
            binding.tvPoint1,
            binding.tvPoint2,
            binding.tvPoint3,
            binding.tvPoint4,
            binding.tvPoint5,
            binding.tvPoint6,
            binding.tvPoint3x,
            binding.tvPoint4x,
            binding.tvPointFull,
            binding.tvPointLowScale,
            binding.tvPointBigScale,
            binding.tvPointYahtzee,
            binding.tvPointChance)
    }

    private fun abbreviationDice(): List<ImageView> {
        return listOf(
            binding.Dice1,
            binding.Dice2,
            binding.Dice3,
            binding.Dice4,
            binding.Dice5,
        )
    }

    private fun lockDice(): ArrayList<Boolean> { //Locks and Unlocks Dice on click on it
        for(d in abbreviationDice()){
            d.isEnabled = true
            d.isVisible = true
            d.setOnClickListener {
                arrayLocked[d.contentDescription.toString().toInt()] = !arrayLocked[d.contentDescription.toString().toInt()]
                if(arrayLocked[d.contentDescription.toString().toInt()]){
                    Extra().setLockedColor(d)
                } else Extra().setUnlockedColor(d)
            }
        }
        return arrayLocked
    }

    private fun threeLaunch(listDice : List<Int>){
        binding.launch.setOnClickListener { //when launch is pressed
            if(Extra().controlDice(arrayLocked) == 1){ //if all dice are locked, do not make another launch
                playSounds("roll")

                ind = null
                binding.play.setBackgroundColor(Color.LTGRAY)
                binding.play.isEnabled = false
                Extra().deletePointVisibility(abbreviation()) //delete all the suggestion of point

                if(launch == 3){
                    Extra().unlockTvPoint(abbreviation(),playedDice) // allow the use of point' suggestion
                } else if(launch == 1){
                    binding.launch.setBackgroundColor(Color.LTGRAY)
                    binding.launch.isEnabled = false // when remaining launch=0, lock launch button
                }

                Extra().randomDice(listDice,lockDice(),abbreviationDice(),arrayList, launch) //launch and animation of dice

                launch -= 1
                binding.launch.text = (getString(R.string.launch)+ "       " + launch.toString())
            }
        }
        lookForPoint() //setOnclick of all tv point
    }

    private fun playSounds(string: String){ //Play sounds if they are active
        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        if(sharedPreferences.getInt("Sound", 0) == 0){
            var mp = MediaPlayer()
            when (string) {
                "roll" -> {
                    when(Random.nextInt(1,4)){
                        1 -> mp = MediaPlayer.create(this, R.raw.roll1)
                        2 -> mp = MediaPlayer.create(this, R.raw.roll2)
                        3 -> mp = MediaPlayer.create(this, R.raw.roll3)
                    }
                }
                "button" -> mp = MediaPlayer.create(this, R.raw.button)
                "tap" -> mp = MediaPlayer.create(this, R.raw.tap_low_volume)
            }

            if (mp.isPlaying) {
                mp.reset()
            }
            mp.start()
        }
    }

    private fun soundEffect(sharedPreferences: SharedPreferences){
        //Set icon sounds
        var sound = sharedPreferences.getInt("Sound", 0) //0 -> On
        if(sound == 0){
            binding.sounds.setImageResource(R.drawable.sound)
        } else {
            binding.sounds.setImageResource(R.drawable.sound_off)
        }
        binding.sounds.setOnClickListener {
            if(sound == 0){ //0 -> On 1 -> Off
                sound = 1
                binding.sounds.setImageResource(R.drawable.sound_off)
            } else {
                sound = 0
                binding.sounds.setImageResource(R.drawable.sound)
            }
            with (sharedPreferences.edit()) {
                putInt("Sound", sound)
                apply()
            }
        }
    }

    private fun lookForPoint(): TextView? {
        ind = null
        val tvPoint = abbreviation()

        for(tv in tvPoint){
            tv.setOnClickListener {
                binding.play.isEnabled = true
                binding.play.setBackgroundColor(getColor(R.color.play))
                ind = tv
                Extra().pointVisibility(ind,tvPoint,arrayList) //show the current point in the pressed textview
                playSounds("tap")
            }
        }

        binding.play.setOnClickListener {  //if PLAY pressed
            playSounds("button")
            Extra().animationDown(abbreviationDice())
            binding.play.isEnabled = false
            binding.launch.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({
                playPressed(ind!!)
            },300)
        }

        return ind
    }

    private fun playPressed(ind: TextView) {
        if(!playedDice[ind.contentDescription.toString().toInt() - 1]){ //if the box is not already played -> i can play it
            playedDice[ind.contentDescription.toString().toInt() - 1] = true

            ind.isEnabled = false //Lock the current played textView for the rest of the match
            ind.setTextColor(Color.DKGRAY)
            ind.text = CountScore().point(ind.contentDescription.toString().toInt(),arrayList).toString() // set the scored point in the textView
            ind.setBackgroundResource(R.drawable.rectangle_full_white)

            //reset launch
            launch = 3
            binding.launch.setBackgroundColor(getColor(R.color.button))
            binding.launch.isEnabled = true
            binding.launch.text = (getString(R.string.launch)+ "      " + launch.toString())
            binding.play.setBackgroundColor(Color.LTGRAY)

            if(ind.contentDescription.toString().toInt() in 1..6){ //control on the left column of pointView for the bonus point
                pointBonus()
            }

            Extra().resetDice(abbreviationDice(),arrayLocked)  // unlock dice
            Extra().lockTvPoint(abbreviation())

            updateScore(ind.text.toString().toInt())
            gameFinish() //control if the game is finish
        }
    }

    private fun pointBonus(){
        var count = 0
        val tvPoint = abbreviation()

        for(i in 0..5){
            if(tvPoint[i].text != "")
                count += tvPoint[i].text.toString().toInt()
        }

        binding.tvPointBonus.text = ("$count/63")

        if(count >= 63){
            binding.tvPointBonus.text = getString(R.string._35)
            binding.tvPointBonus.setBackgroundResource(R.drawable.rectangle_full_white)
            binding.tvPointBonus.textSize = 24F
            binding.tvPointBonus.setTextColor(Color.DKGRAY)
            updateScore(35)
        } else if(Extra().controlColumn(abbreviation())){ //If all the first column is played and the sum of is points is less then 63 -> my bonus point is 0
            binding.tvPointBonus.text = getString(R.string._0)
            binding.tvPointBonus.setBackgroundResource(R.drawable.rectangle_full_white)
            binding.tvPointBonus.textSize = 24F
            binding.tvPointBonus.setTextColor(Color.DKGRAY)
        }
    }

    private fun updateScore(newScore : Int){ //Update the score of the players in each round
        val actualScore = binding.tvScore.text.toString().toInt()
        val totalScore = actualScore + newScore

        binding.tvScore.text = totalScore.toString()
    }

    private fun gameFinish(){ //if all box are played
        for(play in playedDice){
            if(!play){
                return //The game are not finish
            }
        }

        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val defaultName = sharedPreferences.getString("NameDevice", "You")
        val date = MainActivityDataB().date()

        // add match to database
        val user = User(0, "${getString(R.string.score)} ${binding.tvScore.text}", "\t${defaultName}", getString(R.string.single_player), date)
        viewModel.insertUserInfo(user)

        var gamePlayed = sharedPreferences.getInt("NGameSINGLE", 0)
        var totalPoint = sharedPreferences.getInt("SINGLEPoint", 0)

        gamePlayed += 1
        totalPoint += binding.tvScore.text.toString().toInt()

        with (sharedPreferences.edit()) {
            putInt("NGameSINGLE", gamePlayed)
            putInt("SINGLEPoint", totalPoint)
            apply()
        }

        showEndMatchDialog()
    }

    private fun showDialogExit() {
        val dialog = Dialog(this)

        if (bindingExit.root.parent != null) {
            (bindingExit.root.parent as ViewGroup).removeView(bindingExit.root)
        }
        dialog.setContentView(bindingExit.root)

        bindingExit.popupBtnYes.setOnClickListener {
            finish()
            playSounds("tap")
            dialog.dismiss()
        }
        bindingExit.popupBtnNo.setOnClickListener {
            dialog.dismiss()
            playSounds("tap")
        }
        dialog.show()
    }

    private fun showEndMatchDialog() {
        val dialog = Dialog(this)
        if (bindingWin.root.parent != null) {
            (bindingWin.root.parent as ViewGroup).removeView(bindingWin.root)
        }
        dialog.setContentView(bindingWin.root)
        dialog.setCanceledOnTouchOutside(false)

        bindingWin.tvScoreWinner.text = binding.tvScore.text


        bindingWin.popupBtnYes.setOnClickListener {
            finish()
            playSounds("tap")
            dialog.dismiss()
        }
        bindingWin.btnReplay.setOnClickListener {
            replay()
            dialog.dismiss()
            playSounds("tap")
        }
        dialog.show()

    }

    override fun onBackPressed() {
        showDialogExit()
    }

    private fun replay(){
        playedDice = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
        arrayLocked = arrayListOf(false,false,false,false,false)
        arrayList = arrayListOf(0,0,0,0,0)
        launch = 0
        ind = null

        binding.tvPointBonus.text = getString(R.string.set_bonus)
        binding.tvPointBonus.setBackgroundResource(0)
        binding.tvPointBonus.textSize = 14F
        binding.play.setBackgroundColor(Color.LTGRAY)
        binding.play.isEnabled = false
        binding.launch.setBackgroundColor(getColor(R.color.button))
        binding.tvScore.text = "0"
        launch = 3
        binding.launch.text = (getString(R.string.launch)+ "       " + launch.toString())

        val tvPoint1 = abbreviation()
        for(tv in tvPoint1){
            tv.text = ""
            tv.hint= ""
            tv.setBackgroundResource(R.drawable.rectangle_white)
        }
        for (d in abbreviationDice()){
            d.isVisible = false
        }

        Extra().resetDice(abbreviationDice(),arrayLocked) //Reset the dice
        Extra().lockTvPoint(abbreviation()) //lock textview

        val sharedPreferences: SharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val diceSelected = sharedPreferences.getInt("DefaultDice", 0)
        val listDice = Extra().colorDiceDefault(abbreviationDice(), diceSelected)
        threeLaunch(listDice)
    }
}