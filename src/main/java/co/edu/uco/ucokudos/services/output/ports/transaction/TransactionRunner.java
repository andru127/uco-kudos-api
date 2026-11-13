package co.edu.uco.ucokudos.services.output.ports.transaction;

import java.util.function.Supplier;

/** Abstrae la transacción para que los casos de uso no dependan de Spring. */
public interface TransactionRunner {

    <T> T inTransaction(Supplier<T> work);
}
