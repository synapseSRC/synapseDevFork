package com.synapse.social.studioasinc.shared.core.util

import platform.Foundation.NSUUID

actual fun randomUUID(): String = NSUUID().UUIDString()
