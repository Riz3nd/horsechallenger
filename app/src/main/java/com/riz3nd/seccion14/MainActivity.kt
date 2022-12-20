package com.riz3nd.seccion14

import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private var cellSelected_x = 0
    private var cellSelected_y = 0
    private lateinit var board:Array<IntArray>
    private var options = 0
    private var nameColorBlack = "black_cell"
    private var nameColorWhite = "white_cell"
    private var levelMoves = 64
    private var moves = 64
    private var movesRequired = 4
    private var bonus = 0
    private var width_Bonus = 0
    private var checkMovement = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initScreenGame()
        resetBoard()
        setFirstPosition()
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
        seletCell(x, y)
    }

    private fun seletCell(x: Int, y: Int) {
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

    private fun checkGameOver(x: Int, y: Int) {
        if(options == 0){
            if (bonus == 0){
                showMessage("Game Over", "Try Again!", true)
            } else {
                checkMovement = false
            }
        }
    }

    fun showMessage(title:String, message:String, status:Boolean){
        var lyMessage = findViewById<LinearLayout>(R.id.lyMessage)
        var tvIntroLevel = findViewById<TextView>(R.id.tvTitleMessage)
        var tvScoreMessage = findViewById<TextView>(R.id.tvScoreMessage)
        var btnNextLevel = findViewById<Button>(R.id.btnNextLevel)
        var tvTimeData = findViewById<TextView>(R.id.tvTimeData)

        lyMessage.visibility = View.VISIBLE
        if(status){
            tvScoreMessage.text = "Score: ${levelMoves - moves}/$levelMoves"
        }else{
            tvScoreMessage.text = "${tvTimeData.text}"
        }
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
                paintOptions(option_x, option_y)
                if(board[option_x][option_y] == 0) board[option_x][option_y] = 9

            }
        }

    }

    private fun paintOptions(x: Int, y: Int) {
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

    fun checkCellClicked(view: View){
        var name = view.tag.toString()
        var x = name.subSequence(1, 2).toString().toInt()
        var y = name.subSequence(2, 3).toString().toInt()
        checkCell(x, y)
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
        if(checkTrue) seletCell(x, y)
    }
}