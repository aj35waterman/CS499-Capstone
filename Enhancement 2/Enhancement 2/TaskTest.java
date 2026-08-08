package test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import task.Task;

public class TaskTest {

    @Test
    void testTaskValid() {
        Task task = new Task("12345", "work", "complete install", 2,
                LocalDate.of(2026, 8, 1));

        assertTrue(task.getTaskId().equals("12345"));
        assertTrue(task.getName().equals("work"));
        assertTrue(task.getDescription().equals("complete install"));
        assertEquals(2, task.getPriority());
        assertEquals(LocalDate.of(2026, 8, 1), task.getDueDate());
    }

    @Test
    void testOriginalConstructor() {
        Task task = new Task("12345", "work", "complete install");

        assertEquals(3, task.getPriority());
        assertEquals(LocalDate.MAX, task.getDueDate());
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

    @Test
    void testNullDueDate() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345", "work", "complete install", 2, null);
        });
    }

    // testing blank values
    @Test
    void testBlankId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task(" ", "work", "complete install");
        });
    }

    @Test
    void testBlankName() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345", " ", "complete install");
        });
    }

    @Test
    void testBlankDescription() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345", "work", " ");
        });
    }

    // testing lengths
    @Test
    void testIdTooLong() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345678901", "work", "complete install");
        });
    }

    @Test
    void testNameTooLong() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345", "abcdefghijklmnopqrstu", "complete install");
        });
    }

    @Test
    void testDescriptionTooLong() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345", "work", "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxy");
        });
    }

    // testing priority
    @Test
    void testPriorityTooLow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345", "work", "complete install", 0,
                    LocalDate.of(2026, 8, 1));
        });
    }

    @Test
    void testPriorityTooHigh() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task("12345", "work", "complete install", 6,
                    LocalDate.of(2026, 8, 1));
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
    void testUpdatePriorityValid() {
        Task task = new Task("12345", "work", "complete install");
        task.setPriority(1);
        assertEquals(1, task.getPriority());
    }

    @Test
    void testUpdateDueDateValid() {
        Task task = new Task("12345", "work", "complete install");
        task.setDueDate(LocalDate.of(2026, 8, 5));
        assertEquals(LocalDate.of(2026, 8, 5), task.getDueDate());
    }

    @Test
    void testUpdateNameInvalid() {
        Task task = new Task("12345", "work", "complete install");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            task.setName("abcdefghijklmnopqrstu");
        });
    }

    @Test
    void testUpdateDescriptionInvalid() {
        Task task = new Task("12345", "work", "complete install");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            task.setDescription("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxy");
        });
    }

    @Test
    void testUpdatePriorityInvalid() {
        Task task = new Task("12345", "work", "complete install");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            task.setPriority(6);
        });
    }

    @Test
    void testUpdateDueDateInvalid() {
        Task task = new Task("12345", "work", "complete install");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            task.setDueDate(null);
        });
    }
}