A non-trivial tasking example
---
Implementing asynchronous data set deletion with tasks
===

Data set deletion is a tricky endeavour.

The first complexity here lies in our model where we try to balance out two conflicting goals. 
First and foremost we need to protect the integrity of our data *but* there's also the wish to avoid redundancies.
Consequently, data sets are referencing each other in complex clusters and if we don't start with the right data set, deletion readily fails.

Apart from that, deletion is problematic in itself when dealing with multiple users or done asynchronously.
It makes sense to, as quickly as possible, have UI and API present the expected *future state*.
For this reason we need to, *immediately, flag data sets* that are to be deleted.
(And we need to unflag the ones that haven't been deleted at some point, too.)

Fortunately for us, all this can be done within a single task.

## The algorithm we want to implement
### By the caller
1. *Check permissions.*
   This is done on the *data stock level*.
   And it makes sense to have this be handled by whoever instantiates the task/data bundle!

### Immediately
2. *Flag referenced data sets for deletion.* 
There is a visibility flag we can use for this.

### Asynchronously (Queue)
3. *Collect dependencies.**
According to the provided dependencies mode.

4. *Flag dependencies.*
They (obviously) may not have been flagged yet.

5. *Group data sets by type.*
Based on the type we can say how likely it is that these data sets are not dependency to another. 
(Start with high-level data sets such as processes/life cycle models, ...)

6. *Delete DataSets.** 
High level to low level types.
Track results.

### Finally
7. *Unflag failed deletes.*

*Time consuming

## The data bundle
We delete data sets on the basis of `DeleteReference` objects.
These are just `DataSetReference` objects with an added flag that indicates whether a reference is a dependency reference or not.
In particular `DeleteReference` objects have no association with the Entity manager.
This avoides memory leaks.

The only other thing we need in our bundle is the `DependenciesMode`, which determines what dependencies should be fetched.

1. *Sorted reference collection.* (`Map<DataSetDaoType, Set<DeleteReference>>`)
It's important that we have uniqueness here, otherwise we will be flooded with ambiguous failures.

2. *Dependency Mode* (`DependenciesMode`)

## The task
Let's sketch out the task and see where all the bits of the algorithm go.

```java
public class DeleteTask extends AbstractDataSetLoggingTask<DeleteTaskDataBundle> {

   private static final Logger LOGGER = LogManager.getLogger(DeleteTask.class);

   @Override
   protected String getDescription(DeleteTaskDataBundle data) {
      return "Deleting " + data.getCount() +
              " data sets with dependencies mode '" +
              data.getDependenciesMode();
   }

   @Override
   protected TaskProcessingStrategy determineProcessingStrategy(DeleteTaskDataBundle data) {
      return TaskProcessingStrategy.BACKGROUND_QUEUE;
   }

   @Override
   protected void preProcessImmediately(DeleteTaskDataBundle data, User signee) {
      super.preProcessImmediately(data, signee);

      // flag data sets for deletion
   }

   @Override
   protected TaskResult process(DeleteTaskDataBundle data) throws TaskRunException {
      // fetch dependencies, flag for deletion as well

      Comparator<DataSetDaoType> comparator = DataSetDaoType.getComparatorForProcessingOrder().reversed();
      Iterator<DataSetDaoType> keyIterator = DataSetDaoType.iterator(comparator);
      while (keyIterator.hasNext() && isContinue()) {
         DataSetDaoType key = keyIterator.next();
         DataSetDao<?, ?, ?> dao = key.getDao();

         data.getDataSetRefsByDaoType().get(key).forEach(ref -> {
            DataSetReference dsRef = ref.getDsRef();
            try {
                if (!isContinue())
                    throw new InterruptedException();
                
               dao.removeById(dsRef.getId());

               if (ref.isDependency())
                  logInfo(dsRef);
               else
                  logSuccess(dsRef);

            } catch (Exception e) {
               LOGGER.error("Failed to delete data set " + ref, e);
               if (ref.isDependency())
                  logWarning(dsRef);
               else
                  logError(dsRef);
            }
         });
      }

      return new TaskResult(TaskStatus.COMPLETE);
   }

   @Override
   protected TaskResult postProcessFinally(DeleteTaskDataBundle data, User user, TaskResult result) {
      result = super.postProcessFinally(data, user, result);

      // Unflag all data sets that are not listed in the success/infos of the runlog

      return result;
   }

   @Override
   protected JobType determineJobType(DeleteTaskDataBundle data) {
      return JobType.DELETE; // Legacy...
   }
}
```

## Running the task
The handler could trigger the task execution like this

```java
public class Handler {
    //
    // ...
    //
   
   public onDeleteButtonPressed() {

      DeleteTaskDataBundle dataBundle = new DeleteTaskDataBundle(this.selectedItems, this.dependenciesMode)
      DeleteTask deleteTask = new DeleteTask.from(dataBundle, this.getCurrentUserOrNull());
      try {
         deleteTask.runIn(this.getGlobalQueue())
                 .message(this, true);

         this.clearSelection();

      } catch (Throwable t) {
         t.printStackTrace();
      }
   }
   
   //
   // ...
   //
}
```

## Interrupts?
We simply call the method `isContinue()` during loops and act accordingly.
Note that the 'finally' in `postProcessFinally()` implies this method gets called, even as the task is already breaking apart.
There, we un-flag the data sets that have not been deleted, which is an appropriate reaction to interrupts.
Hence, we simply need to break the loops and maybe add some log statements.

```java
public class DeleteTask extends AbstractDataSetLoggingTask<DeleteTaskDataBundle> {

   // ...

   @Override
   protected TaskResult process(DeleteTaskDataBundle data) throws TaskRunException {

      // ...
      
      while (keyIterator.hasNext() && isContinue()) {
          
          // ...

         data.getDataSetRefsByDaoType().get(key).forEach(ref -> {
            DataSetReference dsRef = ref.getDsRef();
            try {
                if (!isContinue())
                    throw new InterruptedException();
                
               // ...

            } catch (Exception e) {
               LOGGER.error("Failed to delete data set " + ref + " (interrupted)");
               
               // ...
            }
         });
      }

      return new TaskResult(TaskStatus.COMPLETE);
   }
   
   // ...
}
```