package com.example.cardcollector.ui

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.cardcollector.R
import com.example.cardcollector.session.SessionManager

class CollectionDetailScreen : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createContent())
    }

    private fun createContent(): View {
        val collectionName = intent.getStringExtra("collectionName") ?: "Collection"
        val gameType = intent.getStringExtra("gameType") ?: "pokemon"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.background))
            setPadding(dp(20), dp(24), dp(20), dp(20))
        }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val backButton = Button(this).apply {
            text = "Back"
            setOnClickListener { finish() }
        }

        val welcome = TextView(this).apply {
            text = "Welcome: ${SessionManager.username(this@CollectionDetailScreen)}"
            gravity = Gravity.END
            setTextColor(getColor(R.color.text_secondary))
        }

        topBar.addView(backButton)
        topBar.addView(welcome, LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ))

        val title = TextView(this).apply {
            text = collectionName
            textSize = 30f
            setTextColor(getColor(R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, dp(40), 0, dp(8))
        }

        val typeText = TextView(this).apply {
            text = if (gameType == "mtg") "Magic: The Gathering collection" else "Pokémon collection"
            textSize = 16f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, 0, 0, dp(32))
        }

        val placeholder = TextView(this).apply {
            text = "Cards will be added here later."
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.text_secondary))
            background = getDrawable(R.drawable.card_background)
            setPadding(dp(20), dp(40), dp(20), dp(40))
        }

        root.addView(topBar)
        root.addView(title)
        root.addView(typeText)
        root.addView(placeholder, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        return root
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
