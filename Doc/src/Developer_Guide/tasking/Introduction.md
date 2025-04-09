# Introduction to tasking
At the core of things lies the simple idea to ***run stuff off the main thread***.
Now, this wish being simple and fine, there immediately is a number of things that can go wrong.
So, naturally, one wants to impose some structure on the endeavour and, where possible, deal with complexities beforehand.

In the following we will briefly discuss our model for tasking.

## Run stuff *'off the main thread'*

One way to mitigate the risks is having a dedicated background thread in which everything is queued.
A *queue* can e.g. make sure that deletion is done before publication is started.

To encapsulate the dependency management, we provide a singleton <code>GlobalQueueHub</code> to all Beans, which actually is a so called <code>TaskingHub</code>.
This means the *GlobalQueue* offers several methods that accept runnables, i.e.
* <code>runImmediately</code> (main thread)
* <code>runAsynchronously</code> (any background thread)
* <code>runInQueue</code> (dedicated background thread)

It is an intriguing idea, however, to see the *processing strategy* (i.e. main thread, any thread, queue) as an intrinsic property of the runnable.
That's because, given enough resources, the main-thread is typically preferable to a background thread.
This becomes an intrinsic property of the runnable if you read *'given enough resources'* as *'given a small enough workload'*.

## Run *'stuff'* off the main thread

So what are these runnables anyway?
If you will, the tasking hub merely presses standardized run-buttons from the appropriate thread.
We could (and we can) just provide it with lambdas.
Unfortunately, the elementary approach introduces headaches.
* The procedures typically are complex and re-written every once in a while, so they shouldn't be duplicated. Where do they go?
* Ideally, there is a central place, where we generically solve the problems of
    * status tracking
    * exception handling
    * interrupt handling
    * ...

So this is why we introduced task objects.
The base class <code>AbstractTask</code> is simply a class that knows how to convert itself into a lambda, baking in all that.

Concrete implementations then define procedural logic in terms of *processing a data bundle*, which in turn is *part of the state* of the concrete instance.
In the very least we need the implementation to
* Give a description of what they're doing
* Define the data they want to process
* Define the procedure in terms of processing this data
* Decide which *ProcessingStrategy* is suitable to procedure *and* data

## *'Run stuff off the main thread'*

What you need to do to run some implemented task is
* Instantiate the data bundle
* Instantiate the task (needs the data bundle)
* Call the task's <code>runIn(TaskingHub hub)</code> method (i.e. <code>task.runIn(hub)</code>).

That's it.

## Remark on the *TaskProcessingStrategy*

Remember when we said, that the processing strategy was made an intrinsic property to the task?

#### What is a *TaskProcessingStrategy*?

Conceptually, a task processing strategy is a way of 'plugging a task into a tasking hub' plus some exception handling.
And applying this strategy yields some result (it could very well be 'empty', though, if the task is not executed on the main thread).

Hence, it can be thought of as a function, that maps a tuple (<code>TaskingHub</code>, <code>Callable</code>) to some <code>Result</code>.
Alias a bi-function.

#### What does that mean?

This means, that the tasking hub does not implement any switches of any sort.
It means, that ultimately the task is not run but a *processing strategy* is *applied to its callable*.
More explicitly: provide the task class with a tasking hub, and it will both generate a callable and apply some suitable processing strategy.

#### 'task.runIn(hub)' vs. 'hub.run(task)'
By the above, it is somewhat natural to use <code>task.runIn(hub)</code>.
One additional upside is, that your IDE will suggest you a way to access a tasking hub, when you call <code>task.runIn( )</code>.