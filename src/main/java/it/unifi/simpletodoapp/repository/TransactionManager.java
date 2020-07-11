package it.unifi.simpletodoapp.repository;

public interface TransactionManager {

	public <T> T doTaskTransaction(TaskTransactionCode<T> code);

}
