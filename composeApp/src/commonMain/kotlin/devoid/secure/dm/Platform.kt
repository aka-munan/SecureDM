package devoid.secure.dm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform