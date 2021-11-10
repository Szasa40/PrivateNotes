package com.example.privatenotesimport android.content.Contextimport android.content.Intentimport android.graphics.drawable.AnimationDrawableimport android.hardware.fingerprint.FingerprintManagerimport androidx.biometric.BiometricPrompt;import androidx.appcompat.app.AppCompatActivityimport android.os.Bundleimport android.text.InputTypeimport android.view.Viewimport android.view.WindowManagerimport android.widget.EditTextimport android.widget.ImageButtonimport android.widget.Toastimport androidx.biometric.BiometricManagerimport androidx.constraintlayout.widget.ConstraintLayoutimport androidx.core.content.ContextCompatimport java.io.*import java.lang.StringBuilderimport java.util.concurrent.Executorclass MainActivity : AppCompatActivity(){    lateinit var executor: Executor    lateinit var biometricPrompt: BiometricPrompt    lateinit var promptInfo: BiometricPrompt.PromptInfo    lateinit var button : ImageButton    lateinit var fm : FingerprintManager    override fun onCreate(savedInstanceState: Bundle?)    {        //window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN        supportActionBar?.hide()        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)        super.onCreate(savedInstanceState)        setContentView(R.layout.activity_main)        val constraintLayout = findViewById<ConstraintLayout>(R.id.layoutAnimatedBackground)        val animationDrawable: AnimationDrawable = constraintLayout.background as AnimationDrawable        animationDrawable.setEnterFadeDuration(2000)        animationDrawable.setExitFadeDuration(4000)        animationDrawable.start()                button = findViewById(R.id.imageButton)        executor = ContextCompat.getMainExecutor(this)        fm = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager        if(!fm.hasEnrolledFingerprints())        {            Toast.makeText(this@MainActivity,"Nincs rögzített újlenyomat. Addj meg egyet!", Toast.LENGTH_LONG).show()            startActivityForResult(Intent(android.provider.Settings.ACTION_BIOMETRIC_ENROLL), 0)        }        biometricPrompt = BiometricPrompt(this@MainActivity, executor, object : BiometricPrompt.AuthenticationCallback() {            override fun onAuthenticationError(errorCode: Int, errString: CharSequence)            {                super.onAuthenticationError(errorCode, errString)                //Toast.makeText(this@MainActivity,"$errString", Toast.LENGTH_SHORT).show()                if (errString.equals("Jelszó"))                {                    val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this@MainActivity)                    builder.setTitle("Jelszó")                    val input = EditText(this@MainActivity)                    var password = ""                    input.setHint("Írd be a jeszavad!")                    input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD                    builder.setView(input)                    builder.setPositiveButton("OK") { _, _ ->                        password = input.text.toString()                        if (readFrom(this@MainActivity) == encrypt(password))                        {                            val intent = Intent(this@MainActivity, Note::class.java)                            startActivity(intent)                            biometricPrompt.cancelAuthentication()                            finish()                        }                        else                        {                            Toast.makeText(this@MainActivity,"Helytelen jelszó", Toast.LENGTH_SHORT).show()                        }                    }                    builder.show()                }            }            override fun onAuthenticationFailed()            {                super.onAuthenticationFailed()                //Toast.makeText(this@MainActivity,"HIBA", Toast.LENGTH_SHORT).show()            }            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult)            {                super.onAuthenticationSucceeded(result)                Toast.makeText(this@MainActivity,"Sikeres Hitelesítés!", Toast.LENGTH_SHORT).show()                val intent = Intent(this@MainActivity, Note::class.java)                startActivity(intent)                finish()            }        })        promptInfo = BiometricPrompt.PromptInfo.Builder()            .setTitle("Biometrikus Azonosítás")            .setNegativeButtonText("Jelszó")            .build()        button.setOnClickListener()        {            readFrom(this)        }    }    fun readFrom(context : Context): String    {        var ret = ""        try        {            val inputStream = context.openFileInput("pass.ps")            if(inputStream != null)            {                val inputStreamReader = InputStreamReader(inputStream)                val bufferedReader = BufferedReader(inputStreamReader)                var receiveString: String? = null                val stringBuilder = StringBuilder()                while ( {receiveString = bufferedReader.readLine(); receiveString}() != null )                {                    stringBuilder.append(receiveString)                }                inputStream.close()                ret = stringBuilder.toString()                biometricPrompt.authenticate(promptInfo)            }        }        catch (e : FileNotFoundException)        {            val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)            builder.setTitle("Jelszó")            val input = EditText(this)            var password = ""            input.setHint("Először adj meg egy jelszót")            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD            builder.setView(input)            builder.setPositiveButton("OK") { _, _ ->                password = input.text.toString()                if (password.length < 5)                {                    Toast.makeText(this, "A jelszó hossza minimum 5 karakter", Toast.LENGTH_LONG).show()                }                else                {                    writeTo(encrypt(password), this)                }            }            builder.show()        }        return ret    }    fun writeTo(data : String,  context: Context)    {        try        {            val outputStreamWriter = OutputStreamWriter(context.openFileOutput("pass.ps", MODE_PRIVATE))            outputStreamWriter.write(data)            outputStreamWriter.close()            //Toast.makeText(this, filesDir.toString(), Toast.LENGTH_LONG).show()        }        catch (e : IOException)        {        }    }    fun encrypt(password: String): String    {        val key1 = 0xe2179        val key2 = 0x510e5        var pas = ""        for (c: Char in password)        {            pas += (c.code xor key1 xor key2).toString()        }        return pas    }}