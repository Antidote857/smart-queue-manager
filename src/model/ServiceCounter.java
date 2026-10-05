package model;

public class ServiceCounter {

    private final int counterId;
    private Customer currentCustomer;

    public ServiceCounter(int counterId) {
        if (counterId <= 0) {
            throw new IllegalArgumentException(
                    "Counter ID must be positive."
            );
        }

        this.counterId = counterId;
    }

    public int getCounterId() {
        return counterId;
    }

    public boolean isAvailable() {
        return currentCustomer == null;
    }

    public void assignCustomer(Customer customer) {
        if (!isAvailable()) {
            throw new IllegalStateException(
                    "Counter " + counterId + " is busy."
            );
        }

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null."
            );
        }

        customer.startService();
        currentCustomer = customer;
    }

    public Customer completeService() {
        if (isAvailable()) {
            throw new IllegalStateException(
                    "Counter " + counterId + " has no customer."
            );
        }

        Customer completedCustomer = currentCustomer;
        completedCustomer.completeService();
        currentCustomer = null;

        return completedCustomer;
    }

    @Override
    public String toString() {
        if (isAvailable()) {
            return "Counter " + counterId + " - AVAILABLE";
        }

        return "Counter " + counterId
                + " - BUSY - Serving "
                + currentCustomer.getQueueNumber()
                + " (" + currentCustomer.getName() + ")";
    }
}