package test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import task.Task;

public class TaskTest {
    @Test
    void testTaskValid() {
        Task task = new Task("12345", "work", "complete install");
        assertTrue(task.getTaskId().equals("12345"));
        assertTrue(task.getName().equals("work"));
        assertTrue(task.getDescription().equals("complete install"));
    }

    // testing nulls
    @Test
    void testNullId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task(null, "work", "complete install");
        });
    }

    @Test
    void testNullName() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345", null, "complete install");
        });
    }

    @Test
    void testNullDescription() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345", "work", null); 
        });
    }

    // testing lengths
    @Test
    void testIdTooLong() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345678901", "work", "complete install"); // 11 characters
        });
    }

    @Test
    void testNameTooLong() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345", "abcdefghijklmnopqrstu", "complete install"); // 21 characters
        });
    }

    @Test
    void testDescriptionTooLong() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345", "work", "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxy"); // 51 characters
        });
    }

    // testing setters
    @Test
    void testUpdateNameValid() {
        Task task = new Task("12345", "work", "complete install");
        task.setName("myjob");
        assertTrue(task.getName().equals("myjob"));
    }

    @Test
    void testUpdateDescriptionValid() {
        Task task = new Task("12345", "work", "complete install");
        task.setDescription("install complete");
        assertTrue(task.getDescription().equals("install complete"));
    }

    @Test
    void testUpdateNameInvalid() {
        Task task = new Task("12345", "work", "complete install");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            task.setName("abcdefghijklmnopqrstu"); // 21 characters
        });
    }

    @Test
    void testUpdateDescriptionInvalid() {
        Task task = new Task("12345", "work", "complete install");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            task.setDescription("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxy"); // 51 characters
        });
    }
}
