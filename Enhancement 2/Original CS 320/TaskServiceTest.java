package test;
import static org.junit.jupiter.api.Assertions.*;

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
}