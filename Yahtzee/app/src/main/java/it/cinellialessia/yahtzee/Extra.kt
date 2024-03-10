package it.cinellialessia.yahtzee

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import java.util.stream.IntStream
import kotlin.random.Random

class Extra {

    //Get the color of dice from setting and save the drawable in listDice and set the image
    fun colorDiceDefault(dice : List<ImageView>, diceSelected : Int): List<Int> {
        var listDice = listOf<Int>()

        when(diceSelected){
            0 -> listDice = listOf(R.drawable.white1,R.drawable.white2,R.drawable.white3,
                R.drawable.white4,R.drawable.white5,R.drawable.white6)
            1 -> listDice = listOf(R.drawable.green1,R.drawable.green2,R.drawable.green3,
                R.drawable.green4,R.drawable.green5,R.drawable.green6)
            2 -> listDice = listOf(R.drawable.pink1,R.drawable.pink2,R.drawable.pink3,
                R.drawable.pink4,R.drawable.pink5,R.drawable.pink6)
            3 -> listDice = listOf(R.drawable.red1,R.drawable.red2,R.drawable.red3,
                R.drawable.red4,R.drawable.red5,R.drawable.red6)
            4 -> listDice = listOf(R.drawable.blue1,R.drawable.blue2,R.drawable.blue3,
                R.drawable.blue4,R.drawable.blue5,R.drawable.blue6)
        }

        var i = 0
        for(d in dice){
            d.setImageResource(listDice[i])
            i += 1
        }

        return listDice
    }

    //the textView are not clickable
    fun lockTvPoint(tvPoint : List<TextView>){
        for(tv in tvPoint){
            tv.isEnabled = false
        }
    }

    //the textView are clickable
    fun unlockTvPoint(tvPoint : List<TextView>, playedDices : List<Boolean>){
        for(i in 0..12){
            if(!playedDices[i]){
                tvPoint[i].isEnabled = true
            }
        }
    }

    //Restart the dice for every play
    fun resetDice(dice: List<ImageView>,arrayLocked: ArrayList<Boolean>){
        for(d in dice){
            d.isEnabled = false
            setUnlockedColor(d) //removes the color filter
        }
        for (i in 0..4){
            arrayLocked[i] = false //unlock all dice
        }
    }

    fun setLockedColor(v: ImageView) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f) //0 means grayscale
        val cf = ColorMatrixColorFilter(matrix)
        v.colorFilter = cf
        v.imageAlpha = 712 // 128 = 0.5
    }

    fun setUnlockedColor(v: ImageView) {
        v.colorFilter = null
        v.imageAlpha = 255
    }

    //After the click of TextView, set the hint whit the current score
    fun pointVisibility(ind : TextView?, tvPoint: List<TextView>, arrayList: ArrayList<Int>){
        deletePointVisibility(tvPoint)
        ind!!.hint = CountScore().point(ind.contentDescription.toString().toInt(),arrayList).toString()
    }

    //Delete everyone hint
    fun deletePointVisibility(tvPoint: List<TextView>){
        for(tv in tvPoint){
            tv.hint = ""
        }
    }

    fun randomDice(listDice: List<Int>, arrayLocked: ArrayList<Boolean>, dice: List<ImageView>, arrayList: ArrayList<Int>, launch: Int){
        for(i in IntStream.range(0, 5)){ //Return 5 dice, if then are unlocked
            if(!arrayLocked[i]){
                arrayList[i] = Random.nextInt(1,7)
            }
        }

        if(launch != 3){
            for((i, d) in dice.withIndex()){ //Animation scaleDown and rotation
                if(!arrayLocked[i]){ //Control lock's dice
                    d.animate().scaleX(0f).scaleY(0f).setDuration(300).start()
                    d.animate().rotation(-300f).setDuration(300).start()
                }
            }
            Handler(Looper.getMainLooper()).postDelayed({ //After 300 millis to first animation
                for ((h, d) in dice.withIndex()) {
                    when (arrayList[h]) {
                        1 -> d.setImageResource(listDice[0])
                        2 -> d.setImageResource(listDice[1])
                        3 -> d.setImageResource(listDice[2])
                        4 -> d.setImageResource(listDice[3])
                        5 -> d.setImageResource(listDice[4])
                        6 -> d.setImageResource(listDice[5])
                    }
                    if(!arrayLocked[h]){ //Animation scaleUp and rotation
                        d.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
                        d.animate().rotation(0f).setDuration(300).start()
                    }
                }
            }, 300)
        } else { //Change animation for the first launch
            for ((h, d) in dice.withIndex()) {
                when (arrayList[h]) {
                    1 -> d.setImageResource(listDice[0])
                    2 -> d.setImageResource(listDice[1])
                    3 -> d.setImageResource(listDice[2])
                    4 -> d.setImageResource(listDice[3])
                    5 -> d.setImageResource(listDice[4])
                    6 -> d.setImageResource(listDice[5])
                }
                d.scaleX = 0f
                d.scaleY = 0f
                d.rotation = -300f

                d.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
                d.animate().rotation(0f).setDuration(300).start()
            }
        }
    }

    fun animationDown(dice: List<ImageView>){ //when play is pressed
        for(d in dice) {
            d.animate().scaleX(0F).scaleY(0f).setDuration(300).start()
            d.animate().rotation(-300f).setDuration(300).start()
        }
    }

    fun animationDown(dice: List<ImageView>, arrayList: ArrayList<Int>, listDice: List<Int>){ //when play is pressed for multiplayer
        for ((h, d) in dice.withIndex()) {

            when (arrayList[h]) {
                1 -> d.setImageResource(listDice[0])
                2 -> d.setImageResource(listDice[1])
                3 -> d.setImageResource(listDice[2])
                4 -> d.setImageResource(listDice[3])
                5 -> d.setImageResource(listDice[4])
                6 -> d.setImageResource(listDice[5])
            }
            d.animate().scaleX(0F).scaleY(0f).setDuration(300).start()
            d.animate().rotation(-300f).setDuration(300).start()
        }
    }

    //Return 1 if at least one dice has not been locked, else 0
    fun controlDice(arrayLocked: ArrayList<Boolean>): Int {
        for (i in 0..4){
            if(!arrayLocked[i]){
                return 1
            }
        }
        return 0
    }

    fun controlColumn(tvPoint: List<TextView>): Boolean { //Control if the first column is completed and return true
        for(tv in 0..5){
            if(tvPoint[tv].text == ""){
                return false
            }
        }
        return true
    }
}