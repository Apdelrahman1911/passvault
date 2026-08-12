@file:OptIn(
    kotlin.concurrent.atomics.ExperimentalAtomicApi::class,
    kotlinx.cinterop.ExperimentalForeignApi::class,
)

package com.passvault.feature.credential.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.action_open_settings
import com.passvault.core.designsystem.generated.resources.ui_totp_camera_permission
import kotlin.concurrent.atomics.AtomicBoolean
import org.jetbrains.compose.resources.stringResource
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIApplicationState
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create

@Composable
internal actual fun PlatformTotpQrScanner(
    onPayload: (String) -> Unit,
    onError: () -> Unit,
    modifier: Modifier,
) {
    var permissionState by remember { mutableStateOf(currentCameraPermissionState()) }
    val isLifecycleResumed = rememberLifecycleResumed {
        permissionState = currentCameraPermissionState()
    }
    val isApplicationActive = rememberApplicationActive {
        permissionState = currentCameraPermissionState()
    }

    if (permissionState == CameraPermissionState.NOT_DETERMINED) {
        LaunchedEffect(Unit) {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) {
                dispatch_async(dispatch_get_main_queue()) {
                    permissionState = currentCameraPermissionState()
                }
            }
        }
    }

    when (permissionState) {
        CameraPermissionState.AUTHORIZED -> {
            if (isLifecycleResumed && isApplicationActive) {
                IosCameraQrPreview(onPayload, onError, modifier)
            } else {
                Box(modifier)
            }
        }
        CameraPermissionState.NOT_DETERMINED -> Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
        }
        CameraPermissionState.DENIED,
        CameraPermissionState.RESTRICTED -> CameraPermissionRecovery(onError, modifier)
    }
}

@Composable
private fun rememberLifecycleResumed(onResume: () -> Unit): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isLifecycleResumed by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    val currentOnResume = rememberUpdatedState(onResume)

    // Opening Settings does not recreate this composable. Re-read the native
    // authorization state when the app becomes active, and remove the camera
    // controller whenever the app is inactive so capture cannot continue
    // behind the SwiftUI privacy cover.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            isLifecycleResumed = source.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if (event == Lifecycle.Event.ON_RESUME) {
                currentOnResume.value()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return isLifecycleResumed
}

@Composable
private fun rememberApplicationActive(onActive: () -> Unit): Boolean {
    var isApplicationActive by remember {
        mutableStateOf(
            UIApplication.sharedApplication.applicationState ==
                UIApplicationState.UIApplicationStateActive,
        )
    }
    val currentOnActive = rememberUpdatedState(onActive)

    // A Compose UIViewController can remain RESUMED while its SwiftUI scene
    // becomes inactive. Native application notifications close that gap so
    // camera capture stops behind the privacy cover and while backgrounded.
    DisposableEffect(Unit) {
        val notificationCenter = NSNotificationCenter.defaultCenter
        val resignedObserver = notificationCenter.addObserverForName(
            name = UIApplicationWillResignActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) {
            isApplicationActive = false
        }
        val activeObserver = notificationCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) {
            currentOnActive.value()
            isApplicationActive = true
        }
        onDispose {
            notificationCenter.removeObserver(resignedObserver)
            notificationCenter.removeObserver(activeObserver)
        }
    }
    return isApplicationActive
}

@Composable
private fun IosCameraQrPreview(
    onPayload: (String) -> Unit,
    onError: () -> Unit,
    modifier: Modifier,
) {
    val currentOnPayload = rememberUpdatedState(onPayload)
    val currentOnError = rememberUpdatedState(onError)
    val controller = remember { ScannerControllerHolder() }

    UIKitView(
        factory = {
            CameraPreviewView().also { previewView ->
                controller.value = IosQrScannerController(
                    previewView = previewView,
                    onPayload = { currentOnPayload.value(it) },
                    onError = { currentOnError.value() },
                ).also(IosQrScannerController::start)
            }
        },
        modifier = modifier,
        onRelease = {
            controller.value?.stop()
            controller.value = null
        },
    )

    DisposableEffect(Unit) {
        onDispose {
            controller.value?.stop()
            controller.value = null
        }
    }
}

@Composable
private fun CameraPermissionRecovery(
    onError: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(Res.string.ui_totp_camera_permission))
        Button(onClick = { openAppSettings(onError) }) {
            Text(stringResource(Res.string.action_open_settings))
        }
    }
}

private fun openAppSettings(onError: () -> Unit) {
    val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
    if (url == null) {
        onError()
        return
    }
    UIApplication.sharedApplication.openURL(
        url = url,
        options = emptyMap<Any?, Any>(),
        completionHandler = { opened ->
            if (!opened) {
                dispatch_async(dispatch_get_main_queue()) { onError() }
            }
        },
    )
}

private fun currentCameraPermissionState(): CameraPermissionState =
    when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
        AVAuthorizationStatusAuthorized -> CameraPermissionState.AUTHORIZED
        AVAuthorizationStatusNotDetermined -> CameraPermissionState.NOT_DETERMINED
        AVAuthorizationStatusRestricted -> CameraPermissionState.RESTRICTED
        AVAuthorizationStatusDenied -> CameraPermissionState.DENIED
        else -> CameraPermissionState.DENIED
    }

private enum class CameraPermissionState {
    AUTHORIZED,
    NOT_DETERMINED,
    DENIED,
    RESTRICTED,
}

private class ScannerControllerHolder {
    var value: IosQrScannerController? = null
}

private class CameraPreviewView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    var previewLayer: AVCaptureVideoPreviewLayer? = null

    override fun layoutSubviews() {
        super.layoutSubviews()
        previewLayer?.frame = bounds
    }
}

private class IosQrScannerController(
    private val previewView: CameraPreviewView,
    private val onPayload: (String) -> Unit,
    private val onError: () -> Unit,
) {
    /** Read on the main and capture queues. */
    private val disposed = AtomicBoolean(false)

    /** Main-thread state. */
    private var delivered = false

    /** Capture-session state is confined to [sessionQueue]. */
    private val sessionQueue = dispatch_queue_create("com.passvault.qr.capture", null)
    private var session: AVCaptureSession? = null
    private var delegate: QrMetadataDelegate? = null

    fun start() {
        if (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) ==
            AVAuthorizationStatusAuthorized
        ) {
            configureSession()
        } else {
            reportError()
        }
    }

    private fun configureSession() {
        if (disposed.load()) return
        dispatch_async(sessionQueue) {
            if (disposed.load()) return@dispatch_async
            val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            if (device == null) {
                reportErrorOnMain()
                return@dispatch_async
            }
            val input = AVCaptureDeviceInput(device, null)
            val output = AVCaptureMetadataOutput()
            val captureSession = AVCaptureSession()
            if (!captureSession.canAddInput(input) || !captureSession.canAddOutput(output)) {
                reportErrorOnMain()
                return@dispatch_async
            }

            captureSession.addInput(input)
            captureSession.addOutput(output)
            val metadataDelegate = QrMetadataDelegate { payload -> deliverOnMain(payload) }
            delegate = metadataDelegate
            output.setMetadataObjectsDelegate(metadataDelegate, dispatch_get_main_queue())
            output.metadataObjectTypes = listOfNotNull(AVMetadataObjectTypeQRCode)
            session = captureSession
            if (disposed.load()) {
                session = null
                delegate = null
                return@dispatch_async
            }
            captureSession.startRunning()

            dispatch_async(dispatch_get_main_queue()) {
                if (disposed.load()) return@dispatch_async
                val layer = AVCaptureVideoPreviewLayer(session = captureSession).apply {
                    videoGravity = AVLayerVideoGravityResizeAspectFill
                    frame = previewView.bounds
                }
                previewView.previewLayer = layer
                previewView.layer.addSublayer(layer)
            }
        }
    }

    private fun reportError() {
        if (!disposed.load() && !delivered) {
            delivered = true
            onError()
        }
    }

    private fun reportErrorOnMain() {
        dispatch_async(dispatch_get_main_queue()) { reportError() }
    }

    private fun deliverOnMain(payload: String) {
        if (!disposed.load() && !delivered) {
            delivered = true
            stop()
            onPayload(payload)
        }
    }

    fun stop() {
        if (!disposed.compareAndSet(expectedValue = false, newValue = true)) return
        previewView.previewLayer?.removeFromSuperlayer()
        previewView.previewLayer = null
        dispatch_async(sessionQueue) {
            session?.stopRunning()
            session = null
            delegate = null
        }
    }
}

private class QrMetadataDelegate(
    private val onPayload: (String) -> Unit,
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        didOutputMetadataObjects
            .filterIsInstance<AVMetadataMachineReadableCodeObject>()
            .firstNotNullOfOrNull { it.stringValue }
            ?.let(onPayload)
    }
}
