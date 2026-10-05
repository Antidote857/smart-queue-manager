package service;

import java.util.Locale;

public record QueueStatistics(
        int totalCustomers,
        int servedCustomers,
        int waitingCustomers,
        int calledCustomers,
        int servingCustomers,
        int priorityCustomersWaiting,
        int cancelledCustomers,
        int availableCounters,
        int busyCounters,
        int waitingTimeSamples,
        double averageWaitingSeconds,
        double longestWaitingSeconds) {

    @Override
    public String toString() {
        String waitingTimes;

        if (waitingTimeSamples == 0) {
            waitingTimes = "Average waiting time:       N/A\n"
                    + "Longest waiting time:       N/A";
        } else {
            waitingTimes = String.format(
                    Locale.ROOT,
                    "Average waiting time:       %.3f seconds%n"
                            + "Longest waiting time:       %.3f seconds",
                    averageWaitingSeconds,
                    longestWaitingSeconds
            );
        }

        return "QUEUE STATISTICS"
                + "\n------------------------------------"
                + "\nTotal registered:           " + totalCustomers
                + "\nCustomers served:           " + servedCustomers
                + "\nCustomers waiting:          " + waitingCustomers
                + "\nCustomers called:           " + calledCustomers
                + "\nCustomers serving:          " + servingCustomers
                + "\nPriority customers waiting: " + priorityCustomersWaiting
                + "\nCancelled customers:        " + cancelledCustomers
                + "\nAvailable counters:         " + availableCounters
                + "\nBusy counters:              " + busyCounters
                + "\nWaiting-time samples:       " + waitingTimeSamples
                + "\n" + waitingTimes;
    }
}