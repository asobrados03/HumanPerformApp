package es.uva.sg.psm.humanperformcenter

data class NavItem(
    val route: String,
    val title: String,
    val icon: Int // se guarda como un ID de recurso (R.drawable)
)