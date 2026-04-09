package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class MinerResponse(
    val status: String = "ok",
    val mined: Boolean,
    val trigger: String,
    val block: BlockDto,
) {
}