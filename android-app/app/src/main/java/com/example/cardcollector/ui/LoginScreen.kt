package com.example.cardcollector.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import com.example.cardcollector.R
import com.example.cardcollector.api.ApiClient
import com.example.cardcollector.api.ApiException
import com.example.cardcollector.session.SessionManager
import org.json.JSONObject
import java.util.concurrent.Executors

class LoginScreen : Activity() {

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SessionManager.isLoggedIn(this)) {
            openCollections()
            return
        }

        setContentView(createContent())
    }

    private fun createContent(): View {
        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(getColor(R.color.background))

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(48), dp(24), dp(24))
        }

        val title = TextView(this).apply {
            text = "Card Collector"
            textSize = 32f
            setTextColor(getColor(R.color.text_primary))
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val subtitle = TextView(this).apply {
            text = "Login"
            textSize = 22f
            setTextColor(getColor(R.color.text_secondary))
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(24))
        }

        emailInput = normalInput(
            hintText = "Email",
            type = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        passwordInput = passwordInput("Password")

        statusText = TextView(this).apply {
            text = ""
            setTextColor(getColor(R.color.danger))
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(8))
        }

        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }

        loginButton = Button(this).apply {
            text = "Login"
            setTextColor(android.graphics.Color.WHITE)
            background = getDrawable(R.drawable.primary_button_background)
            setOnClickListener { login() }
        }

        val registerButton = Button(this).apply {
            text = "Sign up"
            setOnClickListener {
                startActivity(Intent(this@LoginScreen, RegisterScreen::class.java))
            }
        }

        container.addView(title, matchWrap())
        container.addView(subtitle, matchWrap())
        container.addView(emailInput, matchWrapWithMargin(bottom = 14))
        container.addView(passwordInput, matchWrapWithMargin(bottom = 10))
        container.addView(statusText, matchWrap())
        container.addView(progressBar, wrapCentered())
        container.addView(loginButton, matchWrapWithMargin(top = 8, bottom = 8))
        container.addView(registerButton, matchWrapWithMargin(top = 4))

        scrollView.addView(container)
        return scrollView
    }

    private fun login() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (email.isBlank() || password.isBlank()) {
            statusText.text = "Email and password are required."
            return
        }

        setLoading(true)

        executor.execute {
            try {
                val response: JSONObject = ApiClient.login(email, password)

                SessionManager.saveSession(
                    context = this,
                    token = response.getString("token"),
                    userId = response.getInt("userId"),
                    username = response.getString("username"),
                    email = response.getString("email")
                )

                mainHandler.post {
                    setLoading(false)
                    openCollections()
                }
            } catch (e: ApiException) {
                mainHandler.post {
                    setLoading(false)
                    statusText.text = e.message ?: "Login failed"
                }
            } catch (e: Exception) {
                mainHandler.post {
                    setLoading(false)
                    statusText.text = "Could not connect to the backend. Is Docker running?"
                }
            }
        }
    }

    private fun normalInput(
        hintText: String,
        type: Int
    ): EditText {
        return EditText(this).apply {
            hint = hintText
            inputType = type
            typeface = Typeface.DEFAULT
            background = getDrawable(R.drawable.input_background)
            setSingleLine(true)
        }
    }

    private fun passwordInput(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText

            setSingleLine(true)

            setRawInputType(
                InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD or
                        InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            )

            transformationMethod = FixedPasswordTransformationMethod()
            typeface = Typeface.DEFAULT
            background = getDrawable(R.drawable.input_background)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        loginButton.isEnabled = !isLoading
    }

    private fun openCollections() {
        startActivity(Intent(this, CollectionsScreen::class.java))
        finish()
    }

    private fun matchWrap(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun matchWrapWithMargin(
        top: Int = 0,
        bottom: Int = 0
    ): LinearLayout.LayoutParams {
        return matchWrap().apply {
            setMargins(0, dp(top), 0, dp(bottom))
        }
    }

    private fun wrapCentered(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}