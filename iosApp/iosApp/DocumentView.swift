//
//  DocumentView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

/// Pantalla para la gestión de documentos del usuario, replicando la versión de Android.
struct DocumentView: View {
    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()
    @Environment(\.dismiss) private var dismiss

    @StateViewModel private var documentsVM = SharedDependencies.shared.makeUserDocumentsViewModel()

    @State private var showOptions = false
    @State private var showImagePicker = false
    @State private var showDocumentPicker = false
    @State private var pickerSource: UIImagePickerController.SourceType = .photoLibrary

    @State private var documentName: String = ""
    @State private var documentData: Data? = nil

    @State private var showAlert = false
    @State private var alertMessage = ""

    private var isUploading: Bool {
        documentsVM.uploadState is UploadStateLoading
    }

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
                    if isUploading {
                        ProgressView().progressViewStyle(.circular)
                    } else {
                        Text("Subir")
                    }
                }
                .disabled(documentData == nil || isUploading)
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
                documentsVM.resetUploadState()
                dismiss()
            }
        }
        .onChange(of: documentsVM.uploadState) { state in
            switch state {
            case is UploadStateSuccess:
                alertMessage = "Archivo subido correctamente"
                showAlert = true
            case let error as UploadStateError:
                alertMessage = "Error al subir: \(error.message)"
                showAlert = true
            default:
                break
            }
        }
    }

    private func upload() {
        guard let data = documentData, let userId = sessionVM.userData?.id else { return }
        documentsVM.uploadDocument(userId: userId, name: documentName, data: data.asKotlinByteArray())
    }
}

