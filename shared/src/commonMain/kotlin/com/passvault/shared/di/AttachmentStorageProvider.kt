package com.passvault.shared.di

import com.passvault.core.database.attachment.AttachmentBlobStore

/** Creates platform-protected private storage for encrypted attachment blobs. */
expect fun createAttachmentBlobStore(context: Any): AttachmentBlobStore
