package task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class TaskService {

    //Stores tasks by task ID.
    private final Map<String, Task> tasks = new HashMap<>();

    // Stores task dependencies as a graph.
    private final Map<String, Set<String>> taskDependencies = new HashMap<>();

    // Add a task
    public void addTask(Task task) {

        if (task == null) {throw new IllegalArgumentException("Task cannot be null");
        }

        if (tasks.containsKey(task.getTaskId())) {throw new IllegalArgumentException("Task ID already exists");
        }

        tasks.put(task.getTaskId(), task);

        //Every new task begins with an empty dependency set.
        
        taskDependencies.put(task.getTaskId(), new HashSet<>());
    }

    // Delete a task
    public void deleteTask(String taskId) {
        requireTask(taskId);
        tasks.remove(taskId);
        taskDependencies.remove(taskId);

        // Remove the deleted task from all other dependency sets.
         
        for (Set<String> dependencies : taskDependencies.values()) {

            dependencies.remove(taskId);
        }
    }

    // Update task name
    public void updateTaskName(String taskId, String newName) {
        Task task = requireTask(taskId);
        task.setName(newName);
    }

    // Update task description
    public void updateTaskDescription(String taskId, String newDescription) {
        Task task = requireTask(taskId);
        task.setDescription(newDescription);
    }

    // Update task priority
    public void updateTaskPriority(
            String taskId, int newPriority) {

        Task task = requireTask(taskId);
        task.setPriority(newPriority);
    }

    // Update task due date
    public void updateTaskDueDate(
            String taskId, LocalDate newDueDate) {

        Task task = requireTask(taskId);
        task.setDueDate(newDueDate);
    }

    // Get a task by ID
    public Task getTask(String taskId) {
        return tasks.get(taskId);
    }

    // Return the number of stored tasks
    public int getTaskCount() {
        return tasks.size();
    }

    // Adds a dependency making parts need to be completed before repair
     
    public void addDependency(
            String taskId,
            String dependencyTaskId) {

        requireTask(taskId);
        requireTask(dependencyTaskId);

        if (taskId.equals(dependencyTaskId)) {throw new IllegalArgumentException("A task cannot depend on itself");
        }

        taskDependencies
                .get(taskId)
                .add(dependencyTaskId);
    }

    // Remove a dependency
    public void removeDependency(
            String taskId,
            String dependencyTaskId) {
        requireTask(taskId);
        requireTask(dependencyTaskId);
        taskDependencies
                .get(taskId)
                .remove(dependencyTaskId);
    }

    //Returns a copy of a task's dependencies.
    public Set<String> getDependencies(
            String taskId) {
        requireTask(taskId);
        Set<String> copy = new HashSet<>(
                taskDependencies.get(taskId));
        return Collections.unmodifiableSet(copy);
    }

    // Builds the final task schedule.

    public List<Task> buildTaskSchedule() {

        // Counts the number of unfinished dependencies
         
        Map<String, Integer> incomingDependencies =
                new HashMap<>();

        // Stores the tasks that depend on each task. For example, if REPAIR depends on PARTS, PARTS will connect to REPAIR here.
         
        Map<String, List<String>> dependentTasks = new HashMap<>();

        //Give every task an initial dependency count
        
        for (String taskId : tasks.keySet()) {

            incomingDependencies.put(taskId, 0);

            dependentTasks.put(taskId, new ArrayList<>());
        }

        //Build the dependency counts and the graph
        for (Map.Entry<String, Set<String>> entry
                : taskDependencies.entrySet()) {

            String taskId = entry.getKey();
            Set<String> dependencies = entry.getValue();

            incomingDependencies.put( taskId, dependencies.size());

            for (String dependencyId : dependencies) {
                if (!tasks.containsKey(dependencyId)) {
                    throw new IllegalStateException("Missing dependency: " + dependencyId);
                }

                dependentTasks
                        .get(dependencyId)
                        .add(taskId);
            }
        }

        //PriorityQueue ordering. priority, due date, and then task ID.
         
        
        Comparator<Task> scheduleOrder = new Comparator<Task>() {

            @Override
            public int compare(Task firstTask, Task secondTask) {

                int priorityCompare = Integer.compare( firstTask.getPriority(), secondTask.getPriority());

                if (priorityCompare != 0) {
                    return priorityCompare;
                }

                int dateCompare = firstTask.getDueDate().compareTo(
                        secondTask.getDueDate());

                if (dateCompare != 0) {
                    return dateCompare;
                }

                return firstTask.getTaskId().compareTo(
                        secondTask.getTaskId());
            }
        };
        PriorityQueue<Task> availableTasks = new PriorityQueue<>(scheduleOrder);

        //add task with no dependencies
        for (Map.Entry<String, Integer> entry
                : incomingDependencies.entrySet()) {

            if (entry.getValue() == 0) {

                Task availableTask = tasks.get(entry.getKey());
                availableTasks.add(availableTask);
            }
        }

        List<Task> schedule = new ArrayList<>();

        //Kahn's topological sorting algorithm.
         
        while (!availableTasks.isEmpty()) {

            //Remove the best available task from priority que
             
            Task currentTask =
                    availableTasks.remove();

            schedule.add(currentTask);

            // Reduce the dependency count for each task that depended on the completed task.
             
            for (String dependentId : dependentTasks.get(currentTask.getTaskId())) {

                int newCount = incomingDependencies.get(dependentId) - 1;

                incomingDependencies.put(dependentId, newCount);

                // When the dependency count reaches zero the task becomes available.
                
                if (newCount == 0) { availableTasks.add(tasks.get(dependentId));
                }
            }
        }

        //If every task was not scheduled, the graph contains a dependency cycle.
         
        if (schedule.size() != tasks.size()) {

            throw new IllegalStateException("Cannot build schedule because " + "a dependency cycle exists");
        }

        return schedule;
    }

    //Finds a task or throws an exception when its ID does not exist.
    private Task requireTask(String taskId) {

        Task task = tasks.get(taskId);

        if (task == null) {
            throw new IllegalArgumentException("Task ID not found: " + taskId);
        }

        return task;
    }
}