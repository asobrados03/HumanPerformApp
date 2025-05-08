package es.uva.sg.psm.humanperformcenter

// Suponiendo que ya tienes definido:
enum class ProfessionalType { TRAINER, PHYSIO, NUTRITIONIST  }

data class Professional(
    val id: String,
    val name: String,
    val type: ProfessionalType,
    val photoUrl: String? = null
)

val mockProfessionals = listOf(
    Professional(id = "1",  name = "Juan Sanz",    type = ProfessionalType.TRAINER),
    Professional(id = "2",  name = "Pablo Sanz",   type = ProfessionalType.TRAINER),
    Professional(id = "3",  name = "Idaira Prieto", type = ProfessionalType.PHYSIO),
    Professional(id = "4",  name = "Guillermo Duque", type = ProfessionalType.TRAINER),
    Professional(id = "5",  name = "Jorge Mínguez", type = ProfessionalType.TRAINER),
    Professional(id = "6",  name = "Daniel Barroso", type = ProfessionalType.TRAINER),
    Professional(id = "7",  name = "Sergio Sanz",   type = ProfessionalType.TRAINER),
    Professional(id = "8",  name = "Javier Seco",   type = ProfessionalType.PHYSIO),
    Professional(id = "10", name = "María Jimeno",  type = ProfessionalType.PHYSIO),
    Professional(id = "11", name = "Isabel Álvaro", type = ProfessionalType.PHYSIO),
    Professional(id = "12", name = "Adrián Pinilla", type = ProfessionalType.TRAINER),
    Professional(id = "13", name = "Raúl Orejudo",  type = ProfessionalType.TRAINER)
)
