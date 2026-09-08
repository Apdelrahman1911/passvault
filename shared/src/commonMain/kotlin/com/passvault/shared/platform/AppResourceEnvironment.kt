package com.passvault.shared.platform

import androidx.compose.runtime.Composable

/** Publish only from verified app content, never from its outer startup-language provider. */
@Composable
internal expect fun PublishAppResourceEnvironment()
