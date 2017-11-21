package com.medavox.repeats.database;

import com.medavox.repeats.datamodels.CompletedDose;
import com.medavox.repeats.datamodels.IntendedDose;
import com.medavox.repeats.datamodels.Task;

import java.util.List;


/**@author Adam Howard
 * */
public interface Backend {


    /**@return the id of the task in the table*/
    int addTask(Task newTask);

    boolean deleteTask(Task taskToDelete);
    Task[] getTasksDueToday();




    //--------------------old---------------------

    int getIntendedDoseCount();

    void addIntendedDose(IntendedDose intendedDose);
    void addIntendedDoseList(List<IntendedDose> intendedDoses);
    void addIntendedDoses(IntendedDose[] intendedDoses);

    IntendedDose getNextDueDose();

    boolean hasIntendedDoseWithId(int id);

    IntendedDose getIntendedDoseById(int id);

    List<IntendedDose> getAllIntendedDoses();

    /**Only call this when closing, and perhaps pausing the application*/
    void close();

    boolean hasNextDueDose();
    boolean hasPlan();

    /**Wipes all entries from both tables: Completed Doses and Intended Doses
     * @return the total number of rows deleted from both databases*/
    int deletePlan();
}