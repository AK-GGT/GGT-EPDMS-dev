Tasking
------------------
How to write a task
===================

The three basic questions to answer are as follows
<ol>
    <li>What data am I going to process?</li>
    <li>How am I going to process it?</li>
    <li>What's the correct processing strategy?</li>
</ol>

## The data bundle
The first step is to bundle the data into a single class.
```java
public class SomeTaskDataBundle {

    List<Object> objects;
    
    Object something;

    // Constructor, getters
    
}
```
It's often suitable to store data in a normalised way. But keep in mind
that initialising the data bundle happens on the main-thread!

## The task
All we need to do is extending the `AbstractTask` and implement three methods:

```java
public class SomeTask extends AbstractTask<SomeTaskDataBundle> {
    
    @Override
    protected String getDescription(SomeTaskDataBundle data) {
        return "Some description for processing " +
                data.getObjects.size() +
                "objects and " +
                data.getSomething().name();
    }

    @Override
    protected TaskProcessingStrategy determineProcessingStrategy(SomeTaskDataBundle data) {
        return TaskProcessingStrategy.ASYNCHRONOUSLY;
    }
    
    @Override
    protected TaskResult process(SomeTaskDataBundle data) throws TaskRunException {
        List<Objects> objects = data.getObjects;
        Object something = data.getSomething();
        
        // do something
        
        return new TaskResult(TaskStatus.COMPLETE);
    }
    
}
```

Please note however, that this very basic task doesn't count successes, errors and warnings.
We will find out about this in a bit.

### The task result
In a task implementation the `TaskResult` is typically just a wrapper
for the `TaskStatus` enum, most of the functionality is only necessary
by the `AbstractTask` class.

### The Processing strategy
It's an enum, return what ever makes sense for the task at hand
and feel free to implement a switch based on the given data.

## Logging
Typically one wants to keep track of successes and errors. In order to do so we simply
extend `AbstractLoggingTask<T, ? extends RunLog>`. It offers basic logging methods.
The only method one has to additionally implement is initialisation of the run-log.
You may be happy with the `DefaultRunLog` implementation of `RunLog`, but feel free
to write your own.
```java
public class SomeTask extends AbstractLoggingTask<SomeTaskDataBundle, DefaultRunLog> {

    @Override
    protected DefaultRunLog initTaskLog() {
        return new DefaultRunLog();
    }
    
    // Implement methods of AbstractTask
    
}
```
Note: If you're processing data sets, probably the correct approach is to extend
`AbstractDataSetLoggingTask` which offers more specialised logging options and
comes with an already initialised run-log.

### Making the task interruptable
When the thread is interrupted the task typically has some time to stop gracefully. The `AbstractTask`
class provides a method called `isContinue` which can be called regularly to check whether the task has
been interrupted. If it has been, the `onInterrupt` method is called before an exception is thrown.
Typically the `onInterrupt` method simply persists an appropriate job state in the db, but it can, of course,
be extended/overridden.