Before start implementing it ask me 5 questions that I know you understand it well.
Then when you are ready, create 4 parallel tasks related to different files,
When you create the tasks be sure to put detail of each task which contains exact implementations
and interfaces between them, like the method names
So when you create 2 file in parallel they both know what methods from each others they can call,
also if its needed provide information about the other tasks detail so each tasks knows what exactly
can expect
You need to exactly define for each task which files they need to read, and which files they are
allowed to edit or create, otherwise 2 tasks might try to edit the same file and create a conflict.
so you need to prepare everything with detail for each task, doens't matter if tasks are getting so
long.
Then run all the tasks in parallel using subagents.

ALWAYS work as Orchestrator, don't do the tasks yourself it will fill your context windows, use
suagents to do any tasks when I ask for some change or anything.
To Read, To Edit, ... always use subagents. and then read the result of them.