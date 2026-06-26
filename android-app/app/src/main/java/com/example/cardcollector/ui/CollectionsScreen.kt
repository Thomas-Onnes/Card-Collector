package com.example.cardcollector.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.cardcollector.R
import com.example.cardcollector.api.ApiClient
import com.example.cardcollector.api.ApiException
import com.example.cardcollector.models.CollectionItem
import com.example.cardcollector.session.SessionManager
import java.util.concurrent.Executors

class CollectionsScreen : Activity() {

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var gridLayout: GridLayout
    private lateinit var emptyText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var addButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager.isLoggedIn(this)) {
            openLogin()
            return
        }

        setContentView(createContent())
        loadCollections()
    }

    private fun createContent(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.background))
        }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(12))
        }

        val welcomeText = TextView(this).apply {
            text = "Welcome: ${SessionManager.username(this@CollectionsScreen)}"
            textSize = 18f
            setTextColor(getColor(R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val signOutButton = Button(this).apply {
            text = "Sign out"
            setOnClickListener { logout() }
        }

        topBar.addView(
            welcomeText,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        topBar.addView(signOutButton)

        val title = TextView(this).apply {
            text = "Your collections"
            textSize = 26f
            setTextColor(getColor(R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(dp(20), dp(8), dp(20), dp(8))
        }

        val scrollView = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(16))
        }

        emptyText = TextView(this).apply {
            text = "No collections yet. Add your first collection."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, dp(40), 0, dp(40))
        }

        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }

        gridLayout = GridLayout(this).apply {
            columnCount = 2
            useDefaultMargins = true
        }

        content.addView(
            progressBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )

        content.addView(
            emptyText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            gridLayout,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        scrollView.addView(content)

        addButton = Button(this).apply {
            text = "+ Add collection"
            setTextColor(Color.WHITE)
            background = getDrawable(R.drawable.primary_button_background)
            setOnClickListener { showAddCollectionDialog() }
        }

        val bottomBar = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(12), dp(20), dp(20))
            addView(
                addButton,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        root.addView(topBar)
        root.addView(title)
        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        root.addView(bottomBar)

        return root
    }

    private fun loadCollections() {
        val token = SessionManager.token(this) ?: return openLogin()
        progressBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE

        executor.execute {
            try {
                val collections = ApiClient.getCollections(token)
                mainHandler.post {
                    progressBar.visibility = View.GONE
                    renderCollections(collections)
                }
            } catch (e: ApiException) {
                mainHandler.post {
                    progressBar.visibility = View.GONE
                    if (e.statusCode == 401) {
                        SessionManager.clear(this)
                        openLogin()
                    } else {
                        Toast.makeText(
                            this,
                            e.message ?: "Could not load collections",
                            Toast.LENGTH_LONG
                        ).show()
                        renderCollections(emptyList())
                    }
                }
            } catch (e: Exception) {
                mainHandler.post {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Could not connect to backend", Toast.LENGTH_LONG).show()
                    renderCollections(emptyList())
                }
            }
        }
    }

    private fun renderCollections(collections: List<CollectionItem>) {
        gridLayout.removeAllViews()
        emptyText.visibility = if (collections.isEmpty()) View.VISIBLE else View.GONE

        for (collection in collections) {
            gridLayout.addView(collectionCard(collection), cardParams())
        }
    }

    private fun collectionCard(collection: CollectionItem): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = getDrawable(R.drawable.card_background)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            isClickable = true
            setOnClickListener { openCollectionDetail(collection) }
        }

        val name = TextView(this).apply {
            text = collection.collectionName
            textSize = 18f
            setTextColor(getColor(R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val type = TextView(this).apply {
            text = if (collection.gameType == "mtg") "Magic: The Gathering" else "Pokémon"
            textSize = 14f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, dp(6), 0, dp(14))
        }

        val deleteButton = Button(this).apply {
            text = "Delete"
            textSize = 12f
            isAllCaps = false
            minHeight = 0
            minWidth = 0
            minimumHeight = 0
            minimumWidth = 0
            setPadding(dp(12), dp(6), dp(12), dp(6))
            setTextColor(Color.WHITE)
            background = getDrawable(R.drawable.danger_button_background)
            setOnClickListener {
                showDeleteDialog(collection)
            }
        }

        card.addView(name)
        card.addView(type)

        card.addView(
            deleteButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
            }
        )

        return card
    }

    private fun showAddCollectionDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), 0)
        }

        val nameInput = EditText(this).apply {
            hint = "Collection name"
            background = getDrawable(R.drawable.input_background)
            setSingleLine(true)
        }

        val typeSpinner = Spinner(this)
        val typeLabels = arrayOf("Pokémon", "Magic: The Gathering")
        typeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            typeLabels
        )

        dialogLayout.addView(
            nameInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(10), 0, dp(14))
            }
        )

        dialogLayout.addView(typeSpinner)

        AlertDialog.Builder(this)
            .setTitle("Add collection")
            .setView(dialogLayout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Create", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = nameInput.text.toString().trim()
                        val gameType = if (typeSpinner.selectedItemPosition == 0) "pokemon" else "mtg"

                        if (name.isBlank()) {
                            nameInput.error = "Collection name is required"
                        } else {
                            dismiss()
                            createCollection(name, gameType)
                        }
                    }
                }
                show()
            }
    }

    private fun createCollection(name: String, gameType: String) {
        val token = SessionManager.token(this) ?: return openLogin()
        addButton.isEnabled = false

        executor.execute {
            try {
                ApiClient.createCollection(token, name, gameType)

                mainHandler.post {
                    addButton.isEnabled = true
                    loadCollections()
                }
            } catch (e: Exception) {
                mainHandler.post {
                    addButton.isEnabled = true
                    Toast.makeText(
                        this,
                        e.message ?: "Could not create collection.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showDeleteDialog(collection: CollectionItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete collection")
            .setMessage(
                "Are you sure you want to delete ${collection.collectionName}? " +
                        "Deleting this means all the added cards in the collection will be deleted aswell."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                deleteCollection(collection)
            }
            .show()
    }

    private fun deleteCollection(collection: CollectionItem) {
        val token = SessionManager.token(this) ?: return openLogin()

        executor.execute {
            try {
                ApiClient.deleteCollection(token, collection.collectionId)
                mainHandler.post {
                    loadCollections()
                }
            } catch (e: Exception) {
                mainHandler.post {
                    Toast.makeText(
                        this,
                        e.message ?: "Could not delete collection",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun logout() {
        val token = SessionManager.token(this)
        SessionManager.clear(this)

        if (!token.isNullOrBlank()) {
            executor.execute {
                try {
                    ApiClient.logout(token)
                } catch (_: Exception) {
                    // User is already logged out locally.
                }
            }
        }

        openLogin()
    }

    private fun openCollectionDetail(collection: CollectionItem) {
        val intent = Intent(this, CollectionDetailScreen::class.java).apply {
            putExtra("collectionId", collection.collectionId)
            putExtra("collectionName", collection.collectionName)
            putExtra("gameType", collection.gameType)
        }
        startActivity(intent)
    }

    private fun openLogin() {
        startActivity(Intent(this, LoginScreen::class.java))
        finish()
    }

    private fun cardParams(): GridLayout.LayoutParams {
        return GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(dp(6), dp(6), dp(6), dp(12))
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}