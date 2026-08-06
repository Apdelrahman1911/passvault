@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.passvault.feature.credential.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
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
import platform.UIKit.UIView
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue

@Composable
internal actual fun PlatformTotpQrScanner(
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
    private var active = true
    private var delivered = false
    private var session: AVCaptureSession? = null
    private var delegate: QrMetadataDelegate? = null

    fun start() {
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> configureSession()
            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    dispatch_async(dispatch_get_main_queue()) {
                        if (!active) return@dispatch_async
                        if (granted) configureSession() else reportError()
                    }
                }
            }
            else -> reportError()
        }
    }

    private fun configureSession() {
        if (!active) return
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            ?: return reportError()
        val input = AVCaptureDeviceInput(device, null)
        val output = AVCaptureMetadataOutput()
        val captureSession = AVCaptureSession()
        if (!captureSession.canAddInput(input) || !captureSession.canAddOutput(output)) {
            reportError()
            return
        }

        captureSession.addInput(input)
        captureSession.addOutput(output)
        val metadataDelegate = QrMetadataDelegate { payload ->
            if (active && !delivered) {
                delivered = true
                onPayload(payload)
            }
        }
        delegate = metadataDelegate
        output.setMetadataObjectsDelegate(metadataDelegate, dispatch_get_main_queue())
        output.metadataObjectTypes = listOfNotNull(AVMetadataObjectTypeQRCode)

        val layer = AVCaptureVideoPreviewLayer(session = captureSession).apply {
            videoGravity = AVLayerVideoGravityResizeAspectFill
            frame = previewView.bounds
        }
        previewView.previewLayer = layer
        previewView.layer.addSublayer(layer)
        session = captureSession
        dispatch_async(
            dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u),
        ) {
            if (active) captureSession.startRunning()
        }
    }

    private fun reportError() {
        if (active && !delivered) {
            delivered = true
            onError()
        }
    }

    fun stop() {
        if (!active) return
        active = false
        delegate = null
        previewView.previewLayer?.removeFromSuperlayer()
        previewView.previewLayer = null
        val captureSession = session
        session = null
        dispatch_async(
            dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u),
        ) {
            captureSession?.stopRunning()
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
