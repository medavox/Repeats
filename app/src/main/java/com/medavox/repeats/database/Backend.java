package com.medavox.repeats.database;

import com.medavox.repeats.datamodels.CompletedDose;
import com.medavox.repeats.datamodels.IntendedDose;

import java.util.List;


/**@author Adam Howard
 * */
public interface Backend {

    int getIntendedDoseCount();
    int getCompletedDoseCount();

    void addIntendedDose(IntendedDose intendedDose);
    void addIntendedDoseList(List<IntendedDose> intendedDoses);
    void addIntendedDoses(IntendedDose[] intendedDoses);

    void addCompletedDose(CompletedDose completedDose);
    void addCompletedDoseList(List<CompletedDose> completedDoses);
    void addCompletedDoses(CompletedDose[] completedDoses);

    CompletedDose getPreviousDoseCompleted();
    IntendedDose getNextDueDose();

    boolean hasIntendedDoseWithId(int id);
    boolean hasCompletedDoseWithId(int id);

    IntendedDose getIntendedDoseById(int id);
    CompletedDose getCompletedDoseById(int id);

    List<IntendedDose> getAllIntendedDoses();
    List<CompletedDose> getAllCompletedDoses();

    /**Only call this when closing, and perhaps pausing the application*/
    void close();

    boolean hasNextDueDose();
    boolean hasPlan();

    /**Wipes all entries from both tables: Completed Doses and Intended Doses
     * @return the total number of rows deleted from both databases*/
    int deletePlan();
}