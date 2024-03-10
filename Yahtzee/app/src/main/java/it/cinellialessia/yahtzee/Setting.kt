package it.cinellialessia.yahtzee

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import it.cinellialessia.yahtzee.databinding.SettingBinding

class Setting: AppCompatActivity(){

    private val sharedPrefFile = "PreferenceKotlin"
    private lateinit var binding : SettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = this.getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
        val defaultName = sharedPreferences.getString("NameDevice", "")

        if(defaultName != "" && defaultName != "Player 1"){
            binding.tvPlayer1.hint = "$defaultName"
        }

        selectDice(sharedPreferences)
        selectName(sharedPreferences)
        soundEffect(sharedPreferences)

        //Button Exit
        binding.button.setOnClickListener {
            finish()
            soundTap()
        }
    }

    private fun selectDice(sharedPreferences : SharedPreferences){
        var defaultDice = sharedPreferences.getInt("DefaultDice", -1)
        var secondDice = sharedPreferences.getInt("SecondDice", -1)
        val myColor = listOf(binding.white,binding.green, binding.pink, binding.red, binding.blue)
        val myColor2 = listOf(binding.white2,binding.green2, binding.pink2, binding.red2, binding.blue2)

        for (color in myColor){ // Select dice for Player 1
            if(defaultDice == color.contentDescription.toString().toInt()){ //The dice selected have a frame
                rectDice(myColor,color)
            }
            color.setOnClickListener{
                defaultDice = color.contentDescription.toString().toInt() //Save dice selected
                rectDice(myColor, it)
                soundTap()
                with (sharedPreferences.edit()) {
                    putInt("DefaultDice", defaultDice)
                    apply()
                }
            }
        }
        for (color in myColor2){ //Select dice for Player 2
            if(secondDice == color.contentDescription.toString().toInt()){ //The dice selected have a frame
                rectDice(myColor2,color)
            }
            color.setOnClickListener{
                secondDice = color.contentDescription.toString().toInt()
                rectDice(myColor2, it)
                soundTap()
                with (sharedPreferences.edit()) {
                    putInt("SecondDice", secondDice)
                    apply()
                }
            }
        }
    }

    private fun rectDice(myColor: List<ImageView>, selectDice: View){
        for(color in myColor){
            color.setBackgroundResource(0) //Remove in all image the rectangle frame
            color.setPadding(0,0,0,0)
        }
        selectDice.setBackgroundResource(R.drawable.rectangle_dice)
    }

    private fun selectName(sharedPreferences: SharedPreferences){
        binding.tvPlayer1.doAfterTextChanged {
            val defaultName = it.toString()
            with (sharedPreferences.edit()) {
                putString("NameDevice", defaultName)
                apply()
            }
            if(it.toString().isEmpty()){
                with (sharedPreferences.edit()) {
                    putString("NameDevice", "Player 1")
                    apply()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
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
                val mp = MediaPlayer.create(this, R.raw.tap)
                if (mp.isPlaying) {
                    mp.reset()
                }
                mp.start()
            }
            with (sharedPreferences.edit()) {
                putInt("Sound", sound)
                apply()
            }
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

