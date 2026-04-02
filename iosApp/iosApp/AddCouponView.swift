//
//  AddCouponView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import shared

struct AddCouponView: View {
    @State private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()
    @State private var couponsVM = SharedDependencies.shared.makeUserCouponsViewModel()

    var body: some View {
        VStack(spacing: 12) {
            Text("Añadir cupón de descuento")
                .font(.title2)
                .fontWeight(.bold)
                .multilineTextAlignment(.center)
                .padding(.bottom, 24)

            HStack {
                Image(systemName: "tag")
                TextField(
                    "Código de cupón",
                    text: Binding(
                        get: { couponsVM.couponUiState.code },
                        set: { couponsVM.onCouponCodeChanged(code: $0) }
                    )
                )
                .textFieldStyle(.roundedBorder)
                .autocapitalization(.none)
                .disableAutocorrection(true)
            }

            Button(action: {
                if let userId = sessionVM.userData?.id {
                    couponsVM.addCouponToUser(
                        userId: userId,
                        code: couponsVM.couponUiState.code.trimmingCharacters(in: .whitespacesAndNewlines)
                    )
                }
            }) {
                if couponsVM.couponUiState.isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle())
                } else {
                    Text("Validar y guardar")
                        .frame(maxWidth: .infinity)
                }
            }
            .disabled(couponsVM.couponUiState.isLoading)
            .buttonStyle(.borderedProminent)

            if let error = couponsVM.couponUiState.error {
                Text(error)
                    .font(.footnote)
                    .foregroundColor(.red)
            }

            if couponsVM.couponUiState.currentCoupons.isEmpty {
                Text("No hay cupones")
                    .frame(maxWidth: .infinity, alignment: .leading)
            } else {
                Text("Cupones activos:")
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .font(.headline)

                ScrollView {
                    VStack(spacing: 8) {
                        ForEach(couponsVM.couponUiState.currentCoupons, id: \.id) { coupon in
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Código: \(coupon.code)")
                                Text("Descuento: \(coupon.discount)\(coupon.isPercentage ? "%" : "€")")
                            }
                            .padding(12)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color.gray.opacity(0.3))
                            )
                        }
                    }
                }
            }

            Spacer(minLength: 0)
        }
        .padding()
        .onAppear {
            if let userId = sessionVM.userData?.id {
                couponsVM.loadUserCoupons(userId: userId)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }
}
