package com.elysium.softwork

import android.app.Application

/**
 * Process-wide entry point for the SoftWork Employee client.
 *
 * Phase 1 keeps this empty — locale persistence is owned by AppCompat (see
 * `AppLocalesMetadataHolderService` in the manifest), and dependency wiring lives in the
 * future shared bootstrap. Future shared init (Retrofit client, Room database, crash
 * reporters) will be installed here.
 */
class SoftWorkApplication : Application()
