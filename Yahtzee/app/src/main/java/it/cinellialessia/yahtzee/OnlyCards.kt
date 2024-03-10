package it.cinellialessia.yahtzee

import android.app.Dialog
import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import it.cinellialessia.yahtzee.databinding.*
import it.cinellialessia.yahtzee.db.MainActivityDataB
import it.cinellialessia.yahtzee.db.MainActivityViewModel
import it.cinellialessia.yahtzee.db.User

class OnlyCards : AppCompatActivity() {

    private lateinit var bindingWin : PopupEndSpBinding
    private lateinit var bindingExit : PopupExitBinding
    private lateinit var binding : OnlyCardsBinding
    private val sharedPrefFile = "PreferenceKotlin"
    private lateinit var viewModel: MainActivityViewModel

    private var score = intArrayOf(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1) // array of value of boxes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OnlyCardsBinding.inflate(layoutInflater)
        bindingWin = PopupEndSpBinding.inflate(layoutInflater)
        bindingExit = PopupExitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        binding.tvPointBonus.text = getString(R.string.set_bonus)
        binding.tvPointBonus.textSize = 14F
        binding.tvFinishGame.isVisible = false
        binding.imageBack.setOnClickListener {
            showDialogExit()
            soundTap()
        }
        game()
    }

    private fun game() {
        //Value of the spinners
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

        val value = listOf(value1,value2,value3,value4,value5,value6,valueFull,valueLow,valueBig,valueYah) // list of vectors of spinner's values

        // List spinner and textView for the first player
        val spinner = listOf(binding.sp1,binding.sp2,binding.sp3,binding.sp4,binding.sp5,binding.sp6,
            binding.spFull,binding.spLow,binding.spBig,binding.spYah) // list of spinner's binding
        val textView = listOf(binding.ed3x, binding.ed4x, binding.edChance)  // list of text view's binding

        ///////////////////////////////////////////////////////////////////////////

        for((i, sp) in spinner.withIndex()){
            sp.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, value[i])
        }

        for(op in spinner){  //select spinner
            op.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                var ind = 0
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    ind = op.contentDescription.toString().toInt()
                    if(position != 0){
                        val count = value[ind][position]
                        score[ind] = count.toInt() //Set the current score in "score"
                    } else {
                        score[ind] = -1
                    }
                    countScore()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        for(tv in textView){ // select text view
            tv.doAfterTextChanged {
                var i = 0
                when(tv){
                    binding.ed3x -> i = 10
                    binding.ed4x -> i = 11
                    binding.edChance -> i = 12
                }

                when {
                    it.toString().isEmpty() -> {   //control null value
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
    }

    private fun countScore(){  // calculation total score
        var countScore = 0

        for(i in 0..12){
            if (score[i] != -1)
                countScore += score[i]
        }
        binding.tvScore.text = countScore.toString() // change score's text view
        gameFinish()
        checkBonus(countScore)
    }

    private fun checkBonus(countScore: Int){  //  check condition for bonus
        var count = 0

        for(i in 0..5){  // sum score of box1...box6
            if(score[i] != -1)
                count += score[i]
        }

        binding.tvPointBonus.text = ("$count/63")

        if (count >= 63){   // bonus condition check
            binding.tvScore.text = (countScore + 35).toString()
            binding.tvPointBonus.textSize = 24F
            binding.tvPointBonus.text = getString(R.string._35)
        } else if(controlColumn()){
            binding.tvScore.text = countScore.toString()
            binding.tvPointBonus.textSize = 24F
            binding.tvPointBonus.text = getString(R.string._0)
        }
    }

    private fun controlColumn(): Boolean { //Control the first column if is empty or not
        for(tv in 0..5){
            if(score[tv] == -1){
                return false
            }
        }
        return true
    }

    private fun gameFinish() {  // control end game
        for(i in 0..12){
            if(score[i] == -1){
                return
            }
        }
        binding.tvFinishGame.isVisible = true
        binding.tvFinishGame.setOnClickListener {
            val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
            val defaultName = sharedPreferences.getString("NameDevice", "Player 1")

            //Add in database
            val date = MainActivityDataB().date()
            val user = User(0, "${getString(R.string.score)} ${binding.tvScore.text}", "\t${defaultName}",
                getString(R.string.only_tabs), date)
            viewModel.insertUserInfo(user)
            showDialogFine()

            var gamePlayed = sharedPreferences.getInt("NGameONLY", 0)
            var totalPoint = sharedPreferences.getInt("ONLYPoint", 0)

            gamePlayed += 1
            totalPoint += binding.tvScore.text.toString().toInt()  // refresh totalScore

            with (sharedPreferences.edit()) {
                putInt("NGameONLY", gamePlayed)
                putInt("ONLYPoint", totalPoint)
                apply()
            }
        }
    }

    private fun showDialogExit() {  // show pop up exit
        val dialog = Dialog(this)
        if (bindingExit.root.parent != null) {
            (bindingExit.root.parent as ViewGroup).removeView(bindingExit.root)
        }
        dialog.setContentView(bindingExit.root)

        bindingExit.popupBtnYes.setOnClickListener {
            dialog.dismiss()
            finish()
            soundTap()
        }
        bindingExit.popupBtnNo.setOnClickListener {
            dialog.dismiss()
            soundTap()
        }
        dialog.show()
    }

    private fun showDialogFine() { // show pop up end game
        val dialog = Dialog(this)

        if (bindingWin.root.parent != null) {
            (bindingWin.root.parent as ViewGroup).removeView(bindingWin.root)
        }
        dialog.setContentView(bindingWin.root)
        dialog.setCanceledOnTouchOutside(false)

        bindingWin.tvScoreWinner.text = ("${binding.tvScore.text}")

        bindingWin.popupBtnYes.setOnClickListener {
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

    private fun revenge(){ // restart the game
        binding.tvFinishGame.isVisible = false

        score = intArrayOf(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1)
        binding.tvPointBonus.text = getString(R.string.set_bonus)  // textView bonus
        binding.tvPointBonus.textSize = 14F

        val spinner = listOf(binding.sp1,binding.sp2,binding.sp3,binding.sp4,binding.sp5,binding.sp6,
            binding.spFull,binding.spLow,binding.spBig,binding.spYah)
        val textView = listOf(binding.ed3x, binding.ed4x, binding.edChance)

        for(op in spinner){
            op.setSelection(0)
        }
        for(tv in textView){
            tv.text.clear()
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