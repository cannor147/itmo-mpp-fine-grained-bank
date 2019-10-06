package fgbank;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bank implementation.
 */
public class BankImpl implements Bank {
    /**
     * An array of accounts by index.
     */
    private final Account[] accounts;

    /**
     * Creates new bank instance.
     * @param n the number of accounts (numbered from 0 to n-1).
     */
    public BankImpl(int n) {
        accounts = new Account[n];
        for (int i = 0; i < n; i++) {
            accounts[i] = new Account();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfAccounts() {
        return accounts.length;
    }

    @Override
    public long getAmount(int index) {
        accounts[index].lock();
        long result = accounts[index].amount;
        accounts[index].unlock();
        return result;
    }

    @Override
    public long getTotalAmount() {
        long sum = 0;
        try {
            for (Account account : accounts) {
                account.lock();
                sum += account.amount;
            }
        } finally {
            for (Account account : accounts) {
                account.unlock();
            }
        }
        return sum;
    }

    @Override
    public long deposit(int index, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }
        Account account = accounts[index];
        long result;

        try {
            account.lock();
            if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT) {
                throw new IllegalStateException("Overflow");
            }
            account.amount += amount;
            result = account.amount;
        } finally {
            account.unlock();
        }
        return result;
    }

    @Override
    public long withdraw(int index, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }
        Account account = accounts[index];
        long result;

        try {
            account.lock();
            if (account.amount - amount < 0) {
                throw new IllegalStateException("Underflow");
            }
            account.amount -= amount;
            result = account.amount;
        } finally {
            account.unlock();
        }
        return result;
    }

    @Override
    public void transfer(int fromIndex, int toIndex, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }
        Account from = accounts[fromIndex];
        Account to = accounts[toIndex];

        try {
            if (fromIndex == toIndex) {
                throw new IllegalArgumentException("fromIndex == toIndex");
            } else if (fromIndex < toIndex) {
                from.lock();
                to.lock();
            } else {
                to.lock();
                from.lock();
            }
            if (amount > from.amount) {
                throw new IllegalStateException("Underflow");
            } else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT) {
                throw new IllegalStateException("Overflow");
            }
            from.amount -= amount;
            to.amount += amount;
        } finally {
            from.unlock();
            to.unlock();
        }
    }

    /**
     * Private account data structure.
     */
    private static class Account {
        /**
         * Amount of funds in this account.
         */
        private Lock lock = new ReentrantLock();
        private long amount;

        private void lock() {
            lock.lock();
        }

        private void unlock() {
            lock.unlock();
        }
    }
}
