//
//  DocumentView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import shared

/// Pantalla para la gestión de documentos del usuario, replicando la versión de Android.
struct DocumentView: View {
    @EnvironmentObject var userVM: shared.UserViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var showOptions = false
    @State private var showImagePicker = false
    @State private var showDocumentPicker = false
    @State private var pickerSource: UIImagePickerController.SourceType = .photoLibrary

    @State private var documentName: String = ""
    @State private var documentData: Data? = nil

    @State private var showAlert = false
    @State private var alertMessage = ""

    var body: some View {
        VStack(spacing: 16) {
            Text("Subida de documentos")
                .font(.title2)
                .fontWeight(.semibold)
            
            Button("Seleccionar archivo") {
                showOptions = true
            }
            .buttonStyle(.borderedProminent)

            if !documentName.isEmpty {
                Text("Archivo: \(documentName)")
                Button(action: upload) {
                    if userVM.uploadState == .loading {
                        ProgressView().progressViewStyle(.circular)
                    } else {
                        Text("Subir")
                    }
                }
                .disabled(documentData == nil || userVM.uploadState == .loading)
            }

            Spacer()
        }
        .padding()
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .confirmationDialog("Documentos", isPresented: $showOptions, titleVisibility: .visible) {
            Button("Cámara") {
                pickerSource = .camera
                showImagePicker = true
            }
            Button("Galería") {
                pickerSource = .photoLibrary
                showImagePicker = true
            }
            Button("Archivos") {
                showDocumentPicker = true
            }
            Button("Cancelar", role: .cancel) { }
        }
        .sheet(isPresented: $showImagePicker) {
            ImagePicker(sourceType: pickerSource) { image, name in
                if let data = image.jpegData(compressionQuality: 0.9) {
                    documentData = data
                    documentName = name ?? "IMG_\(Int(Date().timeIntervalSince1970)).jpg"
                }
            }
        }
        .sheet(isPresented: $showDocumentPicker) {
            DocumentPicker { url in
                documentData = try? Data(contentsOf: url)
                documentName = url.lastPathComponent
            }
        }
        .alert(alertMessage, isPresented: $showAlert) {
            Button("OK") {
                userVM.resetUploadState()
                dismiss()
            }
        }
        .onChange(of: userVM.uploadState) { state in
            switch state {
            case .success:
                alertMessage = "Archivo subido correctamente"
                showAlert = true
            case .error(let msg):
                alertMessage = "Error al subir: \(msg)"
                showAlert = true
            default:
                break
            }
        }
    }

    private func upload() {
        guard let data = documentData, let userId = userVM.currentUserId else { return }
        userVM.uploadDocument(userId: userId, name: documentName, data: data)
    }
}

