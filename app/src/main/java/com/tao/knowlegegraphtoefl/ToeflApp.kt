package com.tao.knowlegegraphtoefl

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.tao.knowlegegraphtoefl.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ToeflApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Debug builds talk to the real cloud Firebase project by default so data persists
        // permanently across rebuilds. Set firebaseUseEmulator=true in local.properties to
        // switch back to the local emulator suite for offline development.
        if (BuildConfig.DEBUG && BuildConfig.USE_FIREBASE_EMULATOR) {
            val defaultOptions = FirebaseApp.getInstance().options
            val emulatorApp = FirebaseApp.initializeApp(
                this,
                FirebaseOptions.Builder()
                    .setApiKey(defaultOptions.apiKey.orEmpty())
                    .setApplicationId(defaultOptions.applicationId.orEmpty())
                    .setProjectId(BuildConfig.FIREBASE_EMULATOR_PROJECT_ID)
                    .build(),
                LOCAL_EMULATOR_APP_NAME
            ) ?: error("Could not initialize local Firebase app")
            FirebaseAuth.getInstance(emulatorApp)
                .useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, 9099)
            FirebaseFunctions.getInstance(emulatorApp)
                .useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, 5001)
            FirebaseFirestore.getInstance(emulatorApp)
                .useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, 8088)
        }
    }

    companion object {
        const val LOCAL_EMULATOR_APP_NAME = "local-emulator"

        /**
         * Returns the [FirebaseApp] repositories should use: the named emulator app when the
         * local emulator suite is enabled, or the default app (pointing at the real cloud
         * project) otherwise.
         */
        fun activeFirebaseApp(): FirebaseApp =
            if (BuildConfig.DEBUG && BuildConfig.USE_FIREBASE_EMULATOR) {
                FirebaseApp.getInstance(LOCAL_EMULATOR_APP_NAME)
            } else {
                FirebaseApp.getInstance()
            }
    }
}
