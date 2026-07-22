package com.kriptic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kriptic.app.identity.Identity
import com.kriptic.app.identity.IdentityRepository
import com.kriptic.app.identity.UsernameRegistrationScreen
import com.kriptic.app.ui.nav.KripticNavHost
import com.kriptic.app.ui.theme.KripticTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = (application as KripticApplication).identityRepository
        val identity = repo.getIdentity()

        setContent {
            KripticTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (identity != null) {
                        KripticNavHost(identity = identity)
                    } else {
                        UsernameRegistrationScreen(
                            onIdentityCreated = { username ->
                                repo.createIdentity(username)
                                recreate()
                            }
                        )
                    }
                }
            }
        }
    }
}
