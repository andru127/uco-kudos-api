package co.edu.uco.ucokudos.services.output.ports.transaction;

import java.util.function.Supplier;

public interface TransactionRunner {

    <T> T inTransaction(Supplier<T> work);
}
