//
//  ServiceProductViewModel.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import shared  // Importa el módulo KMM compartido

class ServiceProductViewModel: ObservableObject {
    // Lista de todos los servicios disponibles para contratar
    @Published var allServices: [ServiceAvailable] = []
    // Lista de productos que el usuario tiene contratados
    @Published var userProducts: [ServiceItem] = []
    // Producto seleccionado (por ejemplo, para ver detalle o dar de baja)
    @Published var productoSeleccionado: ServiceItem? = nil
    // Servicio seleccionado (para navegar a la pantalla de contratación de productos de ese servicio)
    @Published var selectedService: ServiceAvailable? = nil

    // Caso de uso KMM para productos/servicios (inyectamos el repositorio compartido)
    private let serviceProductUseCase = ServiceProductUseCase(serviceProductRepository: ServiceProductRepositoryImpl())

    /// Computa la lista de productos del usuario sin duplicados (distintos por ID).
    var userProductsDistinct: [ServiceItem] {
        // Filtrar por id único, similar a .distinctBy { it.id } en Kotlin
        var seenIds = Set<Int32>()
        return userProducts.filter { item in
            if seenIds.contains(item.id) {
                return false
            } else {
                seenIds.insert(item.id)
                return true
            }
        }
    }

    /// Indica si el usuario tiene contratado algún producto del servicio con `serviceId` dado.
    func userHasService(_ serviceId: Int32) -> Bool {
        // Verifica si en los productos del usuario hay al menos uno cuyo serviceIds contenga el ID de servicio
        return userProducts.contains { item in
            // Convertir serviceId a KotlinInt para comparación
            let kotlinServiceId = KotlinInt(integerLiteral: Int(serviceId))
            return item.serviceIds.contains(kotlinServiceId)
        }
    }

    /// Carga todos los servicios disponibles llamando al caso de uso KMM.
    func loadAllServices() {
        serviceProductUseCase.getAllServices { [weak self] services, error in
            guard let self = self else { return }
            
            if let servicesList = services {
                // Actualizamos en el hilo principal porque afecta la UI
                DispatchQueue.main.async {
                    self.allServices = servicesList
                }
            } else if let error = error {
                print("❌ Error cargando servicios: \(error.localizedDescription)")
            }
        }
    }

    /// Carga los productos contratados de un usuario específico.
    func loadUserProducts(userId: Int32) {
        serviceProductUseCase.getUserProducts(customerId: userId) { [weak self] products, error in
            guard let self = self else { return }
            
            if let productsList = products {
                DispatchQueue.main.async {
                    self.userProducts = productsList
                }
            } else if let error = error {
                print("❌ Error cargando productos de usuario: \(error.localizedDescription)")
            }
        }
    }

    /// Da de baja (desasigna) un producto del usuario llamando al caso de uso compartido.
    func unassignProductFromUser(productId: Int32, userId: Int32) {
        serviceProductUseCase.unassignProductFromUser(userId: userId, productId: productId) { [weak self] result, error in
            guard let self = self else { return }
            
            if let error = error {
                print("❌ Error al darse de baja del producto: \(error.localizedDescription)")
                return
            }
            
            // Manejo más simple del resultado booleano
            let success = self.extractBooleanFromKMM(result)
            
            if success {
                // Si la baja fue exitosa, recargar la lista de productos del usuario
                DispatchQueue.main.async {
                    self.loadUserProducts(userId: userId)
                }
                print("✅ Producto dado de baja correctamente")
            } else {
                print("⚠️ La operación de baja no fue exitosa")
            }
        }
    }

    /// (Opcional) Asigna un producto al usuario (contratación). Puede usarse tras un flujo de pago exitoso.
    func assignProductToUser(userId: Int32, productId: Int32, paymentMethod: String, couponCode: String? = nil, completion: @escaping (_ success: Bool, _ errorMsg: String?) -> Void) {
        serviceProductUseCase.assignProductToUser(
            userId: userId,
            productId: productId,
            paymentMethod: paymentMethod,
            couponCode: couponCode
        ) { [weak self] result, error in
            guard let self = self else {
                completion(false, "ViewModel deallocated")
                return
            }
            
            if let error = error {
                print("❌ Error al asignar producto: \(error.localizedDescription)")
                DispatchQueue.main.async {
                    completion(false, error.localizedDescription)
                }
                return
            }
            
            // Extraer resultado sin casting específico de KotlinPair
            let (success, errorMessage) = self.extractResultFromKMM(result)
            
            if success {
                print("✅ Producto asignado correctamente")
                // Actualizar lista de productos del usuario
                DispatchQueue.main.async {
                    self.loadUserProducts(userId: userId)
                }
            } else {
                print("⚠️ Falló la asignación del producto: \(errorMessage ?? "Razón desconocida")")
            }
            
            // Notificar resultado
            DispatchQueue.main.async {
                completion(success, errorMessage)
            }
        }
    }
    
    // MARK: - Helper methods para extraer datos de KMM sin casting directo
    
    /// Extrae un valor booleano de un resultado KMM de forma segura
    private func extractBooleanFromKMM(_ result: Any?) -> Bool {
        guard let result = result else { return false }
        
        // Usar reflexión para acceder a propiedades sin casting específico
        let mirror = Mirror(reflecting: result)
        
        // Si es un KotlinBoolean o similar
        if let boolValue = mirror.children.first(where: { $0.label == "value" })?.value as? Bool {
            return boolValue
        }
        
        // Si es un NSNumber
        if let number = result as? NSNumber {
            return number.boolValue
        }
        
        // Si es un Bool directo
        if let bool = result as? Bool {
            return bool
        }
        
        // Intentar convertir a string y evaluar
        let stringValue = String(describing: result).lowercased()
        return stringValue == "true" || stringValue == "1"
    }
    
    /// Extrae un par (success, errorMessage) de un resultado KMM de forma segura
    private func extractResultFromKMM(_ result: Any?) -> (success: Bool, errorMessage: String?) {
        guard let result = result else { return (false, "No result") }
        
        let mirror = Mirror(reflecting: result)
        var success = false
        var errorMessage: String? = nil
        
        // Buscar propiedades first y second (típico de KotlinPair)
        for child in mirror.children {
            if child.label == "first" {
                success = extractBooleanFromKMM(child.value)
            } else if child.label == "second" {
                if let stringValue = child.value as? String {
                    errorMessage = stringValue
                } else if let nsString = child.value as? NSString {
                    errorMessage = nsString as String
                }
            }
        }
        
        // Si no encontramos estructura de pair, asumir que es solo un booleano
        if mirror.children.isEmpty {
            success = extractBooleanFromKMM(result)
        }
        
        return (success, errorMessage)
    }
    
    // MARK: - Convenience methods with Int parameters for easier usage
    
    /// Convenience method that accepts Int and converts to Int32
    func loadUserProducts(userId: Int) {
        loadUserProducts(userId: Int32(userId))
    }
    
    /// Convenience method that accepts Int parameters and converts to Int32
    func unassignProductFromUser(productId: Int, userId: Int) {
        unassignProductFromUser(productId: Int32(productId), userId: Int32(userId))
    }
    
    /// Convenience method that accepts Int parameters and converts to Int32
    func assignProductToUser(userId: Int, productId: Int, paymentMethod: String, couponCode: String? = nil, completion: @escaping (_ success: Bool, _ errorMsg: String?) -> Void) {
        assignProductToUser(userId: Int32(userId), productId: Int32(productId), paymentMethod: paymentMethod, couponCode: couponCode, completion: completion)
    }
    
    /// Convenience method that accepts Int and converts to Int32
    func userHasService(_ serviceId: Int) -> Bool {
        return userHasService(Int32(serviceId))
    }
}
