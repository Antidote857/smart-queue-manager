package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Customer {

    private final String customerId;
    private final String name;
    private final String queueNumber;
    private final boolean priority;
    private final LocalDateTime arrivalTime;

    private QueueStatus status;
    private LocalDateTime serviceStartTime;

    public Customer(
            String customerId,
            String name,
            String queueNumber,
            boolean priority) {

        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer ID cannot be empty."
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer name cannot be empty."
            );
        }

        if (queueNumber == null || queueNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Queue number cannot be empty."
            );
        }

        this.customerId = customerId.trim();
        this.name = name.trim();
        this.queueNumber = queueNumber.trim();
        this.priority = priority;
        this.arrivalTime = LocalDateTime.now();
        this.status = QueueStatus.WAITING;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public boolean isPriority() {
        return priority;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public LocalDateTime getServiceStartTime() {
        return serviceStartTime;
    }

    public long getWaitingTimeMillis() {
        if (serviceStartTime == null) {
            throw new IllegalStateException(
                    "Waiting time is not final until service starts."
            );
        }

        return Math.max(
                0,
                Duration.between(arrivalTime, serviceStartTime).toMillis()
        );
    }

    public void markCalled() {
        if (status != QueueStatus.WAITING) {
            throw new IllegalStateException(
                    "Only waiting customers can be called."
            );
        }

        status = QueueStatus.CALLED;
    }

    public void startService() {
        if (status != QueueStatus.CALLED) {
            throw new IllegalStateException(
                    "Only called customers can start service."
            );
        }

        serviceStartTime = LocalDateTime.now();
        status = QueueStatus.SERVING;
    }

    public void completeService() {
        if (status != QueueStatus.SERVING) {
            throw new IllegalStateException(
                    "Only customers being served can complete service."
            );
        }

        status = QueueStatus.SERVED;
    }

    public void cancel() {
        if (status != QueueStatus.WAITING) {
            throw new IllegalStateException(
                    "Only waiting customers can leave the queue."
            );
        }

        status = QueueStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return "Customer ID: " + customerId
                + "\nName: " + name
                + "\nQueue number: " + queueNumber
                + "\nPriority: " + (priority ? "Yes" : "No")
                + "\nArrival time: " + arrivalTime
                + "\nStatus: " + status;
    }
}