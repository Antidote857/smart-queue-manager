package service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import model.Customer;
import model.QueueStatus;
import model.ServiceCounter;
import util.QueueNumberGenerator;

public class QueueManager {

    private static final int MAX_PRIORITY_STREAK = 2;

    private int priorityStreak = 0;

    private final QueueNumberGenerator generator =
            new QueueNumberGenerator();

    private final Queue<Customer> normalQueue =
            new ArrayDeque<>();

    private final Comparator<Customer> arrivalOrder =
            Comparator.comparing(Customer::getArrivalTime)
                    .thenComparing(Customer::getCustomerId);

    private final PriorityQueue<Customer> priorityQueue =
            new PriorityQueue<>(arrivalOrder);

    private final Map<String, Customer> customersById =
            new HashMap<>();

    private final List<ServiceCounter> counters =
            new ArrayList<>();

    public QueueManager() {
        this(3);
    }

    public QueueManager(int counterCount) {
        if (counterCount <= 0) {
            throw new IllegalArgumentException(
                    "There must be at least one service counter."
            );
        }

        for (int id = 1; id <= counterCount; id++) {
            counters.add(new ServiceCounter(id));
        }
    }

    public Customer addCustomer(String name, boolean priority) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer name cannot be empty."
            );
        }

        Customer customer = new Customer(
                generator.generateCustomerId(),
                name,
                generator.generateQueueNumber(priority),
                priority
        );

        if (priority) {
            priorityQueue.offer(customer);
        } else {
            normalQueue.offer(customer);
        }

        customersById.put(customer.getCustomerId(), customer);

        return customer;
    }

    public List<Customer> getNormalQueue() {
        return new ArrayList<>(normalQueue);
    }

    public List<Customer> getPriorityQueue() {
        List<Customer> customers = new ArrayList<>(priorityQueue);
        customers.sort(arrivalOrder);
        return customers;
    }

    public int getWaitingCount() {
        return normalQueue.size() + priorityQueue.size();
    }

    public Customer callNextCustomer() {
        if (normalQueue.isEmpty() && priorityQueue.isEmpty()) {
            priorityStreak = 0;
            return null;
        }

        Customer nextCustomer;

        if (!priorityQueue.isEmpty()
                && (normalQueue.isEmpty()
                    || priorityStreak < MAX_PRIORITY_STREAK)) {

            nextCustomer = priorityQueue.poll();

            priorityStreak = Math.min(
                    priorityStreak + 1,
                    MAX_PRIORITY_STREAK
            );

        } else {
            nextCustomer = normalQueue.poll();
            priorityStreak = 0;
        }

        nextCustomer.markCalled();
        resetStreakIfQueuesEmpty();

        return nextCustomer;
    }

    private ServiceCounter findCounter(int counterId) {
        for (ServiceCounter counter : counters) {
            if (counter.getCounterId() == counterId) {
                return counter;
            }
        }

        throw new IllegalArgumentException(
                "Counter " + counterId + " does not exist."
        );
    }

    public Customer serveNextCustomer(int counterId) {
        ServiceCounter counter = findCounter(counterId);

        if (!counter.isAvailable()) {
            throw new IllegalStateException(
                    "Counter " + counterId + " is busy."
            );
        }

        Customer customer = callNextCustomer();

        if (customer != null) {
            counter.assignCustomer(customer);
        }

        return customer;
    }

    public Customer completeService(int counterId) {
        return findCounter(counterId).completeService();
    }

    public List<String> getCounterDetails() {
        List<String> details = new ArrayList<>();

        for (ServiceCounter counter : counters) {
            details.add(counter.toString());
        }

        return details;
    }

    public Customer findCustomerById(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer ID cannot be empty."
            );
        }

        return customersById.get(
                customerId.trim().toUpperCase(Locale.ROOT)
        );
    }

    public Customer findCustomerByQueueNumber(String queueNumber) {
        if (queueNumber == null || queueNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Queue number cannot be empty."
            );
        }

        String searchValue = queueNumber.trim();

        for (Customer customer : customersById.values()) {
            if (customer.getQueueNumber()
                    .equalsIgnoreCase(searchValue)) {
                return customer;
            }
        }

        return null;
    }

    public List<Customer> findCustomersByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Search name cannot be empty."
            );
        }

        String searchValue = name.trim().toLowerCase(Locale.ROOT);
        List<Customer> matches = new ArrayList<>();

        for (Customer customer : customersById.values()) {
            if (customer.getName()
                    .toLowerCase(Locale.ROOT)
                    .contains(searchValue)) {

                matches.add(customer);
            }
        }

        matches.sort(arrivalOrder);

        return matches;
    }

    public Customer cancelCustomer(String queueNumber) {
        Customer customer = findCustomerByQueueNumber(queueNumber);

        if (customer == null) {
            throw new IllegalArgumentException(
                    "No customer found with queue number: " + queueNumber
            );
        }

        if (customer.getStatus() != QueueStatus.WAITING) {
            throw new IllegalStateException(
                    "Only waiting customers can leave the queue."
            );
        }

        boolean removed;

        if (customer.isPriority()) {
            removed = priorityQueue.remove(customer);
        } else {
            removed = normalQueue.remove(customer);
        }

        if (!removed) {
            throw new IllegalStateException(
                    "Customer was not found in the waiting queue."
            );
        }

        customer.cancel();
        resetStreakIfQueuesEmpty();

        return customer;
    }

    public QueueStatistics getStatistics() {
        int served = 0;
        int waiting = 0;
        int called = 0;
        int serving = 0;
        int cancelled = 0;
        int priorityWaiting = 0;

        int waitingTimeSamples = 0;
        long totalWaitingMillis = 0;
        long longestWaitingMillis = 0;

        for (Customer customer : customersById.values()) {
            switch (customer.getStatus()) {
                case WAITING -> {
                    waiting++;

                    if (customer.isPriority()) {
                        priorityWaiting++;
                    }
                }
                case CALLED -> called++;
                case SERVING -> serving++;
                case SERVED -> served++;
                case CANCELLED -> cancelled++;
            }

            if (customer.getServiceStartTime() != null) {
                long waitingMillis = customer.getWaitingTimeMillis();

                totalWaitingMillis += waitingMillis;
                longestWaitingMillis = Math.max(
                        longestWaitingMillis,
                        waitingMillis
                );

                waitingTimeSamples++;
            }
        }

        int availableCounters = 0;

        for (ServiceCounter counter : counters) {
            if (counter.isAvailable()) {
                availableCounters++;
            }
        }

        double averageWaitingSeconds = 0;

        if (waitingTimeSamples > 0) {
            averageWaitingSeconds =
                    totalWaitingMillis / (1000.0 * waitingTimeSamples);
        }

        return new QueueStatistics(
                customersById.size(),
                served,
                waiting,
                called,
                serving,
                priorityWaiting,
                cancelled,
                availableCounters,
                counters.size() - availableCounters,
                waitingTimeSamples,
                averageWaitingSeconds,
                longestWaitingMillis / 1000.0
        );
    }

    private void resetStreakIfQueuesEmpty() {
        if (normalQueue.isEmpty() && priorityQueue.isEmpty()) {
            priorityStreak = 0;
        }
    }
}