package test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import task.TaskService;
import task.Task;

public class TaskServiceTest {

    @Test
    void testAddTaskUniqueId() {
        TaskService service = new TaskService();
        Task task = new Task("12345", "work", "complete install");
        service.addTask(task);
        assertEquals(task, service.getTask("12345"));
    }

    @Test
    void testAddTaskDuplicateIdThrowsException() {
        TaskService service = new TaskService();
        service.addTask(new Task("12345", "work", "complete install"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.addTask(new Task("12345", "gym", "workout"));
        });
    }

    @Test
    void testAddNullTaskThrowsException() {
        TaskService service = new TaskService();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.addTask(null);
        });
    }

    @Test
    void testDeleteTaskById() {
        TaskService service = new TaskService();
        service.addTask(new Task("12345", "work", "complete install"));
        service.deleteTask("12345");
        assertNull(service.getTask("12345"));
    }

    @Test
    void testDeleteTaskInvalidIdThrowsException() {
        TaskService service = new TaskService();
        service.addTask(new Task("12345", "work", "complete install"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.deleteTask("11111");
        });
    }

    @Test
    void testUpdateTaskNameById() {
        TaskService service = new TaskService();
        service.addTask(new Task("12345", "work", "complete install"));
        service.updateTaskName("12345", "gym");
        assertEquals("gym", service.getTask("12345").getName());
    }

    @Test
    void testUpdateTaskDescriptionById() {
        TaskService service = new TaskService();
        service.addTask(new Task("12345", "work", "complete install"));
        service.updateTaskDescription("12345", "go home");
        assertEquals("go home", service.getTask("12345").getDescription());
    }

    @Test
    void testUpdateTaskPriorityById() {
        TaskService service = new TaskService();
        service.addTask(new Task("12345", "work", "complete install", 3,
                LocalDate.of(2026, 8, 1)));
        service.updateTaskPriority("12345", 1);
        assertEquals(1, service.getTask("12345").getPriority());
    }

    @Test
    void testUpdateTaskDueDateById() {
        TaskService service = new TaskService();
        service.addTask(new Task("12345", "work", "complete install", 3,
                LocalDate.of(2026, 8, 1)));
        service.updateTaskDueDate("12345", LocalDate.of(2026, 8, 5));
        assertEquals(LocalDate.of(2026, 8, 5),
                service.getTask("12345").getDueDate());
    }

    @Test
    void testPriorityQueueOrder() {
        TaskService service = new TaskService();

        service.addTask(new Task("LOW", "low task", "low priority task", 4,
                LocalDate.of(2026, 8, 1)));

        service.addTask(new Task("HIGH", "high task", "high priority task", 1,
                LocalDate.of(2026, 8, 3)));

        service.addTask(new Task("MEDIUM", "medium task", "medium priority task", 2,
                LocalDate.of(2026, 8, 2)));

        List<Task> schedule = service.buildTaskSchedule();

        assertEquals("HIGH", schedule.get(0).getTaskId());
        assertEquals("MEDIUM", schedule.get(1).getTaskId());
        assertEquals("LOW", schedule.get(2).getTaskId());
    }

    @Test
    void testDueDateOrder() {
        TaskService service = new TaskService();

        service.addTask(new Task("LATER", "later task", "task with later date", 2,
                LocalDate.of(2026, 8, 5)));

        service.addTask(new Task("SOONER", "sooner task", "task with sooner date", 2,
                LocalDate.of(2026, 8, 2)));

        List<Task> schedule = service.buildTaskSchedule();

        assertEquals("SOONER", schedule.get(0).getTaskId());
        assertEquals("LATER", schedule.get(1).getTaskId());
    }

    @Test
    void testDependencyOrder() {
        TaskService service = new TaskService();

        service.addTask(new Task("FIRST", "first task", "must be completed first", 5,
                LocalDate.of(2026, 8, 5)));

        service.addTask(new Task("SECOND", "second task", "depends on first task", 1,
                LocalDate.of(2026, 8, 1)));

        service.addDependency("SECOND", "FIRST");

        List<Task> schedule = service.buildTaskSchedule();

        assertEquals("FIRST", schedule.get(0).getTaskId());
        assertEquals("SECOND", schedule.get(1).getTaskId());
    }

    @Test
    void testMultipleDependencies() {
        TaskService service = new TaskService();

        service.addTask(new Task("PARTS", "order parts", "order repair parts", 1,
                LocalDate.of(2026, 8, 1)));

        service.addTask(new Task("CHECK", "check system", "inspect the system", 2,
                LocalDate.of(2026, 8, 1)));

        service.addTask(new Task("REPAIR", "repair system", "complete the repair", 1,
                LocalDate.of(2026, 8, 2)));

        service.addTask(new Task("VERIFY", "verify repair", "test the repaired system", 2,
                LocalDate.of(2026, 8, 3)));

        service.addDependency("REPAIR", "PARTS");
        service.addDependency("REPAIR", "CHECK");
        service.addDependency("VERIFY", "REPAIR");

        List<Task> schedule = service.buildTaskSchedule();

        assertEquals("PARTS", schedule.get(0).getTaskId());
        assertEquals("CHECK", schedule.get(1).getTaskId());
        assertEquals("REPAIR", schedule.get(2).getTaskId());
        assertEquals("VERIFY", schedule.get(3).getTaskId());
    }

    @Test
    void testMissingDependencyThrowsException() {
        TaskService service = new TaskService();

        service.addTask(new Task("REPAIR", "repair system", "complete the repair", 1,
                LocalDate.of(2026, 8, 2)));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.addDependency("REPAIR", "PARTS");
        });
    }

    @Test
    void testSelfDependencyThrowsException() {
        TaskService service = new TaskService();

        service.addTask(new Task("ONE", "first task", "testing dependency", 1,
                LocalDate.of(2026, 8, 1)));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.addDependency("ONE", "ONE");
        });
    }

    @Test
    void testCycleDetectionThrowsException() {
        TaskService service = new TaskService();

        service.addTask(new Task("ONE", "first task", "testing first task", 1,
                LocalDate.of(2026, 8, 1)));

        service.addTask(new Task("TWO", "second task", "testing second task", 2,
                LocalDate.of(2026, 8, 2)));

        service.addTask(new Task("THREE", "third task", "testing third task", 3,
                LocalDate.of(2026, 8, 3)));

        service.addDependency("TWO", "ONE");
        service.addDependency("THREE", "TWO");
        service.addDependency("ONE", "THREE");

        Assertions.assertThrows(IllegalStateException.class, () -> {
            service.buildTaskSchedule();
        });
    }

    @Test
    void testDeleteTaskRemovesDependency() {
        TaskService service = new TaskService();

        service.addTask(new Task("FIRST", "first task", "dependency task", 1,
                LocalDate.of(2026, 8, 1)));

        service.addTask(new Task("SECOND", "second task", "depends on first task", 2,
                LocalDate.of(2026, 8, 2)));

        service.addDependency("SECOND", "FIRST");
        service.deleteTask("FIRST");

        List<Task> schedule = service.buildTaskSchedule();

        assertEquals(1, schedule.size());
        assertEquals("SECOND", schedule.get(0).getTaskId());
    }
}