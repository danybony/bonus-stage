package net.bonysoft.bonusstage.raapi

/** Failures of a RetroAchievements Web API call. */
sealed class RaApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** The username or web API key was rejected. */
    class Unauthorized : RaApiException("The RetroAchievements credentials were rejected")

    /** The API asked us to slow down. */
    class RateLimited(val retryAfterSeconds: Long?) :
        RaApiException("Rate limited by RetroAchievements, retry after ${retryAfterSeconds ?: "?"}s")

    /** The requested user, game or resource does not exist. */
    class NotFound : RaApiException("Not found on RetroAchievements")

    class Http(val status: Int) : RaApiException("RetroAchievements answered HTTP $status")

    /** The response did not match the models: the API may have changed (see ADR 0003). */
    class UnexpectedResponse(cause: Throwable) :
        RaApiException("Unexpected response from RetroAchievements: ${cause.message}", cause)

    class Network(cause: Throwable) : RaApiException("Network error: ${cause.message}", cause)
}
