package com.example.cardcollector.ui

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.cardcollector.R
import com.example.cardcollector.api.ApiClient
import com.example.cardcollector.models.CardSearchResult
import com.example.cardcollector.models.CollectionCardItem
import com.example.cardcollector.session.SessionManager
import java.util.Locale
import java.util.concurrent.Executors

class CollectionDetailScreen : Activity() {

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val searchHandler = Handler(Looper.getMainLooper())

    private var collectionId: Int = 0
    private var collectionName: String = "Collection"
    private var gameType: String = "pokemon"

    private lateinit var cardsContainer: LinearLayout
    private lateinit var emptyText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var totalPriceText: TextView
    private lateinit var addCardButton: Button

    private enum class SuggestionField {
        MAIN,
        NUMBER,
        RARITY,
        ARTIST
    }

    private data class TextSuggestion(
        val label: String,
        val value: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        collectionId = intent.getIntExtra("collectionId", 0)
        collectionName = intent.getStringExtra("collectionName") ?: "Collection"
        gameType = intent.getStringExtra("gameType") ?: "pokemon"

        if (collectionId <= 0) {
            Toast.makeText(this, "Invalid collection", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContentView(createContent())
        loadCollectionCards()
    }

    private fun createContent(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.background))
        }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(8))
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
        topBar.addView(
            welcome,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val title = TextView(this).apply {
            text = collectionName
            textSize = 28f
            setTextColor(getColor(R.color.text_primary))
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(20), dp(12), dp(20), dp(4))
        }

        val typeText = TextView(this).apply {
            text = if (gameType == "mtg") {
                "Magic: The Gathering collection"
            } else {
                "Pokémon collection"
            }
            textSize = 15f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(dp(20), 0, dp(20), dp(12))
        }

        val scrollView = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(16))
        }

        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }

        emptyText = TextView(this).apply {
            text = "No cards in this collection yet."
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.text_secondary))
            background = getDrawable(R.drawable.card_background)
            setPadding(dp(20), dp(40), dp(20), dp(40))
        }

        cardsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
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

        content.addView(cardsContainer)

        scrollView.addView(content)

        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(20))
        }

        totalPriceText = TextView(this).apply {
            text = "Total: €0.00"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(getColor(R.color.text_primary))
        }

        addCardButton = Button(this).apply {
            text = "+ Add card"
            setTextColor(Color.WHITE)
            background = getDrawable(R.drawable.primary_button_background)
            setOnClickListener { showAddCardDialog() }
        }

        bottomBar.addView(
            totalPriceText,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        bottomBar.addView(
            addCardButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(topBar)
        root.addView(title)
        root.addView(typeText)
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

    private fun loadCollectionCards() {
        val token = SessionManager.token(this) ?: return

        progressBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE

        executor.execute {
            try {
                val response = ApiClient.getCollectionCards(
                    token = token,
                    collectionId = collectionId
                )

                mainHandler.post {
                    progressBar.visibility = View.GONE
                    renderCards(response.cards)
                    updateTotalPrice(response.totalPrice)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        e.message ?: "Could not load cards",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun renderCards(cards: List<CollectionCardItem>) {
        cardsContainer.removeAllViews()
        emptyText.visibility = if (cards.isEmpty()) View.VISIBLE else View.GONE

        for (card in cards) {
            cardsContainer.addView(cardView(card))
        }
    }

    private fun cardView(card: CollectionCardItem): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = getDrawable(R.drawable.card_background)
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val name = TextView(this).apply {
            text = "${card.quantity}x ${card.name}"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(getColor(R.color.text_primary))
        }

        val deleteButton = Button(this).apply {
            text = "Delete"
            textSize = 12f
            isAllCaps = false
            minHeight = 0
            minWidth = 0
            minimumHeight = 0
            minimumWidth = 0
            setPadding(dp(10), dp(4), dp(10), dp(4))
            setTextColor(Color.WHITE)
            background = getDrawable(R.drawable.danger_button_background)
            setOnClickListener {
                showRemoveCardDialog(card)
            }
        }

        topRow.addView(
            name,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        topRow.addView(
            deleteButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val details = mutableListOf<String>()

        if (!card.setName.isNullOrBlank()) {
            details.add(card.setName)
        }

        if (!card.setCode.isNullOrBlank()) {
            details.add(card.setCode)
        }

        if (!card.collectorNumber.isNullOrBlank()) {
            details.add("#${card.collectorNumber}")
        }

        if (!card.rarity.isNullOrBlank()) {
            details.add(card.rarity)
        }

        if (!card.artistOrIllustrator.isNullOrBlank()) {
            details.add(card.artistOrIllustrator)
        }

        val detailsText = TextView(this).apply {
            text = details.joinToString(" - ")
            textSize = 14f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, dp(6), 0, dp(6))
        }

        val collectionInfo = TextView(this).apply {
            val foilText = if (card.isFoil) "Foil" else "Non-foil"
            text = "${card.cardCondition} - ${card.language} - $foilText"
            textSize = 14f
            setTextColor(getColor(R.color.text_secondary))
        }

        val priceInfo = TextView(this).apply {
            val price = card.price
            text = if (price == null) {
                "Price: not available"
            } else {
                "Price: €${formatPrice(price)} each"
            }
            textSize = 14f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, dp(4), 0, 0)
        }

        container.addView(topRow)

        if (details.isNotEmpty()) {
            container.addView(detailsText)
        }

        container.addView(collectionInfo)
        container.addView(priceInfo)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, dp(12))
        }

        container.layoutParams = params

        return container
    }

    private fun showRemoveCardDialog(card: CollectionCardItem) {
        val token = SessionManager.token(this) ?: return

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), 0)
        }

        val message = TextView(this).apply {
            text = if (card.quantity > 1) {
                "You have ${card.quantity} copies of ${card.name}. How many do you want to remove?"
            } else {
                "Remove ${card.name} from this collection?"
            }
            setTextColor(getColor(R.color.text_primary))
            setPadding(0, 0, 0, dp(10))
        }

        val quantityInput = EditText(this).apply {
            hint = "Quantity to remove"
            setText("1")
            inputType = InputType.TYPE_CLASS_NUMBER
            background = getDrawable(R.drawable.input_background)
            setSingleLine(true)
            visibility = if (card.quantity > 1) View.VISIBLE else View.GONE
        }

        dialogLayout.addView(message)
        dialogLayout.addView(quantityInput)

        AlertDialog.Builder(this)
            .setTitle("Delete card")
            .setView(dialogLayout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val quantityToRemove = if (card.quantity > 1) {
                            quantityInput.text.toString().toIntOrNull() ?: 1
                        } else {
                            1
                        }

                        if (quantityToRemove <= 0) {
                            quantityInput.error = "Quantity must be at least 1"
                            return@setOnClickListener
                        }

                        if (quantityToRemove > card.quantity) {
                            quantityInput.error = "You only have ${card.quantity}"
                            return@setOnClickListener
                        }

                        dismiss()

                        removeCardFromCollection(
                            token = token,
                            card = card,
                            quantityToRemove = quantityToRemove
                        )
                    }
                }

                show()
            }
    }

    private fun removeCardFromCollection(
        token: String,
        card: CollectionCardItem,
        quantityToRemove: Int
    ) {
        executor.execute {
            try {
                val response = ApiClient.removeCardFromCollection(
                    token = token,
                    collectionId = collectionId,
                    collectionCardId = card.collectionCardId,
                    quantity = quantityToRemove
                )

                mainHandler.post {
                    renderCards(response.cards)
                    updateTotalPrice(response.totalPrice)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    Toast.makeText(
                        this,
                        e.message ?: "Could not delete card",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showAddCardDialog() {
        val token = SessionManager.token(this) ?: return

        val cardOptions = mutableListOf<CardSearchResult>()
        val cardOptionLabels = mutableListOf<String>()

        val mainSuggestions = mutableListOf<TextSuggestion>()
        val numberSuggestions = mutableListOf<TextSuggestion>()
        val raritySuggestions = mutableListOf<TextSuggestion>()
        val artistSuggestions = mutableListOf<TextSuggestion>()

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), 0)
        }

        var activeField: SuggestionField? = null

        val mainAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )

        val numberAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )

        val rarityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )

        val artistAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )

        val mainSearchInput = autocompleteInput(
            hintText = if (gameType == "mtg") {
                "Card name"
            } else {
                "Set name or set code"
            }
        ).apply {
            setAdapter(mainAdapter)
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    activeField = SuggestionField.MAIN
                }
            }
            setOnItemClickListener { parent, _, position, _ ->
                val label = parent.getItemAtPosition(position).toString()
                val selected = mainSuggestions.firstOrNull { it.label == label }
                if (selected != null) {
                    setInputText(this, selected.value)
                    dismissDropDown()
                }
            }
        }

        val pokemonNumberInput = autocompleteInput("Card number in set").apply {
            visibility = if (gameType == "pokemon") View.VISIBLE else View.GONE
            setAdapter(numberAdapter)
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    activeField = SuggestionField.NUMBER
                }
            }
            setOnItemClickListener { parent, _, position, _ ->
                val label = parent.getItemAtPosition(position).toString()
                val selected = numberSuggestions.firstOrNull { it.label == label }
                if (selected != null) {
                    setInputText(this, selected.value)
                    dismissDropDown()
                }
            }
        }

        val rarityInput = autocompleteInput("Rarity, optional").apply {
            setAdapter(rarityAdapter)
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    activeField = SuggestionField.RARITY
                }
            }
            setOnItemClickListener { parent, _, position, _ ->
                val label = parent.getItemAtPosition(position).toString()
                val selected = raritySuggestions.firstOrNull { it.label == label }
                if (selected != null) {
                    setInputText(this, selected.value)
                    dismissDropDown()
                }
            }
        }

        val artistInput = autocompleteInput(
            hintText = if (gameType == "mtg") {
                "Illustrator, optional"
            } else {
                "Artist, optional"
            }
        ).apply {
            setAdapter(artistAdapter)
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    activeField = SuggestionField.ARTIST
                }
            }
            setOnItemClickListener { parent, _, position, _ ->
                val label = parent.getItemAtPosition(position).toString()
                val selected = artistSuggestions.firstOrNull { it.label == label }
                if (selected != null) {
                    setInputText(this, selected.value)
                    dismissDropDown()
                }
            }
        }

        val quantityInput = EditText(this).apply {
            hint = "Quantity"
            setText("1")
            inputType = InputType.TYPE_CLASS_NUMBER
            background = getDrawable(R.drawable.input_background)
            setSingleLine(true)
        }

        val conditionInput = EditText(this).apply {
            hint = "Condition"
            setText("Near Mint")
            background = getDrawable(R.drawable.input_background)
            setSingleLine(true)
        }

        val languageInput = EditText(this).apply {
            hint = "Language"
            setText("EN")
            background = getDrawable(R.drawable.input_background)
            setSingleLine(true)
        }

        val foilCheckBox = CheckBox(this).apply {
            text = "Foil"
        }

        val cardOptionsSpinner = Spinner(this)
        val cardOptionsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            cardOptionLabels
        )
        cardOptionsSpinner.adapter = cardOptionsAdapter

        val searchStatus = TextView(this).apply {
            text = "Start typing to search."
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, dp(8), 0, dp(8))
        }

        fun addWithMargin(view: View) {
            dialogLayout.addView(
                view,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dp(5), 0, dp(5))
                }
            )
        }

        addWithMargin(mainSearchInput)

        if (gameType == "pokemon") {
            addWithMargin(pokemonNumberInput)
        }

        addWithMargin(rarityInput)
        addWithMargin(artistInput)
        addWithMargin(searchStatus)
        addWithMargin(cardOptionsSpinner)
        addWithMargin(quantityInput)
        addWithMargin(conditionInput)
        addWithMargin(languageInput)
        dialogLayout.addView(foilCheckBox)

        fun reloadSuggestions() {
            val mainSearch = mainSearchInput.text.toString().trim()
            val collectorNumber = pokemonNumberInput.text.toString().trim()
            val rarity = rarityInput.text.toString().trim()
            val artist = artistInput.text.toString().trim()

            val hasEnoughInput = if (gameType == "mtg") {
                mainSearch.length >= 2 || rarity.length >= 2 || artist.length >= 2
            } else {
                mainSearch.length >= 2 ||
                        collectorNumber.isNotBlank() ||
                        rarity.length >= 2 ||
                        artist.length >= 2
            }

            if (!hasEnoughInput) {
                cardOptions.clear()
                cardOptionLabels.clear()
                cardOptionsAdapter.notifyDataSetChanged()

                clearAutocomplete(mainAdapter)
                clearAutocomplete(numberAdapter)
                clearAutocomplete(rarityAdapter)
                clearAutocomplete(artistAdapter)

                searchStatus.text = "Start typing to search."
                return
            }

            searchStatus.text = "Searching..."

            executor.execute {
                try {
                    val results = ApiClient.searchCards(
                        token = token,
                        gameType = gameType,
                        name = if (gameType == "mtg") mainSearch else "",
                        setQuery = if (gameType == "pokemon") mainSearch else "",
                        collectorNumber = if (gameType == "pokemon") collectorNumber else "",
                        rarity = rarity,
                        artistOrIllustrator = artist
                    )

                    mainHandler.post {
                        cardOptions.clear()
                        cardOptions.addAll(results)

                        cardOptionLabels.clear()
                        cardOptionLabels.addAll(results.map { it.displayLabel() })
                        cardOptionsAdapter.notifyDataSetChanged()

                        updateAutocompleteOptions(
                            results = results,
                            mainSuggestions = mainSuggestions,
                            numberSuggestions = numberSuggestions,
                            raritySuggestions = raritySuggestions,
                            artistSuggestions = artistSuggestions,
                            mainAdapter = mainAdapter,
                            numberAdapter = numberAdapter,
                            rarityAdapter = rarityAdapter,
                            artistAdapter = artistAdapter
                        )

                        showActiveDropdown(
                            activeField = activeField,
                            mainInput = mainSearchInput,
                            numberInput = pokemonNumberInput,
                            rarityInput = rarityInput,
                            artistInput = artistInput,
                            mainSuggestions = mainSuggestions,
                            numberSuggestions = numberSuggestions,
                            raritySuggestions = raritySuggestions,
                            artistSuggestions = artistSuggestions
                        )

                        searchStatus.text = if (results.isEmpty()) {
                            "No cards found."
                        } else {
                            "${results.size} card option(s) found."
                        }
                    }
                } catch (e: Exception) {
                    mainHandler.post {
                        cardOptions.clear()
                        cardOptionLabels.clear()
                        cardOptionsAdapter.notifyDataSetChanged()

                        clearAutocomplete(mainAdapter)
                        clearAutocomplete(numberAdapter)
                        clearAutocomplete(rarityAdapter)
                        clearAutocomplete(artistAdapter)

                        searchStatus.text = "Search failed."
                    }
                }
            }
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Not needed.
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                searchHandler.removeCallbacksAndMessages(null)
                searchHandler.postDelayed(
                    {
                        reloadSuggestions()
                    },
                    350
                )
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed.
            }
        }

        mainSearchInput.addTextChangedListener(watcher)
        pokemonNumberInput.addTextChangedListener(watcher)
        rarityInput.addTextChangedListener(watcher)
        artistInput.addTextChangedListener(watcher)

        AlertDialog.Builder(this)
            .setTitle("Add card")
            .setView(dialogLayout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (cardOptions.isEmpty()) {
                            Toast.makeText(
                                this@CollectionDetailScreen,
                                "Select a card first.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@setOnClickListener
                        }

                        val selectedIndex = cardOptionsSpinner.selectedItemPosition

                        if (selectedIndex < 0 || selectedIndex >= cardOptions.size) {
                            Toast.makeText(
                                this@CollectionDetailScreen,
                                "Invalid card selection.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@setOnClickListener
                        }

                        val quantity = quantityInput.text.toString().toIntOrNull() ?: 1
                        val condition = conditionInput.text.toString().trim()
                        val language = languageInput.text.toString().trim()
                        val isFoil = foilCheckBox.isChecked
                        val selectedCard = cardOptions[selectedIndex]

                        if (quantity <= 0) {
                            quantityInput.error = "Quantity must be at least 1"
                            return@setOnClickListener
                        }

                        if (condition.isBlank()) {
                            conditionInput.error = "Condition is required"
                            return@setOnClickListener
                        }

                        if (language.isBlank()) {
                            languageInput.error = "Language is required"
                            return@setOnClickListener
                        }

                        dismiss()

                        addCardToCollection(
                            token = token,
                            card = selectedCard,
                            quantity = quantity,
                            condition = condition,
                            isFoil = isFoil,
                            language = language
                        )
                    }
                }

                show()
            }
    }

    private fun updateAutocompleteOptions(
        results: List<CardSearchResult>,
        mainSuggestions: MutableList<TextSuggestion>,
        numberSuggestions: MutableList<TextSuggestion>,
        raritySuggestions: MutableList<TextSuggestion>,
        artistSuggestions: MutableList<TextSuggestion>,
        mainAdapter: ArrayAdapter<String>,
        numberAdapter: ArrayAdapter<String>,
        rarityAdapter: ArrayAdapter<String>,
        artistAdapter: ArrayAdapter<String>
    ) {
        mainSuggestions.clear()
        numberSuggestions.clear()
        raritySuggestions.clear()
        artistSuggestions.clear()

        if (gameType == "mtg") {
            mainSuggestions.addAll(
                results
                    .map { TextSuggestion(label = it.name, value = it.name) }
                    .distinctBy { it.value.lowercase() }
                    .take(6)
            )
        } else {
            mainSuggestions.addAll(
                results
                    .mapNotNull { card ->
                        val setName = card.setName
                        val setCode = card.setCode

                        if (setName.isNullOrBlank() && setCode.isNullOrBlank()) {
                            null
                        } else {
                            val label = when {
                                !setName.isNullOrBlank() && !setCode.isNullOrBlank() -> "$setName ($setCode)"
                                !setName.isNullOrBlank() -> setName
                                else -> setCode ?: ""
                            }

                            TextSuggestion(
                                label = label,
                                value = setName ?: setCode ?: ""
                            )
                        }
                    }
                    .distinctBy { it.label.lowercase() }
                    .take(6)
            )

            numberSuggestions.addAll(
                results
                    .mapNotNull { card ->
                        val number = card.collectorNumber

                        if (number.isNullOrBlank()) {
                            null
                        } else {
                            TextSuggestion(
                                label = number,
                                value = number
                            )
                        }
                    }
                    .distinctBy { it.value.lowercase() }
                    .take(6)
            )
        }

        raritySuggestions.addAll(
            results
                .mapNotNull { card ->
                    val rarity = card.rarity

                    if (rarity.isNullOrBlank()) {
                        null
                    } else {
                        TextSuggestion(
                            label = rarity,
                            value = rarity
                        )
                    }
                }
                .distinctBy { it.value.lowercase() }
                .take(6)
        )

        artistSuggestions.addAll(
            results
                .mapNotNull { card ->
                    val artist = card.artistOrIllustrator

                    if (artist.isNullOrBlank()) {
                        null
                    } else {
                        TextSuggestion(
                            label = artist,
                            value = artist
                        )
                    }
                }
                .distinctBy { it.value.lowercase() }
                .take(6)
        )

        refreshAutocomplete(
            adapter = mainAdapter,
            values = mainSuggestions.map { it.label }
        )

        refreshAutocomplete(
            adapter = numberAdapter,
            values = numberSuggestions.map { it.label }
        )

        refreshAutocomplete(
            adapter = rarityAdapter,
            values = raritySuggestions.map { it.label }
        )

        refreshAutocomplete(
            adapter = artistAdapter,
            values = artistSuggestions.map { it.label }
        )
    }

    private fun showActiveDropdown(
        activeField: SuggestionField?,
        mainInput: AutoCompleteTextView,
        numberInput: AutoCompleteTextView,
        rarityInput: AutoCompleteTextView,
        artistInput: AutoCompleteTextView,
        mainSuggestions: List<TextSuggestion>,
        numberSuggestions: List<TextSuggestion>,
        raritySuggestions: List<TextSuggestion>,
        artistSuggestions: List<TextSuggestion>
    ) {
        when (activeField) {
            SuggestionField.MAIN -> showDropdownIfPossible(mainInput, mainSuggestions)
            SuggestionField.NUMBER -> showDropdownIfPossible(numberInput, numberSuggestions)
            SuggestionField.RARITY -> showDropdownIfPossible(rarityInput, raritySuggestions)
            SuggestionField.ARTIST -> showDropdownIfPossible(artistInput, artistSuggestions)
            null -> Unit
        }
    }

    private fun showDropdownIfPossible(
        input: AutoCompleteTextView,
        suggestions: List<TextSuggestion>
    ) {
        if (suggestions.isEmpty()) {
            input.dismissDropDown()
            return
        }

        if (input.hasFocus()) {
            input.post {
                input.showDropDown()
            }
        }
    }

    private fun autocompleteInput(hintText: String): AutoCompleteTextView {
        return AutoCompleteTextView(this).apply {
            hint = hintText
            threshold = 1
            inputType = InputType.TYPE_CLASS_TEXT
            background = getDrawable(R.drawable.input_background)
            setSingleLine(true)
        }
    }

    private fun refreshAutocomplete(
        adapter: ArrayAdapter<String>,
        values: List<String>
    ) {
        adapter.clear()
        adapter.addAll(values)
        adapter.notifyDataSetChanged()
    }

    private fun clearAutocomplete(adapter: ArrayAdapter<String>) {
        adapter.clear()
        adapter.notifyDataSetChanged()
    }

    private fun setInputText(
        input: AutoCompleteTextView,
        value: String
    ) {
        input.setText(value, false)
        input.setSelection(input.text.length)
    }

    private fun addCardToCollection(
        token: String,
        card: CardSearchResult,
        quantity: Int,
        condition: String,
        isFoil: Boolean,
        language: String
    ) {
        addCardButton.isEnabled = false

        executor.execute {
            try {
                val response = ApiClient.addCardToCollection(
                    token = token,
                    collectionId = collectionId,
                    cardId = card.cardId,
                    quantity = quantity,
                    cardCondition = condition,
                    isFoil = isFoil,
                    language = language
                )

                mainHandler.post {
                    addCardButton.isEnabled = true
                    renderCards(response.cards)
                    updateTotalPrice(response.totalPrice)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    addCardButton.isEnabled = true
                    Toast.makeText(
                        this,
                        e.message ?: "Could not add card",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun updateTotalPrice(totalPrice: Double) {
        totalPriceText.text = "Total: €${formatPrice(totalPrice)}"
    }

    private fun formatPrice(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}