package com.humanperformcenter.shared

import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.darwin.NSObject
/*import sharedKit.PaymentViewController

actual fun openPaymentWebView(url: String, onResult: (Boolean) -> Unit) {
    val rootViewController: UIViewController? =
        UIApplication.sharedApplication.keyWindow?.rootViewController

    val paymentVC = PaymentViewController().apply {
        paymentUrl = url
    }

    val observer = object : NSObject() {
        @ObjCAction
        fun handleNotification(notification: NSNotification) {
            val info = notification.userInfo
            val result = info?.get("success") as? Boolean ?: false
            onResult(result)

            // Eliminar el observer después de usarlo
            NSNotificationCenter.defaultCenter.removeObserver(this)
        }
    }

    // Registrar observer para "AddonPaymentResult"
    NSNotificationCenter.defaultCenter.addObserver(
        observer,
        NSSelectorFromString("handleNotification:"),
        name = NSNotification.Name("AddonPaymentResult"),
        `object` = null
    )

    rootViewController?.presentViewController(paymentVC, animated = true, completion = null)
}
*/