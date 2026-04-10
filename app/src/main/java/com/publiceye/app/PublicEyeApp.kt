package com.publiceye.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class — entry point for Hilt dependency injection.
 * Also the right place to initialise app-wide SDKs (Firebase is auto-initialised via google-services plugin).
 */
@HiltAndroidApp
class PublicEyeApp : Application()
