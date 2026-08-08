package task;

import java.time.LocalDate;
import java.util.List;

public class TaskSchedulerDemo {

    public static void main(String[] args) {

        TaskService service = new TaskService();

        service.addTask(new Task("PARTS", "Order parts", "Order the required analyzer parts", 1, LocalDate.of(2026, 7, 26)));

        service.addTask(new Task("CHECK","Inspect system","Inspect the analyzer before repair",2,LocalDate.of(2026, 7, 26)));

        service.addTask(new Task("REPAIR","Repair system","Install parts and complete the repair",1,LocalDate.of(2026, 7, 27)));

        service.addTask(new Task("VERIFY","Verify repair","Run testing and verify operation",2,LocalDate.of(2026, 7, 27)));
        
        service.addDependency("REPAIR", "PARTS");
        service.addDependency("REPAIR", "CHECK");
        service.addDependency("VERIFY", "REPAIR");
        List<Task> schedule = service.buildTaskSchedule();
        System.out.println("Task schedule:");
        for (int index = 0; index < schedule.size(); index++) {

            Task task = schedule.get(index);

            System.out.println((index + 1) + ". " + task.getTaskId() + " - " + task.getName() + " (priority " + task.getPriority() + ", due " + task.getDueDate() + ")");
        }
    }
}
