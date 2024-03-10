package it.cinellialessia.yahtzee

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.*
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import it.cinellialessia.yahtzee.databinding.*
import it.cinellialessia.yahtzee.db.MainActivityDataB
import it.cinellialessia.yahtzee.db.MainActivityViewModel
import it.cinellialessia.yahtzee.db.User

class OnlyCards2 : AppCompatActivity() {

    private lateinit var binding : OnlyCards2Binding
    private lateinit var bindingName : PopupPlayer2Binding
    private lateinit var bindingWin : PopupWinnerBinding
    private lateinit var bindingExit : PopupExitBinding
    private val sharedPrefFile = "PreferenceKotlin"
    private lateinit var viewModel: MainActivityViewModel

    private val score = intArrayOf(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1) // array of value of boxes player 1
    private val score2 = intArrayOf(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1) // array of value of boxes player 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = OnlyCards2Binding.inflate(layoutInflater)
        bindingWin = PopupWinnerBinding.inflate(layoutInflater)
        bindingExit = PopupExitBinding.inflate(layoutInflater)
        bindingName = PopupPlayer2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        showDialogName(sharedPreferences)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding.tvFinishGame.isVisible = false
        binding.tvPointBonus.setTextColor(Color.BLACK)
        binding.tvPointBonus.textSize = 12F
        binding.tvPointBonus2.textSize = 12F
        binding.tvPointBonus2.setTextColor(Color.BLACK)
        binding.imageBack.setOnClickListener {
            showDialogExit()
            soundTap()
        }

        game()
    }

    private fun showDialogName(sharedPreferences: SharedPreferences){  //popup insert names
        val dialog = Dialog(this)
        dialog.setContentView(bindingName.root)
        dialog.setCanceledOnTouchOutside(false)

        with (sharedPreferences.edit()) {
            remove("FirstName")
            putString("SecondName","Player 2")
            apply()
        }

        dialog.setOnCancelListener { //stops the activity if the user click onBackPressed
            finish()
            dialog.dismiss()
        }

        val defaultName = sharedPreferences.getString("NameDevice", "Player 1")  // set default name
        var secondName = sharedPreferences.getString("SecondName", "Player 2")  // set second name

        if(defaultName!!.isEmpty()){
            bindingName.etPlayer1.hint = "Player 1"
        } else {
            bindingName.etPlayer1.hint = defaultName
        }
        bindingName.etPlayer2.hint = secondName

        bindingName.etPlayer1.doAfterTextChanged {
            val firstName = bindingName.etPlayer1.text.toString()
            with (sharedPreferences.edit()) {
                putString("FirstName", firstName)
                apply()
            }
            if(bindingName.etPlayer1.text.toString() == ""){
                with (sharedPreferences.edit()) {
                    putString("FirstName", "Player 1")
                    apply()
                }
            }
        }
        bindingName.etPlayer2.doAfterTextChanged {
            secondName = bindingName.etPlayer2.text.toString()
            with (sharedPreferences.edit()) {
                putString("SecondName", secondName)
                apply()
            }
            if(bindingName.etPlayer2.text.toString() == ""){
                with (sharedPreferences.edit()) {
                    putString("SecondName", bindingName.etPlayer2.hint.toString())
                    apply()
                }
            }
        }
        bindingName.popupBtnYes.setOnClickListener{
            setNameGame(sharedPreferences)
            dialog.dismiss()
            soundTap()
        }
        setNameGame(sharedPreferences)
        dialog.show()
    }

    private fun setNameGame(sharedPreferences : SharedPreferences){
        val tvFirstPlayer = binding.textScore
        val tvSecondPlayer = binding.textScore2
        val defaultName = sharedPreferences.getString("NameDevice", "Player 1")

        tvFirstPlayer.text = sharedPreferences.getString("FirstName", defaultName)
        tvSecondPlayer.text = sharedPreferences.getString("SecondName", "Player 2")
    }

    private fun game(){
        val value1 = arrayOf(" ", "0", "1", "2", "3", "4", "5")
        val value2 = arrayOf(" ", "0", "2", "4", "6", "8", "10")
        val value3 = arrayOf(" ", "0", "3", "6", "9", "12", "15")
        val value4 = arrayOf(" ", "0", "4", "8", "12", "16", "20")
        val value5 = arrayOf(" ", "0", "5", "10", "15", "20", "25")
        val value6 = arrayOf(" ", "0", "6", "12", "18", "24", "30")
        val valueFull = arrayOf(" ", "0", "25")
        val valueLow = arrayOf(" ", "0", "30")
        val valueBig = arrayOf(" ", "0", "40")
        val valueYah = arrayOf(" ", "0", "50")

        val value = listOf(value1,value2,value3,value4,value5,value6,valueFull,valueLow,valueBig,valueYah)  // list of vectors of spinner's values

        // List spinner and textView for the first player
        val spinner = listOf(binding.sp1,binding.sp2,binding.sp3,binding.sp4,binding.sp5,binding.sp6,
            binding.spFull,binding.spLow,binding.spBig,binding.spYah)
        val textView = listOf(binding.ed3x, binding.ed4x, binding.edChance)

        // List spinner and textView for the second player
        val spinner2 = listOf(binding.sp12,binding.sp22,binding.sp32,binding.sp42,binding.sp52,binding.sp62,
            binding.spFull2,binding.spLow2,binding.spBig2,binding.spYah2)
        val textView2 = listOf(binding.ed3x2, binding.ed4x2, binding.edChance2)

        ///////////////////////////////////////////////////////////////////////////

        for((i, sp) in spinner.withIndex()){
            sp.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, value[i])
        }

        for((i, sp) in spinner2.withIndex()){
            sp.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, value[i])
        }

        for(op in spinner){ // select spinner player 1
            op.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                var ind = 0
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    ind = op.contentDescription.toString().toInt()
                    if(position != 0){
                        val count = value[ind][position]
                        score[ind] = count.toInt()
                    } else {
                        score[ind] = -1
                    }
                    countScore()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        for(op in spinner2){   // select spinner player 2
            op.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                var ind = 0
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    ind = op.contentDescription.toString().toInt()
                    if(position != 0){
                        val count = value[ind][position]
                        score2[ind] = count.toInt()
                    } else {
                        score2[ind] = -1
                    }
                    countScore2()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        for (tv in textView){    // select text view player 1
            tv.doAfterTextChanged {
                var i = 0
                when(tv){
                    binding.ed3x -> i = 10
                    binding.ed4x -> i = 11
                    binding.edChance -> i = 12
                }
                when {
                    it.toString().isEmpty() -> {
                        score[i] = -1
                    }
                    it.toString().toInt() in 0..30 -> {
                        score[i] = it.toString().toInt()
                    }
                    else -> {
                        score[i] = -1
                        tv.text.clear()
                    }
                }
                countScore()
            }
        }

        for(tv in textView2){   // select text view player 2
            tv.doAfterTextChanged {
                var i = 0
                when(tv){
                    binding.ed3x2 -> i = 10
                    binding.ed4x2 -> i = 11
                    binding.edChance2 -> i = 12
                }

                when {
                    it.toString().isEmpty() -> { //Clear points to score
                        score2[i] = -1
                    }
                    it.toString().toInt() in 0..30 -> { //Set points to score
                        score2[i] = it.toString().toInt()
                    }
                    else -> { //It's not possible to insert a value bigger then 30 point
                        score2[i] = -1
                        tv.text.clear()
                    }
                }
                countScore2()
            }
        }
    }

    private fun countScore(){   // calculation total score player 1
        var countScore = 0

        for(i in 0..12){
            if (score[i] != -1)
                countScore += score[i]
        }
        binding.tvScore.text = countScore.toString()
        gameFinish(score)
        checkBonus(countScore)
    }

    private fun countScore2(){  // calculation total score player 2
        var countScore2 = 0

        for(i in 0..12){
            if (score2[i] != -1)
                countScore2 += score2[i]
        }
        binding.tvScore2.text = countScore2.toString()
        gameFinish(score2)
        checkBonus2(countScore2)
    }

    private fun checkBonus(countScore: Int){  //  check condition for bonus player 1
        var count = 0

        for(i in 0..5){
            if(score[i] != -1)
                count += score[i]
        }

        binding.tvPointBonus.text = ("$count/63")  //refresh textView bonus

        if (count >= 63){
            binding.tvScore.text = (countScore + 35).toString()
            binding.tvPointBonus.textSize = 24F
            binding.tvPointBonus.text = getString(R.string._35)
        } else if(controlColumn(score)){
            binding.tvScore.text = countScore.toString()
            binding.tvPointBonus.textSize = 24F
            binding.tvPointBonus.text = getString(R.string._0)
        }
    }

    private fun checkBonus2(countScore: Int){  //  check condition for bonus player 2
        var count = 0

        for(i in 0..5){
            if(score2[i] != -1)
                count += score2[i]
        }

        binding.tvPointBonus2.text = ("$count/63")

        if (count >= 63){  // check condition for bonus
            binding.tvScore2.text = (countScore + 35).toString()
            binding.tvPointBonus2.textSize = 24F
            binding.tvPointBonus2.text = getString(R.string._35)
        } else if(controlColumn(score2)){
            binding.tvScore2.text = countScore.toString()
            binding.tvPointBonus2.textSize = 24F
            binding.tvPointBonus2.text = getString(R.string._0)
        }
    }

    private fun controlColumn(score: IntArray): Boolean {  // check values of first column of table
        for(tv in 0..5){
            if(score[tv] == -1){
                return false
            }
        }
        return true
    }

    private fun gameFinish(value: IntArray) {
        for(i in 0..12){ //Control if the match is finished
            if(value[i] == -1){
                return
            }
        }
        binding.tvFinishGame.isVisible = true  // visibility button end game
        binding.tvFinishGame.setOnClickListener {
            val sharedPreferences: SharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
            val defaultName = sharedPreferences.getString("NameDevice", "Player 1")
            val firstName = sharedPreferences.getString("FirstName", defaultName)
            val secondName = sharedPreferences.getString("SecondName", "Player 2")
            val date = MainActivityDataB().date()
            val winner = winner()

            //Add in database
            if(winner == getString(R.string.draw)){
                val user = User(0, "\t${winner}", "\t${defaultName} (${binding.tvScore.text}) \tVS\t $secondName (${binding.tvScore2.text})",
                    getString(R.string.only_tabs_2), date)
                viewModel.insertUserInfo(user)
            } else {
                val user = User(0, "${getString(R.string.winner)}\t${winner}", "\t${firstName} (${binding.tvScore.text}) \tVS\t $secondName (${binding.tvScore2.text})",
                    getString(R.string.only_tabs_2), date)
                viewModel.insertUserInfo(user)
            }

            var gamePlayed = sharedPreferences.getInt("NGameONLY2", 0)
            var totalPoint = sharedPreferences.getInt("ONLY2POINT", 0)

            gamePlayed += 1
            totalPoint += binding.tvScore.text.toString().toInt()

            with (sharedPreferences.edit()) {
                putInt("NGameONLY2", gamePlayed)
                putInt("ONLY2POINT", totalPoint)
                apply()
            }

            showDialogFine()
        }
    }

    private fun showDialogExit() {  // popup exit
        val dialog = Dialog(this)
        if (bindingExit.root.parent != null) {
            (bindingExit.root.parent as ViewGroup).removeView(bindingExit.root)
        }
        dialog.setContentView(bindingExit.root)

        bindingExit.popupBtnYes.setOnClickListener {
            finish()
            dialog.dismiss()
            soundTap()
        }
        bindingExit.popupBtnNo.setOnClickListener {
            dialog.dismiss()
            soundTap()
        }
        dialog.show()
    }

    private fun showDialogFine() {  // popup end game
        val dialog = Dialog(this)

        if (bindingWin.root.parent != null) {
            (bindingWin.root.parent as ViewGroup).removeView(bindingWin.root)
        }
        dialog.setContentView(bindingWin.root)
        dialog.setCanceledOnTouchOutside(false)

        if(winner() == getString(R.string.draw)){   //if Draw
            bindingWin.tvWinner.text = getString(R.string.draw)
            bindingWin.tvScoreWinner.text = binding.tvScore.text
            bindingWin.tvSecond.isInvisible = true
            bindingWin.tvScoreSecond.isInvisible = true
            bindingWin.imageView3.isInvisible = true
        } else {
            bindingWin.tvWinner.text = (winner())
            bindingWin.imageView3.isInvisible = false

            if(winner() == binding.textScore.text){ //if win first player
                bindingWin.tvScoreWinner.text = binding.tvScore.text
                bindingWin.tvSecond.text = binding.textScore2.text

                bindingWin.tvScoreSecond.text = binding.tvScore2.text

            } else { //if win second player
                bindingWin.tvScoreWinner.text = binding.tvScore2.text
                bindingWin.tvScoreSecond.text = binding.tvScore.text

                bindingWin.tvSecond.text = binding.textScore.text
            }
        }

        bindingWin.popupBtnYes.setOnClickListener {
            resetName()
            finish()
            dialog.dismiss()
            soundTap()
        }

        bindingWin.btnReplay.setOnClickListener{
            revenge()
            dialog.dismiss()
            soundTap()
        }
        dialog.show()
    }

    private fun winner(): String {  // select winner player
        val winner : String
        val score1 = binding.tvScore.text.toString().toInt()
        val score2 = binding.tvScore2.text.toString().toInt()

        winner = when {
            score1 > score2 -> {
                binding.textScore.text as String
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

    private fun revenge() { //Restart the game keeping players' names
        binding.tvFinishGame.isVisible = false

        val spinner = listOf(binding.sp1,binding.sp2,binding.sp3,binding.sp4,binding.sp5,binding.sp6,
            binding.spFull,binding.spLow,binding.spBig,binding.spYah)
        val textView = listOf(binding.ed3x, binding.ed4x, binding.edChance)

        // List spinner and textView for the second player
        val spinner2 = listOf(binding.sp12,binding.sp22,binding.sp32,binding.sp42,binding.sp52,binding.sp62,
            binding.spFull2,binding.spLow2,binding.spBig2,binding.spYah2)
        val textView2 = listOf(binding.ed3x2, binding.ed4x2, binding.edChance2)

        binding.tvPointBonus.textSize = 24F
        binding.tvPointBonus2.textSize = 24F
        binding.tvPointBonus.text = "0"
        binding.tvPointBonus2.text = "0"
        binding.tvScore.text = "0"
        binding.tvScore2.text = "0"

        for(tv in textView){
            tv.setText("")
            tv.hint= ""
        }
        for(tv in textView2){
            tv.setText("")
            tv.hint= ""
        }
        for(sp in spinner){
            sp.setSelection(0)
        }
        for(sp in spinner2){
            sp.setSelection(0)
        }
        game()
    }

    private fun resetName(){ //Reset the name every time multiplayer button is clicked
        val sharedPreferences: SharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val defaultName = sharedPreferences.getString("NameDevice", "Player 1")
        with (sharedPreferences.edit()) {
            putString("FirstName", defaultName)
            putString("SecondName", "Player 2")
            apply()
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

    override fun onBackPressed() {  // pressed button back
        showDialogExit()
    }
}