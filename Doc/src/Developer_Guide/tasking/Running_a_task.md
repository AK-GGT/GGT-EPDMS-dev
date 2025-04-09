Tasking
------------------
How to run a pre-defined task
============================

If an user-action is keeping the main-thread busy for too long, we typically want it
to run asynchronously. This can be achieved in several ways of course, but picking
the most suitable may be a bit tricky. Our `AbstactHandler` provides access to a
`TaskingHub` which knows how to run `runnables` and `calllables` asynchronously
or in a (singleton) queue. Since, however, either picking the correct strategy
or the implementation of the action itself may be complex, it makes sense to consider
writing a re-usable task. And chances are it's already been written. Now, how do we run it?

### Running the task
```java
public class SomeBean extends AbstractHandler {
    
    // ...
    
    public void buttonPressed() {
        // User input
        List<Object> selectedObjects = getSelectedObjects();
        Object someOtherInput = getSomeOtherInput();
      
        // Initialising the task
        SomeTaskDataBundle dataBundle = new SomeTaskDataBundle(selectedObjects, someOtherInput);
        SomeTask taskInstance = new SomeTask(dataBundle, super.getUser());
        
        // Running the task
        taskInstance.runIn(super.getGlobalQueueHub());
    }
    
    // ...
    
}
```

### Variations
Some tasks provide convenience methods to initialise the data-bundle.
```java
public class SomeBean extends AbstractHandler {
    
    // ...
    
    public void buttonPressed() {
        // User input
        List<Object> selectedObjects = getSelectedObjects();
        Object someOtherInput = getSomeOtherInput();
      
        // Initialising the task
        SomeTask taskInstance = new SomeTask.from(selectedObjects, someOtherInput, super.getUser());
        
        // Running the task
        // ...
    }
    
    // ...
    
}
```
Also, if preferred (and one knows how to get the TaskingHub without auto-completion),
one can hand over the task to the queue.
```java
public class SomeBean extends AbstractHandler {
    
    // ...
    
    public void buttonPressed() {
        // User input
        // ...
      
        // Initialising the task
        // ...
        
        // Running the task
        super.getGlobalQueueHub().run(taskInstance);
    }
    
    // ...
    
}
```

That's it. The task will be automatically processed according to the processing strategy.
One doesn't need to concern oneself with the implementation or with what strategy to pick.

### FacesMessages

All methods to run the task, independently of the processing and logging strategy, yield
a `TaskResult`. The result instance knows how add a suitable `FacesMessage` to the UI. It
just needs the corresponding handler. As a bonus, it optionally prints an error-log to the
console (if it exists).
```java
public class SomeBean extends AbstractHandler {
    
    // ...
    
    public void buttonPressed() {
        // User input
        // ...
      
        // Initialising the task
        // ...
        
        // Running the task
        TaskResult taskResult = taskInstance.runIn(super.getGlobalQueueHub());
        taskResult.message(this);
    }
    
    // ...
    
}
```
