package com.rebootech.fruitlogix.distributor.orderManagement.domain.model

/** Creating an order on the server failed. The screen shows a message for each [reason]. */
class OrderSaveException(
    val reason: Reason,
    /** The server's own explanation (e.g. "quantity below the minimum"), when it sent one. */
    val serverMessage: String? = null,
    cause: Throwable? = null
) : Exception(reason.name, cause) {
    enum class Reason {
        /** No answer (no signal or the server is waking up): it may or may not have been saved. */
        NO_CONNECTION,

        /** The server answered with an error. */
        REJECTED
    }
}
