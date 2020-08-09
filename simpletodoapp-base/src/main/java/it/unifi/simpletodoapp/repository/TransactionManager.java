package it.unifi.simpletodoapp.repository;

public interface TransactionManager {
	public <T> T doTaskTransaction(TaskTransactionCode<T> code);
	public <T> T doTagTransaction(TagTransactionCode<T> code);
	public <T> T doCompositeTransaction(CompositeTransactionCode<T> code);
}