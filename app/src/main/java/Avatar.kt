package com.example.app_a_void
enum class Gender { MALE, FEMALE }

data class Customization(
    var hairStyle: String = "Default",
    var clothing: String = "Default"
)

data class Avatar(
    var gender: Gender,
    var customization: Customization = Customization() // Initialize with default customization
)
