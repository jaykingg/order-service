package io.hobbyful.orderservice.core

import org.springframework.http.HttpStatus

class ErrorCodeException(reason: String, errorCode: String) :
    AbstractErrorCodeException(HttpStatus.BAD_REQUEST, reason, errorCode) {
    companion object {
        fun of(error: BaseError) = ErrorCodeException(error.message, error.code)

        fun of(error: ErrorResponse) = ErrorCodeException(
            reason = error.message ?: "알 수 없는 오류가 발생했습니다",
            errorCode = error.code ?: "unknown_error"
        )
    }
}
