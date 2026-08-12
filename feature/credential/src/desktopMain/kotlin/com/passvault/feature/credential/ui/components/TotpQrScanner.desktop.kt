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
import java.awt.KeyboardFocusManager
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferDouble
import java.awt.image.DataBufferFloat
import java.awt.image.DataBufferInt
import java.awt.image.DataBufferShort
import java.awt.image.DataBufferUShort
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
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
    val dialogTitle = stringResource(Res.string.ui_totp_choose_qr_image)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isReading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    scope.launch {
                        isReading = true
                        try {
                            val selectedFile = withContext(Dispatchers.Swing) {
                                chooseQrImage(dialogTitle)
                            }
                            val result = if (selectedFile == null) {
                                DesktopQrResult.Cancelled
                            } else {
                                withContext(Dispatchers.IO) { decodeQrImage(selectedFile) }
                            }
                            when (result) {
                                DesktopQrResult.Cancelled -> Unit
                                DesktopQrResult.Error -> onError()
                                is DesktopQrResult.Success -> onPayload(result.payload)
                            }
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (_: Exception) {
                            onError()
                        } finally {
                            isReading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(dialogTitle)
            }
        }
    }
}

private fun chooseQrImage(title: String): File? {
    val dialog = FileDialog(
        activeOwnerFrame(),
        title,
        FileDialog.LOAD,
    ).apply {
        setFilenameFilter { _, name ->
            name.endsWith(".png", ignoreCase = true) ||
                name.endsWith(".jpg", ignoreCase = true) ||
                name.endsWith(".jpeg", ignoreCase = true)
        }
    }
    return try {
        dialog.isVisible = true
        val directory = dialog.directory
        val fileName = dialog.file
        if (directory == null || fileName == null) null else File(directory, fileName)
    } finally {
        dialog.dispose()
    }
}

private fun activeOwnerFrame(): Frame? {
    val activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
    return activeWindow as? Frame ?: Frame.getFrames().firstOrNull { it.isActive }
}

private fun decodeQrImage(file: File): DesktopQrResult {
    if (!file.isFile || file.length() > MAX_IMAGE_BYTES) return DesktopQrResult.Error

    return try {
        // Read directly from the selected file. ImageIO's default generic
        // stream may cache a QR image containing an OTP secret in a second
        // temporary file.
        val image = FileImageInputStream(file).use { stream ->
            val readers = ImageIO.getImageReaders(stream)
            require(readers.hasNext())
            val reader = readers.next()
            try {
                reader.input = stream
                val width = reader.getWidth(0)
                val height = reader.getHeight(0)
                requireValidImageDimensions(width, height)
                reader.read(0).also { decoded ->
                    // Re-check the actual decoded raster. A malformed or custom
                    // ImageIO provider must not bypass the header dimensions and
                    // trigger an unbounded pixel-array allocation below.
                    requireValidImageDimensions(decoded.width, decoded.height)
                }
            } finally {
                reader.dispose()
            }
        }

        try {
            val pixels = IntArray(image.width * image.height)
            try {
                image.getRGB(0, 0, image.width, image.height, pixels, 0, image.width)
                val source = RGBLuminanceSource(image.width, image.height, pixels)
                try {
                    val bitmap = BinaryBitmap(HybridBinarizer(source))
                    DesktopQrResult.Success(QRCodeReader().decode(bitmap).text)
                } finally {
                    source.matrix.fill(0)
                }
            } finally {
                pixels.fill(0)
            }
        } finally {
            image.clearPixelStorage()
        }
    } catch (_: Exception) {
        DesktopQrResult.Error
    }
}

private fun requireValidImageDimensions(width: Int, height: Int) {
    require(width > 0 && height > 0)
    require(width.toLong() * height.toLong() <= MAX_IMAGE_PIXELS)
}

private fun BufferedImage.clearPixelStorage() {
    when (val buffer = raster.dataBuffer) {
        is DataBufferByte -> buffer.bankData.forEach { it.fill(0) }
        is DataBufferInt -> buffer.bankData.forEach { it.fill(0) }
        is DataBufferShort -> buffer.bankData.forEach { it.fill(0) }
        is DataBufferUShort -> buffer.bankData.forEach { it.fill(0) }
        is DataBufferFloat -> buffer.bankData.forEach { it.fill(0f) }
        is DataBufferDouble -> buffer.bankData.forEach { it.fill(0.0) }
        else -> {
            repeat(buffer.numBanks) { bank ->
                repeat(buffer.size) { index -> buffer.setElem(bank, index, 0) }
            }
        }
    }
    flush()
}

private sealed interface DesktopQrResult {
    data object Cancelled : DesktopQrResult
    data object Error : DesktopQrResult
    data class Success(val payload: String) : DesktopQrResult
}

private const val MAX_IMAGE_BYTES = 10L * 1_024L * 1_024L
private const val MAX_IMAGE_PIXELS = 25_000_000L
