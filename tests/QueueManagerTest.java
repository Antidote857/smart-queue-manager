import java.util.Objects;
import model.Customer;
import model.QueueStatus;
import service.QueueManager;
import service.QueueStatistics;

public class QueueManagerTest {
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        test("Normal FIFO", () -> {
            QueueManager q = new QueueManager();
            q.addCustomer("First", false);
            q.addCustomer("Second", false);
            equal("A001", q.serveNextCustomer(1).getQueueNumber());
            equal("A002", q.serveNextCustomer(2).getQueueNumber());
        });
        test("Priority and fairness", () -> {
            QueueManager q = new QueueManager();
            q.addCustomer("Normal", false);
            q.addCustomer("Priority one", true);
            q.addCustomer("Priority two", true);
            q.addCustomer("Priority three", true);
            for (String ticket : new String[]{"P001", "P002", "A001", "P003"}) {
                equal(ticket, q.serveNextCustomer(1).getQueueNumber());
                q.completeService(1);
            }
        });
        test("Priority-only queue", () -> {
            QueueManager q = new QueueManager();
            for (int i = 0; i < 4; i++) q.addCustomer("Priority " + i, true);
            for (int i = 1; i <= 4; i++) {
                equal("P00" + i, q.serveNextCustomer(1).getQueueNumber());
                q.completeService(1);
            }
        });
        test("Empty queue", () -> equal(null, new QueueManager().serveNextCustomer(1)));
        test("Busy counter preserves queue", () -> {
            QueueManager q = new QueueManager();
            q.addCustomer("One", false);
            q.addCustomer("Two", false);
            q.serveNextCustomer(1);
            expect(IllegalStateException.class, () -> q.serveNextCustomer(1));
            equal(1, q.getWaitingCount());
            equal("A002", q.getNormalQueue().get(0).getQueueNumber());
        });
        test("Complete and reuse counter", () -> {
            QueueManager q = new QueueManager(1);
            Customer c = q.addCustomer("One", false);
            q.serveNextCustomer(1);
            equal(QueueStatus.SERVING, c.getStatus());
            equal(c, q.completeService(1));
            equal(QueueStatus.SERVED, c.getStatus());
            q.addCustomer("Two", false);
            equal("A002", q.serveNextCustomer(1).getQueueNumber());
        });
        test("Cancellation keeps searchable record", () -> {
            QueueManager q = new QueueManager();
            Customer c = q.addCustomer("Buhle", false);
            equal(c, q.cancelCustomer("a001"));
            equal(0, q.getWaitingCount());
            equal(QueueStatus.CANCELLED, q.findCustomerById("c001").getStatus());
            expect(IllegalStateException.class, () -> q.cancelCustomer("A001"));
        });
        test("Serving customer cannot cancel", () -> {
            QueueManager q = new QueueManager();
            q.addCustomer("One", false);
            q.serveNextCustomer(1);
            expect(IllegalStateException.class, () -> q.cancelCustomer("A001"));
        });
        test("Search ID, ticket and partial name", () -> {
            QueueManager q = new QueueManager();
            Customer c = q.addCustomer("Buhle", false);
            equal(c, q.findCustomerById(" c001 "));
            equal(c, q.findCustomerByQueueNumber(" a001 "));
            equal(c, q.findCustomersByName("BUH").get(0));
            equal(null, q.findCustomerById("C999"));
            equal(0, q.findCustomersByName("Missing").size());
        });
        test("Validation and invalid counters", () -> {
            QueueManager q = new QueueManager();
            expect(IllegalArgumentException.class, () -> q.addCustomer("  ", false));
            equal("C001", q.addCustomer("Valid", false).getCustomerId());
            expect(IllegalArgumentException.class, () -> q.serveNextCustomer(99));
            equal(1, q.getWaitingCount());
            expect(IllegalStateException.class, () -> q.completeService(1));
            expect(IllegalArgumentException.class, () -> q.cancelCustomer("A999"));
            expect(IllegalArgumentException.class, () -> new QueueManager(0));
        });
        test("Queue snapshots cannot remove waiting customers", () -> {
            QueueManager q = new QueueManager();
            q.addCustomer("One", false);
            q.addCustomer("Two", true);
            q.getNormalQueue().clear();
            q.getPriorityQueue().clear();
            equal(2, q.getWaitingCount());
        });
        test("Statistics and waiting-time calculation", () -> {
            QueueManager q = new QueueManager(3);
            q.addCustomer("John", false);
            q.addCustomer("Buhle", false);
            Customer p1 = q.addCustomer("Mary", true);
            Customer p2 = q.addCustomer("Rumbidzai", true);
            q.addCustomer("Alex", true);
            equal(0, q.getStatistics().waitingTimeSamples());
            q.serveNextCustomer(1);
            q.serveNextCustomer(2);
            Customer normal = q.serveNextCustomer(3);
            q.completeService(1);
            q.cancelCustomer("A002");
            QueueStatistics s = q.getStatistics();
            equal(5, s.totalCustomers());
            equal(1, s.servedCustomers());
            equal(1, s.waitingCustomers());
            equal(0, s.calledCustomers());
            equal(2, s.servingCustomers());
            equal(1, s.priorityCustomersWaiting());
            equal(1, s.cancelledCustomers());
            equal(1, s.availableCounters());
            equal(2, s.busyCounters());
            equal(3, s.waitingTimeSamples());
            long a = p1.getWaitingTimeMillis();
            long b = p2.getWaitingTimeMillis();
            long c = normal.getWaitingTimeMillis();
            equal((a + b + c) / 3000.0, s.averageWaitingSeconds());
            equal(Math.max(a, Math.max(b, c)) / 1000.0, s.longestWaitingSeconds());
        });
        System.out.println("\nResults: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) System.exit(1);
    }

    private static void test(String name, Runnable action) {
        try {
            action.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (AssertionError | RuntimeException error) {
            failed++;
            System.out.println("FAIL: " + name + " - " + error.getMessage());
        }
    }

    private static void equal(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    private static void expect(Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException error) {
            if (type.isInstance(error)) return;
            throw new AssertionError("Expected " + type.getSimpleName() + ", got " + error);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}
