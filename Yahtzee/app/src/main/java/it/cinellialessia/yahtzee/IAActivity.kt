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
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import it.cinellialessia.yahtzee.databinding.*
import it.cinellialessia.yahtzee.db.*
import kotlin.random.Random

class IAActivity: AppCompatActivity() {
    private lateinit var binding : Player2Binding
    private lateinit var bindingWin : PopupWinnerBinding
    private lateinit var bindingExit: PopupExitBinding
    private val sharedPrefFile = "PreferenceKotlin"
    private lateinit var viewModel: MainActivityViewModel

    private var playedDice = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
    private var playedDice2 = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
    private var arrayLocked = arrayListOf(false,false,false,false,false)
    private var arrayList = arrayListOf(0,0,0,0,0)
    private var arrayIa = arrayListOf(0,0,0,0,0)
    private var launch : Int = 0
    private var ind : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = Player2Binding.inflate(layoutInflater)
        bindingWin = PopupWinnerBinding.inflate(layoutInflater)
        bindingExit = PopupExitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences: SharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        val defaultName = sharedPreferences.getString("NameDevice", getString(R.string.you))
        binding.tvPointBonus.text = getString(R.string.set_bonus)
        binding.tvPointBonus2.text = getString(R.string.set_bonus)
        binding.tvPointBonus.textSize = 12F
        binding.tvPointBonus2.textSize = 12F
        binding.play.setBackgroundColor(Color.LTGRAY)
        binding.launch.setBackgroundColor(getColor(R.color.blue))
        binding.play.isEnabled = false
        binding.textScore1.text = defaultName
        binding.textScore2.text = getString(R.string.robot)

        Extra().resetDice(abbreviationDice(),arrayLocked) //Reset all dice and remove the color filter
        Extra().lockTvPoint(abbreviation()) //lock the tvPoint of the player
        Extra().lockTvPoint(abbreviation2()) //lock the IA's tvPoint

        for (d in abbreviationDice()){
            d.isVisible = false
        }

        val diceSelected = sharedPreferences.getInt("DefaultDice", 4)
        val listDice = Extra().colorDiceDefault(abbreviationDice(), diceSelected)
        threeLaunch(listDice)
        soundEffect(sharedPreferences)

        binding.imageBack.setOnClickListener {
            showDialogExit()
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

    private fun abbreviation2(): List<TextView> {
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
            binding.tvPointChance2
        )
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

    private fun threeLaunch(listDice: List<Int>) {
        launch = 3
        binding.launch.text = (getString(R.string.launch)+ "      " + launch.toString())
        binding.launch.setOnClickListener { //when launch is pressed
            if(Extra().controlDice(arrayLocked) == 1){ //if all dice are locked, do not make another launch
                val sharedPreferences: SharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
                if(sharedPreferences.getInt("Sound", 0) == 0){
                    playSounds("roll") // make the sound
                }
                ind = null
                Extra().deletePointVisibility(abbreviation()) //delete all the suggestion of point
                binding.play.setBackgroundColor(Color.LTGRAY)
                binding.play.isEnabled = false
                if(launch == 3){
                    Extra().unlockTvPoint(abbreviation(),playedDice) // allow the use of point' suggestion
                } else if(launch == 1){
                    binding.launch.setBackgroundColor(Color.LTGRAY)
                    binding.launch.isEnabled = false //when remaining launch=0, lock launch button
                }

                Extra().randomDice(listDice,lockDice(),abbreviationDice(),arrayList, launch) //launch and animation of dice

                launch -= 1
                binding.launch.text = (getString(R.string.launch)+ "       " + launch.toString())
            }
        }
        lookForPoint()//setOnclick of all tv point
    }//The Start of the game

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
                playSounds("tap")
                binding.play.isEnabled = true
                binding.play.setBackgroundColor(getColor(R.color.play))
                ind = tv
                Extra().pointVisibility(ind,abbreviation(),arrayList) //show the current point in the pressed textview
            }
        }

        binding.play.setOnClickListener { //if PLAY pressed
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

    private fun playPressed(ind: TextView){
        if(!playedDice[ind.contentDescription.toString().toInt() - 1]){ //if the box is not already played -> i can play it

            playedDice[ind.contentDescription.toString().toInt() - 1] = true

            ind.isEnabled = false //Lock the current played textView for the rest of the match
            ind.setTextColor(Color.DKGRAY)
            ind.text = CountScore().point(ind.contentDescription.toString().toInt(),arrayList).toString()
            ind.setBackgroundResource(R.drawable.rectangle_full_white)

            launch = 3
            binding.launch.setBackgroundColor(getColor(R.color.blue))
            binding.launch.isEnabled = true
            binding.launch.text = (getString(R.string.launch)+ "       " + launch.toString())
            binding.play.setBackgroundColor(Color.LTGRAY)
            if(ind.contentDescription.toString().toInt() in 1..6){ //control on the left column of pointView for the bonus point
                pointBonus(1)
            }
            iaGame()

            Extra().resetDice(abbreviationDice(),arrayLocked) // unlock dice
            Extra().lockTvPoint(abbreviation())
            updateScore(ind.text.toString().toInt(),1)
        }
    }

    private fun pointBonus(turn : Int){
        if(turn == 1){
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
        } else {
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

    private fun updateScore(newScore : Int, turn: Int){ //add the point in the total score
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
        println(playedDice2)
        for(play in playedDice2){
            if(!play){
                return
            }
        }
        showEndMatchDialog()
        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val defaultName = sharedPreferences.getString("NameDevice", "Player 1")
        val secondName = "Robot"

        val date = MainActivityDataB().date()

        when (winner()) {
            "Draw!" -> {
                val user = User(0, "\t${winner()}", "\t${defaultName} (${binding.tvScore.text}) \tVS\t $secondName (${binding.tvScore2.text})",
                    getString(R.string.you_vs_robot), date)
                viewModel.insertUserInfo(user)
            }
            "Robot" -> {
                val user = User(0, getString(R.string.robotWinner), "\t${defaultName} (${binding.tvScore.text}) \tVS\t $secondName (${binding.tvScore2.text})",
                    getString(R.string.you_vs_robot), date)
                viewModel.insertUserInfo(user)
            }
            else -> {
                val user = User(0, "${getString(R.string.winner)}\t${winner()}", "\t${defaultName} (${binding.tvScore.text}) \tVS\t $secondName (${binding.tvScore2.text})",
                    getString(R.string.you_vs_robot), date)
                viewModel.insertUserInfo(user)
            }
        }

        var gamePlayed = sharedPreferences.getInt("NGameIA", 0)
        var totalPoint = sharedPreferences.getInt("IAPoint", 0)

        gamePlayed += 1
        totalPoint += binding.tvScore.text.toString().toInt()

        with (sharedPreferences.edit()) {
            putInt("NGameIA", gamePlayed)
            putInt("IAPoint", totalPoint)
            apply()
        }
    }

    private fun showDialogExit() {
        val dialog = Dialog(this)
        if (bindingExit.root.parent != null) {
            (bindingExit.root.parent as ViewGroup).removeView(bindingExit.root)
        }
        dialog.setContentView(bindingExit.root)

        bindingExit.popupBtnYes.setOnClickListener {
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
            bindingWin.tvWinner.text = (winner())
            bindingWin.imageView3.isInvisible = false
            bindingWin.tvSecond.isInvisible = false
            bindingWin.tvScoreSecond.isInvisible = false

            if(winner() == binding.textScore1.text){ //Se vince il primo player
                bindingWin.tvScoreWinner.text = binding.tvScore.text
                bindingWin.tvSecond.text = binding.textScore2.text

                bindingWin.tvScoreSecond.text = binding.tvScore2.text

            } else { //Se vince il secondo player
                bindingWin.tvScoreWinner.text = binding.tvScore2.text
                bindingWin.tvScoreSecond.text = binding.tvScore.text

                bindingWin.tvSecond.text = binding.textScore1.text
            }
        }

        bindingWin.popupBtnYes.setOnClickListener {
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

    private fun winner(): String {
        val winner : String
        val score1 = binding.tvScore.text.toString().toInt()
        val score2 = binding.tvScore2.text.toString().toInt()

        winner = when {
            score1 > score2 -> {
                binding.textScore1.text as String
            }
            score2 > score1 -> {
                binding.textScore2.text as String
            }
            else -> {
                getString(R.string.draw)
            }
        }
        return winner
    }

    private fun iaGame(){
        arrayIa = arrayListOf(0,0,0,0,0)
        for(i in 0..4){
            arrayIa[i] = Random.nextInt(1,7)
        }

        val foundDice = arrayListOf(0,0,0,0,0,0)
        for(i in arrayIa){
            foundDice[i-1] += 1
        }

        if(callWhatIHave(2)){ //see what i have
            return
        }

        // if "what i have" return nothing, the robot plays where it makes max point
        println("Out")
        var max = 0
        var point: Int
        var tvMax : TextView? = null

        for(tv in abbreviation2()){ // Found max point available and in which TextView
            val ind = tv.contentDescription.toString().toInt()
            if(tv.text == ""){
                point = CountScore().point(ind,arrayIa)

                if((ind <= 3 && (foundDice[ind - 1] == 2 || foundDice[ind - 1] == 3)) && point != 25){ //Couple or tris in the less box
                    if(point >= max){
                        tvMax = tv
                        writePointIa(tvMax,arrayIa)
                        return
                    }
                } else if(ind in 4..6){ // Is better make 0pt in cards like "1" and "2"
                    if(point > max){
                        max = point
                        tvMax = tv
                    }
                } else { // More difficult are the less probable, so is better to sacrifice them
                    if(point >= max){
                        max = point
                        tvMax = tv
                    }
                }

                if(tvMax == null){
                    tvMax = tv
                }
            }
        }

        if((tvMax == abbreviation2()[6] || tvMax == abbreviation2()[7] || tvMax == abbreviation2()[12]) && max <= 15){
            if(sacrificePoint()){
                return
            }
        } else if((tvMax == abbreviation2()[5] && max < 12) || (tvMax == abbreviation2()[4] && max < 10)){
            if(sacrificePoint()){
                return
            }
        }

        if (tvMax != null) {
            writePointIa(tvMax,arrayIa)
            return
        }
    }

    private fun whatIHave(foundDice: ArrayList<Int>): String { //Return what i have
        var max = 0
        var num = 0
        for(i in 0..5){
            if(foundDice[i] >= max){
                num = i + 1
                max = foundDice[i]
            }
        }
        when(max){
            1 -> return when {
                CountScore().point(binding.tvPointBigScale2.contentDescription.toString().toInt(),arrayIa) == 40 -> {
                    "BigScale"
                }
                CountScore().point(binding.tvPointLowScale2.contentDescription.toString().toInt(),arrayIa) == 30 -> {
                    "LowScale"
                }
                else -> {
                    "Nothing"
                }
            }
            2 -> {
                return if(CountScore().point(binding.tvPointLowScale2.contentDescription.toString().toInt(),arrayIa) == 30){
                    "LowScale"
                } else {
                    "2x$num"
                }
            }
            3 -> return if(CountScore().point(binding.tvPointFull2.contentDescription.toString().toInt(),arrayIa) == 25){
                "Full$num"
            } else {
                "3x$num"
            }
            4 -> return "4x$num"
            5 -> return "Yahtzee$num"
        }
        return "Nothing"
    }

    private fun callWhatIHave(remLaunch: Int): Boolean { //it calls what i have and choose what is better to do
        val foundDice = arrayListOf(0,0,0,0,0,0)
        for(i in arrayIa){
            foundDice[i-1] += 1
        }

        println("Dice IA: $arrayIa in launch rem:$remLaunch In fun ${whatIHave(foundDice)}")

        when(whatIHave(foundDice).substring(0,2)){
            "2x" -> {
                if(when2x(
                        whatIHave(foundDice).substring(2,3).toInt(),
                        remLaunch,
                        foundDice))
                    return true
            }
            "3x" -> {
                if(when3x(whatIHave(foundDice).substring(2,3).toInt(),remLaunch))
                    return true
            }
            "4x" -> {
                if(when4x(whatIHave(foundDice).substring(2,3).toInt(),remLaunch))
                    return true
            }
        }
        when(whatIHave(foundDice)){
            "LowScale" -> {
                if(whenLowScale(remLaunch,foundDice))
                    return true
            }
            "BigScale" -> {
                if(whenBigScale(remLaunch))
                    return true
            }
            "Nothing" -> {
                if(!playedDice2[12]){
                    if(tryChance(remLaunch)){
                        return true
                    }
                }
            }
        }
        when(whatIHave(foundDice).length){
            8 -> { //Yahtzee
                if(whatIHave(foundDice).subSequence(0,7) == "Yahtzee"){
                    if(whenYahtzee(whatIHave(foundDice).substring(7,8).toInt()))
                        return true
                }
            }
            5 -> { //Full
                if(whenFull((whatIHave(foundDice).substring(4,5).toInt()), remLaunch))
                    return true
            }
        }
        if(!playedDice2[12]){
            if(tryChance(remLaunch)){
                return true
            }
        }
        return false
    }

    private fun when2x(num: Int, remLaunch: Int, foundDice: ArrayList<Int>): Boolean { //it choose the better option when the robot has 2x
        if(remLaunch == 0){//launch are finished
            if(!playedDice2[num - 1]) { //if is free
                writePointIa(abbreviation2()[num - 1],arrayIa)
                return true
            }
            return false
        }

        // <= 4 ???
        var couple = 0
        val numCouple = mutableListOf(0,0) //Number searched
        for(i in 0..5){
            if(foundDice[i] == 2){
                numCouple[couple] = i + 1
                couple += 1
            }
        }

        if(!playedDice2[8]) { //if full is free -> control if there is another couple -> if true, try Full
            if(couple == 2){
                println("try full\n")
                if(tryFullWithCouples(numCouple, remLaunch)){
                    return true
                }
                if(!playedDice2[numCouple[0] - 1]){ //If the less couple is free
                    writePointIa(abbreviation2()[numCouple[0] - 1],arrayIa)
                    return true
                }
            }
        }

        if(couple == 2 && playedDice2[numCouple[0] - 1] && playedDice2[numCouple[1] - 1]){
            if(!playedDice2[12]){
                if(tryChance(remLaunch)){
                    return true
                }
                return false
            }
        }

        if(playedDice2[num - 1] && playedDice2[6] && playedDice2[7]) { //num, 3x 4x
            if(couple == 2){
                //Control if also the other couple is played
                if(!playedDice2[numCouple[0] - 1]){
                    println("Other couple")
                    val random = listOf(Random.nextInt(1,7),Random.nextInt(1,7),Random.nextInt(1,7))
                    arrayIa = arrayListOf(numCouple[0],numCouple[0],random[0],random[1],random[2])

                    if(callWhatIHave(remLaunch - 1)){
                        return true
                    }
                }
            }
        }

        val random = listOf(Random.nextInt(1,7),Random.nextInt(1,7),Random.nextInt(1,7))
        arrayIa = arrayListOf(num,num,random[0],random[1],random[2])

        if(callWhatIHave(remLaunch - 1)){
            return true
        }
        return false
    }

    private fun when3x(num: Int,  remLaunch: Int): Boolean { //it choose the better option when the robot has 3x
        if(remLaunch == 0){//launch are finished
            if(num >= 4){
                if(!playedDice2[6]){ //3x
                    writePointIa(binding.tvPoint3x2,arrayIa)
                    return true
                }
                if(!playedDice2[num - 1]){ //if the number is free
                    writePointIa(abbreviation2()[num - 1],arrayIa)
                    return true
                }
            } else {
                if(!playedDice2[num - 1]){ //if the number is free
                    writePointIa(abbreviation2()[num - 1],arrayIa)
                    return true
                }
                if(!playedDice2[6]){ //if 3x is free
                    writePointIa(binding.tvPoint3x2,arrayIa)
                    return true
                }
            }
            return false
        }
        if(num >= 4){
            if(try4xAndYahtzee(num,remLaunch)){
                return true
            }

            if(!playedDice2[num - 1]){ //if the number is free
                writePointIa(abbreviation2()[num - 1],arrayIa)
                return true
            }

            if(!playedDice2[6]){ //if 3x is free
                writePointIa(binding.tvPoint3x2,arrayIa)
                return true
            }

        } else {
            if(!playedDice2[11]){ //If Yahtzee is free
                if(try4xAndYahtzee(num,remLaunch)){
                    return true
                }
            }
            if(!playedDice2[num - 1]){ //if the number is free
                writePointIa(abbreviation2()[num - 1],arrayIa)
                return true
            }
            if(!playedDice2[12]){//if chance is free
                if(tryChance(remLaunch)){
                    return true
                }
            }
            if(!playedDice2[7]){ //if 4x is free
                if(try4xAndYahtzee(num,remLaunch)){
                    return true
                }
            }
            return false
        }
        return false
    }

    private fun when4x(num: Int,  remLaunch: Int): Boolean { //it choose the better option when the robot has 4x
        if(remLaunch == 0){ //launch are finished
            if(num >= 4){
                if(!playedDice2[7]){ // if 4x is free
                    writePointIa(binding.tvPoint4x2,arrayIa)
                    return true
                }
                if(!playedDice2[num - 1]){ //if the number is free
                    writePointIa(abbreviation2()[num - 1],arrayIa)
                    return true
                }
                if(!playedDice2[6]){ //if 3x is free
                    writePointIa(binding.tvPoint3x2,arrayIa)
                    return true
                }
                if(!playedDice2[12]){ //if chance is free
                    writePointIa(binding.tvPointChance2,arrayIa)
                    return true
                }
            } else { //if num < 4
                if(!playedDice2[num - 1]){ //if number is free
                    writePointIa(abbreviation2()[num - 1],arrayIa)
                    return true
                }
            }
        } else {
            val random = Random.nextInt(1,7)
            arrayIa = arrayListOf(num,num,num,num, random)

            if(callWhatIHave(remLaunch - 1)){
                return true
            }
        }
        return false
    }

    private fun whenFull( num: Int, remLaunch: Int): Boolean { //it choose the better option when the robot has full
        if(!playedDice2[8]){ //if full is free
            writePointIa(binding.tvPointFull2,arrayIa)
            return true
        }
        if(when3x(num, remLaunch)){ //Call 3x if full is played
            return true
        }
        if(sacrificePoint()){ //if there are no good play
            return true
        }
        return false
    }

    private fun whenLowScale( remLaunch: Int, foundDice: ArrayList<Int>): Boolean { //it choose the better option when the robot has LowScale
        if(remLaunch == 0){
            if(!playedDice2[9]){ //if LowScale is free
                writePointIa(binding.tvPointLowScale2,arrayIa)
                return true
            }
            if(!playedDice2[12]){ //if Chance is free
                if(CountScore().point(13,arrayIa) >= 16){
                    writePointIa(binding.tvPointChance2,arrayIa)
                    return true
                }
            }
            return false
        }
        if(!playedDice2[10]){ //if BigScale is free, try it
            println("try bigScale")
            if(tryBigScale(foundDice, remLaunch)){
                return true
            }
        }
        if(!playedDice2[9]){ //if LowScale is free
            writePointIa(binding.tvPointLowScale2,arrayIa)
            return true
        }
        if(!playedDice2[12]){
            if(tryChance(remLaunch)){ //if chance is free
                return true
            }
        }
        return false
    }

    private fun whenBigScale(remLaunch: Int): Boolean { //it choose the better option when the robot has BigScale
        if(!playedDice2[10]){ //if BigScale is free
            writePointIa(binding.tvPointBigScale2,arrayIa)
            return true

        }
        if(!playedDice2[9]){ //if LowScale is free
            writePointIa(binding.tvPointLowScale2,arrayIa)
            return true
        }
        if(!playedDice2[12]){
            if(tryChance(remLaunch)){ //if chance is free
                return true
            }
        }
        return false
    }

    private fun whenYahtzee(num: Int): Boolean { //if the robot has Yahtzee
        if(!playedDice2[11]){ //if Yahtzee is free
            writePointIa(binding.tvPointYahtzee2, arrayIa)
            return true
        }
        if(!playedDice2[num - 1]){ //if the respective number box is free
            writePointIa(abbreviation2()[num - 1], arrayIa)
            return true
        }
        if(num >= 4){
            if(!playedDice2[7]){ //if 4x is free
                writePointIa(binding.tvPoint4x2, arrayIa)
                return true
            }
            if(!playedDice2[6]){ // if 3x is free
                writePointIa(binding.tvPoint3x2, arrayIa)
                return true
            }
        }
        return false
    }

    private fun tryChance(remLaunch: Int): Boolean { //it try to have biggest possible number to make an higher chance
        if(remLaunch <= 0){
            if(!playedDice2[12]){ //if chance is free
                writePointIa(binding.tvPointChance2, arrayIa)
                return true
            }
            return false
        }
        val foundDice = arrayListOf(0,0,0,0,0,0)
        for(i in arrayIa){
            foundDice[i-1] += 1
        }

        if(!playedDice2[12]){ //if chance is free
            var h = 0
            for(i in 3..5){
                if(foundDice[i] == 1){
                    arrayIa[h] = i + 1
                    h += 1
                } else {
                    arrayIa[h] = Random.nextInt(1,7)
                    h += 1
                }
            }
            arrayIa[3] = Random.nextInt(1,7)
            arrayIa[4] = Random.nextInt(1,7)

            if(tryChance(remLaunch - 1)){ //if chance is free
                return true
            }
        }
        return false
    }

    private fun tryFullWithCouples(num: MutableList<Int>, remLaunch: Int): Boolean { //Try to make a Full if the robot have 2 couple
        val searchedNumber = Random.nextInt(1,7)
        arrayIa = arrayListOf(num[0], num[0], num[1], num[1], searchedNumber)

        if(callWhatIHave(remLaunch - 1)){
            return true
        }
        return false
    }

    private fun tryBigScale(foundDice: ArrayList<Int>, remLaunch: Int): Boolean{ //If the robot has a LowScale it try to make a big scale
        var case = 0
        for(i in 0..3){ //1-2-3-4
            if(foundDice[i] >= 1){
                case = 1
            }
        }
        for(i in 1..4){ //2-3-4-5
            if(foundDice[i] >= 1){
                case = 2
            }
        }
        for(i in 2..5){ //3-4-5-6
            if(foundDice[i] >= 1){
                case = 3
            }
        }

        when(Random.nextInt(1,7)){
            1 -> if(case == 2){
                arrayIa = arrayListOf(1,2,3,4,5)
            }
            2 -> if(case == 3){
                arrayIa = arrayListOf(2,3,4,5,6)
            }
            5 -> if(case == 1){
                arrayIa = arrayListOf(1,2,3,4,5)
            }
            6 -> if(case == 2){
                arrayIa = arrayListOf(2,3,4,5,6)
            }
        }
        if(callWhatIHave(remLaunch - 1)){
            return true
        }
        return false
    }

    private fun try4xAndYahtzee(myNumber: Int, remLaunch: Int): Boolean { //When the robot has 3x he try to have more of that number
        if(remLaunch == 0){
            return false
        }

        val random1 = Random.nextInt(1,7)
        val random2 = Random.nextInt(1,7)

        arrayIa = arrayListOf(myNumber, myNumber,myNumber,random1,random2)

        if(callWhatIHave(remLaunch - 1)){
            return true
        }

        return false
    }

    private fun sacrificePoint(): Boolean {
        println("sacrifice point")
        val foundDice = arrayListOf(0,0,0,0,0,0)
        for(i in arrayIa){
            foundDice[i-1] += 1
        }

        if(foundDice[1] > 0){ //if i have one "two"
            if(!playedDice2[1]){ //if 2 is free
                writePointIa(binding.tvPoint22,arrayIa)
                return true
            }
        }
        if(!playedDice2[0]){ //if 1 is free
            writePointIa(binding.tvPoint12,arrayIa)
            return true

        }
        if(!playedDice2[1]){ //if 2 is free
            writePointIa(binding.tvPoint22,arrayIa)
            return true
        }
        if(!playedDice2[11]){ //if Yahtzee is free
            writePointIa(binding.tvPointYahtzee2,arrayIa)
            return true
        }
        if(!playedDice2[10]){ //if BigScale is free
            writePointIa(binding.tvPointBigScale2,arrayIa)
            return true
        }

        return false
    }

    private fun writePointIa(tvMax : TextView, arrayIa : ArrayList<Int>) {
        tvMax.text = CountScore().point(tvMax.contentDescription.toString().toInt(),arrayIa).toString()
        tvMax.setTextColor(Color.DKGRAY)
        updateScore(tvMax.text.toString().toInt(),2)
        if(tvMax.contentDescription.toString().toInt() <= 6){
            pointBonus(2)
        }
        playedDice2[tvMax.contentDescription.toString().toInt() - 1] = true
        Handler(Looper.getMainLooper()).postDelayed({gameFinish()}, 300)
    }

    private fun revenge() { //Restart the game keeping players' names
        val sharedPreferences: SharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val defaultName = sharedPreferences.getString("NameDevice", "You")

        playedDice = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
        playedDice2 = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
        arrayLocked = arrayListOf(false,false,false,false,false)
        arrayList = arrayListOf(0,0,0,0,0)
        arrayIa = arrayListOf(0,0,0,0,0)
        ind = null
        launch = 0

        binding.tvPointBonus.text = getString(R.string.set_bonus)
        binding.tvPointBonus.textSize = 12F
        binding.tvPointBonus.setBackgroundResource(0)
        binding.tvPointBonus2.text = getString(R.string.set_bonus)
        binding.tvPointBonus2.textSize = 12F
        binding.tvPointBonus2.setBackgroundResource(0)
        binding.play.setBackgroundColor(Color.LTGRAY)
        binding.play.isEnabled = false
        binding.launch.setBackgroundColor(getColor(R.color.blue))
        binding.textScore1.text = defaultName
        binding.textScore2.text = getString(R.string.robot)
        binding.tvScore.text = "0"
        binding.tvScore2.text = "0"

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

        val diceSelected = sharedPreferences.getInt("DefaultDice", 4)
        val listDice = Extra().colorDiceDefault(abbreviationDice(), diceSelected)
        Extra().resetDice(abbreviationDice(),arrayLocked)
        threeLaunch(listDice)
    }

    override fun onBackPressed() {
        showDialogExit()
    }
}
