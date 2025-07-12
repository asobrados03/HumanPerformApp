import UIKit
import WebKit

@objcMembers
public class PaymentViewController: UIViewController, WKNavigationDelegate {
    public var paymentUrl: String = ""

    private var webView: WKWebView!

    public override func viewDidLoad() {
        super.viewDidLoad()

        webView = WKWebView(frame: self.view.bounds)
        webView.navigationDelegate = self
        view.addSubview(webView)

        if let url = URL(string: paymentUrl) {
            webView.load(URLRequest(url: url))
        }
    }

    public func webView(_ webView: WKWebView,
                        decidePolicyFor navigationAction: WKNavigationAction,
                        decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        guard let url = navigationAction.request.url?.absoluteString else {
            decisionHandler(.allow)
            return
        }

        if url.hasPrefix("https://miapp.com/pago_ok") {
            notifyResult(success: true)
            decisionHandler(.cancel)
        } else if url.hasPrefix("https://miapp.com/pago_error") || url.hasPrefix("https://miapp.com/pago_cancel") {
            notifyResult(success: false)
            decisionHandler(.cancel)
        } else {
            decisionHandler(.allow)
        }
    }

    private func notifyResult(success: Bool) {
        NotificationCenter.default.post(
            name: NSNotification.Name("AddonPaymentResult"),
            object: nil,
            userInfo: ["success": success]
        )
        dismiss(animated: true)
    }
}
