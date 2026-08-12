package com.passvault.feature.credential.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BinaryBitmap
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.action_open_settings
import com.passvault.core.designsystem.generated.resources.action_retry
import com.passvault.core.designsystem.generated.resources.ui_totp_camera_permission
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import org.jetbrains.compose.resources.stringResource

@Composable
internal actual fun PlatformTotpQrScanner(
    onPayload: (String) -> Unit,
    onError: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        permissionRequested = true
    }
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) {
        if (!hasPermission && !permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        CameraQrPreview(
            onPayload = onPayload,
            onError = onError,
            modifier = modifier,
        )
    } else {
        val canRequestAgain = context.findActivity()?.let { activity ->
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA,
            )
        } == true
        CameraPermissionPrompt(
            permissionRequested = permissionRequested,
            canRequestAgain = canRequestAgain,
            onRetry = {
                permissionRequested = true
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onOpenSettings = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                )
                runCatching { settingsLauncher.launch(intent) }.onFailure { onError() }
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun CameraPermissionPrompt(
    permissionRequested: Boolean,
    canRequestAgain: Boolean,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(Res.string.ui_totp_camera_permission))
        Button(onClick = if (!permissionRequested || canRequestAgain) onRetry else onOpenSettings) {
            Text(
                stringResource(
                    if (!permissionRequested || canRequestAgain) {
                        Res.string.action_retry
                    } else {
                        Res.string.action_open_settings
                    },
                ),
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun CameraQrPreview(
    onPayload: (String) -> Unit,
    onError: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val currentOnPayload = rememberUpdatedState(onPayload)
    val currentOnError = rememberUpdatedState(onError)

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize(),
    )

    DisposableEffect(previewView, lifecycleOwner) {
        val session = AndroidQrCameraSession(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            onPayload = { payload -> currentOnPayload.value(payload) },
            onError = { currentOnError.value() },
        )
        session.start()
        onDispose(session::dispose)
    }
}

private class AndroidQrCameraSession(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val onPayload: (String) -> Unit,
    private val onError: () -> Unit,
) {
    private val analyzerExecutor = Executors.newSingleThreadExecutor()
    private val disposed = AtomicBoolean(false)
    private val delivered = AtomicBoolean(false)
    private val mainExecutor = ContextCompat.getMainExecutor(context)
    private var cameraProvider: ProcessCameraProvider? = null

    fun start() {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener(
            {
                if (!disposed.get()) {
                    runCatching { bind(providerFuture.get()) }
                        .onFailure { reportError() }
                }
            },
            mainExecutor,
        )
    }

    private fun bind(provider: ProcessCameraProvider) {
        cameraProvider = provider
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        val analysis = createAnalysis(provider, preview)
        provider.unbindAll()
        provider.bindToLifecycle(lifecycleOwner, selectCamera(provider), preview, analysis)
    }

    private fun createAnalysis(
        provider: ProcessCameraProvider,
        preview: Preview,
    ): ImageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also { analysis ->
            analysis.setAnalyzer(analyzerExecutor) { image ->
                analyzeImage(image, provider, preview, analysis)
            }
        }

    private fun analyzeImage(
        image: ImageProxy,
        provider: ProcessCameraProvider,
        preview: Preview,
        analysis: ImageAnalysis,
    ) {
        try {
            val payload = decodeQrPayload(image)
            if (payload != null && delivered.compareAndSet(false, true)) {
                mainExecutor.execute {
                    if (!disposed.get()) {
                        runCatching { provider.unbind(preview, analysis) }
                        onPayload(payload)
                    }
                }
            }
        } finally {
            image.close()
        }
    }

    private fun reportError() {
        cameraProvider?.unbindAll()
        if (!disposed.get() && delivered.compareAndSet(false, true)) {
            onError()
        }
    }

    fun dispose() {
        disposed.set(true)
        cameraProvider?.unbindAll()
        analyzerExecutor.shutdown()
    }
}

private fun selectCamera(provider: ProcessCameraProvider): CameraSelector = when {
    provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
    provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
    else -> error("No camera is available")
}

private fun decodeQrPayload(image: ImageProxy): String? {
    return try {
        val yPlane = image.planes.first()
        val buffer = yPlane.buffer
        val width = image.width
        val height = image.height
        val rowStride = yPlane.rowStride
        val pixelStride = yPlane.pixelStride
        require(width > 0 && height > 0)
        val pixelCount = width.toLong() * height.toLong()
        require(pixelCount in 1..MAX_CAMERA_PIXELS)
        require(rowStride in 1..buffer.limit())
        require(rowStride <= MAX_CAMERA_ROW_BYTES)
        require(pixelStride in 1..MAX_CAMERA_PIXEL_STRIDE)
        val luminance = ByteArray(pixelCount.toInt())

        try {
            copyLuminancePlane(buffer, width, height, rowStride, pixelStride, luminance)

            val source = PlanarYUVLuminanceSource(
                luminance,
                width,
                height,
                0,
                0,
                width,
                height,
                false,
            )
            QRCodeReader().decode(BinaryBitmap(HybridBinarizer(source))).text
        } finally {
            luminance.fill(0)
        }
    } catch (_: Exception) {
        null
    }
}

private fun copyLuminancePlane(
    buffer: ByteBuffer,
    width: Int,
    height: Int,
    rowStride: Int,
    pixelStride: Int,
    destination: ByteArray,
) {
    val row = ByteArray(rowStride)
    try {
        val requiredBytes = (width - 1L) * pixelStride.toLong() + 1L
        require(requiredBytes in 1..rowStride.toLong())
        for (y in 0 until height) {
            val rowStart = y.toLong() * rowStride.toLong()
            require(rowStart <= buffer.limit().toLong())
            buffer.position(rowStart.toInt())
            val bytesInRow = minOf(rowStride, buffer.remaining())
            require(requiredBytes <= bytesInRow.toLong())
            buffer.get(row, 0, bytesInRow)
            for (x in 0 until width) {
                val sourceIndex = x.toLong() * pixelStride.toLong()
                destination[y * width + x] = row[sourceIndex.toInt()]
            }
        }
    } finally {
        row.fill(0)
    }
}

private const val MAX_CAMERA_PIXELS = 40_000_000L
private const val MAX_CAMERA_ROW_BYTES = 1_048_576
private const val MAX_CAMERA_PIXEL_STRIDE = 16
