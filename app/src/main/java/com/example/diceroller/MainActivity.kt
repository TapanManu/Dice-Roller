package com.example.diceroller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rollButton : Button = findViewById(R.id.button)
        rollButton.setOnClickListener{
            rollDice()
        }
    }

    private fun rollDice() {
        val dice = Dice(6)
        val diceroll = dice.roll()
        val dispText : TextView = findViewById(R.id.textView)
        dispText.text = diceroll.toString()
    }
}
class Dice(val numSides:Int) {
    fun roll(): Int {
        return (1..numSides).random()
    }
}