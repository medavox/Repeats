package com.medavox.repeats.datamodels;

import java.util.concurrent.TimeUnit;

/**
 * @author Adam Howard on 04/10/16.
 */

public class Task {
//todo: allow users to edit a 'default' or prototype task,
    //which all newly created Tasks will inherit the settings of
    //(except for name and description)
    /**The task's short name*/
    private String name;
    /**The task's (optional) longer description*/
    private String description = "";

    /**The task should repeat every ?*/
    private int intervalNumber;
    /**The task should repeat every ? units?*/
    private TimeUnit intervalUnit;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCurrentlyDone() {
        return currentlyDone;
    }

    public void setCurrentlyDone(boolean currentlyDone) {
        this.currentlyDone = currentlyDone;
    }

    /**if true, task only starts counting down once it's marked as done*/
    private boolean onlyRepeatsOnceDone = true;
    private boolean createNotification = true;
    private boolean audioAlarm = false;

    /**whether the task should become due at the start of a given TimeUnit, eg day.*/
    private boolean snapDueTime = false;
    /**The TimeUnit to become due at the start of*/
    private TimeUnit snapToUnit = null;

    private boolean taskIsUrgentIfNotDoneAfterTime = false;
    private int timeFromDueTilUrgent;
    private TimeUnit timeFromDueTilUrgentUnit;

    /**Whether the tasks has been done for its current cycle.*/
    private boolean currentlyDone = false;

    /**Hide default constructor to force using parameterised constructor*/
    private Task() throws InstantiationException {
        throw new InstantiationException("Task needs at least a name, repeatInterval and a repeatIntervalUnit!");
    }

    public Task(String name, int repeatInterval, TimeUnit repeatIntervalUnit ) {

    }

    public int getIntervalNumber() {
        return intervalNumber;
    }

    public void setInterval(int intervalNumber, TimeUnit intervalUnit) {
        this.intervalNumber = intervalNumber;
        this.intervalUnit = intervalUnit;
    }

    public TimeUnit getIntervalUnit() {
        return intervalUnit;
    }

    public boolean isOnlyRepeatsOnceDone() {
        return onlyRepeatsOnceDone;
    }

    public void setOnlyRepeatsOnceDone(boolean onlyRepeatsOnceDone) {
        this.onlyRepeatsOnceDone = onlyRepeatsOnceDone;
    }

    public boolean shouldCreateNotification() {
        return createNotification;
    }

    public void setCreateNotification(boolean createNotification) {
        this.createNotification = createNotification;
    }

    public boolean shouldPlayAudioAlarm() {
        return audioAlarm;
    }

    public void setAudioAlarm(boolean audioAlarm) {
        this.audioAlarm = audioAlarm;
    }

    public boolean isSnapDueTime() {
        return snapDueTime;
    }

    public void setDoNotSnapDueTime(boolean snapDueTime) {
        snapDueTime = false;
        this.snapToUnit = null;
    }

    public TimeUnit getSnapToUnit() {
        return snapToUnit;
    }

    public void setSnapToUnit(TimeUnit snapToUnit) {
        this.snapDueTime = true;
        this.snapToUnit = snapToUnit;
    }

    public boolean isTaskIsUrgentIfNotDoneAfterTime() {
        return taskIsUrgentIfNotDoneAfterTime;
    }

    public void setTaskIsUrgentIfNotDoneAfterTime(boolean taskIsUrgentIfNotDoneAfterTime) {
        this.taskIsUrgentIfNotDoneAfterTime = taskIsUrgentIfNotDoneAfterTime;
    }

    public int getTimeFromDueTilUrgent() {
        return timeFromDueTilUrgent;
    }

    public void setTimeFromDueTilUrgent(int timeFromDueTilUrgent) {
        this.timeFromDueTilUrgent = timeFromDueTilUrgent;
    }

    public TimeUnit getTimeFromDueTilUrgentUnit() {
        return timeFromDueTilUrgentUnit;
    }

    public void setTimeFromDueTilUrgentUnit(TimeUnit timeFromDueTilUrgentUnit) {
        this.timeFromDueTilUrgentUnit = timeFromDueTilUrgentUnit;
    }

    public Task(String name, Task template ) {
        this.name = name;
        this.intervalNumber = template.intervalNumber;
        this.intervalUnit = template.intervalUnit;

        this.onlyRepeatsOnceDone = template.onlyRepeatsOnceDone;
        this.createNotification = template.createNotification;
        this.audioAlarm = template.audioAlarm;

        this.snapDueTime = template.snapDueTime;
        this.snapToUnit = template.snapToUnit;

        this.taskIsUrgentIfNotDoneAfterTime = template.taskIsUrgentIfNotDoneAfterTime;
        this.timeFromDueTilUrgent = template.timeFromDueTilUrgent;
        this.timeFromDueTilUrgentUnit = template.timeFromDueTilUrgentUnit;

        this.currentlyDone = template.currentlyDone;
    }
}
