package com.handbook.app.core.net

object HttpResponse {
    const val HTTP_TOO_MANY_REQUESTS:       Int = 429
    const val HTTP_PAYLOAD_TOO_LARGE:       Int = 413
    const val HTTP_INVALID_TOKEN:           Int = 498
    const val HTTP_RANGE_NOT_SATISFIABLE:   Int = 416
    const val HTTP_UNPROCESSABLE_CONTENT:   Int = 422
}