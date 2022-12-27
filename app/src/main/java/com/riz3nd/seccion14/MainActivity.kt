package com.riz3nd.seccion14

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.TypedValue
import android.view.Gravity.apply
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.test.runner.screenshot.ScreenCapture
import androidx.test.runner.screenshot.Screenshot
import androidx.test.runner.screenshot.Screenshot.capture
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private var gaming = true
    private var cellSelected_x = 0
    private var cellSelected_y = 0
    private lateinit var board:Array<IntArray>
    private var options = 0
    private var nameColorBlack = "black_cell"
    private var nameColorWhite = "white_cell"
    private var levelMoves = 64
    private var moves = 64
    private var movesRequired = 16
    private var bonus = 0
    private var width_Bonus = 0
    private var checkMovement = true
    private var mHandler:Handler? = null
    private var timeInSecons:Long = 0
    private var bitmap:Bitmap? = null
    private var string_share = ""
    private var level = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initScreenGame()
        startGame()
    }

    fun startGame() {
        resetStatus()
        resetBoard()
        clearBoard()
        setFirstPosition()
        resetTime()
        startTime()
    }

    fun startGame(view: View) {
        resetStatus()
        resetBoard()
        clearBoard()
        setFirstPosition()
        hideMessage()
        resetTime()
        startTime()
    }

    private fun resetStatus(){
        gaming = true
        moves = levelMoves
        bonus = 0
        options = 0
    }

    private fun clearBoard() {
        var iv: ImageView
        var colorBlack = ContextCompat.getColor(this,
            resources.getIdentifier(nameColorBlack, "color", packageName))
        var colorWhite = ContextCompat.getColor(this,
            resources.getIdentifier(nameColorWhite, "color", packageName))
        for (i in 0..7){
            for (j in 0..7){
                iv = findViewById(resources.getIdentifier("c$i$j", "id", packageName))
//                iv.setImageResource(R.drawable.horse)
                iv.setImageResource(0)
                if (checkColorCell(i, j) == "black") iv.setBackgroundColor(colorBlack)
                else iv.setBackgroundColor(colorWhite)
            }
        }
    }


    /**
     * Metodo para reiniciar el tablero
     * @author Oscar Argaez
     */
    private fun resetBoard() {
        // 0 Casilla libre
        // 1 Casilla marcada
        // 2 Bonus
        // 9 Opcion del movimiento actual

        board = arrayOf(
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        )
    }

    private fun initScreenGame() {
        setSizeBoard()
        hideMessage()
    }

    private fun setSizeBoard() {
        var iv:ImageView
        val display =  windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        var width_dp = (width / resources.displayMetrics.density)
        var lateralMarginsDP = 0
        val width_cell = (width_dp - lateralMarginsDP)/8
        val height_cell = width_cell

        width_Bonus = 2 * width_cell.toInt()

        for(i in 0..7){
            for (j in 0..7){
                iv = findViewById(resources.getIdentifier("c$i$j","id", packageName))
                var height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height_cell, resources.displayMetrics).toInt()
                var width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width_cell, resources.displayMetrics).toInt()
                iv.layoutParams = TableRow.LayoutParams(width, height)
            }
        }
    }


    private fun hideMessage() {
        var lyMessage = findViewById<LinearLayout>(R.id.lyMessage)
        lyMessage.visibility = View.GONE
    }

    private fun setFirstPosition(){
        var x = 0
        var y = 0
        x = (0..7).random()
        y = (0..7).random()
        cellSelected_x = x
        cellSelected_y = y
        selectCell(x, y)
    }

    fun launcheShareGame(v: View){
        shareGame()
    }

    private fun shareGame() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        var ssc: ScreenCapture = capture(this);
        bitmap = ssc.bitmap
        if (bitmap != null){
            var idGame = SimpleDateFormat("yyyy/MM/dd").format(Date())
            idGame = idGame.replace(":","")
            idGame = idGame.replace("/","")
            val path = saveImage(bitmap, "$idGame.jpg")
            val bmpUri = Uri.parse(path)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
            shareIntent.putExtra(Intent.EXTRA_TEXT, string_share)
            shareIntent.type = "image/png"
            val finalShareIntent = Intent.createChooser(shareIntent, "Selecciona la app donde quieres compartir tu puntaje")
            finalShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(finalShareIntent)
        }
    }

    private fun saveImage(bitmap: Bitmap?, fileName: String): String? {
        if (bitmap == null)
            return null

        if (SDK_INT >= VERSION_CODES.O){
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+"/Screenshots")
            }
            val uri = this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if(uri != null){
                this.contentResolver.openOutputStream(uri).use {
                    if (it == null)
                        return@use

                    bitmap.compress(Bitmap.CompressFormat.PNG, 85, it)
                    it.flush()
                    it.close()

                    MediaScannerConnection.scanFile(this, arrayOf(uri.toString()), null, null)
                }
            }
            return uri.toString()
        }
        val filePath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES+"/Screenshots"
        ).absolutePath

        val dir = File(filePath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        val fOut = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut)
        fOut.flush()
        fOut.close()

        MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null, null)
        return filePath
    }

    private fun checkCell(x: Int, y: Int) {
        var checkTrue = true
        if (checkMovement){
            var dif_x = x - cellSelected_x
            var dif_y = y - cellSelected_y
            checkTrue = false
            if (dif_x == 1 && dif_y == 2)   checkTrue = true // right - top long
            if (dif_x == 1 && dif_y == -2)  checkTrue = true // right - bottom long
            if (dif_x == 2 && dif_y == 1)   checkTrue = true // right long - top
            if (dif_x == 2 && dif_y == -1)  checkTrue = true // right long - bottom
            if (dif_x == -1 && dif_y == 2)  checkTrue = true // left - top long
            if (dif_x == -1 && dif_y == -2) checkTrue = true // left - bottom long
            if (dif_x == -2 && dif_y == 1)  checkTrue = true // left long - top
            if (dif_x == -2 && dif_y == -1) checkTrue = true // left long - bottom
        }else{
            if(board[x][y] != 1){
                bonus--
                var tvBonusData = findViewById<TextView>(R.id.tvBonusData)
                tvBonusData.text = "$bonus"
            }
        }
        if(board[x][y] == 1) checkTrue = false
        if(checkTrue) selectCell(x, y)
    }

    private fun selectCell(x: Int, y: Int) {
        moves--
        var tvModesData = findViewById<TextView>(R.id.tvMovesData)
        tvModesData.text = moves.toString()

        growProgressBonus()
        if(board[x][y] == 2){
            bonus++
            var tvBonusData = findViewById<TextView>(R.id.tvBonusData)
            var tvSumBonus = findViewById<TextView>(R.id.tvSumBonus)
            tvSumBonus.visibility = View.VISIBLE
            tvBonusData.visibility = View.VISIBLE
            tvBonusData.text = "$bonus"
        }

        board[x][y] = 1
        paintHorseCell(cellSelected_x, cellSelected_y, "previus_cell")
        cellSelected_x = x
        cellSelected_y = y
        clearOptions()
        paintHorseCell(x, y, "selected_cell")
        checkMovement = true
        checkOptions(x, y)

        if(moves > 0){
            checkMoveBonus()
            checkGameOver(x, y)
        }
        else showMessage("You Win!", "Next Level", false)
    }

    fun checkCellClicked(view: View){
        var name = view.tag.toString()
        var x = name.subSequence(1, 2).toString().toInt()
        var y = name.subSequence(2, 3).toString().toInt()
        checkCell(x, y)
    }


    private fun checkGameOver(x: Int, y: Int) {
        if(options == 0){
            if (bonus > 0){
                checkMovement = false
                paintAllOptions()
            }else showMessage("Game Over", "Try Again!", true)
        }
    }

    private fun paintAllOptions() {
        for (i in 0..7){
            for (j in 0..7){
                if(board[i][j] != 1) paintOption(i, j)
                if(board[i][j] == 0) board[i][j] = 9
            }
        }
    }

    private fun paintOption(x: Int, y: Int) {
        var iv:ImageView = findViewById(resources.getIdentifier("c$x$y","id", packageName))
        if(checkColorCell(x, y) == "black") iv.setBackgroundResource(R.drawable.option_black)
        else iv.setBackgroundResource(R.drawable.option_white)
    }


    private fun checkColorCell(x: Int, y: Int): String {
        var blackColumn_x = arrayOf(0,2,4,6)
        var blackRow_x = arrayOf(1,3,5,7)
        if((blackColumn_x.contains(x) && blackColumn_x.contains(y))
            || (blackRow_x.contains(x) && blackRow_x.contains(y)))
            return "black"
        else return  "white"
    }

    private fun paintHorseCell(x: Int, y: Int, color: String) {
        var iv:ImageView = findViewById(resources.getIdentifier("c$x$y","id", packageName))
        iv.setBackgroundColor(ContextCompat.getColor(this, resources.getIdentifier(color, "color", packageName)))
        iv.setImageResource(R.drawable.horse)
    }


    fun showMessage(title:String, message:String, gameOver:Boolean){
        var lyMessage = findViewById<LinearLayout>(R.id.lyMessage)
        var tvIntroLevel = findViewById<TextView>(R.id.tvTitleMessage)
        var tvScoreMessage = findViewById<TextView>(R.id.tvScoreMessage)
        var btnNextLevel = findViewById<TextView>(R.id.btnNextLevel)
        var tvTimeData = findViewById<TextView>(R.id.tvTimeData)
        var score = ""
        gaming = false

        lyMessage.visibility = View.VISIBLE
        if(gameOver){
//            tvScoreMessage.text = "Score: ${levelMoves - moves}/$levelMoves"
            score = "Score: ${levelMoves - moves}/$levelMoves"
            string_share = "Casi!!. $score"
        }else{
            score = "${tvTimeData.text}"
//            tvScoreMessage.text = "${tvTimeData.text}"
            string_share = "Vamos!!, Nuevo nivel completado. Nivel: $level ($score)"
        }
        tvScoreMessage.text = score
        tvIntroLevel.text = title
        btnNextLevel.text = message
    }


    private fun  growProgressBonus() {
        var moves_done = levelMoves - moves
        var bonus_done = moves_done / movesRequired
        var moves_rest = movesRequired * (bonus_done)
        var bonus_grow = moves_done - moves_rest
        var v = findViewById<View>(R.id.vNewBonus)
        var widthBonus = ((width_Bonus/movesRequired) * bonus_grow).toFloat()
        var height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        var width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthBonus, resources.displayMetrics).toInt()
        v.setLayoutParams(TableRow.LayoutParams(width, height))
    }

    private fun checkMoveBonus() {
        if(moves % movesRequired == 0){
            var bonusCell_x = 0
            var bonusCell_y = 0
            var bonusCell = false
            while (bonusCell == false){
                bonusCell_x = (0..7).random()
                bonusCell_y = (0..7).random()
                if(board[bonusCell_x][bonusCell_y] == 0) bonusCell = true
            }
            board[bonusCell_x][bonusCell_y] = 2
            paintBonusCell(bonusCell_x, bonusCell_y)
        }
    }

    private fun paintBonusCell(x: Int, y: Int) {
        var iv: ImageView = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        iv.setImageResource(R.drawable.bonus)
    }

    private fun clearOptions() {
        for (i in 0..7){
            for (j in 0..7){
                if (board[i][j] == 9 || board[i][j] == 2){
                    if (board[i][j] == 9) board[i][j] = 0
                    clearOptions(i, j)
                }
            }
        }
    }

    private fun clearOptions(x: Int, y: Int){
        var iv:ImageView = findViewById(resources.getIdentifier("c$x$y","id", packageName))
        if(checkColorCell(x, y) == "black") iv.setBackgroundColor(ContextCompat.getColor(
            this, resources.getIdentifier(nameColorBlack, "color", packageName)))
        else iv.setBackgroundColor(ContextCompat.getColor(
            this, resources.getIdentifier(nameColorWhite, "color", packageName)))
        if (board[x][y] == 1) iv.setBackgroundColor(ContextCompat.getColor(
            this, resources.getIdentifier("previus_cell", "color", packageName)))
    }

    private fun checkOptions(x: Int, y: Int) {
        options = 0
        checkMove(x, y, 1, 2)     // check move right - top long
        checkMove(x, y, 2, 1)     // check move right long - top
        checkMove(x, y, 1, -2)    // check move right - bottom long
        checkMove(x, y, 2, -1)    // check move right long - bottom
        checkMove(x, y, -1, 2)    // check move left - top long
        checkMove(x, y, -2, 1)    // check move left long -top
        checkMove(x, y, -1, -2)   // check move left - bottom long
        checkMove(x, y, -2, -1)   // check move left long bottom
        var tvOptionsData = findViewById<TextView>(R.id.tvOptonsData)
        tvOptionsData.text = "$options"
    }

    private fun checkMove(x: Int, y: Int, mov_x: Int, mov_y: Int) {
        var option_x = x + mov_x
        var option_y = y + mov_y
        if(option_x < 8 && option_y <8 && option_x >= 0 && option_y >= 0){
            if(board[option_x][option_y] == 0 ||
                board[option_x][option_y] == 2){
                options++
                paintOption(option_x, option_y)
                if(board[option_x][option_y] == 0) board[option_x][option_y] = 9

            }
        }

    }

    private var chronometer: Runnable = object: Runnable{
        override fun run() {
            try {
                if (gaming){
                    timeInSecons++
                    updateStopWatchView(timeInSecons)
                }
            } finally {
                mHandler!!.postDelayed(this, 1000)
            }
        }
    }

    private fun updateStopWatchView(timeInSecons: Long) {
        val formattedTime = getFormattedStopWatch(timeInSecons * 1000)
        var tvTimeData = findViewById<TextView>(R.id.tvTimeData)
        tvTimeData.text = formattedTime
    }

    private fun getFormattedStopWatch(ms: Long): String {
        var miliSecons = ms
        val minutes = TimeUnit.MILLISECONDS.toMinutes(miliSecons)
        miliSecons -= TimeUnit.MINUTES.toMillis(minutes)
        val secons = TimeUnit.MILLISECONDS.toSeconds(miliSecons)
        return "${if(minutes < 10) "0" else ""}$minutes:"+
                "${if(secons < 10) "0" else ""}$secons"
    }

    private fun startTime(){
        mHandler = Handler(Looper.getMainLooper())
        chronometer.run()

    }

    private fun resetTime(){
        mHandler?.removeCallbacks(chronometer)
        timeInSecons = 0
        var tvTimeData = findViewById<TextView>(R.id.tvTimeData)
        tvTimeData.text = "00:00"
    }

}