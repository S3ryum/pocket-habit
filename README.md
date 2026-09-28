# Pocket Habit

An offline Kotlin command-line habit tracker. Habits and completion dates stay in a local habits.tsv file.

## Features

- Add, list, complete, and remove habits.
- Records whether a habit was completed today.
- Stores data locally; no account or network connection is used.

## Requirements

JDK 17 or newer and the Kotlin command-line compiler.

## Build and run

~~~sh
kotlinc PocketHabit.kt -include-runtime -d pocket-habit.jar
java -jar pocket-habit.jar add "Read for ten minutes"
java -jar pocket-habit.jar list
java -jar pocket-habit.jar done 1
~~~

The data file is created in the current directory. Use java -jar pocket-habit.jar help to see all commands.
