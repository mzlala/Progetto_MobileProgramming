package it.cinellialessia.yahtzee

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import it.cinellialessia.yahtzee.databinding.AboutUsBinding
import it.cinellialessia.yahtzee.databinding.ActivityMainBinding
import it.cinellialessia.yahtzee.databinding.ActivityRulesBinding
import it.cinellialessia.yahtzee.db.MainActivityDataB

class MainActivity : AppCompatActivity(), View.OnClickListener{

    private lateinit var binding : ActivityMainBinding
    private lateinit var bindingRules : ActivityRulesBinding
    private lateinit var bindingAbout : AboutUsBinding
    private val sharedPrefFile = "PreferenceKotlin"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        bindingRules = ActivityRulesBinding.inflate(layoutInflater)
        bindingAbout = AboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Intent
        binding.modSingle.setOnClickListener(this)
        binding.onlyCards.setOnClickListener(this)
        binding.modMultiLocal.setOnClickListener(this)
        binding.btnDataBase.setOnClickListener(this)
        binding.modIA.setOnClickListener(this)
        binding.OneVsOneOnline.setOnClickListener(this)
        binding.btnSettings.setOnClickListener(this)
        binding.onlyCards.setOnClickListener(this)
        binding.onlyCards2.setOnClickListener(this)

        //Dialog
        binding.btnRules.setOnClickListener{
            showDialogRules()
            sound()
        }
        binding.aboutUs.setOnClickListener {
            showDialogAboutUs()
            sound()
        }
    }

    override fun onClick(view: View?) { //starts the intents and plays the sound after the click
        if(view != null){
            when(view.id){
                binding.modSingle.id -> intent = Intent(this, SinglePlayer::class.java)
                binding.modMultiLocal.id -> intent = Intent(this, MultiPlayer::class.java)
                binding.btnDataBase.id -> intent = Intent(this, MainActivityDataB::class.java)
                binding.modIA.id -> intent = Intent(this, IAActivity::class.java)
                binding.OneVsOneOnline.id -> intent = Intent(this, Bluetooth::class.java)
                binding.btnSettings.id -> intent = Intent(this, Setting::class.java)
                binding.onlyCards.id -> intent = Intent(this, OnlyCards::class.java)
                binding.onlyCards2.id -> intent = Intent(this, OnlyCards2::class.java)
            }
            sound()
            startActivity(intent)
        }
    }

    private fun sound(){ //if the sounds are enabled they are reproduced
        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        if(sharedPreferences.getInt("Sound", 0) == 0){
            val mp = MediaPlayer.create(this, R.raw.tap)
            if (mp.isPlaying) {
                mp.reset()
            }
            mp.start()
        }
    }

    private fun showDialogAboutUs() {
        val dialog = Dialog(this)
        if (bindingAbout.root.parent != null) {
            (bindingAbout.root.parent as ViewGroup).removeView(bindingAbout.root)
        }
        dialog.setContentView(bindingAbout.root)
        dialog.show()
        dialog.setOnCancelListener {
            dialog.dismiss()
        }
        setFinishOnTouchOutside(true)
    }

    private fun showDialogRules() {
        val dialog = Dialog(this)
        if (bindingRules.root.parent != null) {
            (bindingRules.root.parent as ViewGroup).removeView(bindingRules.root)
        }
        dialog.setContentView(bindingRules.root)
        dialog.show()
        dialog.setOnCancelListener {
            dialog.dismiss()
        }
        setFinishOnTouchOutside(true)
    }
}