package io.hobbyful.orderservice.core

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class NotFoundException : ResponseStatusException(HttpStatus.NOT_FOUND)

class InternalServerException : ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
