package it.cinellialessia.yahtzee

import android.app.Activity
import android.app.Dialog
import android.bluetooth.*
import android.content.*
import android.graphics.*
import android.media.MediaPlayer
import android.os.*
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.lifecycle.ViewModelProvider
import it.cinellialessia.yahtzee.databinding.*
import it.cinellialessia.yahtzee.db.*
import java.io.*
import java.util.*
import kotlin.random.Random

class Bluetooth : AppCompatActivity() {
    private lateinit var binding: Player2Binding
    private lateinit var bindingBl : PopupBluetoothBinding
    private lateinit var bindingData: DatabaseBinding
    private lateinit var bindingItem : ItemDbBinding
    private lateinit var bindingWin : PopupWinnerBinding
    private lateinit var bindingExit: PopupExitBinding
    private lateinit var bindingSetting: SettingBinding
    private val sharedPrefFile = "PreferenceKotlin"
    private lateinit var viewModel: MainActivityViewModel
    //we use two arrays whose elements correspond to the boxes, and they take value true if they have been played
    private var playedDice = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
    private var playedDice2 = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
    private var arrayLocked = arrayListOf(false,false,false,false,false) //true if the corresponding dice is locked
    private var arrayList = arrayListOf(0,0,0,0,0) //the dice of the current match
    private var launch : Int = 0
    private var ind : TextView? = null

    lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var btArray: Array<BluetoothDevice?>
    var sendReceive: Bluetooth.SendReceive? = null

    companion object {
        const val STATE_LISTENING = 1
        const val STATE_CONNECTING = 2
        const val STATE_CONNECTED = 3
        const val STATE_CONNECTION_FAILED = 4
        const val STATE_MESSAGE_RECEIVED = 5
        private const val APP_NAME = "Yahtzee"
        private val MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Player2Binding.inflate(layoutInflater)
        bindingBl = PopupBluetoothBinding.inflate(layoutInflater)
        bindingWin = PopupWinnerBinding.inflate(layoutInflater)
        bindingExit = PopupExitBinding.inflate(layoutInflater)
        bindingSetting = SettingBinding.inflate(layoutInflater)
        bindingData = DatabaseBinding.inflate(layoutInflater)
        bindingItem = ItemDbBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showDialogConnected()

        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        //Set screen changes
        binding.tvPointBonus.text = getString(R.string.set_bonus)
        binding.tvPointBonus2.text = getString(R.string.set_bonus)
        binding.tvPointBonus.textSize = 12F
        binding.tvPointBonus2.textSize = 12F
        binding.play.setBackgroundColor(Color.LTGRAY)
        binding.play.isEnabled = false
        binding.launch.setBackgroundColor(getColor(R.color.button))

        for (d in abbreviationDice()){
            d.isVisible = false
        }

        Extra().resetDice(abbreviationDice(),arrayLocked)  //Unlock All Dice and make them invisible
        Extra().lockTvPoint(abbreviation())  //Lock all text view

        val diceSelected = sharedPreferences.getInt("DefaultDice", 0) //Default Dice color = White
        val listDice = Extra().colorDiceDefault(abbreviationDice(), diceSelected) // Set the selected Dice color
        soundEffect(sharedPreferences) //Set the sound effects -> On is default
        threeLaunch(listDice) //Start the game

        binding.imageBack.setOnClickListener {
            showDialogExit()
            playSounds("tap")
        }
    }

    private fun getName() { //Get the name saved and send it to the opponent's name
        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val tvFirstPlayer = binding.textScore1
        val defaultName = sharedPreferences.getString("NameDevice", "You")
        tvFirstPlayer.text = defaultName

        sendReceive?.write("2${tvFirstPlayer.text}".toByteArray()) //"2" is a code for looking up the name in the buffer
    }

    private fun showDialogConnected(){
        val dialog = Dialog(this)
        if (bindingBl.root.parent != null) {
            (bindingBl.root.parent as ViewGroup).removeView(bindingBl.root)
        }
        dialog.setContentView(bindingBl.root)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        //Show popup to able the bluetooth on the device or kill activity
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) { //Show a popup for enable the bluetooth if are unable
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if(result.resultCode == Activity.RESULT_CANCELED){ //If the user not enable the bluetooth the activity finish
                    dialog.dismiss()
                    finish()
                }
            }
            startForResult.launch(Intent(enableIntent))
        }

        bindingBl.btnOk.setOnClickListener{
            if(bindingBl.status.text == getString(R.string.connected)){
                getName()
                dialog.dismiss()
                playSounds("tap")
            }
        }

        dialog.setOnCancelListener { //stops the activity if the dialog is closed and the devices have not connected
            finish()
            dialog.dismiss()
        }

        implementListeners() //The work of the dialog
    }

    private fun implementListeners() {
        bindingBl.listDevices.setOnClickListener {
            bindingBl.textList.isVisible = false
            bindingBl.listview.isVisible = true
            if(bindingBl.status.text != getString(R.string.connected)){
                val bt = bluetoothAdapter.bondedDevices //Create a list of bonded devices
                val list = arrayOfNulls<String>(bt.size) //List with the names of the bonded devices
                btArray = arrayOfNulls(bt.size)
                var index = 0
                if (bt.size > 0) { //Set the listView
                    for (device in bt) {
                        btArray[index] = device
                        list[index] = device.name
                        index++
                    }
                    val arrayAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, list)
                    bindingBl.listview.adapter = arrayAdapter
                }
            }
            playSounds("tap")
        }
        bindingBl.listen.setOnClickListener { //Create the ServerClass if Host is pressed
            if(bindingBl.status.text != getString(R.string.connected)){
                val serverClass: Bluetooth.ServerClass = ServerClass()
                serverClass.start()
            }
            playSounds("tap")
        }
        bindingBl.listview.onItemClickListener =
            OnItemClickListener { _, _, i, _ ->
                if(bindingBl.status.text != getString(R.string.connected)){ //Create the ClientClass if listView is pressed
                    btArray[i]?.let { ClientClass(it) }?.start()
                    bindingBl.status.text = getString(R.string.connecting)
                    playSounds("tap")
                }
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

    private fun abbreviation2(): List<TextView>{
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

    private fun lockDice(): ArrayList<Boolean> { //lock and unlock the dice if they are clicked
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

    private fun threeLaunch(listDice: List<Int>){
        launch = 3
        binding.launch.text = (getString(R.string.launch)+ "       " + launch.toString())
        binding.launch.setOnClickListener { //when launch is pressed
            if(Extra().controlDice(arrayLocked) == 1){ //if all dice are locked, do not make another launch
                playSounds("roll")

                Extra().deletePointVisibility(abbreviation()) //delete all the suggestion of point for the column of the first player
                binding.play.setBackgroundColor(Color.LTGRAY)
                binding.play.isEnabled = false
                if(launch == 3){
                    Extra().unlockTvPoint(abbreviation(),playedDice) //allow the use of point' suggestion for the first player
                } else if(launch == 1){
                    binding.launch.setBackgroundColor(Color.LTGRAY)
                    binding.launch.isEnabled = false
                }

                Extra().randomDice(listDice,lockDice(),abbreviationDice(),arrayList, launch) //launch and animation of dice

                launch -= 1
                binding.launch.text = (getString(R.string.launch)+ "       " + launch.toString())
            }
        }
        lookForPoint() //setOnclick of all tv point, shows the scores made in that box
    }

    private fun lookForPoint(): TextView? {
        ind = null

        for(tv in abbreviation()){
            tv.setOnClickListener {
                playSounds("tap")
                binding.play.isEnabled = true
                binding.play.setBackgroundColor(getColor(R.color.play))
                ind = tv
                Extra().pointVisibility(ind,abbreviation(),arrayList)  //show the current point in the pressed textview
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

    private fun playPressed(ind: TextView) {
        if(!playedDice[ind.contentDescription.toString().toInt() - 1]){ //if the box is not already played -> i can play it
            playedDice[ind.contentDescription.toString().toInt() - 1] = true

            ind.isEnabled = false //Lock the current played textView for the rest of the match
            ind.setTextColor(Color.DKGRAY)
            ind.text = CountScore().point(ind.contentDescription.toString().toInt(),arrayList).toString() // set the scored point in the textView
            ind.setBackgroundResource(R.drawable.rectangle_full_white)

            val point = CountScore().point(ind.contentDescription.toString().toInt(),arrayList).toString()
            var playedBox = ind.contentDescription.toString()

            if(playedBox.length == 1){ //To send a two-character code
                playedBox = "0$playedBox"
            }

            val string = playedBox + point
            sendReceive?.write(string.toByteArray())

            binding.play.setBackgroundColor(Color.LTGRAY)
            launch = 3
            binding.launch.setBackgroundColor(getColor(R.color.button))
            binding.launch.isEnabled = true
            binding.launch.text = (getString(R.string.launch)+ "       " + launch.toString())

            if(ind.contentDescription.toString().toInt() in 1..6){ //control on the left column of pointView for the bonus point
                pointBonus(1)
            }
            Extra().resetDice(abbreviationDice(),arrayLocked)  // unlock dice
            Extra().lockTvPoint(abbreviation())

            updateScore(point.toInt(), 1)
            gameFinish() //control if the game is finish
        }
    }

    private fun pointBonus(turn : Int){
        var count = 0
        if(turn == 1){ //For first Player
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
        } else { //For second Player
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

    private fun soundEffect(sharedPreferences: SharedPreferences){
        var sound = sharedPreferences.getInt("Sound", 0) //0 -> On
        if(sound == 0){ //Set icon of sounds
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

    private fun gameFinish(){
        for(play in playedDice){ //Check if I wrote in all the boxes
            if(!play){
                return
            }
        }
        //If the match is over for this device
        binding.launch.isEnabled = false
        binding.launch.text = (getString(R.string.launch)+ "       " + "0")
        binding.launch.setBackgroundColor(Color.LTGRAY)
        binding.play.isEnabled = false
        binding.play.setBackgroundColor(Color.LTGRAY)

        for(play in playedDice2){ //check if the opponent has finished the game
            if(!play){
                return
            }
        }
        //Add the game in the database
        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val defaultName = sharedPreferences.getString("NameDevice", "You")
        val secondName = binding.textScore2.text.toString()
        val date = MainActivityDataB().date()
        val winner = winner()

        //Add in database
        if(winner == "Draw!"){
            val user = User(0, "\t${winner}", "\t${defaultName} (${binding.tvScore.text}) \tVS\t $secondName (${binding.tvScore2.text})",
                getString(R.string.bluetooth_match), date)
            viewModel.insertUserInfo(user)
        } else {
            val user = User(0, "${getString(R.string.winner)}\t${ winner}", "\t${defaultName} (${binding.tvScore.text}) \tVS\t $secondName (${binding.tvScore2.text})",
                getString(R.string.bluetooth_match), date)
            viewModel.insertUserInfo(user)
        }
        //Set values for the chart
        var gamePlayed = sharedPreferences.getInt("NGameBLUE", 0)
        var totalPoint = sharedPreferences.getInt("BLUEPOINT", 0)

        gamePlayed += 1
        totalPoint += binding.tvScore.text.toString().toInt()

        with (sharedPreferences.edit()) {
            putInt("NGameBLUE", gamePlayed)
            putInt("BLUEPOINT", totalPoint)
            apply()
        }
        showEndMatchDialog()
    }

    private fun winner(): String { //Return the winner's name
        val score1 = binding.tvScore.text.toString().toInt()
        val score2 = binding.tvScore2.text.toString().toInt()

        return when {
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
    }

    override fun onBackPressed() {
        showDialogExit()
    }

    var handler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            STATE_LISTENING -> bindingBl.status.text = getString(R.string.listening)
            STATE_CONNECTING -> bindingBl.status.text = getString(R.string.connecting)
            STATE_CONNECTION_FAILED -> bindingBl.status.text = getString(R.string.connection_failed)
            STATE_CONNECTED -> {
                bindingBl.status.text = getString(R.string.connected)
                bindingBl.status.setTextColor(Color.GREEN)
                bindingBl.status.textSize = 16F
                bindingBl.btnOk.isVisible = true
            }

            STATE_MESSAGE_RECEIVED -> { //When I got the message
                val readBuff = msg.obj as ByteArray
                val tempMsg = String(readBuff, 0, msg.arg1)

                if(tempMsg[0].toString() == "2"){ //Name of seconds player
                    if(tempMsg == "2You"){
                        binding.textScore2.text = getString(R.string.player_2)
                    } else {
                        binding.textScore2.text = tempMsg.substring(1,tempMsg.length)
                    }
                } else if(tempMsg == "Exit"){ //If one player exit to the game
                    showDialogDisconnected()
                } else {
                    pointReceived(tempMsg)
                    gameFinish()
                }
            }
        }
        true
    }

    private fun showDialogDisconnected() {
        Toast.makeText(this,getString(R.string.disconnected),Toast.LENGTH_SHORT).show()
        try {
            ServerClass().serverClose()
        } catch (e: IOException) {
            // failed
        }
        finish()
    }



    private fun showDialogExit() {
        val dialog = Dialog(this)

        if (bindingExit.root.parent != null) {
            (bindingExit.root.parent as ViewGroup).removeView(bindingExit.root)
        }
        dialog.setContentView(bindingExit.root)

        bindingExit.popupBtnYes.setOnClickListener {
            sendReceive?.write("Exit".toByteArray()) //Send the message "exit" to inform the other device of the cancellation
            try {
                ServerClass().serverClose() //Closes the server if one of the participants leaves
            } catch (e: IOException) {
                // failed
            }
            dialog.dismiss()
            finish()
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

        if(winner() == getString(R.string.draw)){ //If we have draw
            bindingWin.tvWinner.text = getString(R.string.draw)
            bindingWin.tvScoreWinner.text = binding.tvScore.text
            bindingWin.tvSecond.isInvisible = true
            bindingWin.tvScoreSecond.isInvisible = true
            bindingWin.imageView3.isInvisible = true

        } else { // set the popup information about the winner
            bindingWin.tvWinner.text = (winner())
            bindingWin.tvSecond.isInvisible = false
            bindingWin.tvScoreSecond.isInvisible = false
            bindingWin.imageView3.isInvisible = false

            if(winner() == binding.textScore1.text){ //If the winner is the first player
                bindingWin.tvScoreWinner.text = binding.tvScore.text
                bindingWin.tvScoreSecond.text = binding.tvScore2.text

                bindingWin.tvSecond.text = binding.textScore2.text

            } else { //If the winner is the second player
                bindingWin.tvScoreWinner.text = binding.tvScore2.text
                bindingWin.tvScoreSecond.text = binding.tvScore.text

                bindingWin.tvSecond.text = binding.textScore1.text
            }
        }

        bindingWin.popupBtnYes.setOnClickListener {
            try {
                ServerClass().serverClose()
            } catch (e: IOException) {
                // failed
            }
            dialog.dismiss()
            finish()
            playSounds("tap")
        }
        bindingWin.btnReplay.setOnClickListener {
            dialog.dismiss()
            replay()
            playSounds("tap")
        }
        dialog.show()
    }

    private fun pointReceived(tempMsg: String) { //tempMsg = "$$$$" 4 digits, teh first two refer for the box played, the other two for the score
        val playedBox = if(tempMsg.subSequence(0,1) == "0"){ //Codes to distinguish a sent messages
            tempMsg.subSequence(1,2)
        } else {
            tempMsg.subSequence(0,2)
        }

        val point = tempMsg.subSequence(2,tempMsg.length)
        val tvPoint2 = abbreviation2()

        for(tv in tvPoint2){
            if(tv.contentDescription == playedBox){
                tv.text = point
                playedDice2[tv.contentDescription.toString().toInt() - 1] = true
                if(tv.contentDescription.toString().toInt() <= 6){
                    pointBonus(2)
                }
            }
        }
        updateScore(point.toString().toInt(),2)
    }

    private fun replay(){ //Reset the information for a new game
        playedDice = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
        playedDice2 = arrayListOf(false,false,false,false,false,false,false,false,false,false,false,false,false)
        arrayLocked = arrayListOf(false,false,false,false,false)
        arrayList = arrayListOf(0,0,0,0,0)

        binding.tvPointBonus.text = getString(R.string.set_bonus)
        binding.tvPointBonus2.text = getString(R.string.set_bonus)
        binding.tvPointBonus.textSize = 12F
        binding.tvPointBonus2.textSize = 12F
        binding.play.setBackgroundColor(Color.LTGRAY)
        binding.play.isEnabled = false
        binding.launch.isEnabled = true
        binding.launch.setBackgroundColor(getColor(R.color.button))
        binding.tvScore.text = "0"
        binding.tvScore2.text = "0"
        binding.tvPointBonus.setBackgroundResource(0)
        binding.tvPointBonus2.setBackgroundResource(0)

        Extra().resetDice(abbreviationDice(),arrayLocked) // Unlock All Dice and make them invisible
        Extra().lockTvPoint(abbreviation()) //Lock all text view

        val tvPoint = abbreviation()
        val tvPoint2 = abbreviation2()

        for(tv in tvPoint){
            tv.text = ""
            tv.hint = ""
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

        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val diceSelected = sharedPreferences.getInt("DefaultDice", 0)
        val listDice = Extra().colorDiceDefault(abbreviationDice(), diceSelected)
        threeLaunch(listDice)
    }

    private inner class ServerClass : Thread() {
        var serverSocket: BluetoothServerSocket? = null
        override fun run() {
            var socket: BluetoothSocket? = null
            while (socket == null) {
                try {
                    val message = Message.obtain()
                    message.what = STATE_CONNECTING
                    handler.sendMessage(message)
                    socket = serverSocket!!.accept() //It does this,
                    // otherwise it has an exception
                    //.accept() wait until the connection is established
                } catch (e: IOException) {
                    e.printStackTrace()
                    val message = Message.obtain()
                    message.what = STATE_CONNECTION_FAILED
                    handler.sendMessage(message)
                }
                if (socket != null) { //If no exception was thrown
                    val message = Message.obtain()
                    message.what = STATE_CONNECTED
                    handler.sendMessage(message)
                    sendReceive = SendReceive(socket)
                    sendReceive!!.start()
                    break
                }
            }
        }

        fun serverClose(){
            serverSocket!!.close()
        }

        init {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private inner class ClientClass(device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null
        override fun run() {
            try {
                socket!!.connect() //Attempt to connect to a remote device
                val message = Message.obtain()
                message.what = STATE_CONNECTED
                handler.sendMessage(message)
                sendReceive = SendReceive(socket!!)
                sendReceive!!.start()
            } catch (e: IOException) {
                e.printStackTrace()
                val message = Message.obtain()
                message.what = STATE_CONNECTION_FAILED
                handler.sendMessage(message)
            }
        }

        init {
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    inner class SendReceive(bluetoothSocket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream?
        private val outputStream: OutputStream?

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int
            while (true) { //endless loop
                try {
                    bytes = inputStream!!.read(buffer)
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,
                        bytes, -1, buffer
                    ).sendToTarget()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun write(bytes: ByteArray?) { //write in the buffer message
            try {
                outputStream!!.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        init {
            var tempIn: InputStream? = null
            var tempOut: OutputStream? = null
            try {
                tempIn = bluetoothSocket.inputStream
                tempOut = bluetoothSocket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            inputStream = tempIn
            outputStream = tempOut
        }
    }
}