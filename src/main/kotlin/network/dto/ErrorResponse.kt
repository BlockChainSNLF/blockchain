package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val status: String,
    val error: ErrorDto
)

@Serializable
data class ErrorDto(
    val code: String,
    val message: String
)