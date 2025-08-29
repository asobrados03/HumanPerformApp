//
//  AddCouponView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import shared

/// Pantalla para añadir un cupón promocional replicando la versión de Android.
struct AddCouponView: View {
    @EnvironmentObject var userVM: UserViewModel

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
                        get: { userVM.couponState.code },
                        set: { userVM.onCouponCodeChanged($0) }
                    )
                )
                .textFieldStyle(.roundedBorder)
                .autocapitalization(.none)
                .disableAutocorrection(true)
            }

            Button(action: {
                if let id = userVM.currentUserId {
                    userVM.addCouponToUser(
                        userId: id,
                        code: userVM.couponState.code.trimmingCharacters(in: .whitespacesAndNewlines)
                    )
                }
            }) {
                if userVM.couponState.isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle())
                } else {
                    Text("Validar y guardar")
                        .frame(maxWidth: .infinity)
                }
            }
            .disabled(userVM.couponState.isLoading)
            .buttonStyle(.borderedProminent)

            if let error = userVM.couponState.error {
                Text(error)
                    .font(.footnote)
                    .foregroundColor(.red)
            }

            if userVM.couponState.currentCoupons.isEmpty {
                Text("No hay cupones")
                    .frame(maxWidth: .infinity, alignment: .leading)
            } else {
                Text("Cupones activos:")
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .font(.headline)

                ScrollView {
                    VStack(spacing: 8) {
                        ForEach(userVM.couponState.currentCoupons, id: \.id) { coupon in
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
            if let id = userVM.currentUserId {
                userVM.loadUserCoupons(id)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }
}

