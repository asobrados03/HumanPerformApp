import DataDetection

struct RegisterFormData {
    var firstName: String
    var lastName: String
    var email: String
    var phone: String
    var password: String
    var sex: String       // e.g. "Male" or "Female"
    var dateOfBirthText: String
    var postalCode: String
    var postalAddress: String
    var dni: String
    var profilePicBytes: Data?
    var profilePicName: String?
}
