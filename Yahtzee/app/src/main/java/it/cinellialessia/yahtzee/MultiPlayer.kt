package it.cinellialessia.yahtzee

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import it.cinellialessia.yahtzee.databinding.*
import it.cinellialessia.yahtzee.db.*
import kotlin.random.Random

class MultiPlayer: AppCompatActivity() {

    private lateinit var binding : Player2Binding
    private lateinit var bindingName : PopupPlayer2Binding
    private lateinit var bindingWin : PopupWinnerBinding
    private lateinit var bindingExit : PopupExitBinding
    private val sharedPrefFile = "PreferenceKotlin"
    private lateinit var viewModel: MainActivityViewModel
    //we use two arrays whose elements correspond to the boxes, and they take value true if they have been played
    private var playedDice = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
    private var playedDice2 = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
    private var arrayLocked = arrayListOf(false,false,false,false,false) //true if the corresponding dice is locked
    private var arrayList = arrayListOf(0,0,0,0,0) //the dice of the current match
    private var ind : TextView? = null
    private var launch : Int = 0
    private var turn : Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = Player2Binding.inflate(layoutInflater)
        bindingWin = PopupWinnerBinding.inflate(layoutInflater)
        bindingExit = PopupExitBinding.inflate(layoutInflater)
        bindingName = PopupPlayer2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        //Set color of two players' dice
        val diceSelected = sharedPreferences.getInt("DefaultDice", 4)
        val listDice = Extra().colorDiceDefault(abbreviationDice(), diceSelected)
        val secondDice = sharedPreferences.getInt("SecondDice", 3)
        val listDice2 = Extra().colorDiceDefault(abbreviationDice(), secondDice)

        showDialogName(sharedPreferences) //He has the two players choose their name for the game

        binding.tvPointBonus.text = getString(R.string.set_bonus)
        binding.tvPointBonus.textSize = 12F
        binding.tvPointBonus2.text = getString(R.string.set_bonus)
        binding.tvPointBonus2.textSize = 12F
        binding.play.setBackgroundColor(Color.LTGRAY)
        binding.play.isEnabled = false
        binding.launch.setBackgroundColor(getColor(R.color.blue))

        for(d in abbreviationDice()){
            d.isVisible = false
        }

        resetDice() // Unlock All Dice and make them invisible
        threeLaunch(listDice,listDice2) //Start the game
        engraveNameTurn(1) //Set the font of Players' name in bold when his turn arrived
        soundEffect(sharedPreferences)

        binding.imageBack.setOnClickListener {
            showDialogExit()
            playSounds("tap")
        }
    }

    private fun abbreviation(): List<TextView> { //squares of the first player
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
            binding.tvPointChance
        )
    }

    private fun abbreviation2(): List<TextView>{ //squares of the second player
        return listOf(
            binding.tvPoint12,
            binding.tvPoint22,
            binding.tvPoint32,
            binding.tvPoint42,
            binding.tvPoint52,
            binding.tvPoint62,
            binding.tvPoint3x2,
            binding.tvPoint4x2,
            binding.tvPointFull2,
            binding.tvPointLowScale2,
            binding.tvPointBigScale2,
            binding.tvPointYahtzee2,
            binding.tvPointChance2)
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

    private fun setNameGame(sharedPreferences : SharedPreferences){
        val defaultName = sharedPreferences.getString("NameDevice", "Player 1")

        // if exist took the saved name. The default name is "Player 1"
        binding.textScore1.text = sharedPreferences.getString("FirstName", defaultName.toString())

        // it took the second name if it was changed in the start pop-up. The default name is "Player 2"
        binding.textScore2.text = sharedPreferences.getString("SecondName", "Player 2")
    }

    private fun setColorDice(): List<Int> { //Return the list of dice' image based on the color selected
        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        // Took the color of Player 1 choose in the settings. Default is BLUE
        val diceSelected = sharedPreferences.getInt("DefaultDice", 4)
        //Took the color of Player 2 choose in the settings. Default is RED
        val dice2Selected = sharedPreferences.getInt("SecondDice", 3)

        val listDice = Extra().colorDiceDefault(abbreviationDice(),diceSelected)
        val listDice2 = Extra().colorDiceDefault(abbreviationDice(),dice2Selected)

        return if(turn == 1){
            listDice
        } else {
            listDice2
        }
    }

    private fun resetDice(){ //Reset dice, lock and unlock the textView depending by the current turn
        Extra().resetDice(abbreviationDice(),arrayLocked) //removes the color filter and unlock all dice

        if(turn == 1){
            Extra().lockTvPoint(abbreviation2())
        } else Extra().lockTvPoint(abbreviation())

        if(launch == 3){
            Extra().lockTvPoint(abbreviation())
            Extra().lockTvPoint(abbreviation2())
        }
        setColorDice()
    }

    private fun lockDice(): ArrayList<Boolean> { //Locks and Unlocks Dice on click on it
        for(d in abbreviationDice()){
            d.isEnabled = true
            d.isVisible = true
            d.setOnClickListener {
                arrayLocked[d.contentDescription.toString().toInt()] = !arrayLocked[d.contentDescription.toString().toInt()]
                if(arrayLocked[d.contentDescription.toString().toInt()]){
                    Extra().setLockedColor(d) //Change the color filter
                } else Extra().setUnlockedColor(d)
            }
        }
        return arrayLocked
    }

    private fun threeLaunch(listDice: List<Int>, listDice2: List<Int>) {
        launch = 3
        binding.launch.text = (getString(R.string.launch)+ "      " + launch.toString())
        Extra().lockTvPoint(abbreviation())
        Extra().lockTvPoint(abbreviation2())

        binding.launch.setOnClickListener { //when launch is pressed
            if(Extra().controlDice(arrayLocked) == 1){ //if all dice are locked, do not make another launch
                playSounds("roll")
                ind = null
                Extra().deletePointVisibility(abbreviation()) //delete all the suggestion of point in the first column
                Extra().deletePointVisibility(abbreviation2()) //delete all the suggestion of point in the second column
                binding.play.setBackgroundColor(Color.LTGRAY)
                binding.play.isEnabled = false

                if(launch == 3){
                    if(turn == 1){
                        Extra().unlockTvPoint(abbreviation(),playedDice) // allow the use of point' suggestion for the first player
                        Extra().lockTvPoint(abbreviation2()) // lock the use of point' suggestion for the second player
                    } else {
                        Extra().unlockTvPoint(abbreviation2(),playedDice2) // allow the use of point' suggestion for the second player
                        Extra().lockTvPoint(abbreviation()) //lock the use of point' suggestion for the first player
                    }

                } else if(launch == 1){
                    binding.launch.setBackgroundColor(Color.LTGRAY)
                    binding.launch.isEnabled = false // when remaining launch=0, lock launch button
                }
                if(turn == 1){
                    Extra().randomDice(listDice,lockDice(),abbreviationDice(),arrayList, launch) //launch and animation of dice
                } else {
                    Extra().randomDice(listDice2, lockDice(), abbreviationDice(), arrayList, launch) //launch and animation of dice
                }
                launch -= 1
                binding.launch.text = (getString(R.string.launch)+ "       " + launch.toString())
            }
        }
        lookForPoint() //setOnclick of all tv point, shows the scores made in that box
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
        var sound = sharedPreferences.getInt("Sound", 0)

        if(sound == 0){ //Set the icon
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
        val tvPoint2 = abbreviation2()

        for(tv in tvPoint){
            tv.setOnClickListener {
                playSounds("tap")
                binding.play.isEnabled = true
                binding.play.setBackgroundColor(getColor(R.color.play))
                ind = tv
                Extra().pointVisibility(ind,tvPoint,arrayList) //show the current point in the pressed textview
            }
        }
        for(tv in tvPoint2){
            tv.setOnClickListener {
                playSounds("tap")
                binding.play.isEnabled = true
                binding.play.setBackgroundColor(getColor(R.color.play))
                ind = tv
                Extra().pointVisibility(ind,tvPoint2,arrayList) //show the current point in the pressed textview
            }
        }

        binding.play.setOnClickListener { //if PLAY pressed
            playSounds("button")
            val listDice = setColorDice()

            Extra().animationDown(abbreviationDice(),arrayList,listDice)
            binding.play.isEnabled = false
            binding.launch.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({
                playPressed(ind!!)
            },300)
        }
        return ind
    }

    private fun playPressed(ind: TextView){
        if(!playedDice[ind.contentDescription.toString().toInt() - 1] || !playedDice2[ind.contentDescription.toString().toInt() - 1]){ //if the box is not already played -> i can play it

            ind.isEnabled = false //Lock the current played textView for the rest of the match
            ind.setTextColor(Color.DKGRAY)
            ind.text = CountScore().point(ind.contentDescription.toString().toInt(),arrayList).toString() // set the scored point in the textView
            ind.setBackgroundResource(R.drawable.rectangle_full_white)

            launch = 3
            binding.launch.isEnabled = true
            binding.launch.text = (getString(R.string.launch)+ "       " + launch.toString())
            binding.launch.setBackgroundColor(getColor(R.color.blue))
            binding.play.setBackgroundColor(Color.LTGRAY)

            if(ind.contentDescription.toString().toInt() in 1..6){ //control on the left column of pointView for the bonus point
                pointBonus(turn)
            }
            updateScore(ind.text.toString().toInt(),turn)

            if(turn == 1){
                resetDice() // unlock dice and change color
                turn = 2
                playedDice[ind.contentDescription.toString().toInt() - 1] = true
            } else {
                resetDice() // unlock dice and change color
                playedDice2[ind.contentDescription.toString().toInt() - 1] = true
                gameFinish() //control if the game is finish
                turn = 1
            }
            engraveNameTurn(turn) // put in evidence the name of player who had to play
        }
    }

    private fun pointBonus(turn : Int){
        if(turn == 1){ //For first player
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
                updateScore(35, 1)
            } else if(Extra().controlColumn(tvPoint)){ //If all the first column is played and the sum of is points is less then 63 -> my bonus point is 0
                binding.tvPointBonus.text = getString(R.string._0)
                binding.tvPointBonus.setBackgroundResource(R.drawable.rectangle_full_white)
                binding.tvPointBonus.textSize = 24F
                binding.tvPointBonus.setTextColor(Color.DKGRAY)
            }
        } else { //For second player
            var count = 0
            val tvPoint = abbreviation2()

            for(i in 0..5){
                if(tvPoint[i].text != "")
                    count += tvPoint[i].text.toString().toInt()
            }
            binding.tvPointBonus2.text = ("$count/63")

            if(count >= 63){
                binding.tvPointBonus2.text = getString(R.string._35)
                binding.tvPointBonus2.setBackgroundResource(R.drawable.rectangle_full_white)
                binding.tvPointBonus2.textSize = 24F
                binding.tvPointBonus2.setTextColor(Color.DKGRAY)
                updateScore(35, 2)
            } else if(Extra().controlColumn(tvPoint)){ //If all the first column is played and the sum of is points is less then 63 -> my bonus point is 0
                binding.tvPointBonus2.text = getString(R.string._0)
                binding.tvPointBonus2.setBackgroundResource(R.drawable.rectangle_full_white)
                binding.tvPointBonus2.textSize = 24F
                binding.tvPointBonus2.setTextColor(Color.DKGRAY)
            }
        }
    }

    private fun updateScore(newScore : Int, turn: Int){ //Update the score of the players in each round
        if(turn == 1){
            val actualScore = binding.tvScore.text.toString().toInt()
            val totalScore = actualScore + newScore

            binding.tvScore.text = totalScore.toString()
        } else {
            val actualScore = binding.tvScore2.text.toString().toInt()
            val totalScore = actualScore + newScore

            binding.tvScore2.text = totalScore.toString()
        }
    }

    private fun gameFinish(){ //if all box are played
        for(play in playedDice2){
            if(!play){
                return
            }
        }

        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val defaultName = binding.textScore1.text
        val secondName = binding.textScore2.text
        val date = MainActivityDataB().date()
        val winner = winner()

        // add match to database
        if(winner == "Draw!"){
            val user = User(0, "\t${winner}", "\t${defaultName} (${binding.tvScore.text}) \tVS\t $secondName (${binding.tvScore2.text})",
                getString(R.string.local_multiplayer), date)
            viewModel.insertUserInfo(user)
        } else {
            val user = User(0, "${getString(R.string.winner)}\t${winner}", "\t${defaultName} (${binding.tvScore.text}) \tVS\t $secondName (${binding.tvScore2.text})",
                getString(R.string.local_multiplayer), date)
            viewModel.insertUserInfo(user)
        }

        var gamePlayed = sharedPreferences.getInt("NGameMULTI", 0)
        var totalPoint = sharedPreferences.getInt("MULTIPOINT", 0)

        gamePlayed += 1
        totalPoint += binding.tvScore.text.toString().toInt()

        with (sharedPreferences.edit()) {
            putInt("NGameMULTI", gamePlayed)
            putInt("MULTIPOINT", totalPoint)
            apply()
        }

        showEndMatchDialog()
    }

    private fun winner(): CharSequence {
        val score1 = binding.tvScore.text.toString().toInt()
        val score2 = binding.tvScore2.text.toString().toInt()

        return when {
            score1 > score2 -> {
                binding.textScore1.text
            }
            score2 > score1 -> {
                binding.textScore2.text
            }
            else -> {
                getString(R.string.draw)
            }
        }
    }

    private fun showDialogName(sharedPreferences: SharedPreferences){
        val dialog = Dialog(this)
        if (bindingName.root.parent != null) {
            (bindingName.root.parent as ViewGroup).removeView(bindingName.root)
        }
        dialog.setContentView(bindingName.root)
        dialog.setCanceledOnTouchOutside(false)

        dialog.setOnCancelListener { //stops the activity if the user click onBackPressed
            finish()
            dialog.dismiss()
        }

        with (sharedPreferences.edit()) {
            remove("FirstName") //Remove the temporary string FirstName
            putString("SecondName","Player 2") //put the default string Player 2
            apply()
        }

        val defaultName = sharedPreferences.getString("NameDevice", "Player 1")
        val secondName = sharedPreferences.getString("SecondName", "Player 2")

        if(defaultName!!.isEmpty()){
            bindingName.etPlayer1.hint = "Player 1"
        } else {
            bindingName.etPlayer1.hint = defaultName //the name chosen in the settings is used as the hint
        }

        bindingName.etPlayer2.hint = secondName

        bindingName.etPlayer1.doAfterTextChanged { //The first player's name is saved if changed
            with (sharedPreferences.edit()) {
                putString("FirstName", it.toString())
                apply()
            }
            if(it.toString() == ""){
                with (sharedPreferences.edit()) {
                    putString("FirstName", "Player 1")
                    apply()
                }
            }
        }
        bindingName.etPlayer2.doAfterTextChanged { //The second player's name is saved if changed
            with (sharedPreferences.edit()) {
                putString("SecondName", it.toString())
                apply()
            }
            if(it.toString() == ""){
                with (sharedPreferences.edit()) {
                    putString("SecondName", bindingName.etPlayer2.hint.toString())
                    apply()
                }
            }
        }

        bindingName.popupBtnYes.setOnClickListener{
            setNameGame(sharedPreferences)
            dialog.dismiss()
            playSounds("tap")
        }
        dialog.show()
    }

    private fun showDialogExit() {
        val dialog = Dialog(this)
        if (bindingExit.root.parent != null) {
            (bindingExit.root.parent as ViewGroup).removeView(bindingExit.root)
        }
        dialog.setContentView(bindingExit.root)

        bindingExit.popupBtnYes.setOnClickListener {
            resetName()
            finish()
            dialog.dismiss()
            playSounds("tap")
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

        if(winner() == getString(R.string.draw)){ //If draw
            bindingWin.tvWinner.text = getString(R.string.draw)
            bindingWin.tvScoreWinner.text = binding.tvScore.text
            bindingWin.tvSecond.isInvisible = true
            bindingWin.tvScoreSecond.isInvisible = true
            bindingWin.imageView3.isInvisible = true

        } else {
            bindingWin.tvWinner.text = ("${winner()}")
            bindingWin.imageView3.isInvisible = false
            bindingWin.tvSecond.isInvisible = false
            bindingWin.tvScoreSecond.isInvisible = false

            if(winner() == binding.textScore1.text){ //Se vince il primo player
                bindingWin.tvScoreWinner.text = binding.tvScore.text
                bindingWin.tvScoreSecond.text = binding.tvScore2.text

                bindingWin.tvSecond.text = binding.textScore2.text
            } else { //Se vince il secondo player
                bindingWin.tvScoreWinner.text = binding.tvScore2.text
                bindingWin.tvScoreSecond.text = binding.tvScore.text

                bindingWin.tvSecond.text = binding.textScore1.text
            }
        }

        bindingWin.popupBtnYes.setOnClickListener {
            resetName()
            finish()
            dialog.dismiss()
            playSounds("tap")
        }

        bindingWin.btnReplay.setOnClickListener{
            revenge()
            dialog.dismiss()
            playSounds("tap")
        }
        dialog.show()
    }

    private fun resetName(){ //Reset the name every time multiplayer button is clicked
        val sharedPreferences: SharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        var defaultName = sharedPreferences.getString("NameDevice", "Player 1")
        if(defaultName!!.isEmpty()){
            defaultName = "Player 1"
        }
        with (sharedPreferences.edit()) {
            putString("FirstName", defaultName)
            putString("SecondName", "Player 2")
            apply()
        }
    }

    //Set the font of Players' name in bold when his turn arrived
    private fun engraveNameTurn(turn: Int){
        if(turn == 1){
            binding.textScore1.typeface = Typeface.DEFAULT_BOLD
            binding.textScore2.typeface = Typeface.DEFAULT
        } else {
            binding.textScore1.typeface = Typeface.DEFAULT
            binding.textScore2.typeface = Typeface.DEFAULT_BOLD
        }
    }

    //Restart the game keeping players' names
    private fun revenge() {
        val sharedPreferences: SharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        setContentView(binding.root)

        playedDice = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
        playedDice2 = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
        arrayLocked = arrayListOf(false,false,false,false,false)
        arrayList = arrayListOf(0,0,0,0,0)
        ind = null
        launch = 0
        turn = 1

        binding.tvPointBonus.text = getString(R.string.set_bonus)
        binding.tvPointBonus2.text = getString(R.string.set_bonus)
        binding.tvPointBonus.setBackgroundResource(0)
        binding.tvPointBonus2.setBackgroundResource(0)
        binding.tvPointBonus.textSize = 12F
        binding.tvPointBonus2.textSize = 12F
        binding.tvScore.text = "0"
        binding.tvScore2.text = "0"
        binding.play.setBackgroundColor(Color.LTGRAY)
        binding.launch.setBackgroundColor(getColor(R.color.blue))
        binding.play.isClickable = false

        val tvPoint1 = abbreviation()
        val tvPoint2 = abbreviation2()
        for(tv in tvPoint1){
            tv.text = ""
            tv.hint= ""
            tv.setBackgroundResource(R.drawable.rectangle_white)
        }
        for(tv in tvPoint2){
            tv.text = ""
            tv.hint = ""
            tv.setBackgroundResource(R.drawable.rectangle_white)
        }
        for (d in abbreviationDice()){
            d.isVisible = false
        }

        val diceSelected = sharedPreferences.getInt("DefaultDice", 4) //Set the default dice in blue if this aren't selected
        val listDice = Extra().colorDiceDefault(abbreviationDice(), diceSelected)
        val secondDice = sharedPreferences.getInt("SecondDice", 3) //Set the default dice in red if this aren't selected
        val listDice2 = Extra().colorDiceDefault(abbreviationDice(), secondDice) //Set the color of dice

        resetDice() //Reset the dice
        threeLaunch(listDice,listDice2)
    }

    override fun onBackPressed() {
        showDialogExit()
    }
}