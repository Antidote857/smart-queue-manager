package util;

import java.util.Locale;

public class QueueNumberGenerator {

    private int customerCount = 0;
    private int normalCount = 0;
    private int priorityCount = 0;

    public String generateCustomerId() {
        customerCount++;
        return String.format(Locale.ROOT, "C%03d", customerCount);
    }

    public String generateQueueNumber(boolean priority) {
        if (priority) {
            priorityCount++;
            return String.format(Locale.ROOT, "P%03d", priorityCount);
        }

        normalCount++;
        return String.format(Locale.ROOT, "A%03d", normalCount);
    }
}