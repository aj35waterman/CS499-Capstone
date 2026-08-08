package task;
import java.time.LocalDate;
public class Task {
    private final String taskId;  // cannot be changed
    private String name;
    private String description;
    private int priority;
    private LocalDate dueDate;
    
 // Original constructor
    public Task(String taskId, String name, String description) {
        this(taskId, name, description, 3, LocalDate.MAX);
    }
    // Constructor
    public Task(String taskId, String name, String description, int priority, LocalDate dueDate) {
    	validateTaskId(taskId);
        validateName(name);
        validateDescription(description);
        validatePriority(priority);
        validateDueDate(dueDate);
      

        this.taskId = taskId;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
    }
    //creating getters
    public String getTaskId() { return taskId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    // setters made for only name and description
    public void setName(String name) { validateName(name); this.name = name; }
    public void setDescription(String description) { validateDescription(description); this.description = description; }
    public void setPriority(int priority) { validatePriority(priority); this.priority = priority; }
    public void setDueDate(LocalDate dueDate) { validateDueDate(dueDate); this.dueDate = dueDate; }
    
	//validation for methods 
	private static void validateTaskId(String taskId) {
	    if (taskId == null
                || taskId.isBlank()
                || taskId.length() > 10) {

            throw new IllegalArgumentException("Task ID must contain 1 to 10 characters");
        }
    }
	private static void validateName(String name) {

        if (name == null
                || name.isBlank()
                || name.length() > 20) {

            throw new IllegalArgumentException("Task name must contain 1 to 20 characters");
        }
    }
	private static void validateDescription(String description) {

        if (description == null
                || description.isBlank()
                || description.length() > 50) {

            throw new IllegalArgumentException("Task description must contain 1 to 50 characters");
        }
    }
	private static void validatePriority(int priority) {

        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException("Task priority must be between 1 and 5");
        }
    }
	private static void validateDueDate(LocalDate dueDate) {

        if (dueDate == null) {
            throw new IllegalArgumentException("Task due date cannot be null");
        }
    }
}
    


