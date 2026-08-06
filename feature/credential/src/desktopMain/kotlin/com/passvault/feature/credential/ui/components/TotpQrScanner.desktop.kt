package com.passvault.feature.credential.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.ui_totp_choose_qr_image
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource

@Composable
internal actual fun PlatformTotpQrScanner(
    onPayload: (String) -> Unit,
    onError: () -> Unit,
    modifier: Modifier,
) {
    val scope = rememberCoroutineScope()
    var isReading by remember { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isReading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    scope.launch {
                        isReading = true
                        when (val result = withContext(Dispatchers.IO) { chooseAndDecodeQrImage() }) {
                            DesktopQrResult.Cancelled -> Unit
                            DesktopQrResult.Error -> onError()
                            is DesktopQrResult.Success -> onPayload(result.payload)
                        }
                        isReading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.ui_totp_choose_qr_image))
            }
        }
    }
}

private fun chooseAndDecodeQrImage(): DesktopQrResult {
    val dialog = FileDialog(
        null as Frame?,
        "Choose authenticator QR image",
        FileDialog.LOAD,
    ).apply {
        setFilenameFilter { _, name ->
            name.endsWith(".png", ignoreCase = true) ||
                name.endsWith(".jpg", ignoreCase = true) ||
                name.endsWith(".jpeg", ignoreCase = true)
        }
    }
    val file = try {
        dialog.isVisible = true
        val directory = dialog.directory ?: return DesktopQrResult.Cancelled
        val fileName = dialog.file ?: return DesktopQrResult.Cancelled
        File(directory, fileName)
    } finally {
        dialog.dispose()
    }
    if (!file.isFile || file.length() > MAX_IMAGE_BYTES) return DesktopQrResult.Error

    return try {
        val image = ImageIO.createImageInputStream(file)?.use { stream ->
            val readers = ImageIO.getImageReaders(stream)
            require(readers.hasNext())
            val reader = readers.next()
            try {
                reader.input = stream
                val width = reader.getWidth(0)
                val height = reader.getHeight(0)
                require(width > 0 && height > 0)
                require(width.toLong() * height.toLong() <= MAX_IMAGE_PIXELS)
                reader.read(0)
            } finally {
                reader.dispose()
            }
        } ?: error("Unsupported image")

        val pixels = IntArray(image.width * image.height)
        image.getRGB(0, 0, image.width, image.height, pixels, 0, image.width)
        val source = RGBLuminanceSource(image.width, image.height, pixels)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        DesktopQrResult.Success(QRCodeReader().decode(bitmap).text)
    } catch (_: Exception) {
        DesktopQrResult.Error
    }
}

private sealed interface DesktopQrResult {
    data object Cancelled : DesktopQrResult
    data object Error : DesktopQrResult
    data class Success(val payload: String) : DesktopQrResult
}

private const val MAX_IMAGE_BYTES = 10L * 1_024L * 1_024L
private const val MAX_IMAGE_PIXELS = 25_000_000L
